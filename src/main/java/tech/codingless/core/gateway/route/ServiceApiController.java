package tech.codingless.core.gateway.route;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.codingless.core.gateway.BaseController;
import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.util.AssertUtil;

@RestController
@RequestMapping(value = "/gateway/service")
public class ServiceApiController extends BaseController {

	@GetMapping("/")
	public GatewayResponse get() {
		return resp().addContent("services", RouteDefinitionData.backends).addContent("disabledServices", RouteDefinitionData.DISABLED_SERVICE);
	}

	@PostMapping("/{id}")
	public GatewayResponse create(@PathVariable("id") String id, @RequestBody RouteDefinitionData.ServiceDefinitionParam param) {
		AssertUtil.assertNotEmpty(param.getHost(), "host_EMPTY");
		AssertUtil.assertNotEmpty(param.getServiceId(), "service_id_empty");
		AssertUtil.assertNotEmpty(param.getInstanceId(), "instance_id_empty");
		AssertUtil.assertNotEmpty(param.getMatchPath(), "match_path_empty");
		AssertUtil.assertTrue(param.getInstanceId().equalsIgnoreCase(id), "instance_id_error");
		AssertUtil.assertTrue(param.getPort() > 1000, "port_error");

		param.setUri("http://" + param.getHost() + ":" + param.getPort());
		RouteDefinitionData.addService(param);
		RouteDefinitionData.persistence();
		return resp().addContent("service", param);
	}

	@PostMapping("/{id}")
	public GatewayResponse disable(@PathVariable("id") String id) {
		RouteDefinitionData.disableService(id);
		return resp().addContent("id", id);
	}
	
	@PostMapping("/{id}")
	public GatewayResponse enable(@PathVariable("id") String id) {
		RouteDefinitionData.enableService(id);
		return resp().addContent("id", id);
	}
	

	@DeleteMapping("/{id}")
	public GatewayResponse del(@PathVariable("id") String id) {
		RouteDefinitionData.deleteService(id);
		RouteDefinitionData.persistence();
		return resp().success();
	}

}
