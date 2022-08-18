package tech.codingless.core.gateway;

import tech.codingless.core.gateway.data.GatewayResponse;

public class BaseController {

	protected GatewayResponse resp() {
		GatewayResponse resp = new GatewayResponse();
		return resp.success();
	}
}
