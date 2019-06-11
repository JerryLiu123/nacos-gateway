package com.lgh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

@SpringBootApplication
@EnableDiscoveryClient
//从中心服务 nacos 中加在配置为example的配置源，并开启动态更新
@NacosPropertySource(dataId = "gateway", groupId="GATEWAY_GROUP" ,autoRefreshed = true)
public class NacosGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosGatewayApplication.class, args);
	}

}
