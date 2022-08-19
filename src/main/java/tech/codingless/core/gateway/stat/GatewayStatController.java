package tech.codingless.core.gateway.stat;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.sun.management.OperatingSystemMXBean;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.core.gateway.BaseController;
import tech.codingless.core.gateway.annotation.MyBiz;
import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.helper.RequestMonitorHelper;
import tech.codingless.core.gateway.util.MacAddressUtil;

@Slf4j
@RestController
@RequestMapping(value = "/gateway/stat")
public class GatewayStatController extends BaseController implements ApplicationListener<ApplicationStartedEvent> {
	private long startedTime;

	@MyBiz(disableResponseLog = true)
	@GetMapping(value = "/thread/dump")
	public String threadDump() {
		StringBuffer sb = new StringBuffer();
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		sb.append("<pre>");
		for (Thread t : map.keySet()) {
			sb.append("<br/>");
			String threadStr = String.format("'%s' #%s prio=%s state=%s", t.getName(), t.getId(), t.getPriority(), t.getState().name());
			sb.append(threadStr);
			sb.append("<br/>");
			StackTraceElement[] traces = map.get(t);
			for (StackTraceElement trace : traces) {
				String str = String.format("\t at %s(%s:%s)", trace.getClassName(), trace.getMethodName(), trace.getLineNumber());
				sb.append(str);
				sb.append("<br/>");
			}
		}
		sb.append("</pre>");
		return sb.toString();
	}

	@MyBiz(disableResponseLog = true)
	@GetMapping(value = "/req/history")
	public GatewayResponse reqLog(String uri) {
		if (StringUtil.isEmpty(uri)) {
			return resp().success().addContent("reqs", RequestMonitorHelper.getAllLog().values());
		}
		return resp().success().addContent("reqs", RequestMonitorHelper.getLog(uri));
	}

	@PostMapping(value = "/req/clear")
	public GatewayResponse reqClear() {
		RequestMonitorHelper.clear();
		return resp().success();
	}

	@PostMapping(value = "/gc")
	public GatewayResponse gc() {
		System.gc();
		return resp().success();
	}

	@MyBiz(disableResponseLog = true)
	@GetMapping(value = "/info")
	public GatewayResponse info() {
		GatewayResponse resp = resp();
		resp.success();
		resp.addContent("startedTime", startedTime);
		resp.addContent("jvmTotalMemory", Runtime.getRuntime().totalMemory()); 
		resp.addContent("jvmFreeMemory", Runtime.getRuntime().freeMemory()); 
		resp.addContent("jvmMaxMemory", Runtime.getRuntime().maxMemory());  
		
		OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		resp.addContent("physicalTotalMemorySize", mxbean.getTotalMemorySize()); 
		resp.addContent("physicalFreeMemorySize", mxbean.getFreeMemorySize()); 
		resp.addContent("physicalUsedMemorySize", mxbean.getTotalMemorySize()-mxbean.getFreeMemorySize()); 
		resp.addContent("osName", System.getProperty("os.name"));  
		resp.addContent("timezone", System.getProperty("user.timezone"));  
		resp.addContent("availableProcessors", mxbean.getAvailableProcessors()); 
		resp.addContent("cpuLoad", mxbean.getCpuLoad());   
		resp.addContent("committedVirtualMemorySize", mxbean.getCommittedVirtualMemorySize()); 
		resp.addContent("systemLoadAverage", mxbean.getSystemLoadAverage());    
		resp.addContent("freeSwapSpaceSize", mxbean.getFreeSwapSpaceSize());   
		resp.addContent("processCpuTime", mxbean.getProcessCpuTime());     
		resp.addContent("processCpuLoad", mxbean.getProcessCpuLoad());     
		try {
			resp.addContent("hostAddress", InetAddress.getLocalHost().getHostAddress());  
			resp.addContent("hostName", InetAddress.getLocalHost().getHostName());  
			resp.addContent("mac", MacAddressUtil.getMACAddress(InetAddress.getLocalHost()));
		} catch (Exception e) { 
			 
		}   
		
		
		
		return resp;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.startedTime = System.currentTimeMillis();

	}

}
