package tech.codingless.core.gateway.interceptor;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.stereotype.Component;

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
		 
		RequestDataContext context = (RequestDataContext) request.getContext();
		String path = context.getClientRequest().getUrl().getPath(); 
 
		return RouteDefinitionData.next(path);
	 
	}

}
