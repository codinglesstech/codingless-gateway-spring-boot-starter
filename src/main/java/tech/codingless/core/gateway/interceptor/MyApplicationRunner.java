package tech.codingless.core.gateway.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import com.alibaba.csp.sentinel.util.StringUtil;

import tech.codingless.core.gateway.BaseController;
import tech.codingless.core.gateway.annotation.MySentinelRule;
 

@Component
public class MyApplicationRunner implements ApplicationRunner {
	private static final Logger LOG = LoggerFactory.getLogger(MyApplicationRunner.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Autowired
	public void setBaseControllers(BaseController[] apis) {

		LOG.info("设置流控");
		List<FlowRule> rules = new ArrayList<>();
		for (BaseController api : apis) {

			RequestMapping requestMapping = api.getClass().getAnnotation(RequestMapping.class);
			if (requestMapping == null) {
				continue;
			}

			for (Method method : api.getClass().getMethods()) {
				MySentinelRule mySentinelRule = method.getAnnotation(MySentinelRule.class);
				if (mySentinelRule == null) {
					continue;
				}

				String uri = "";
				if (StringUtil.isEmpty(uri)) {
					GetMapping getMapping = method.getAnnotation(GetMapping.class);
					uri = getMapping != null ? getMapping.value()[0] : "";
				}

				if (StringUtil.isEmpty(uri)) {
					PostMapping postMapping = method.getAnnotation(PostMapping.class);
					uri = postMapping != null ? postMapping.value()[0] : "";
				}

				if (StringUtil.isEmpty(uri)) {
					PutMapping putMapping = method.getAnnotation(PutMapping.class);
					uri = putMapping != null ? putMapping.value()[0] : "";
				}

				if (StringUtil.isEmpty(uri)) {
					DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
					uri = deleteMapping != null ? deleteMapping.value()[0] : "";
				}

				if (StringUtil.isEmpty(uri)) {
					// is not api method
					continue;
				}

				String resourceName = "";
				resourceName = requestMapping.value()[0];
				resourceName = resourceName.endsWith("/") ? resourceName.substring(0, resourceName.length() - 2) : resourceName;
				resourceName += uri.startsWith("/") ? uri : "/" + uri;

				if (mySentinelRule.qps() > -1) {
					// 当规则不存在时，流控QPS默认设置50
					FlowRule rule = new FlowRule();
					rule.setResource(resourceName);
					rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
					rule.setCount(mySentinelRule.qps() == 0 ? 100 : mySentinelRule.qps());
					rules.add(rule);
					LOG.info("Sentinel Resource --> {} QPS Quota:{}", resourceName, rule.getCount());

				}



			} 
			
		}
		if (!rules.isEmpty()) {
			FlowRuleManager.loadRules(rules);
		}
		
		//默认流控统计
		StatisticSlotCallbackRegistry.addEntryCallback("default", new ProcessorSlotEntryCallback() {

			@Override
			public void onPass(Context context, ResourceWrapper resourceWrapper, Object param, int count, Object... args) throws Exception {
				  
			}

			@Override
			public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, Object param, int count, Object... args) {
				System.out.println("BlockException:"+ex.toString());
				

			}
		});

	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		LOG.info("程序启动成功");

	}

}
