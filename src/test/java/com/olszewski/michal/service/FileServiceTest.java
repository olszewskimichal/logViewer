package com.olszewski.michal.service;

import static java.time.LocalDate.of;
import static java.util.Date.from;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.olszewski.michal.builders.FileEntryBuilder;
import com.olszewski.michal.domain.FileEntry;
import com.olszewski.michal.domain.FileType;
import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.SortMethod;
import com.olszewski.michal.domain.search.SearchFileName;
import com.olszewski.michal.domain.search.SearchModifiedDate;
import com.olszewski.michal.domain.search.SearchProperties;
import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(JUnitPlatform.class)
public class FileServiceTest {

	@Mock
	private HttpSession httpSession;

	FileService fileService;

	@BeforeEach
	public void setUp() throws Exception {
		initMocks(this);
		fileService = new FileService(httpSession);
	}

	@org.junit.jupiter.api.Test
	public void shouldReturnCorrectTxtFile() {
		FileEntry fileEntry = fileService.createFileEntry(Paths.get("D:/logi/file.txt"));
		assertThat(fileEntry).isNotNull();
		assertThat(fileEntry.getFilename()).isEqualTo("file.txt");
		assertThat(fileEntry.getModified()).isNotNull();
		assertThat(fileEntry.getFileType()).isEqualTo(FileType.FILE);
		assertThat(fileEntry.getSize()).isGreaterThan(0L);
	}

	@org.junit.jupiter.api.Test
	public void shouldReturnCorrectZipFile() {
		FileEntry fileEntry = fileService.createFileEntry(Paths.get("D:/logi/file.zip"));
		assertThat(fileEntry).isNotNull();
		assertThat(fileEntry.getFilename()).isEqualTo("file.zip");
		assertThat(fileEntry.getFileType()).isEqualTo(FileType.ARCHIVE);
		assertThat(fileEntry.getSize()).isGreaterThan(0L);

		fileEntry = fileService.createFileEntry(Paths.get("D:/logi/file.7z"));
		assertThat(fileEntry).isNotNull();
		assertThat(fileEntry.getFilename()).isEqualTo("file.7z");
		assertThat(fileEntry.getFileType()).isEqualTo(FileType.ARCHIVE);
		assertThat(fileEntry.getSize()).isGreaterThan(0L);
	}

	@org.junit.jupiter.api.Test
	public void shouldSortFileEntriesByName() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withName("aac").build(), new FileEntryBuilder().withName("aaa").build(), new FileEntryBuilder().withName("aab").build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.FILENAME, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withName("aaa").build(), new FileEntryBuilder().withName("aab").build(), new FileEntryBuilder().withName("aac").build());
	}

	@org.junit.jupiter.api.Test
	public void shouldSortFileEntriesBySize() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withSize(3L).build(), new FileEntryBuilder().withSize(1L).build(), new FileEntryBuilder().withSize(4L).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.SIZE, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withSize(1L).build(), new FileEntryBuilder().withSize(3L).build(), new FileEntryBuilder().withSize(4L).build());
	}

	@org.junit.jupiter.api.Test
	public void shouldSortFileEntriesByFileType() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withType(FileType.ARCHIVE).build(), new FileEntryBuilder().withType(FileType.FILE).build(), new FileEntryBuilder().withType(FileType.DIRECTORY).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.FILETYPE, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withType(FileType.FILE).build(), new FileEntryBuilder().withType(FileType.DIRECTORY).build(), new FileEntryBuilder().withType(FileType.ARCHIVE).build());
	}

	@org.junit.jupiter.api.Test
	public void shouldSortFileEntriesByModified() {
		//given
		Instant now = Instant.now();
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withModified(now).build(), new FileEntryBuilder().withModified(Instant.MAX).build(), new FileEntryBuilder().withModified(Instant.MIN).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.MODIFIED, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withModified(Instant.MIN).build(), new FileEntryBuilder().withModified(now).build(), new FileEntryBuilder().withModified(Instant.MAX).build());
	}

	@org.junit.jupiter.api.Test
	public void getFileContent() throws IOException, ArchiveException {
		List<String> fileContent = fileService.getFileContent(Paths.get("D:\\logi\\file.zip"), "file.txt", Optional.empty());
		System.out.println(fileContent);
		assertThat(fileContent).isNotEmpty();
	}

	@org.junit.jupiter.api.Test
	public void getFileContent2() throws IOException, ArchiveException {
		List<String> fileContent = fileService.getFileContent(Paths.get("D:\\logi\\file.7z"), "file.txt", Optional.empty());
		System.out.println(fileContent);
		assertThat(fileContent).isNotEmpty();
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesFromDateToDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateFrom(of(from(of(2017, 03, 25).atStartOfDay().toInstant(ZoneOffset.UTC))));
		modifiedDate.setDateTo(of(from(LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesFromDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateFrom(of(from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesToDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateTo(of(from(of(2017, 03, 25).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(4);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchAllEntries() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(5);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesWithRegexName() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchFileName searchFileName = new SearchFileName();
		searchFileName.setUseRegex(true);
		searchFileName.setContent(".*.log");
		searchProperties.setSearchFileName(searchFileName);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesWithNameContent() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchFileName searchFileName = new SearchFileName();
		searchFileName.setUseRegex(false);
		searchFileName.setContent("spring.log");
		searchProperties.setSearchFileName(searchFileName);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@org.junit.jupiter.api.Test
	public void shouldSearchEntriesWithFileContent() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		searchProperties.setFileContent("Spring");
		searchProperties.setRecursive(true);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(14);
		List<SearchResult> linesFromFiles = fileService.getLinesFromFiles(Paths.get("D:\\logi"), searchProperties);
		assertThat(linesFromFiles.size()).isEqualTo(1);
		System.out.println(linesFromFiles);
	}

	@org.junit.jupiter.api.Test
	@DisplayName("dupaTest")
	void lambdaExpressions() {
		List<Integer> numbers = Arrays.asList(1, 2, 3);
		assertTrue(numbers
				.stream()
				.mapToInt(i -> i)
				.sum() > 5, "Sum should be greater than 5");
	}

}