package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 通过AccessKey机制来访问该接口 
 * <pre> 
 *   常见属性
 *   =====================================================================
 *   MyAccessKeyAuth.ACCESS_KEY.get()		|	access key
 *   MyAccessKeyAuth.CURRENT_COMPANY_ID.get()	|	公司ID
 *   MyAccessKeyAuth.READ_ABLE.get()		|	可读 
 *   MyAccessKeyAuth.WRITE_ABLE.get()		|	可写 
 *   =====================================================================
 * </pre>
 * @author 王鸿雁
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MySignAuth { 
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> ACCESS_KEY = new ThreadLocal<String>();
	
  
	/**
	 * 
	 * @return true 需要写权限
	 */
	boolean requiredWriteAble() default false;
 
  
}
