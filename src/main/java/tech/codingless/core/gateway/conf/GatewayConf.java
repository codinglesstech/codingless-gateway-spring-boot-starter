package tech.codingless.core.gateway.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConf {

 
  
 
	@Bean
	public ModuleConf initModuleConf() {
		return new ModuleConf();
	}

 
 
	
 
}
