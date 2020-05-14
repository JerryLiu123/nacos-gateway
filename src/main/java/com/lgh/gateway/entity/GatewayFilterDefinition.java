package com.lgh.gateway.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 路由过滤器配置
 * @author lizhiting
 * @date 2020-05-14 14:56
 */
public class GatewayFilterDefinition {

    /**过滤器名称**/
    private String name;
    /**对应的规则**/
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
