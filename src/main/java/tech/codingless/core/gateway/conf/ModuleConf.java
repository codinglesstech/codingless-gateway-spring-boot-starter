package tech.codingless.core.gateway.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import tech.codingless.core.gateway.util.StringUtil;

@Data
public class ModuleConf {
	private List<Module> modules;

	@Data
	public static class Module {
		private String name;
		private boolean enable;
		private String pkg;
	}

	/**
	 * 解析模块信息
	 * @param properties 从配置文件中读取的配置
	 * @return 模块信息
	 */
	public static ModuleConf build(Map<String, String> properties) {

		ModuleConf conf = new ModuleConf();
		conf.setModules(new ArrayList<>());
		Map<String, Map<String, String>> tmp = new HashMap<>();
		properties.keySet().forEach(key -> {
			String name = key.substring("tech.codingless.modules".length());
			if (StringUtil.isEmpty(name)) {
				return;
			}
			String[] group = name.split("[.]", 2);
			if (group.length != 2) {
				return;
			}
			if (!tmp.containsKey(group[0])) {
				tmp.put(group[0], new HashMap<>());
			}
			tmp.get(group[0]).put(group[1], properties.get(key));  
		});
		
		tmp.keySet().forEach(key->{
			Map<String, String> attr = tmp.get(key);
			ModuleConf.Module module = new ModuleConf.Module();
			module.setEnable("true".equalsIgnoreCase(attr.get("enable")));
			module.setName(attr.get("name"));
			module.setPkg(attr.get("pkg"));
			conf.getModules().add(module); 
		});
		 
		return conf;
	}

}
