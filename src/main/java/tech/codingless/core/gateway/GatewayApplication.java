package tech.codingless.core.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;


@EnableDiscoveryClient
@NacosPropertySource(dataId = "nacos_attr1", autoRefreshed = true)
@EnableScheduling 
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = { "tech.codingless.core.gateway" })
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args); 
		//GracefulShutdownCallback
	}
	
 

}
