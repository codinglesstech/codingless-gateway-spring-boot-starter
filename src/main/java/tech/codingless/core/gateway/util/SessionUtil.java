package tech.codingless.core.gateway.util;

import java.util.HashMap;
import java.util.Map;

public class SessionUtil {
	 
	public static final String CACHE_KEY_ACCESS_TOKEN_UID_PRE = "ACCESS_TOKEN:UID:";
 
	public static final String CACHE_KEY_UID_ACCESS_TOKEN_PRE = "UID:ACCESS_TOKEN:";
	private static final String MAP_KEY_CURRENT_COMPNAY_ID = "CURRENT_COMPNAY_ID";
	private static final String MAP_KEY_CURRENT_USER_ID = "CURRENT_USER_ID";

	 
	public static void clean() {
		RID.remove();
		CURRENT_PLATFORM.remove();
		CURRENT_ACCESS_TOKEN.remove();
		CACHE_KEY.remove();
		CACHE_RESPONSE.remove();
		CACHE_TIME.remove();
		CURRENT_COMPANY_ID.remove();
		CURRENT_RESPONSE.remove(); 
	}

	/*
	 * 请求ID
	 */
	public static ThreadLocal<String> RID = new ThreadLocal<String>();
	/**
	 * 当前平台
	 */
	public static ThreadLocal<String> CURRENT_PLATFORM = new ThreadLocal<String>();
	/**
	 * 当前登录的用户ID
	 */
	public static ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	/**
	 * 1:代表生产环境，2：测试环境
	 */
	public static ThreadLocal<Integer> CURRENT_ENV = new ThreadLocal<Integer>();
	/**
	 * 当前授权TOKEN
	 */
	public static ThreadLocal<String> CURRENT_ACCESS_TOKEN = new ThreadLocal<String>();
	/**
	 * cache key
	 */
	public static ThreadLocal<String> CACHE_KEY = new ThreadLocal<String>();
	public static ThreadLocal<String> CACHE_RESPONSE = new ThreadLocal<String>();
	public static ThreadLocal<String> CACHE_READ = new ThreadLocal<String>();
	public static ThreadLocal<Integer> CACHE_TIME = new ThreadLocal<Integer>();
	/**
	 * 当前请求的响应
	 */
	public static ThreadLocal<String> CURRENT_RESPONSE = new ThreadLocal<String>();

 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> newSessionMap(int size) {
		Map map = new HashMap((int) ((size + 2) / 0.75 + 1));
		map.put(MAP_KEY_CURRENT_COMPNAY_ID, CURRENT_COMPANY_ID.get());
		map.put(MAP_KEY_CURRENT_USER_ID, CURRENT_USER_ID.get());
		return map;
	}

}
