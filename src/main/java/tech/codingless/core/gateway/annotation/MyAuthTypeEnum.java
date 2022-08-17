package tech.codingless.core.gateway.annotation;
/**
 * 
 * 权限类型
 * @author 王鸿雁
 * @version  2021年10月22日
 */
public enum MyAuthTypeEnum {
	
	/**
	 * 查看
	 */
	READ("READ","查看"),
	/**
	 * 修改
	 */
	UPDATE("UPDATE","修改"),
	/**
	 * 删除
	 */
	DEL("DEL","删除"),
	/**
	 * 修改
	 */
	CREATE("CREATE","修改"),
	UNKNOW("UNKNOW","未知"),
	 
;
	private String code;
	private String name;
	private MyAuthTypeEnum(String code,String name) {
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
