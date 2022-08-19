package tech.codingless.core.gateway;

import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.util.SessionUtil;

public class BaseController {

	protected GatewayResponse resp() {
		GatewayResponse resp = new GatewayResponse();
		resp.setRequestId(SessionUtil.RID.get());
		return resp.success();
	}
	
	protected GatewayResponse resp(String tag) { 
		return this.resp().setContentTag(tag);
	}
}
