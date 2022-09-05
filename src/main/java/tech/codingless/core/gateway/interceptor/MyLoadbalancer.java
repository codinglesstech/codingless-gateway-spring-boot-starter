package tech.codingless.core.gateway.interceptor;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import reactor.core.publisher.Mono;
import tech.codingless.core.gateway.route.RouteDefinitionData;

@Component
public class MyLoadbalancer implements ReactorServiceInstanceLoadBalancer {

	@SuppressWarnings("rawtypes")
	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {

		if(!(request.getContext()  instanceof RequestDataContext)) {
			return Mono.just(new EmptyResponse());
		}
		
		System.out.println(JSON.toJSONString(RouteDefinitionData.routes.values())); 
		RequestDataContext context = (RequestDataContext) request.getContext();
		String path = context.getClientRequest().getUrl().getPath(); 

		System.out.println(path);
		System.out.println(JSON.toJSONString(request.getContext())); 
		System.out.println("进来了自定义负载..");
		
		return RouteDefinitionData.next(path);
		
		/**
		DefaultServiceInstance instance = new DefaultServiceInstance();
		instance.setHost("47.113.109.101");
		instance.setPort(8082);
		instance.setServiceId("selleroa-ext");
		instance.setInstanceId("47.113.109.101");
		// instance.setUri(URI.create("lb://selleroa-ext1"));
		instance.setUri(URI.create("http://47.113.109.101:8082"));
		Response<ServiceInstance> resp = new DefaultResponse(instance);
		return Mono.just(resp);
		*/
	}

}
