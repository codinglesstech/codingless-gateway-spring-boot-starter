package tech.codingless.core.gateway.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson2.JSON;

import tech.codingless.core.gateway.data.GatewayResponse;
import tech.codingless.core.gateway.util.SessionUtil;

public class GatewayResponseHttpMessageConverter extends AbstractHttpMessageConverter<GatewayResponse> {

	@Override
	protected boolean supports(Class<?> clazz) {
		return GatewayResponse.class == clazz;
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return GatewayResponse.class==clazz;
	}

	@Override
	protected GatewayResponse readInternal(Class<? extends GatewayResponse> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return null;
	}

	@Override
	protected void writeInternal(GatewayResponse resp, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException { 
		HttpHeaders headers = outputMessage.getHeaders(); 
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE); 
		String body = JSON.toJSONString(resp);
		SessionUtil.CURRENT_RESPONSE.set(body); 
		StreamUtils.copy(body, Charset.forName("utf-8"), outputMessage.getBody()); 
	}

}
