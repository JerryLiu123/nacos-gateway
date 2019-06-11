package com.lgh.gateway.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DynamicRouteByNacos implements InitializingBean {

	private Logger logger = LoggerFactory.getLogger(DynamicRouteByNacos.class);
	@Autowired
    private DynamicRoute dynamicRoute;
    
    @Value("${gateway.nacos.dataid}")
    private String dataId;
    
    @Value("${gateway.nacos.group}")
    private String group;
    
    @Value("${nacos.config.server-addr}")
    private String url;
    
    
    
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		dynamicRouteByNacosListener();
		
	}
    /**
     * 监听Nacos Server下发的动态路由配置
     * @param dataId
     * @param group
     * @throws NacosException 
     */
    public void dynamicRouteByNacosListener () throws NacosException {
        try {
            ConfigService configService=NacosFactory.createConfigService(url);
            configService.addListener(dataId, group, new Listener()  {
                @Override
                public void receiveConfigInfo(String configInfo) {
                	//获得现在的路由
                	List<RouteDefinition> definitions = JSON.parseObject(configInfo, new TypeReference<List<RouteDefinition>>(){});
                	//获得之前的路由
                	List<String> oldDefinitionsIDS = new ArrayList<String>();
                	Flux<RouteDefinition> oldDefinitions = dynamicRoute.get();
                	//更新 sentinel 规则
                	Set<ApiDefinition> senDefinitions = new HashSet<>();
                	HashSet<ApiPredicateItem> apiItems = new HashSet<ApiPredicateItem>();
                	//之前的路由ID
                	oldDefinitions.map(r -> {
                		return r.getId();
                	}).subscribe(oldDefinitionsIDS::add);
                	logger.debug("原来的规则:{}", oldDefinitionsIDS);
                	//更新规则
                	definitions.stream().forEach(obj -> {
                		//更新转发规则
                		dynamicRoute.update(obj);
                		
                		//添加 sentinel 监控
                		obj.getPredicates().forEach(acd -> {
                			String pattern = acd.getArgs().get("pattern");
                			if(pattern.indexOf("**") > -1) {
                				apiItems.add(new ApiPathPredicateItem().setPattern(pattern).setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_PREFIX));
                			}else {
                				apiItems.add(new ApiPathPredicateItem().setPattern(pattern));
                			}
                			
                		});
                		
                		//如果ID在现在的规则里面没有则说明 规则已经被删除
                		if(oldDefinitionsIDS!= null && (!oldDefinitionsIDS.isEmpty())) {
                			oldDefinitionsIDS.remove(obj.getId());
                		}
                	});
                	//删除没有的规则
                	if(oldDefinitionsIDS!= null && (!oldDefinitionsIDS.isEmpty())) {
	                	oldDefinitionsIDS.stream().forEach(obj -> {
	                		logger.debug("-----删除规则ID：{}", obj);
	                		dynamicRoute.delete(obj);
	                	});
                	}
                	
                	//更新 sentinel 中的资源
                	senDefinitions.add(new ApiDefinition("some_customized_api").setPredicateItems(apiItems));
                	GatewayApiDefinitionManager.loadApiDefinitions(senDefinitions);
                	
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
        	throw e;
        }
    }
    
	public DynamicRoute getDynamicRoute() {
		return dynamicRoute;
	}
	public void setDynamicRoute(DynamicRoute dynamicRoute) {
		this.dynamicRoute = dynamicRoute;
	}
	public String getDataId() {
		return dataId;
	}
	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
