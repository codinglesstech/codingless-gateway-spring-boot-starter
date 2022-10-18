package tech.codingless.core.gateway.conf;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import tech.codingless.core.gateway.route.RouteDefinitionData;

@Component
public class StartedInit implements ApplicationListener<ApplicationStartedEvent>, ApplicationEventPublisherAware {

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) { 
		RouteDefinitionData.reload();
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(true));

	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;

	}

}
