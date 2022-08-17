package tech.codingless.core.gateway.annotation;
/**
 * 
 * 权限级别 
 * @author 王鸿雁
 * @version  2021年10月22日
 */
public enum MyAuthLevelEnum {
	
	/**
	 * 公司权限
	 */
	COMPANY_LEVEL("COMPANY","公司"),
	/**
	 * 部门权限
	 */
	DEPT_LEVEL("DEPT","部门"),
	/**
	 * 部门及子部门权限
	 */
	DEPT_AND_CHILD_LEVEL("DEPT_AND_CHILD","部门及子部门"),
	/**
	 * 团队主管
	 */
	TEAM_LEADER_LEVEL("TEAM_LEADER","团队主管"),
	/**
	 * 团队成员
	 */
	TEAM_MEMBER_LEVEL("TEAM_MEMBER","团队成员"),
;
	private String code;
	private String name;
	private MyAuthLevelEnum(String code,String name) {
		this.code=code;
		this.name=name;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
}
