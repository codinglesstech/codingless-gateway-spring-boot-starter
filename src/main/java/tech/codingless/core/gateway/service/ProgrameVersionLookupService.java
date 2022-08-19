package tech.codingless.core.gateway.service;

import lombok.Data;
/**
 * 程序版本
 * @author wanghongyan
 *
 */
public interface ProgrameVersionLookupService {

	@Data
	public static class VersionInfo{
		private String version;
		private String title;
		private String summary;
	}
	/**
	 * 
	 * @return 版本信息
	 */
	VersionInfo version();
	
}
