package com.olszewski.michal.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrorLogEventListener implements ApplicationListener<ErrorLogEvent> {
	@Override
	public void onApplicationEvent(ErrorLogEvent errorLogEvent) {
		log.info(errorLogEvent.getMessage());
	}
}
