package com.lgh.gateway.entity;

import org.springframework.cloud.gateway.route.RouteDefinition;

public class NacosRouteDefinition {

	/**状态**/
	private String state;
	/**规则**/
	private RouteDefinition definition;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public RouteDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(RouteDefinition definition) {
		this.definition = definition;
	}
	
	
}
