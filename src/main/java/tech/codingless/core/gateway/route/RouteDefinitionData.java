package tech.codingless.core.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.route.RouteDefinition;

import lombok.Data;

public class RouteDefinitionData {
	public final static Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());

	@Data
	public static class RouteDefinitionParam{
		private String id;
		private Integer order; 
		private String uri;
		private List<PredicateDefinitionParam> predicates;
	}

	@Data
	public static class PredicateDefinitionParam{
		private String name;
		private Map<String,String> args;
	}
}
