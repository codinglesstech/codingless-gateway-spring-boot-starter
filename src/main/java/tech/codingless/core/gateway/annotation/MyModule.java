package tech.codingless.core.gateway.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Conditional;

/**
 * 
 * 我的授权 ，请在需要授权的Resource上都打上这个标签
 * @author 王鸿雁
 * @version  2021年10月22日
 */
@Conditional(GrantModuleCondition.class) 
@Retention(RetentionPolicy.RUNTIME)
public @interface MyModule {

}
