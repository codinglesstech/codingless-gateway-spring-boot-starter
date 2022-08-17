package tech.codingless.core.gateway.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import tech.codingless.core.gateway.util.InstallEvnUtl;
import tech.codingless.core.gateway.util.ModuleUtil;
 

/**
 * 
 * 授权的核心类  
 * @author 王鸿雁
 * @version  2021年10月22日
 */
public class GrantModuleCondition implements Condition {
	private static final Logger LOG = LoggerFactory.getLogger(GrantModuleCondition.class);
	private static final String GRANT_PREFIX="tech.codingless.biz.module.";
	private static final String ENABLE="true";
	
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
	 
		String grantStr = metadata.toString();
		if(!grantStr.startsWith(GRANT_PREFIX)) {
			//不符合条件的全部默认授权
			return true;
		}
		
		
		ModuleUtil.ModuleInfo info =  ModuleUtil.pickModuleInfoFromClassName(grantStr);
		
	  
		String val = InstallEvnUtl.getApplicationProperites(GRANT_PREFIX.concat(info.getModuleCode()).concat(".").concat(info.getModuleVersion()).concat(".").concat("enable"));
		if(!ENABLE.equalsIgnoreCase(val)) {
			LOG.info("Disable Module[{}/{}],Resource[{}]",info.getModuleCode(),info.getModuleVersion(),info.getResource());
			return false;
		}
		
		//TODO 到中央认证服务器去拿授权信息，对于未开通模块的租户不给加载
		
		
		LOG.info("Enable Module[{}/{}],Resource[{}]",info.getModuleCode(),info.getModuleVersion(),info.getResource());
		return true;
	}

}
