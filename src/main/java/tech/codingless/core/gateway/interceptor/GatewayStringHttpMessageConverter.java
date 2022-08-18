package tech.codingless.core.gateway.interceptor;

import java.io.IOException;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.StringHttpMessageConverter;

import tech.codingless.core.gateway.util.SessionUtil;

public class GatewayStringHttpMessageConverter extends StringHttpMessageConverter { 
	
	
	@Override
	protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException { 
		return super.readInternal(clazz, inputMessage);
	}
	 
	@Override
	protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException { 
		SessionUtil.CURRENT_RESPONSE.set(str);
		super.writeInternal(str, outputMessage);
	}
}
