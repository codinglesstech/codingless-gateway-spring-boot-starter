package tech.codingless.core.gateway.interceptor;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.gateway.annotation.GrantModuleCondition;
import tech.codingless.core.gateway.annotation.MyBiz;
import tech.codingless.core.gateway.annotation.MySignAuth;
import tech.codingless.core.gateway.annotation.MyTokenAuth;
import tech.codingless.core.gateway.data.MyMemoryAnalysisFlag;
import tech.codingless.core.gateway.helper.AccessKeyHelper;
import tech.codingless.core.gateway.helper.AccessKeyHelper.AccessKey;
import tech.codingless.core.gateway.helper.RequestMonitorHelper;
import tech.codingless.core.gateway.service.AuthService;
import tech.codingless.core.gateway.util.DateUtil;
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
	private static final String DEFAULT_MODULE = "00000";
	private static final String ACCESS_TOKEN = "Access-Token";

	private static final ThreadLocal<MyMemoryAnalysisFlag> flag = new ThreadLocal<MyMemoryAnalysisFlag>();
	private static final ThreadLocal<Long> t = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> DISABLE_LOG = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> DISABLE_RESPONSE_LOG = new ThreadLocal<>();
	private static final ThreadLocal<String> REQUEST_BODY = new ThreadLocal<String>();

	@Autowired(required = false)
	private AuthService authService;

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		AsyncHandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			if (!(handler instanceof HandlerMethod)) {
				return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
			}
			clearSession();
			t.set(System.currentTimeMillis());
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			MyBiz myBiz = handlerMethod.getMethodAnnotation(MyBiz.class);
			if (myBiz != null) {
				DISABLE_LOG.set(myBiz.disableRequestLog());
				DISABLE_RESPONSE_LOG.set(myBiz.disableResponseLog());
			} 

			String moduleName = GrantModuleCondition.findModuleNameByResourcePkg(handlerMethod.getBean().getClass());
			REQUEST_BODY.remove();
			if (handlerMethod.getMethodAnnotation(MySignAuth.class) != null) {
				/**
				 * 签名认证
				 */
				MySignAuth mySignAuth = handlerMethod.getMethodAnnotation(MySignAuth.class);
				String accessKeyStr = request.getHeader(ACCESS_KEY);
				String accessTimeStamp = request.getHeader(ACCESS_TIMESTAMP);
				String accessSign = request.getHeader(ACCESS_SIGN);
				if (StringUtil.hasEmpty(accessKeyStr, accessTimeStamp, accessSign)) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}

				String signData = null;
				if ("GET".equalsIgnoreCase(request.getMethod())) {
					TreeMap<String, String> singParam = new TreeMap<>();
					request.getParameterNames().asIterator().forEachRemaining(paramName -> {
						singParam.put(paramName, request.getParameter(paramName));
					});
					signData = SignUtil.toSignSrc(singParam, accessTimeStamp);
				} else if ("application/json".equalsIgnoreCase(request.getContentType()) && request instanceof BodyReaderHttpServletRequestWrapper) {
					BodyReaderHttpServletRequestWrapper wrapper = (BodyReaderHttpServletRequestWrapper) request;
					String requestBody = wrapper.getRequestBody();
					REQUEST_BODY.set(requestBody);
					signData = requestBody + "&" + accessTimeStamp;
				}

				if (authService != null) {
					AuthService.SignAuthRequest authRequest = new AuthService.SignAuthRequest();
					authRequest.setIp(request.getHeader(X_REAL_IP));
					authRequest.setUri(request.getRequestURI());
					authRequest.setSign(accessSign);
					authRequest.setSignKey(accessKeyStr);
					authRequest.setSignTimestamp(accessTimeStamp);
					authRequest.setSignData(signData);
					AuthService.SignAuthResponse authResponse = authService.signAuth(authRequest);
					if (!authResponse.isAllowed()) {
						notAuthResponse(request, response, handlerMethod);
						return false;
					}
					setRequestLog(moduleName, request, response);
					MySignAuth.CURRENT_COMPANY_ID.set(authResponse.getCompanyId());
					MySignAuth.ACCESS_KEY.set(authResponse.getSignKey());
					SessionUtil.CURRENT_COMPANY_ID.set(authResponse.getCompanyId());
					SessionUtil.CURRENT_USER_ID.set(authResponse.getSignKey());
					return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
				}

				AccessKey accessKey = AccessKeyHelper.get(accessKeyStr.trim());
				if (StringUtil.isEmpty(moduleName)) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}

				if (!accessKey.isReadAble(moduleName) && !accessKey.isWriteAble(moduleName)) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}
				if (mySignAuth.requiredWriteAble() && BooleanUtils.isFalse(accessKey.isWriteAble(moduleName))) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}
				boolean signVerify = SHAUtil.verifySign(accessKey.getSecret(), signData, accessSign);
				if (!signVerify) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}
				setRequestLog(moduleName, request, response);
				MySignAuth.CURRENT_COMPANY_ID.set(accessKey.getCompany());
				MySignAuth.ACCESS_KEY.set(accessKey.getKey());
				SessionUtil.CURRENT_COMPANY_ID.set(accessKey.getCompany());
				SessionUtil.CURRENT_USER_ID.set(accessKey.getKey());
				return AsyncHandlerInterceptor.super.preHandle(request, response, handler);

			} else if (handlerMethod.getMethodAnnotation(MyTokenAuth.class) != null) {
				/**
				 * Token 认证
				 */

				MyTokenAuth myTokenAuth = handlerMethod.getMethodAnnotation(MyTokenAuth.class);
			
				if (authService != null) { 
					AuthService.TokenAuthResponse authResponse = authService.tokenAuth(request);
					if (myTokenAuth.required()&&!authResponse.isAllowed()) {
						notAuthResponse(request, response, handlerMethod);
						return false;
					}
					setRequestLog(moduleName, request, response);  
					SessionUtil.CURRENT_COMPANY_ID.set(authResponse.getCompanyId());
					SessionUtil.CURRENT_USER_ID.set(authResponse.getUserId());
					SessionUtil.CURRENT_USER_NAME.set(authResponse.getUserName());
					return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
				} 
				
				String token = request.getHeader(ACCESS_TOKEN);
				if (myTokenAuth.required()&&StringUtil.isEmpty(token)) {
					notAuthResponse(request, response, handlerMethod);
					return false;
				}
				
				if(myTokenAuth.required()) { 
					notAuthResponse(request, response, handlerMethod);
					return false;
				} 
				return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
			} else {
				/**
				 * 无认证
				 */
				setRequestLog(DEFAULT_MODULE, request, response);
				return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
			} 
		} catch (Throwable e) {
			log.error("error", e);
		} finally {
		}

		return false;
	}

	private void notAuthResponse(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws IOException {
		try {

			response.setHeader(MyTokenAuth.UNAUTHORIZED_MSG, "1");
			response.sendError(MyTokenAuth.UNAUTHORIZED_CODE);
			SessionUtil.CURRENT_RESPONSE.set(MyTokenAuth.UNAUTHORIZED_MSG);
			appendLog(request, response, handlerMethod, null);

		} catch (Throwable e) {

		} finally {
			clearSession();
		}
	}

	private void appendLog(HttpServletRequest request, HttpServletResponse response, Object handlerMethod, Exception ex) throws IOException {
		if (BooleanUtils.isTrue(DISABLE_LOG.get())) {
			return;
		}
		// 记请求日志
		String uri = getUri(request, handlerMethod);

		String urlParam = request.getParameterMap().isEmpty() ? null : JSON.toJSONString(request.getParameterMap());
		long cost = System.currentTimeMillis() - t.get();
		JSONObject req = new JSONObject();
		req.put("ip", request.getHeader(X_REAL_IP));
		req.put("t", System.currentTimeMillis());
		req.put("method", request.getMethod());
		req.put("companyId", MySignAuth.CURRENT_COMPANY_ID.get());
		req.put("access_key", MySignAuth.ACCESS_KEY.get());
		req.put("cost", cost);
		req.put("uri", uri);
		req.put("url", request.getRequestURL().toString());
		req.put("url_param", urlParam);
		if ("POST".equalsIgnoreCase(request.getMethod()) && "application/json".equalsIgnoreCase(request.getContentType()) && request instanceof BodyReaderHttpServletRequestWrapper) {
			req.put("req_body", REQUEST_BODY.get());
		}
		if (BooleanUtils.isNotTrue(DISABLE_RESPONSE_LOG.get())) {
			req.put("response", SessionUtil.CURRENT_RESPONSE.get());
		}
		StringBuilder str = new StringBuilder();
		str.append("REQUEST_INFO:").append(req);
		log.info(str.toString());

		RequestMonitorHelper.append(SessionUtil.CURRENT_COMPANY_ID.get(), SessionUtil.CURRENT_USER_ID.get(), SessionUtil.CURRENT_USER_NAME.get(), SessionUtil.RID.get(), uri,
				request.getRequestURL().toString(), cost, urlParam, REQUEST_BODY.get(), BooleanUtils.isNotTrue(DISABLE_RESPONSE_LOG.get()) ? SessionUtil.CURRENT_RESPONSE.get() : null, ex);

	}

	private void setRequestLog(String moduleName, HttpServletRequest request, HttpServletResponse response) {
		if(moduleName==null) {
			moduleName="00000";
		}
		String requestId = DateUtil.formatYYYYMMDD(new Date()) + "-REQ-" + moduleName.replace("/", "").toUpperCase() + "-" + StringUtil.genGUID() + "-" + StringUtil.genShortGUID().toLowerCase();
		response.addHeader("Request-Id", requestId);
		SessionUtil.RID.set(requestId);
		MyMemoryAnalysisFlag maf = new MyMemoryAnalysisFlag("REQ:" + request.getRequestURL().toString(), requestId);
		maf.setReqBody(REQUEST_BODY.get());
		if (!request.getParameterMap().isEmpty()) {
			maf.setUrlParam(JSON.toJSONString(request.getParameterMap()));
		}
		flag.set(maf);
		RequestMonitorHelper.append(maf);
	}

	private void clearSession() {
		RequestMonitorHelper.clear(flag.get());
		flag.remove();
		MySignAuth.CURRENT_COMPANY_ID.remove();
		MySignAuth.ACCESS_KEY.remove();
		REQUEST_BODY.remove();
		SessionUtil.CURRENT_COMPANY_ID.remove();
		SessionUtil.CURRENT_USER_ID.remove();
		DISABLE_LOG.remove();
		SessionUtil.clean();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		try {

			// 记请求日志
			appendLog(request, response, handler, ex);
		} catch (Throwable e) {

		} finally {
			clearSession();
		}

		AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

	private static final ConcurrentHashMap<String, String> URI_CATCH = new ConcurrentHashMap<>();

	private String getUri(HttpServletRequest request, Object handler) {
		if (URI_CATCH.containsKey(handler.toString())) {
			return URI_CATCH.get(handler.toString());
		}
		StringBuilder uri = new StringBuilder();
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;

			RequestMapping reqMapping = method.getBeanType().getAnnotation(RequestMapping.class);
			if (reqMapping != null) {
				uri.append(reqMapping.value()[0]);
			}
			GetMapping getMapping = method.getMethodAnnotation(GetMapping.class);
			if (getMapping != null) {
				uri.append(getMapping.value()[0]);
			} else if (method.getMethodAnnotation(PostMapping.class) != null) {
				uri.append(method.getMethodAnnotation(PostMapping.class).value()[0]);
			} else if (method.getMethodAnnotation(PutMapping.class) != null) {
				uri.append(method.getMethodAnnotation(PutMapping.class).value()[0]);
			} else if (method.getMethodAnnotation(DeleteMapping.class) != null) {
				uri.append(method.getMethodAnnotation(DeleteMapping.class).value()[0]);
			} else {
				uri.append(request.getRequestURI());
			}
		}
		URI_CATCH.put(handler.toString(), uri.toString());
		return uri.toString();
	}

}
