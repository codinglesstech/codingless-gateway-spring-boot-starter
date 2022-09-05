package tech.codingless.core.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.route.RouteDefinition;

import lombok.Data;
import reactor.core.publisher.Mono;
import tech.codingless.core.gateway.util.IntegerUtil;

public class RouteDefinitionData {
	public final static Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());
	public final static ConcurrentHashMap<String, List<Response<ServiceInstance>>> backends = new ConcurrentHashMap<>();

	@Data
	public static class RouteDefinitionParam {
		private String id;
		private Integer order;
		private String uri;
		private List<PredicateDefinitionParam> predicates;
	}

	private static Response<ServiceInstance> empty = new EmptyResponse();

	@Data
	public static class PredicateDefinitionParam {
		private String name;
		private Map<String, String> args;
	}

	
	@Data
	public static class ServiceDefinitionParam {
		private String matchPath;
		private String serviceId;
		private int port;
		private String host;
		private String instanceId;
		private String uri;
		
		 
	}
	
	
	
	public static Mono<Response<ServiceInstance>> next(String path) {
		if (backends.isEmpty()) {
			return Mono.just(empty);
		}

		Enumeration<String> keys = backends.keys();

		String key = null;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			if (path.startsWith(key)) {
				List<Response<ServiceInstance>> list = backends.get(key);
				if(list.isEmpty()) {
					continue;
				}
				return Mono.just(list.get(IntegerUtil.random(0, list.size()-1))); 
			} 
		}

		return Mono.just(empty);
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



	public static void addService(ServiceDefinitionParam param) {
		 
		DefaultServiceInstance instance = new DefaultServiceInstance();
		instance.setHost(param.getHost());
		instance.setPort(param.getPort());
		instance.setServiceId(param.getServiceId());
		instance.setInstanceId(param.getInstanceId()); 
		instance.setUri(URI.create(param.getUri()));
		Response<ServiceInstance> resp = new DefaultResponse(instance);
		
		if(!backends.containsKey(param.getMatchPath())) {
			backends.put(param.getMatchPath(), new ArrayList<>());
		}  
		List<Response<ServiceInstance>> list = 	backends.get(param.getMatchPath());
		list.removeIf(item->item.getServer().getInstanceId().equals(param.getInstanceId()));
		list.add(resp); 
		
	}



	public static void deleteService(String instanceId) { 
		backends.values().forEach(list->{
			list.removeIf(item->item.getServer().getInstanceId().equals(instanceId));
		});
	}
}
