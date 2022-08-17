package tech.codingless.core.gateway;

import javax.annotation.Resource;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

 
@RestController
@RequestMapping(value = "/test")
public class TestController{

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
 
	 
	@Resource
	private RestTemplate restTemplate;

 

	@GetMapping(value = "/echo2")
	public String echo2() { 
		return restTemplate.getForObject("http://biz/module/test/v1/test/echo?hello=abc", String.class);
	}

	 
}
