package com.olszewski.michal.tasks;

import static java.util.Date.from;
import static java.util.Optional.of;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.search.SearchModifiedDate;
import com.olszewski.michal.domain.search.SearchProperties;
import com.olszewski.michal.events.ErrorLogEventPublisher;
import com.olszewski.michal.service.FileService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorMsgAlarmTask {

	private final FileService fileService;
	private final ErrorLogEventPublisher eventPublisher;

	@Value("${logging.path}")
	public String loggingPath;

	public ErrorMsgAlarmTask(FileService fileService, ErrorLogEventPublisher eventPublisher) {
		this.fileService = fileService;
		this.eventPublisher = eventPublisher;
	}

	@Scheduled(cron = "0 30 7 * * *")
	public void searchExceptions() throws IOException {
		log.info("Szukam w logach jakis wyjatków");
		SearchProperties searchProperties = new SearchProperties();
		searchProperties.setRecursive(true);
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateFrom(of(from(LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))));
		modifiedDate.setDateTo(of(from(LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		searchProperties.setFileContent("");
		List<SearchResult> linesFromFiles = fileService.getLinesFromFiles(Paths.get(loggingPath), searchProperties);
		linesFromFiles.forEach(v -> v.getResult().stream().filter(line ->
				line.toLowerCase().contains("FileNotFoundException".toLowerCase()) ||
						line.toLowerCase().contains("FileProcessingException".toLowerCase()) ||
						line.toLowerCase().contains("NullPointerException".toLowerCase())
		).forEach(eventPublisher::doStuffAndPublish));

	}

}
