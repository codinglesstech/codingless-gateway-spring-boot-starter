package tech.codingless.core.gateway.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import tech.codingless.core.gateway.util.RedisUtil;
@Component
public class GatewayLoaded implements ApplicationListener<ApplicationStartedEvent> {

  
	@Value("${redis.hostName:}")
	private String host;
	@Value("${redis.port:}")
	private String port;
	@Value("${redis.password:}")
	private String pwd;
	@Value("${redis.database:}")
	private String db;
	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) { 
		RedisUtil.init(host, port, pwd, db);
	}

}
