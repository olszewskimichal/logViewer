package com.olszewski.michal.controller;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.olszewski.michal.domain.SortMethod;
import com.olszewski.michal.domain.search.SearchProperties;
import com.olszewski.michal.service.FileService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequestMapping("/log")
public class LogController {

	private final FileService fileService;

	@Value("${logging.path}")
	public String loggingPath;

	public LogController(FileService fileService) {
		this.fileService = fileService;
	}

	@GetMapping()
	public String folderViewPage(
			@RequestParam(required = false, defaultValue = "FILENAME") SortMethod sortBy,
			@RequestParam(required = false, defaultValue = "false") Boolean desc,
			@RequestParam(required = false) String file,
			Model model) {
		log.info(sortBy.toString() + " " + desc + " " + file + " " + loggingPath);
		if (file != null && !"null".equalsIgnoreCase(file))
			fileService.setFileNameInSession(file);
		String path = fileService.getFileNameFromSession(loggingPath);
		model.addAttribute("files", fileService.sortFileEntry(fileService.getFilesEntryFromPath(Paths.get(path)), sortBy, desc));
		model.addAttribute("currentFolder", path);
		model.addAttribute("searchProperties", new SearchProperties());
		Path parentPath = fileService.getParentPath(Paths.get(path));
		if (parentPath != null)
			model.addAttribute("parent", parentPath.toString().replaceAll("\\\\", "%5c"));
		return "index";
	}

}
