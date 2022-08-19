package tech.codingless.core.gateway.helper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import tech.codingless.core.gateway.util.StackTraceUtil;

public class RequestMonitorHelper {

	private static ConcurrentHashMap<String, RequestAnalysis> CACHE = new ConcurrentHashMap<>();

	private static int MAX_NORMALS_SIZE=20;
	private static int MAX_ERRORS_SIZE=20;
	
	@Data
	public static class RequestAnalysis {
		private String uri;
		private long cost; 
		private long times;
		private LinkedList<RequestLog> normals; //
		private LinkedList<RequestLog> errors;
	}

	@Data
	public static class RequestLog {
		private String url;
		private long cost;
		private long t;
		private String companyId;
		private String userId;
		private String reqId;
		private String urlParam;
		private String reqBody;
		private String response;
		private String exceptionTrace;
	}

	public static ConcurrentHashMap<String, RequestAnalysis> getAllLog(){
		return CACHE;
	}
	
	public static List<RequestAnalysis> getLog(String subUri){
		List<RequestAnalysis> list = new ArrayList<>();
		Enumeration<String> keys = CACHE.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			if(key.contains(subUri)) {
				list.add(CACHE.get(key));
			}
		}
		return list;
	}
	public static void clear() {
		CACHE.clear();
	}

	public static void append(String companyId, String userId, String reqId, String uri, String url, long cost, String urlParam, String reqBody, String response,Exception e) {
		RequestAnalysis ra = CACHE.get(uri);
		if(ra==null) {
			ra = new RequestAnalysis();
			ra.setUri(uri);
			ra.setCost(0);  
			ra.setTimes(0);  
			ra.setNormals(new LinkedList<>());
			ra.setErrors(new LinkedList<>());
			CACHE.put(uri, ra);
		}
		
		ra.setCost(ra.getCost()+cost);
		ra.setTimes(ra.getTimes()+1); 
		RequestLog log = new RequestLog();
		log.setUrl(url);
		log.setCompanyId(companyId);
		log.setUserId(userId);
		log.setT(System.currentTimeMillis());
		log.setReqId(reqId);
		log.setCost(cost);
		log.setUrlParam(urlParam);
		log.setReqBody(reqBody);
		log.setResponse(response);
		if(e!=null) {
			if(ra.getErrors().size()>MAX_ERRORS_SIZE) {
				ra.getErrors().pollLast();
			}
			ra.getErrors().push(log);
			log.setExceptionTrace(StackTraceUtil.format(e));  
		}else {
			if(ra.getNormals().size()>MAX_NORMALS_SIZE) {
				ra.getNormals().pollLast();
			}
			ra.getNormals().push(log);
		}

		
	}

}
