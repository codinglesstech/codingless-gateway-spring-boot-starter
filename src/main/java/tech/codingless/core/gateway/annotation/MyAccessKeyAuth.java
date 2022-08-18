package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 通过AccessKey机制来访问该接口 
 * <pre>
 *   URL传参签名(GET):   
 *   参数1=xx&参数2=xxx& ... &TimeStamp
 *   参数按ASCCI码排序
 *   
 *   BODY传参签名
 *   
 *   Body内容&TimeStamp
 *   
 *   
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
public @interface MyAccessKeyAuth { 
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> ACCESS_KEY = new ThreadLocal<String>();
	
  
	/**
	 * 需要写权限
	 * @return
	 */
	boolean requiredWriteAble() default false;
 
  
}
