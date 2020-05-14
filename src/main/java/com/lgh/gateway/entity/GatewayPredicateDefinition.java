package com.lgh.gateway.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * gateway 断言配置实体
 * @author lizhiting
 * @date 2020-02-26 11:30
 */
public class GatewayPredicateDefinition {

    /**断言对应的Name**/
    private String name;
    /**配置的断言规则**/
    private Map<String, String> args = new LinkedHashMap<>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getArgs() {
		return args;
	}
	public void setArgs(Map<String, String> args) {
		this.args = args;
	}
    
    
}
