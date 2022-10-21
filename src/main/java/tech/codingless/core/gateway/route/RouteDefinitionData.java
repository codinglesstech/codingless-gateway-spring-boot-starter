package tech.codingless.core.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.route.RouteDefinition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import tech.codingless.core.gateway.util.IntegerUtil;

@Slf4j
public class RouteDefinitionData {
	public final static Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());
	public final static ConcurrentHashMap<String, List<Response<ServiceInstance>>> backends = new ConcurrentHashMap<>();
	public final static ConcurrentHashMap<String, List<Response<ServiceInstance>>> DISABLED_SERVICE = new ConcurrentHashMap<>();

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
				if (list.isEmpty()) {
					continue;
				}
				return Mono.just(list.get(IntegerUtil.random(0, list.size() - 1)));
			}
		}

		return Mono.just(empty);
		/**
		 * DefaultServiceInstance instance = new DefaultServiceInstance();
		 * instance.setHost("47.113.109.101"); instance.setPort(8082);
		 * instance.setServiceId("selleroa-ext");
		 * instance.setInstanceId("47.113.109.101"); //
		 * instance.setUri(URI.create("lb://selleroa-ext1"));
		 * instance.setUri(URI.create("http://47.113.109.101:8082"));
		 * Response<ServiceInstance> resp = new DefaultResponse(instance); return
		 * Mono.just(resp);
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

		if (!backends.containsKey(param.getMatchPath())) {
			backends.put(param.getMatchPath(), new ArrayList<>());
		}
		List<Response<ServiceInstance>> list = backends.get(param.getMatchPath());
		list.removeIf(item -> item.getServer().getInstanceId().equals(param.getInstanceId()));
		list.add(resp);

	}

	public static void addService(String matchPath, ServiceInstance serviceInstance) {

		Response<ServiceInstance> resp = new DefaultResponse(serviceInstance);
		if (!backends.containsKey(matchPath)) {
			backends.put(matchPath, new ArrayList<>());
		}
		List<Response<ServiceInstance>> list = backends.get(matchPath);
		list.removeIf(item -> item.getServer().getInstanceId().equals(serviceInstance.getInstanceId()));
		list.add(resp);

	}

	public static void deleteService(String instanceId) {
		backends.values().forEach(list -> {
			list.removeIf(item -> item.getServer().getInstanceId().equals(instanceId));
		});
	}

	public static void disableService(String id) {
		
		//remove service instance from ready list to disableList
		for (Map.Entry<String, List<Response<ServiceInstance>>> entry : backends.entrySet()) {
			entry.getValue().forEach(service -> {
				if (service.getServer().getInstanceId().equalsIgnoreCase(id)) {
					if(!DISABLED_SERVICE.containsKey(entry.getKey())) {
						DISABLED_SERVICE.put(entry.getKey(), new ArrayList<>());
					}
					DISABLED_SERVICE.get(entry.getKey()).add(service);
				} 
			});
		}

		backends.values().forEach(list -> {  
			list.removeIf(item -> item.getServer().getInstanceId().equals(id));
		});
	}

	
	public static void enableService(String id) {
		for (Map.Entry<String, List<Response<ServiceInstance>>> entry : DISABLED_SERVICE.entrySet()) {
			entry.getValue().forEach(service -> {
				if (service.getServer().getInstanceId().equalsIgnoreCase(id)) {
					  
					DefaultServiceInstance instance = new DefaultServiceInstance();
					instance.setHost(service.getServer().getHost());
					instance.setPort(service.getServer().getPort());
					instance.setServiceId(service.getServer().getServiceId());
					instance.setInstanceId(service.getServer().getInstanceId()); 
					instance.setUri(service.getServer().getUri());
					addService(entry.getKey(), instance);
					 
				} 
			});
		} 
		DISABLED_SERVICE.values().forEach(list -> {  
			list.removeIf(item -> item.getServer().getInstanceId().equals(id));
		});
		
	}
	
	/**
	 * 持久化到本地配置文件
	 */
	public static void persistence() {
		List<String> configLines = new ArrayList<>();
		routes.values().forEach(route -> {
			JSONObject json = new JSONObject();
			json.put("type", "route");
			json.put("data", route);
			configLines.add(json.toJSONString());
		});

		Enumeration<String> enr = backends.keys();
		while (enr.hasMoreElements()) {
			String matchPath = enr.nextElement();
			List<Response<ServiceInstance>> list = backends.get(matchPath);
			list.forEach(service -> {
				JSONObject json = new JSONObject();
				json.put("type", "service");
				json.put("matchPath", matchPath);
				json.put("data", service);
				configLines.add(json.toJSONString());
			});
		}

		
		enr = DISABLED_SERVICE.keys();
		while (enr.hasMoreElements()) {
			String matchPath = enr.nextElement();
			List<Response<ServiceInstance>> list = DISABLED_SERVICE.get(matchPath);
			list.forEach(service -> {
				JSONObject json = new JSONObject();
				json.put("type", "service");
				json.put("matchPath", matchPath);
				json.put("data", service);
				configLines.add(json.toJSONString());
			});
		}
		
		
		FileOutputStream conf = null;
		try {
			File file = new File(System.getProperty("user.home") + File.separator + "gateway.conf");
			conf = new FileOutputStream(file);
			IOUtils.writeLines(configLines, "\r\n", conf, "utf-8");
		} catch (Exception e) {
			log.error("save_error", e);
		} finally {
			IOUtils.closeQuietly(conf);

		}
	}

	/**
	 * 加载配置文件
	 */
	public static void reload() {

		File file = new File(System.getProperty("user.home") + File.separator + "gateway.conf");
		if (!file.exists()) {
			return;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			IOUtils.readLines(fis, "utf-8").forEach(line -> {
				JSONObject oneConf = JSON.parseObject(line);
				if ("route".equalsIgnoreCase(oneConf.getString("type"))) {
					RouteDefinition route = JSON.parseObject(oneConf.getString("data"), RouteDefinition.class);
					routes.put(route.getId(), route);
				} else if ("service".equalsIgnoreCase(oneConf.getString("type"))) {
					String matchPath = oneConf.getString("matchPath");
					JSONObject serviceConf = oneConf.getJSONObject("data").getJSONObject("server");

					DefaultServiceInstance instance = new DefaultServiceInstance();
					instance.setHost(serviceConf.getString("host"));
					instance.setPort(serviceConf.getInteger("port"));
					instance.setServiceId(serviceConf.getString("serviceId"));
					instance.setInstanceId(serviceConf.getString("instanceId"));
					instance.setUri(URI.create(serviceConf.getString("uri")));
					addService(matchPath, instance);
				}

			});
		} catch (Exception e) {
			log.error("reload", e);
		} finally {
			IOUtils.closeQuietly(fis);

		}

	}



}
