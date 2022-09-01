package tech.codingless.core.gateway.service;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;

public interface AuthService {
	@Data
	public static class SignAuthRequest {
		private String signKey;
		private String signTimestamp;
		private String sign;
		private String signData;
		private String uri;
		private String ip;
	}
	
	SignAuthResponse signAuth(SignAuthRequest authRequest);
	
	@Data
	public static class SignAuthResponse {
		private String signKey;
		private String companyId;
		private String signName;
		private boolean allowed;
		private boolean expired;
	}

	@Data
	public static class TokenAuthRequest {
		private String token; 
		private String uri;
		private String ip;
	}
	@Data
	public static class TokenAuthResponse { 
		private String companyId;
		private String userName;
		private String userId;
		private boolean allowed;
		private boolean expired;
	}
	

	TokenAuthResponse tokenAuth(HttpServletRequest request);
	
	

}
