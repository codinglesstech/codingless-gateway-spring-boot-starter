package tech.codingless.core.gateway.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * 安装环境下的工具类
 * 
 * @author 王鸿雁
 * @version 2021年9月29日
 */
public class InstallEvnUtl {	
	private static final Logger LOG = LoggerFactory.getLogger(InstallEvnUtl.class);
	private static final ConcurrentHashMap<String, String> APPLICATION_PROPETIES_CACHE=new ConcurrentHashMap<>();

	public static void main(String[] args) {
		System.out.println(InstallEvnUtl.getProperites("a", "b"));
	}

 
	public static String getApplicationProperites(String key){
		 return getApplicationProperites(key,"test").get(key);
	}
	public static Map<String,String> getApplicationProperites(String...keys){ 
		
		if(APPLICATION_PROPETIES_CACHE.isEmpty()) {
			try { 
				Properties prop = new Properties();
				prop.load(InstallEvnUtl.class.getResourceAsStream("/application.properties"));
				for(Object key:prop.keySet()) {
					APPLICATION_PROPETIES_CACHE.put(key.toString(), prop.getProperty(key.toString())); 
				}
			
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
		
		Map<String,String> result = new HashMap<>();
		for(String key:keys) { 
			result.put(key, APPLICATION_PROPETIES_CACHE.get(key)); 
		}
		return result; 
	}
	 
	public static Map<String, String> getProperites(String... keys) {
		if (keys == null) {
			return new HashMap<>();
		}
		//~/.io.uni.biz.propties作为用户安装程序，自定义配置数据库等的配置信息
		File propfile = new File(System.getProperty("user.home")+File.separator+".io.uni.biz.propties");
		if(!propfile.exists()) { 
			LOG.info("Not Found Install Propties File {}",propfile.getAbsolutePath());
			return new HashMap<>();
		}
		
		Map <String, String> map=	new HashMap<>();
		Properties prop = new Properties();
		try { 
			prop.load(new FileInputStream(propfile));
			List.of(keys).forEach(key->{
				map.put(key, prop.getProperty(key, ""));
			});
		} catch (Exception e) {
			LOG.error("Load Propties File Error ",e); 
		}
		return map;
	}

	public static Map<String, String> getByPrefix(String prefix) {
		if(APPLICATION_PROPETIES_CACHE.isEmpty()) {
			try { 
				Properties prop = new Properties();
				prop.load(InstallEvnUtl.class.getResourceAsStream("/application.properties"));
				for(Object key:prop.keySet()) {
					APPLICATION_PROPETIES_CACHE.put(key.toString(), prop.getProperty(key.toString())); 
				}
			
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
		Map<String,String> result = new HashMap<>();
		
		APPLICATION_PROPETIES_CACHE.keys().asIterator().forEachRemaining(key->{
			if(key.startsWith(prefix)) {
				result.put(key, APPLICATION_PROPETIES_CACHE.get(key)); 
			}
		}); 
		return result; 
	}
}
