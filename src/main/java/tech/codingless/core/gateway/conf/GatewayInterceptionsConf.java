package tech.codingless.core.gateway.conf;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tech.codingless.core.gateway.interceptor.GatewayInterceptor;
import tech.codingless.core.gateway.interceptor.GatewayResponseHttpMessageConverter;
import tech.codingless.core.gateway.interceptor.GatewayStringHttpMessageConverter;

 
@Configuration
public class GatewayInterceptionsConf  implements WebMvcConfigurer{
	
	@Resource
	private GatewayInterceptor gatewayInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {  
		registry.addInterceptor(gatewayInterceptor).addPathPatterns("/**");
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		GatewayStringHttpMessageConverter converter = new GatewayStringHttpMessageConverter();
		//converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON)); 
		converters.add(0, converter); //保证高优先级处理,不然不会处理
		
		GatewayResponseHttpMessageConverter respConverter = new GatewayResponseHttpMessageConverter(); 
		converters.add(0,respConverter); 
		WebMvcConfigurer.super.extendMessageConverters(converters);
	}
	
}
