package tech.codingless.core.gateway.interceptor;

import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
	private String AUTHED_UID = "authed-uid";
	private String AUTHED_USERNAME = "authed-username";
	private String AUTHED_COMPANYID = "authed-companyid";
	private String AUTHED_DEPTID = "authed-deptid";
	private String AUTHED_DEPTNAME = "authed-deptname";
	private String AUTHED_ISADMIN = "authed-isadmin";

	@Override
	public int getOrder() {
		return -2; // 这个值要小于-1，才能修改响应，不然不管用
	}

	@Data
	private static class AuthInfo {
		String userId = "";
		String companyId;
		String userName;
		String deptId;
		String deptName;
		String isAdmin;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String consoleToken = exchange.getRequest().getHeaders().getFirst("console-token");
		String tmpUid = exchange.getRequest().getHeaders().getFirst("console-tmp-uid");

		log.info("Token:{}", consoleToken);
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
				log.info("vals:{}", JSON.toJSONString(vals));
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
				try {
					if (StringUtil.isNotEmpty(authInfo.getUserId())) {
						headers.put(AUTHED_UID, List.of(authInfo.getUserId()));
					}
					if (StringUtil.isNotEmpty(authInfo.getUserName())) {
						headers.put(AUTHED_USERNAME, List.of(URLEncoder.encode(authInfo.getUserName(), "utf-8")));

					}
					if (StringUtil.isNotEmpty(authInfo.getCompanyId())) {
						headers.put(AUTHED_COMPANYID, List.of(authInfo.getCompanyId()));
					}
					if (StringUtil.isNotEmpty(authInfo.getDeptId())) {
						headers.put(AUTHED_DEPTID, List.of(authInfo.getDeptId()));
					}
					if (StringUtil.isNotEmpty(authInfo.getDeptName())) {
						headers.put(AUTHED_DEPTNAME, List.of(URLEncoder.encode(authInfo.getDeptName(), "utf-8")));
					}
					if (StringUtil.isNotEmpty(authInfo.getIsAdmin())) {
						headers.put(AUTHED_ISADMIN, List.of(authInfo.getIsAdmin()));
					}
				} catch (Exception e) {

				}
				return headers;
			}

		};

		exchange.getResponse().getHeaders().add("gateway", "codingless");

		//ServerHttpResponseDecorator responseDecorator = processResponse(exchange.getResponse(), exchange.getResponse().bufferFactory());
		return chain.filter(exchange.mutate().request(requestDecorator).build());
		//return chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build());
		// return
		// chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build());
	}

	private ServerHttpResponseDecorator processResponse(ServerHttpResponse response, DataBufferFactory bufferFactory) {
		return new ServerHttpResponseDecorator(response) {

			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

				if (body instanceof Flux) {
					Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;

					response.getHeaders().setAccessControlAllowOrigin("*");
					//response.getHeaders().remove("access-control-allow-origin");
					
					return super.writeWith(flux.buffer().map(dataList -> {
						
						   
						StringBuilder oldBody = new StringBuilder();
						dataList.forEach(dataItem -> {
							CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataItem.asByteBuffer());
							DataBufferUtils.release(dataItem);
							oldBody.append(charBuffer.toString());
						});
						boolean isJson = false;
						if (MediaType.TEXT_HTML == response.getHeaders().getContentType() && oldBody.toString().startsWith("{")) {
							response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
							isJson = true;
						} else if (MediaType.APPLICATION_JSON == response.getHeaders().getContentType()) {
							isJson = true;
						}
						log.info("response:{}", oldBody.toString());
						if (isJson) {

							JSONObject json = JSON.parseObject(oldBody.toString());
							return bufferFactory.wrap(json.toString().getBytes(StandardCharsets.UTF_8));
						} else {
							return bufferFactory.wrap(oldBody.toString().getBytes(StandardCharsets.UTF_8));
						}

					}));

				}

				return super.writeWith(body);
			}
		};
	}

}
