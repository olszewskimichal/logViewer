package com.olszewski.michal.controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.search.SearchProperties;
import com.olszewski.michal.exceptions.FileNotFoundException;
import com.olszewski.michal.service.FileService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/log")
@Slf4j
public class LogRestController {

	@Value("${logging.path}")
	public String loggingPath;

	private final FileService fileService;

	public LogRestController(FileService fileService) {
		this.fileService = fileService;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody public List<SearchResult> searchFiles(SearchProperties properties) throws IOException {
		String path = fileService.getFileNameFromSession(loggingPath);
		return fileService.getLinesFromFiles(Paths.get(path), properties);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/view")
	@ResponseBody public List<String> fileContentViewPage(@RequestParam String file, @RequestParam(required = false) Integer tailLines) {
		log.info(file + " " + tailLines);
		try {
			String path = fileService.getFileNameFromSession(loggingPath);
			if (tailLines != null)
				return fileService.tailContent(Paths.get(path), file, tailLines);
			else
				return fileService.getFileContent(Paths.get(path), file);
		}
		catch (IOException e) {
			throw new FileNotFoundException("Blad podczas wyswietlania podgladu pliku " + e);
		}
	}
}
