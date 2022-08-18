package tech.codingless.core.gateway.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import tech.codingless.core.gateway.util.InstallEvnUtl;
import tech.codingless.core.gateway.util.StringUtil; 

public class AccessKeyHelper {
	private static final String PREFIX_KEY = "tech.codingless.accesskey.";
	private static final String DEFAULT_MODULE = "*/*";
	public static AccessKey DEFAULT_ACCESS_KEY = new AccessKey();
	public static Object LOCK = new Object();
	public static ConcurrentHashMap<String, AccessKey> ACCESS_KEYS = null;
	private static boolean loadsuccess=false;

	@Data
	public static class AccessKey {
		private String secret;
		private String key;
		private String company;
		public Map<String, ModuleGrant> modules = new HashMap<>();

	 
		public boolean isReadAble(String moduleAndversion) { 
			if(modules.containsKey(moduleAndversion)) {
				return modules.get(moduleAndversion).isReadable();
			}
			if(modules.containsKey(DEFAULT_MODULE)) {
				return modules.get(DEFAULT_MODULE).isReadable();
			}
			return false;
		}

	 
		public boolean isWriteAble(String moduleAndversion) {
			if(modules.containsKey(moduleAndversion)) {
				return modules.get(moduleAndversion).isWriteable();
			}
			if(modules.containsKey(DEFAULT_MODULE)) {
				return modules.get(DEFAULT_MODULE).isWriteable();
			}
			return false;
		}
	}

	@Data
	public static class ModuleGrant {
		private String module;
		private String version;
		private boolean readable;
		private boolean writeable;
	}

	public static AccessKey get(String key) {
		if (ACCESS_KEYS != null&&loadsuccess) {
			return ACCESS_KEYS.containsKey(key) ? ACCESS_KEYS.get(key) : DEFAULT_ACCESS_KEY;
		}

		synchronized (key) {
			if (ACCESS_KEYS == null) {
				ACCESS_KEYS = new ConcurrentHashMap<>();
				Map<String, String> map = InstallEvnUtl.getByPrefix(PREFIX_KEY);
				Map<String, Map<String, String>> tmp = new HashMap<>();
				map.keySet().forEach(k -> {
					String[] strs = k.substring(PREFIX_KEY.length()).split("[.]");
					if (!tmp.containsKey(strs[0])) {
						tmp.put(strs[0], new HashMap<>());
					}
					tmp.get(strs[0]).put(strs[1], map.get(k));
				});

				tmp.values().forEach(item -> {
					String secret = item.get("secret");
					String modules = item.get("modules");
					String keyId = item.get("key");
					String company = item.get("company");
					if (StringUtil.hasEmpty(secret, modules, keyId, company)) {
						return;
					}

					AccessKey accessKey = new AccessKey();
					accessKey.setKey(keyId.trim());
					accessKey.setSecret(secret.trim());
					accessKey.setCompany(company.trim());

					for (String module : modules.trim().split(",")) {
						if (module.split("/").length != 3) {
							continue;
						}
						String[] grant = module.split("/");
						ModuleGrant moduleGrant = new ModuleGrant();
						moduleGrant.setModule(grant[0]);
						moduleGrant.setVersion(grant[1]);
						moduleGrant.setReadable(grant[2].toLowerCase().contains("r"));
						moduleGrant.setWriteable(grant[2].toLowerCase().contains("w"));
						accessKey.getModules().put(grant[0] + "/" + grant[1], moduleGrant);

					}
					ACCESS_KEYS.put(accessKey.getKey(), accessKey);
				});
				loadsuccess=true;
			}
		} 
		return ACCESS_KEYS.containsKey(key) ? ACCESS_KEYS.get(key) : DEFAULT_ACCESS_KEY;
	}
}
