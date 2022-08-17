package tech.codingless.core.gateway.stat;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.codingless.core.gateway.BaseController; 

@RestController 
@RequestMapping(value = "/gateway/stat")
public class GatewayStatController extends BaseController {
 
   
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
}
