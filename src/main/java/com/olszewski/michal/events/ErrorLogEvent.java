package com.olszewski.michal.events;

import org.springframework.context.ApplicationEvent;

public class ErrorLogEvent extends ApplicationEvent {

	private String message;

	public ErrorLogEvent(Object source, String message) {
		super(source);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
