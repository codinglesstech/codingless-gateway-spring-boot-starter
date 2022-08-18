package tech.codingless.core.gateway.interceptor;

import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.gateway.annotation.GrantModuleCondition;
import tech.codingless.core.gateway.annotation.MyAccessKeyAuth;
import tech.codingless.core.gateway.annotation.MyAuth;
import tech.codingless.core.gateway.annotation.MyBiz;
import tech.codingless.core.gateway.data.MyMemoryAnalysisFlag;
import tech.codingless.core.gateway.helper.AccessKeyHelper;
import tech.codingless.core.gateway.helper.AccessKeyHelper.AccessKey;
import tech.codingless.core.gateway.util.SHAUtil;
import tech.codingless.core.gateway.util.SessionUtil;
import tech.codingless.core.gateway.util.SignUtil;
import tech.codingless.core.gateway.util.StringUtil;

@Slf4j
public class GatewayInterceptor implements AsyncHandlerInterceptor {
	private static final String ACCESS_KEY = "Access-Key";
	private static final String ACCESS_TIMESTAMP = "Access-Timestamp";
	private static final String ACCESS_SIGN = "Access-Sign";
	private static final String X_REAL_IP = "X-Real-IP";
	
	
	

	private static final ThreadLocal<MyMemoryAnalysisFlag> flag = new ThreadLocal<MyMemoryAnalysisFlag>();
	private static final ThreadLocal<Long> t = new ThreadLocal<>(); 
	private static final ThreadLocal<Boolean> DISABLE_RESPONSE_LOG = new ThreadLocal<>(); 
	private static final ThreadLocal<String> REQUEST_BODY=new ThreadLocal<String>(); 

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		AsyncHandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			if(!(handler  instanceof HandlerMethod)) {
				return AsyncHandlerInterceptor.super.preHandle(request, response, handler); 
			}
			clearSession();
			t.set(System.currentTimeMillis());
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			MyBiz myBiz = handlerMethod.getMethodAnnotation(MyBiz.class);
			if(myBiz!=null) {
				DISABLE_RESPONSE_LOG.set(myBiz.disableResponseLog());
			}
			String requestId = StringUtil.genGUID();
			response.addHeader("Request-Id", requestId);
			flag.set(new MyMemoryAnalysisFlag("REQ:" + request.getRequestURL().toString(), requestId));
			
			REQUEST_BODY.remove();
			//检查认证
			MyAccessKeyAuth myAccessKeyAuth = handlerMethod.getMethodAnnotation(MyAccessKeyAuth.class);
			if(myAccessKeyAuth!=null) {
				String accessKeyStr = request.getHeader(ACCESS_KEY);
				String accessTimeStamp = request.getHeader(ACCESS_TIMESTAMP);
				String accessSign = request.getHeader(ACCESS_SIGN);
				if (StringUtil.hasEmpty(accessKeyStr, accessTimeStamp, accessSign)) {
					response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
					response.sendError(MyAuth.UNAUTHORIZED_CODE);
					return false;
				}
				AccessKey accessKey = AccessKeyHelper.get(accessKeyStr.trim());  
				String moduleName = GrantModuleCondition.findModuleNameByResourcePkg(handlerMethod.getBean().getClass()); 
				if(StringUtil.isEmpty(moduleName)) {
					response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
					response.sendError(MyAuth.UNAUTHORIZED_CODE);
					return false; 
				}
				 
				
				if (!accessKey.isReadAble(moduleName) && !accessKey.isWriteAble(moduleName)) {
					response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
					response.sendError(MyAuth.UNAUTHORIZED_CODE);
					return false;
				} 
				if(myAccessKeyAuth.requiredWriteAble()&&BooleanUtils.isFalse(accessKey.isWriteAble(moduleName))) {
					response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
					response.sendError(MyAuth.UNAUTHORIZED_CODE);
					return false;
				}

 
				
				if ("GET".equalsIgnoreCase(request.getMethod())) {

					TreeMap<String, String> singParam = new TreeMap<>();
					request.getParameterNames().asIterator().forEachRemaining(paramName -> {
						singParam.put(paramName, request.getParameter(paramName));
					});
					boolean verifySuccess = SHAUtil.verifySign(accessKey.getSecret(), SignUtil.toSignSrc(singParam, accessTimeStamp), accessSign);
					if (!verifySuccess) {
						response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
						response.sendError(MyAuth.UNAUTHORIZED_CODE);
						return false;
					}
					MyAccessKeyAuth.CURRENT_COMPANY_ID.set(accessKey.getCompany()); 
					MyAccessKeyAuth.ACCESS_KEY.set(accessKey.getKey());
					return AsyncHandlerInterceptor.super.preHandle(request, response, handler);

				} else if ("application/json".equalsIgnoreCase(request.getContentType()) && request instanceof BodyReaderHttpServletRequestWrapper) {

					BodyReaderHttpServletRequestWrapper wrapper = (BodyReaderHttpServletRequestWrapper) request;
					String requestBody = wrapper.getRequestBody();
					REQUEST_BODY.set(requestBody);
					boolean verifySuccess = SHAUtil.verifySign(accessKey.getSecret(), requestBody + "&" + accessTimeStamp, accessSign);
					if (!verifySuccess) {
						response.setHeader(MyAuth.UNAUTHORIZED_MSG, "1");
						response.sendError(MyAuth.UNAUTHORIZED_CODE);
						return false;
					}
					MyAccessKeyAuth.CURRENT_COMPANY_ID.set(accessKey.getCompany()); 
					MyAccessKeyAuth.ACCESS_KEY.set(accessKey.getKey());
					return AsyncHandlerInterceptor.super.preHandle(wrapper, response, handler); 
				} 
				return false; 
			}  

			  
			return AsyncHandlerInterceptor.super.preHandle(request, response, handler);

		} catch (Throwable e) {
			log.error("error",e);
		} finally { 
		}
		return false;
	}

	private void clearSession() {
		flag.remove();
		MyAccessKeyAuth.CURRENT_COMPANY_ID.remove();
		MyAccessKeyAuth.ACCESS_KEY.remove();
		REQUEST_BODY.remove();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		try {
			
			JSONObject req = new JSONObject(); 
			req.put("ip", request.getHeader(X_REAL_IP));
			req.put("t", System.currentTimeMillis());
			req.put("method", request.getMethod());
			req.put("companyId", MyAccessKeyAuth.CURRENT_COMPANY_ID.get());
			req.put("access_key", MyAccessKeyAuth.ACCESS_KEY.get());
			req.put("cost", System.currentTimeMillis() - t.get());
			req.put("uri", request.getRequestURI());
			req.put("url_param", JSON.toJSONString(request.getParameterMap()));
			if("POST".equalsIgnoreCase(request.getMethod())&&"application/json".equalsIgnoreCase(request.getContentType()) && request instanceof BodyReaderHttpServletRequestWrapper) {
				req.put("req_body", JSON.toJSONString(request.getParameterMap()));
			} 
			if(BooleanUtils.isNotTrue(DISABLE_RESPONSE_LOG.get())) {
				req.put("response", SessionUtil.CURRENT_RESPONSE.get());
			} 
			StringBuilder str = new StringBuilder(); 
			str.append("REQUEST_INFO:").append(req); 
			log.info(str.toString());
		} catch (Throwable e) {

		}finally {
			clearSession();
		}

		AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

}
