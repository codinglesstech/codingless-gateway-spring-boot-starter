package tech.codingless.core.gateway.data;

/**
 * 
 * 用户生产环境内存问题定位
 */
public class MyMemoryAnalysisFlag {

	public MyMemoryAnalysisFlag(String scene,String reqId) {
		this.scene = scene;
		this.t = System.currentTimeMillis();
		this.reqId = reqId;
	}
	
	private String urlParam;
	private String reqBody;
	private String reqId;
	private String scene;
	private long t;
	
	public String getReqBody() {
		return reqBody;
	}
	
	public String getUrlParam() {
		return urlParam;
	}
	public void setReqBody(String reqBody) {
		this.reqBody = reqBody;
	}
	public void setUrlParam(String urlParam) {
		this.urlParam = urlParam;
	}
	public String getReqId() {
		return reqId;
	}

	public void setT(long t) {
		this.t = t;
	}

	public long getT() {
		return t;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public String getScene() {
		return scene;
	}

}
