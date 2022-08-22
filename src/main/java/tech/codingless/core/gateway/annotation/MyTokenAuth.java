package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * 权限代码，请写在Controller层,打了这个标签，意味着需要登录，并且授权才能访问
 * <pre>
 * 
 * 		MyAuth.CURRENT_COMPANY_ID.get()
 * 
 * </pre>
 * @author 王鸿雁
 * @version  2021年10月22日
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTokenAuth {
	public static final String AUTH_TOKEN="Auth-Token";
	public static final int UNAUTHORIZED_CODE=401;
	public static final String UNAUTHORIZED_MSG="Unauthorized";
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_GROUP_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_GROUP_NAME = new ThreadLocal<String>();
	public static ThreadLocal<String> CURRENT_TOKEN = new ThreadLocal<String>();
 
	/**
	 * 
	 * @return 有权限则解析，无权限则直接执行
	 */
	boolean required() default true;
	 
	/* 
	 * 
	 * 权限代码,对于同一代码的会认为是同一权限
	 *
	 */
	String code() default "";
	/**
	 * 
	 * @return 权限名称
	 */
	String name() default "";
	/* 
	 * 
	 * 操作类型
	 *
	 */
	MyAuthTypeEnum type() default MyAuthTypeEnum.UNKNOW;
	/**
	 * 
	 * 
	 * @return 支持的数据权限级别
	 *
	 */
	MyAuthLevelEnum [] level() default {};
}
