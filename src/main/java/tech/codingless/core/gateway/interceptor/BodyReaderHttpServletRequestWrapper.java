package tech.codingless.core.gateway.interceptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private byte[] bodyData;

	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		try {
			bodyData = IOUtils.toByteArray(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getRequestBody() {
		try {
			return new String(this.bodyData, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException { 
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bodyData);

		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		};
	}

}
