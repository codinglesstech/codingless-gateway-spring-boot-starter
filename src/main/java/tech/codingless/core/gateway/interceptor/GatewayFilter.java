package tech.codingless.core.gateway.interceptor;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayFilter implements GlobalFilter, Ordered { 
	@Override
	public int getOrder() { 
		return -2; //这个值要小于-1，才能修改响应，不然不管用
	} 
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String consoleToken = exchange.getRequest().getHeaders().getFirst("Console-Token");
		String consoleTmpUid = exchange.getRequest().getHeaders().getFirst("Console-Tmp-Uid");
		log.info("执行了filter"); 
		/**
		if(StringUtil.isEmpty(consoleToken)) {
			exchange.getResponse().setRawStatusCode(505);
			return exchange.getResponse().setComplete();
		}
		*/
		log.info("head:{}",JSON.toJSONString(exchange.getRequest().getHeaders())); 
	 
		exchange.getResponse().getHeaders().add("gateway", "codingless");
	 
	  
		
		ServerHttpResponseDecorator responseDecorator = processResponse(exchange.getResponse(), exchange.getResponse().bufferFactory());
		return chain.filter(exchange.mutate().response(responseDecorator).build());
	}
	
    private ServerHttpResponseDecorator processResponse(ServerHttpResponse response, DataBufferFactory bufferFactory) {
        return new ServerHttpResponseDecorator(response) {
 
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(flux.map(buffer -> { 
                    	CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
                        DataBufferUtils.release(buffer);
                        String old = charBuffer.toString();
                        JSONObject json = JSON.parseObject(old); 
                        json.put("modifyFromGateway", true); 
                        return bufferFactory.wrap(json.toString().getBytes(StandardCharsets.UTF_8));
                    }));
                }
                return super.writeWith(body);
            }
        };
    }
 
	
	

}
