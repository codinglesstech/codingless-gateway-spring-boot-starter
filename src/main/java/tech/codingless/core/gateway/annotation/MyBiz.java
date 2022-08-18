package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface MyBiz {
 
  
	/**
	 * 
	 * @return 业务代码
	 */
	String bizCode() default "";
	/**
	 * 
	 * @return 业务名字
	 */
	String bizName() default "";
	
	/**
	 * 
	 * @return 是否禁止响应内容打印,对响应态多的GET请求，建议禁止
	 */
	boolean disableResponseLog() default false;
	boolean disableRequestLog() default false;
 
}
