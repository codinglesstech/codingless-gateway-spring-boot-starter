package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface MyEncryptionAuth { 
	public static ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<String>();
	public static ThreadLocal<String> ACCESS_KEY = new ThreadLocal<String>();
	 
}
