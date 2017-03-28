package com.olszewski.michal.controller;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.SortMethod;
import com.olszewski.michal.domain.search.SearchProperties;
import com.olszewski.michal.exceptions.FileNotFoundException;
import com.olszewski.michal.service.FileService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

	@GetMapping(value = "/view")
	public void fileContentViewPage(@RequestParam String file, @RequestParam(required = false) Integer tailLines, HttpServletResponse response) {
		try {
			String path = fileService.getFileNameFromSession(loggingPath);
			if (tailLines != null)
				fileService.tailContent(Paths.get(path), file, response.getOutputStream(), tailLines);
			else
				fileService.streamContent(Paths.get(path), file, response.getOutputStream());
		}
		catch (IOException e) {
			throw new FileNotFoundException("Blad podczas wyswietlania podgladu pliku {}", e);
		}
	}

	@PostMapping
	public void confirmSearch(@Valid SearchProperties searchProperties, HttpServletResponse response) throws IOException {
		log.info(searchProperties.toString());
		String path = fileService.getFileNameFromSession(loggingPath);
		ServletOutputStream outputStream = response.getOutputStream();
		List<SearchResult> linesFromFiles = fileService.getLinesFromFiles(Paths.get(path), searchProperties);
		linesFromFiles.forEach(v -> {
			try {
				outputStream.println(v.getEntry().getFilePath() + " " + v.getEntry().getFilename());
				for (String s : v.getResult()) {
					outputStream.println(s);
				}
			}
			catch (IOException e) {
				log.error(e.getMessage());
			}
		});
	}
}
