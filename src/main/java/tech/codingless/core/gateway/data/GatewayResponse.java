package tech.codingless.core.gateway.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果
 * 
 * @author wanghongyan
 *
 */
public class GatewayResponse {
	private Map<String, Object> content = new HashMap<>();
	private String requestId;
	private String contentBiz;
	private String code;
	private String errorCode; 
	
	public String getErrorCode() {
		return errorCode;
	} 
	private String msg;

	public String getContentBiz() {
		return contentBiz;
	}

	public String getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}


	public GatewayResponse fail(String errorCode,String errorMsg) {
		this.code = "fail";
		this.errorCode = errorCode;
		this.msg = errorMsg;
		return this;
	}
	
	public GatewayResponse fail(String errorMessage) {
		this.code = "fail";
		this.msg = errorMessage;
		return this;
	}

	public GatewayResponse success() {
		this.code = "success";
		return this;
	}

	public String getRequestId() {
		return requestId;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	/**
	 * 为了方便统一内容拦截，请设置数据类型
	 * 
	 * @param contentBiz
	 * @return this
	 */
	public GatewayResponse setContentBiz(String contentBiz) {
		this.contentBiz = contentBiz;
		return this;
	}

	public GatewayResponse addContent(String name, Object value) {
		content.put(name, value);
		return this;
	}
}
