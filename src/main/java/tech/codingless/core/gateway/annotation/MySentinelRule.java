package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 借助阿里巴巴Sentinel组件进行流控，该注解起到一个快速定义的功能 
 *  
 * 流控、熔断、规则定义
 * @author 王鸿雁
 * @version  2021年12月29日
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MySentinelRule {
 
 
	 
	/**
	 * 流量控制阈值, -1: 不行进流控
	 * @return
	 *
	 */
	int qps() default 0;
	 
}
