package tech.codingless.core.gateway.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.util.StringUtil;

@Component
@WebFilter("/*")
public class WrapRequestFilter implements Filter {

	private static final String APPLICATION_JSON = "application/json";
	private static final String ACCESS_KEY = "Access-Key";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if (request instanceof HttpServletRequest && APPLICATION_JSON.equalsIgnoreCase(request.getContentType()) && StringUtil.isNotEmpty(((HttpServletRequest) request).getHeader(ACCESS_KEY))) {
			chain.doFilter(new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request), response);
		} else {
			chain.doFilter(request, response);
		}

	}

}
