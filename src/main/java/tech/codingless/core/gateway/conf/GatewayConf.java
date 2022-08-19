package tech.codingless.core.gateway.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tech.codingless.core.gateway.interceptor.GatewayInterceptor;
import tech.codingless.core.gateway.interceptor.WrapRequestFilter;
import tech.codingless.core.gateway.stat.GatewayStatController;

@Configuration
public class GatewayConf {

	@Bean
	public GatewayInterceptor initGatewayInterceptor() {
		GatewayInterceptor catewayInterceptor = new GatewayInterceptor();
		return catewayInterceptor;
	}

	@Bean
	public GatewayStatController initGatewayStatController() {
		return new GatewayStatController();
	}
	 
	@Bean
	public GatewayInterceptionsConf initGatewayInterceptionsConf() {
		return new GatewayInterceptionsConf();
	}
	
	@Bean
	public ModuleConf initModuleConf() {
		return new ModuleConf();
	}

	@Bean
	public WrapRequestFilter initWrapRequestFilter() { 
		return new WrapRequestFilter();
	}
 
 
	
 
}
