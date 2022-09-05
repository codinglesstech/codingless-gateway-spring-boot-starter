package tech.codingless.core.gateway.interceptor;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.codingless.core.gateway.util.RedisUtil;
import tech.codingless.core.gateway.util.StringUtil;

@Slf4j
@Component
public class GatewayFilter implements GlobalFilter, Ordered {
	private String AUTHED_UID = "Authed-Uid";
	private String AUTHED_USERNAME = "Authed-UserName";
	private String AUTHED_COMPANYID = "Authed-CompanyId";
	private String AUTHED_DEPTID = "Authed-DeptId";
	private String AUTHED_DEPTNAME = "Authed-DeptName";
	private String AUTHED_ISADMIN = "Authed-IsAdmin";

	@Override
	public int getOrder() {
		return -2; // 这个值要小于-1，才能修改响应，不然不管用
	}

	@Data
	private static class AuthInfo {
		String userId;
		String companyId;
		String userName;
		String deptId;
		String deptName;
		String isAdmin;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String consoleToken = exchange.getRequest().getHeaders().getFirst("Console-Token");
		String tmpUid = exchange.getRequest().getHeaders().getFirst("Console-Tmp-Uid");

		AuthInfo authInfo = new AuthInfo();
		if (StringUtil.isNotEmpty(consoleToken)) {
			String authKey = "CONSOLE:TOKEN:AUTH:" + consoleToken;
			if (StringUtil.isNotEmpty(tmpUid)) {
				List<String> vals = RedisUtil.hmget(authKey, "ADMIN/" + tmpUid, "USERID/" + tmpUid, "COMPANYID", "USERNAME", "USERNAME/" + tmpUid, "DEPTID/" + tmpUid);

				authInfo.setIsAdmin(vals.get(0));
				authInfo.setUserId(vals.get(1));
				authInfo.setCompanyId(vals.get(2));
				authInfo.setUserName(vals.get(4));
				authInfo.setDeptId(vals.get(5));

			} else {
				List<String> vals = RedisUtil.hmget(authKey, "ADMIN", "USERID", "URI:", "COMPANYID", "USERNAME", "POSITION", "EMPLOYEE_NUMBER", "MOBILE", "DEPT_ID", "DEPT_NAME", "DEPT_CODE");
				authInfo.setUserId(vals.get(1));
				authInfo.setCompanyId(vals.get(3));
				authInfo.setIsAdmin(vals.get(0));
				authInfo.setUserName(vals.get(4));
				authInfo.setDeptId(vals.get(8));
				authInfo.setDeptName(vals.get(9));
			}
		}

		ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {

			@Override
			public HttpHeaders getHeaders() {

				HttpHeaders headers = HttpHeaders.writableHttpHeaders(super.getHeaders());
				headers.remove(AUTHED_UID);
				headers.remove(AUTHED_USERNAME);
				headers.remove(AUTHED_COMPANYID);
				headers.remove(AUTHED_DEPTID);
				headers.remove(AUTHED_DEPTNAME);
				headers.remove(AUTHED_ISADMIN);
				headers.put(AUTHED_UID, List.of(authInfo.getUserId()));
				headers.put(AUTHED_USERNAME, List.of(authInfo.getUserName()));
				headers.put(AUTHED_COMPANYID, List.of(authInfo.getCompanyId()));
				headers.put(AUTHED_DEPTID, List.of(authInfo.getDeptId()));
				headers.put(AUTHED_DEPTNAME, List.of(authInfo.getDeptName()));
				headers.put(AUTHED_ISADMIN, List.of(authInfo.getIsAdmin()));
				return headers;
			}

		};

		exchange.getResponse().getHeaders().add("gateway", "codingless");

		ServerHttpResponseDecorator responseDecorator = processResponse(exchange.getResponse(), exchange.getResponse().bufferFactory());
		return chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build());
	}

	private ServerHttpResponseDecorator processResponse(ServerHttpResponse response, DataBufferFactory bufferFactory) {
		return new ServerHttpResponseDecorator(response) {

			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;

					return super.writeWith(flux.buffer().map(dataList -> {
						StringBuilder oldBody = new StringBuilder();
						dataList.forEach(dataItem -> {
							CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataItem.asByteBuffer());
							oldBody.append(charBuffer.toString());
						});
						JSONObject json = JSON.parseObject(oldBody.toString()); 
						return bufferFactory.wrap(json.toString().getBytes(StandardCharsets.UTF_8));
					}));

				}

				return super.writeWith(body);
			}
		};
	}

}