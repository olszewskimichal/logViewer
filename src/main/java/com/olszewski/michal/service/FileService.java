package com.olszewski.michal.service;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.olszewski.michal.domain.FileEntry;
import com.olszewski.michal.domain.FileType;
import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.SortMethod;
import com.olszewski.michal.domain.search.SearchProperties;
import com.olszewski.michal.exceptions.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileService {

	private static final String FILE_NAME = "fileName";
	private final HttpSession httpSession;

	public FileService(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public List<FileEntry> getFilesEntryFromPath(Path path) {
		try {
			final List<FileEntry> files = new ArrayList<>();
			switch (getFileType(path)) {
				case FILE:
				case DIRECTORY:
					DirectoryStream<Path> paths = newDirectoryStream(path);
					paths.forEach(filepath -> files.add(createFileEntry(filepath)));
					paths.close();
					break;
				case ARCHIVE:
					getFilesFromArchive(path, files);
					break;
			}

			return files;
		}
		catch (IOException | ArchiveException e) {
			throw new FileProcessingException("Blad podczas pobierania getFilesEntryFromPath", e);
		}
	}

	private void getFilesFromArchive(Path path, List<FileEntry> files) throws IOException, ArchiveException {
		if (iz7z(path)) {
			SevenZFile sevenZFile = new SevenZFile(path.toFile());
			SevenZArchiveEntry entry;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				files.add(createFileEntryFromTarEntry(entry, path));
			}
			sevenZFile.close();
		}
		else {
			ArchiveInputStream input = new ArchiveStreamFactory()
					.createArchiveInputStream(new BufferedInputStream(new FileInputStream(path.toFile())));
			ArchiveEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				files.add(createFileEntryFromTarEntry(entry, path));
			}
		}
	}

	public List<FileEntry> sortFileEntry(List<FileEntry> entries, SortMethod sortMethod, Boolean desc) {
		if (desc)
			entries.sort(getComparator(sortMethod));
		else entries.sort(getComparator(sortMethod).reversed());
		return entries;
	}

	private Comparator<FileEntry> getComparator(SortMethod sortMethod) {
		switch (sortMethod) {
			case FILENAME:
				return Comparator.comparing(FileEntry::getFilename);
			case SIZE:
				return Comparator.comparingLong(FileEntry::getSize);
			case MODIFIED:
				return Comparator.comparing(FileEntry::getModified);
			case FILETYPE:
				return Comparator.comparing(FileEntry::getFileType);
		}
		throw new IllegalArgumentException("Incorrect method type");
	}

	public FileEntry createFileEntry(Path path) {
		final FileEntry fileEntry = new FileEntry();
		try {
			fileEntry.setFilename(path.getFileName().toString());
			fileEntry.setModified(Files.getLastModifiedTime(path).toInstant());
			fileEntry.setSize(Files.size(path));
			fileEntry.setFilePath(path);
		}
		catch (IOException e) {
			throw new FileProcessingException("Unable to get file attribute", e);
		}
		fileEntry.setFileType(getFileType(path));
		return fileEntry;
	}

	private static FileEntry createFileEntryFromTarEntry(ArchiveEntry entry, Path path) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setFilename(entry.getName());
		fileEntry.setFilePath(Paths.get(path.toString()));
		fileEntry.setSize(entry.getSize());
		fileEntry.setFileType(entry.isDirectory() ? FileType.DIRECTORY : FileType.FILE);
		fileEntry.setModified(entry.getLastModifiedDate().toInstant());
		fileEntry.setFileType(getFileType(Paths.get(path.toString(), entry.getName())));
		return fileEntry;
	}

	public Path getParentPath(Path loggingPath) {
		return loggingPath.getParent();
	}

	private static FileType getFileType(Path path) {
		FileType fileType;
		if (path.toFile().isDirectory()) {
			fileType = FileType.DIRECTORY;
		}
		else if (isZip(path) || iz7z(path)) {
			fileType = FileType.ARCHIVE;
		}
		else {
			fileType = FileType.FILE;
		}
		return fileType;
	}


	private static boolean isZip(Path path) {
		return !path.toFile().isDirectory() && path.getFileName().toString().endsWith(".zip");
	}

	private static boolean iz7z(Path path) {
		return !path.toFile().isDirectory() && path.getFileName().toString().endsWith(".7z");
	}


	public String getFileNameFromSession(String loggingPath) {
		if (httpSession.getAttribute(FILE_NAME) == null) {
			httpSession.setAttribute(FILE_NAME, loggingPath);
		}
		return (String) httpSession.getAttribute(FILE_NAME);
	}

	public void setFileNameInSession(String file) {
		httpSession.setAttribute(FILE_NAME, file);
	}


	public List<String> tailContent(Path path, String filename, Optional<String> term, int lines) throws IOException {
		if (isZip(path) || iz7z(path))
			return singletonList("Nie mozna tailować pliku znajdującego się w archiwum");
		List<String> content = new ArrayList<>();

		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(Paths.get(path.toString(), filename).toFile(), Charset.defaultCharset())) {
			int i = 0;
			String line;
			while ((line = reader.readLine()) != null && i++ < lines) {
				content.add(line);
			}
			Collections.reverse(content);
		}
		term.ifPresent(v ->
				content.removeIf(c -> !c.toLowerCase().contains(v.toLowerCase()))
		);
		return content;

	}

	public List<String> getFileContent(Path file, String filename, Optional<String> term) throws IOException {
		List<String> lines = new ArrayList<>();
		if (iz7z(file)) {
			readLinesFrom7zArchive(file, filename, lines);
		}
		else if (isZip(file)) {
			readLinesFromZipArchive(file, filename, lines);
		}
		else if (getFileType(file).equals(FileType.DIRECTORY))
			lines.addAll(IOUtils.readLines(new FileInputStream(Paths.get(file.toString(), filename).toFile()), Charset.defaultCharset()));
		else
			lines.addAll(IOUtils.readLines(new FileInputStream(file.toFile()), Charset.defaultCharset()));
		return term.map(s -> lines.stream().filter(v -> v.toLowerCase().contains(s.toLowerCase())).collect(toList())).orElse(lines);
	}

	private void readLinesFrom7zArchive(Path file, String filename, List<String> lines) throws IOException {
		SevenZFile sevenZFile = new SevenZFile(file.toFile());
		SevenZArchiveEntry entry;
		while ((entry = sevenZFile.getNextEntry()) != null) {
			if (entry.getName().equalsIgnoreCase(filename)) {
				byte[] content = new byte[(int) entry.getSize()];
				sevenZFile.read(content, 0, content.length);
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				lines.addAll(IOUtils.readLines(bais, Charset.defaultCharset()));
			}
		}
		sevenZFile.close();
	}

	private void readLinesFromZipArchive(Path file, String filename, List<String> lines) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file.toFile());
		try (ArchiveInputStream input = new ArchiveStreamFactory()
				.createArchiveInputStream(new BufferedInputStream(fileInputStream))) {
			ArchiveEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				if (entry.getName().equalsIgnoreCase(filename))
					lines.addAll(IOUtils.readLines(input, Charset.defaultCharset()));
			}
		}
		catch (ArchiveException e) {
			throw new FileProcessingException(e.getMessage());
		}
		finally {
			fileInputStream.close();
		}
	}

	public List<FileEntry> getAllFileEntries(Path path, SearchProperties searchProperties) throws IOException {
		List<FileEntry> result = new ArrayList<>();
		List<FileEntry> files = getFilesEntryFromPath(path);
		Optional.ofNullable(searchProperties.getSearchModifiedDate()).
				ifPresent(x ->
						files.removeIf(
								v -> v.getModified().isBefore(x.getInstantDateFrom()) ||
										v.getModified().isAfter(x.getInstantDateTo())));
		Optional.ofNullable(searchProperties.getSearchFileName()).ifPresent(x -> {
			if (x.getUseRegex()) {
				Pattern r = Pattern.compile(x.getContent());
				files.removeIf(v -> !r.matcher(v.getFilename()).matches());
			}
			else {
				files.removeIf(v -> !v.getFilename().toLowerCase().contains(x.getContent().toLowerCase()));
			}

		});

		for (FileEntry entry : files) {
			result.add(entry);
			if (searchProperties.getRecursive() && !getFileType(Paths.get(path.toString(), entry.getFilename())).equals(FileType.FILE))
				result.addAll(getAllFileEntries(Paths.get(path.toString(), entry.getFilename()), searchProperties));
		}
		return result;
	}

	public List<SearchResult> getLinesFromFiles(Path path, SearchProperties searchProperties) throws IOException {
		List<SearchResult> searchResults = new ArrayList<>();
		for (FileEntry fileEntry : getAllFileEntries(path, searchProperties)) {
			Optional<SearchResult> searchResult = searchContentInEntry(searchProperties.getFileContent(), fileEntry);
			searchResult.ifPresent(searchResults::add);
		}
		return searchResults;
	}

	private Optional<SearchResult> searchContentInEntry(String term, FileEntry entry) throws IOException {
		if (entry.getFileType().equals(FileType.FILE)) {
			List<String> resultPerFile = new ArrayList<>();
			List<String> fileContent = getFileContent(entry.getPath(), entry.getFilename(), Optional.empty());
			for (int i = 0; i < fileContent.size(); i++) {
				if (fileContent.get(i).contains(term)) {
					resultPerFile.add(String.format("\tline %d: %s", i + 1, fileContent.get(i)));
				}
			}
			if (!resultPerFile.isEmpty()) {
				SearchResult searchResult = new SearchResult();
				searchResult.setEntry(entry);
				searchResult.setResult(resultPerFile);
				return Optional.of(searchResult);
			}
		}
		return Optional.empty();
	}
}
