package tech.codingless.core.gateway.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.util.NestedServletException;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.util.MyException;

@Slf4j
@Component
@WebFilter("/*")
public class WrapRequestFilter implements Filter {
	
	private static final String APPLICATION_JSON = "application/json";
	private static final String ACCESS_KEY = "Access-Key";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		try {
			if (request instanceof HttpServletRequest && APPLICATION_JSON.equalsIgnoreCase(request.getContentType()) && StringUtil.isNotEmpty(((HttpServletRequest) request).getHeader(ACCESS_KEY))) {
				chain.doFilter(new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request), response);
			} else {
				chain.doFilter(request, response);
			} 
		}catch(NestedServletException e) {

			log.error("error",e); 
			Throwable cause = e.getCause();
			if(cause instanceof MyException) {
			 
				GatewayResponse resp = new GatewayResponse();
				String [] msg = cause.getMessage().split(":");
				resp.fail(msg[0], msg.length==2?msg[1]:cause.getMessage());
				if(response instanceof HttpServletResponse) {
					((HttpServletResponse)response).addHeader("Content-Type", "application/json");
				}
				response.getWriter().write(JSON.toJSONString(resp));
			} 
			
		}catch(MyException e) {
			GatewayResponse resp = new GatewayResponse();
			String [] msg = e.getMessage().split(":");
			resp.fail(msg.length==2?msg[0]:"FAIL", msg.length==2?msg[1]:e.getMessage());
			response.getWriter().write(JSON.toJSONString(resp));
			log.error("error",e); 
		}catch(Throwable e) {
			log.error("error",e);
		}


	}

}
