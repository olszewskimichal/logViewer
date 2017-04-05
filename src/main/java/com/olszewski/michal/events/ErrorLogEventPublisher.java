package com.olszewski.michal.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrorLogEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public ErrorLogEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void doStuffAndPublish(final String message) {
		ErrorLogEvent errorLogEvent = new ErrorLogEvent(this, message);
		eventPublisher.publishEvent(errorLogEvent);
	}
}
