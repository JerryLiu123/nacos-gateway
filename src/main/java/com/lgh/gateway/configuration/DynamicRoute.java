package com.lgh.gateway.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DynamicRoute implements ApplicationEventPublisherAware {

//    @Autowired
//    private RouteDefinitionWriter routeDefinitionWriter;
    
    @Autowired
    private RouteDefinitionRepository routeDefinitionRepository;
    
    private ApplicationEventPublisher publisher;
    
    /**
     * 获得当前所有的路由
     * @Title: get
     * @Description: 
     * @Author lizhiting
     * @DateTime May 5, 2019 11:19:46 AM
     * @return
     */
    public Flux<RouteDefinition> get() {
    	Flux<RouteDefinition> route = this.routeDefinitionRepository.getRouteDefinitions();
    	return route;
    }
    
    /**
     * 
     * @Title: add
     * @Description: 增加路由
     * @Author lizhiting
     * @DateTime May 5, 2019 11:20:02 AM
     * @param definition
     * @return
     */
    public String add(RouteDefinition definition) {
    	this.routeDefinitionRepository.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }
    
    /**
     * 
     * @Title: update
     * @Description: 更新路由
     * @Author lizhiting
     * @DateTime May 5, 2019 11:20:34 AM
     * @param definition
     * @return
     */
    public String update(RouteDefinition definition) {
        try {
        	this.routeDefinitionRepository.delete(Mono.just(definition.getId()));
        	this.routeDefinitionRepository.save(Mono.just(definition)).subscribe();
            //发布事件
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return "success";
        } catch (Exception e) {
            return "update route  fail";
        }
    }
    
    
	/**
	 * 
	 * @Title: delete
	 * @Description: 删除路由
	 * @Author lizhiting
	 * @DateTime May 5, 2019 11:20:46 AM
	 * @param id
	 * @return
	 */
    public Mono<ResponseEntity<Object>> delete(String id) {
    	try {
        	this.routeDefinitionRepository.delete(Mono.just(id)).subscribe();
        	return Mono.just(ResponseEntity.ok().build());
		} catch (Exception e) {
			// TODO: handle exception
			return Mono.just(ResponseEntity.notFound().build());
		}

//        return this.routeDefinitionRepository.delete(Mono.just(id)).then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
//                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }
    
    /**
     * 刷新规则
     * @Title: refresh
     * @Description: 
     * @Author lizhiting
     * @DateTime May 5, 2019 11:35:48 AM
     * @return
     */
	public Mono<Void> refresh() {
	    this.publisher.publishEvent(new RefreshRoutesEvent(this));
		return Mono.empty();
	}
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

}
