package com.olszewski.michal.domain.search;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SearchModifiedDate {
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Optional<Date> dateFrom=Optional.empty();
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Optional<Date> dateTo=Optional.empty();

	public Instant getInstantDateFrom() {
		return dateFrom.map(Date::toInstant).orElse(Instant.MIN);
	}

	public Instant getInstantDateTo() {
		return dateTo.map(Date::toInstant).orElse(Instant.MAX);
	}
}
