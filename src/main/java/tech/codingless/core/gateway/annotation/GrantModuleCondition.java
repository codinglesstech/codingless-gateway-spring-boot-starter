package tech.codingless.core.gateway.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import tech.codingless.core.gateway.conf.ModuleConf;
import tech.codingless.core.gateway.util.InstallEvnUtl;
 

/**
 * 
 * 授权的核心类  
 * @author 王鸿雁
 * @version  2021年10月22日
 */
public class GrantModuleCondition implements Condition {
	private static final Logger LOG = LoggerFactory.getLogger(GrantModuleCondition.class); 
	ModuleConf conf;
	//加载信息
	private static Map<String,Boolean> MODULE_LOADED=new HashMap<String,Boolean>();
	private static Map<String,String> MODULE_NAME=new HashMap<String,String>();
	private static ConcurrentHashMap<Class<?>, String> CLASS_MODULE_NAME= new ConcurrentHashMap<>();
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		if(conf==null) {
			conf = ModuleConf.build(InstallEvnUtl.getByPrefix("tech.codingless.modules")); 
			conf.getModules().forEach(module->{
				if(module.isEnable()) {
					MODULE_LOADED.put(module.getPkg(), module.isEnable());
					MODULE_NAME.put(module.getPkg(), module.getName());
				}
			}); 
		}
		
		
		for(String path:MODULE_LOADED.keySet()) {
			if(metadata.toString().startsWith(path)) {
				LOG.info("load :{}",metadata.toString());
				return true;
			} 
		}
		
		
		  
		return false;
	}

	public static String findModuleNameByResourcePkg(Class<?> clazz) {
		if(CLASS_MODULE_NAME.containsKey(clazz)) {
			return CLASS_MODULE_NAME.get(clazz);
		}
		String pkg = clazz.getName();
		for(String path:MODULE_NAME.keySet()) {
			if(pkg.startsWith(path)) {
				CLASS_MODULE_NAME.put(clazz, MODULE_NAME.get(path));
				return MODULE_NAME.get(path);
			}
		}
		return null;
	}
}
