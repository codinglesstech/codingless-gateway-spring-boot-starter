package tech.codingless.core.gateway.route;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import tech.codingless.core.gateway.BaseController;
import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.util.AssertUtil;

@RestController
@RequestMapping(value = "/gateway/route")
public class RouteApiController extends BaseController implements ApplicationEventPublisherAware {
	private ApplicationEventPublisher applicationEventPublisher;
	@Resource
	private RouteDefinitionWriter routeDefinitionWriter;

	@GetMapping("/")
	public GatewayResponse get() {
		return resp().addContent("routes", RouteDefinitionData.routes.values());
	}

	@GetMapping("/{id}")
	public GatewayResponse getById(@PathVariable("id") String id) {
		return resp().addContent("route", RouteDefinitionData.routes.get(id));
	}

	@PostMapping("/refresh")
	public GatewayResponse refresh() {
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(true));
		return resp().addContent("routes", RouteDefinitionData.routes.values());
	}

	@PostMapping("/{id}")
	public GatewayResponse create(@PathVariable("id") String id, @RequestBody RouteDefinitionData.RouteDefinitionParam param) {
		AssertUtil.assertNotEmpty(param.getId(), "ID_EMPTY");
		AssertUtil.assertNotEmpty(param.getUri(), "URI_EMPTY");
		AssertUtil.assertNotEmpty(param.getPredicates(), "PREDICATES_EMPTY");
		AssertUtil.assertTrue(id.equalsIgnoreCase(param.getId()), "ID_NOT_MATCH");

		RouteDefinition route = new RouteDefinition();
		route.setId(param.getId());
		route.setOrder(param.getOrder() == null ? 0 : param.getOrder());

		List<PredicateDefinition> predicates = new ArrayList<>();
		param.getPredicates().forEach(predicate -> {
			PredicateDefinition pd = new PredicateDefinition();
			pd.setName(predicate.getName());// Path
			// Map<String,String> args = new HashMap<>();
			// args.put("Path", "/v1/oaExt/**");
			pd.setArgs(predicate.getArgs());
			predicates.add(pd);
		});

		route.setPredicates(predicates);
		// route.setUri(URI.create("lb://selleroa-ext"));
		route.setUri(URI.create(param.getUri()));
		RouteDefinitionData.routes.put(param.getId(), route);
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(true));
		return resp().addContent("route", route);
	}

	@DeleteMapping("/{id}")
	public GatewayResponse del(@PathVariable("id") String id) {
		RouteDefinitionData.routes.remove(id);
		routeDefinitionWriter.delete(Mono.just(id));
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(true));
		return resp().success();
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;

	}
}
