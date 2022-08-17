package tech.codingless.core.gateway.conf;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tech.codingless.core.gateway.interceptor.GatewayInterceptor;

 
@Configuration
public class GatewayInterceptionsConf  implements WebMvcConfigurer{
	
	@Resource
	private GatewayInterceptor gatewayInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {  
		registry.addInterceptor(gatewayInterceptor).addPathPatterns("/**");
		WebMvcConfigurer.super.addInterceptors(registry);
	}
}
