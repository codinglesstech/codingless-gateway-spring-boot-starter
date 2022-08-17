package tech.codingless.core.gateway.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.gateway.annotation.MyBiz;
import tech.codingless.core.gateway.data.MyMemoryAnalysisFlag;
import tech.codingless.core.gateway.util.StringUtil;

@Slf4j
public class GatewayInterceptor implements AsyncHandlerInterceptor {

	private ThreadLocal<MyMemoryAnalysisFlag> flag = new ThreadLocal<MyMemoryAnalysisFlag>();
	private ThreadLocal<Long> t = new ThreadLocal<>();

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		AsyncHandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			t.set(System.currentTimeMillis());
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			MyBiz myBiz = handlerMethod.getMethodAnnotation(MyBiz.class);
			String requestId = StringUtil.genGUID();
			response.addHeader("Request-Id", requestId);
			flag.set(new MyMemoryAnalysisFlag("REQ:" + request.getRequestURL().toString(), requestId));
			return AsyncHandlerInterceptor.super.preHandle(request, response, handler);

		} catch (Throwable e) {

		} finally {
			flag.remove();

		}
		return false;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		try {
			StringBuilder str = new StringBuilder();
			str.append("REQ ").append(request.getMethod());
			str.append("\tcost:").append(System.currentTimeMillis() - t.get());
			str.append("\turl:").append(request.getRequestURL().toString());
			str.append("\tparam:").append(JSON.toJSONString(request.getParameterMap()));
			str.append("\tresponse:").append("xxx"); 
			log.info(str.toString());
		} catch (Throwable e) {

		}

		AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

}
