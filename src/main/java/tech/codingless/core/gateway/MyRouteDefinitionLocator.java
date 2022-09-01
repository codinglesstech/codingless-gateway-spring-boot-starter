package tech.codingless.core.gateway;

import static java.util.Collections.synchronizedMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MyRouteDefinitionLocator implements RouteDefinitionRepository,ApplicationEventPublisherAware  {


	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());
	
	private boolean testDel;
	
	ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private RouteDefinitionWriter routeDefinitionWriter;
	@Override
	public Flux<RouteDefinition> getRouteDefinitions() { 
		List<RouteDefinition> list = new ArrayList<>();
		if(testDel) { 
			return Flux.fromIterable(list);
			
		}
		
		RouteDefinition route = new RouteDefinition();
		route.setId("test1");
		route.setOrder(0);
		
		PredicateDefinition pd = new PredicateDefinition();
		pd.setName("Path");
		Map<String,String> args = new HashMap<>();
		args.put("Path", "/v1/oaExt/**");
		pd.setArgs(args);
		
		
		PredicateDefinition limiter = new PredicateDefinition();
		limiter.setName("MyLimiter");
		limiter.setArgs(new HashMap<>());
		//limiter.addArg("key-resolver", "#{@hostAddrKeyResolver}");
		limiter.addArg("redis-rate-limiter.replenishRate", "1");
		limiter.addArg("redis-rate-limiter.burstCapacity", "1");
		limiter.addArg("redis-rate-limiter.requestedTokens", "1");
		
		route.setPredicates(List.of(pd));
		//route.setUri(URI.create("http://47.113.109.101:8082"));
		//route.setUri(URI.create("lb://servicea"));
		route.setUri(URI.create("lb://selleroa-ext"));
		
		
		/**
		FilterDefinition fd = new FilterDefinition(); 
		fd.setName("RequestRateLimiter");
		fd.setArgs(new HashMap<>());
		fd.addArg("key-resolver", "#{@hostAddrKeyResolver}");
		fd.addArg("rateLimiter", "#{@myRateLimiter}");
		fd.addArg("redis-rate-limiter.replenishRate", "1");
		fd.addArg("redis-rate-limiter.burstCapacity", "2");
		fd.addArg("redis-rate-limiter.requestedTokens", "1");
		route.setFilters(List.of(fd));
		*/
		
		list.add(route); 
		return Flux.fromIterable(list);
	}

	 

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher; 
	}



	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) { 
		testDel=false;
		return null;
	}



	@Override
	public Mono<Void> delete(Mono<String> routeId) { 
		testDel=true;
		return null;
	}

}
