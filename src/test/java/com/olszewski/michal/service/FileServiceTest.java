package com.olszewski.michal.service;

import static java.time.LocalDate.of;
import static java.util.Date.from;
import static java.util.Optional.of;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import com.olszewski.michal.builders.FileEntryBuilder;
import com.olszewski.michal.domain.FileEntry;
import com.olszewski.michal.domain.FileType;
import com.olszewski.michal.domain.SearchResult;
import com.olszewski.michal.domain.SortMethod;
import com.olszewski.michal.domain.search.SearchFileName;
import com.olszewski.michal.domain.search.SearchModifiedDate;
import com.olszewski.michal.domain.search.SearchProperties;
import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class FileServiceTest {

	@Mock
	private HttpSession httpSession;

	FileService fileService;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		fileService = new FileService(httpSession);
	}

	@Test
	public void shouldReturnCorrectTxtFile() {
		FileEntry fileEntry = fileService.createFileEntry(Paths.get("D:/logi/file.txt"));
		assertThat(fileEntry).isNotNull();
		assertThat(fileEntry.getFilename()).isEqualTo("file.txt");
		assertThat(fileEntry.getModified()).isNotNull();
		assertThat(fileEntry.getFileType()).isEqualTo(FileType.FILE);
		assertThat(fileEntry.getSize()).isGreaterThan(0L);
	}

	@Test
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

	@Test
	public void shouldSortFileEntriesByName() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withName("aac").build(), new FileEntryBuilder().withName("aaa").build(), new FileEntryBuilder().withName("aab").build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.FILENAME, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withName("aaa").build(), new FileEntryBuilder().withName("aab").build(), new FileEntryBuilder().withName("aac").build());
	}

	@Test
	public void shouldSortFileEntriesBySize() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withSize(3L).build(), new FileEntryBuilder().withSize(1L).build(), new FileEntryBuilder().withSize(4L).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.SIZE, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withSize(1L).build(), new FileEntryBuilder().withSize(3L).build(), new FileEntryBuilder().withSize(4L).build());
	}

	@Test
	public void shouldSortFileEntriesByFileType() {
		//given
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withType(FileType.ARCHIVE).build(), new FileEntryBuilder().withType(FileType.FILE).build(), new FileEntryBuilder().withType(FileType.DIRECTORY).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.FILETYPE, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withType(FileType.FILE).build(), new FileEntryBuilder().withType(FileType.DIRECTORY).build(), new FileEntryBuilder().withType(FileType.ARCHIVE).build());
	}

	@Test
	public void shouldSortFileEntriesByModified() {
		//given
		Instant now = Instant.now();
		List<FileEntry> list = Arrays.asList(new FileEntryBuilder().withModified(now).build(), new FileEntryBuilder().withModified(Instant.MAX).build(), new FileEntryBuilder().withModified(Instant.MIN).build());
		//when
		List<FileEntry> fileEntries = fileService.sortFileEntry(list, SortMethod.MODIFIED, true);
		//then
		assertThat(fileEntries).containsExactly(new FileEntryBuilder().withModified(Instant.MIN).build(), new FileEntryBuilder().withModified(now).build(), new FileEntryBuilder().withModified(Instant.MAX).build());
	}

	@Test
	public void getFileContent() throws IOException, ArchiveException {
		List<String> fileContent = fileService.getFileContent(Paths.get("D:\\logi\\file.zip"), "file.txt");
		System.out.println(fileContent);
		assertThat(fileContent).isNotEmpty();
	}

	@Test
	public void getFileContent2() throws IOException, ArchiveException {
		List<String> fileContent = fileService.getFileContent(Paths.get("D:\\logi\\file.7z"), "file.txt");
		System.out.println(fileContent);
		assertThat(fileContent).isNotEmpty();
	}

	@Test
	public void shouldSearchEntriesFromDateToDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateFrom(of(from(of(2017, 03, 25).atStartOfDay().toInstant(ZoneOffset.UTC))));
		modifiedDate.setDateTo(of(from(LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void shouldSearchEntriesFromDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateFrom(of(from(of(2017, 03, 25).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void shouldSearchEntriesToDate() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchModifiedDate modifiedDate = new SearchModifiedDate();
		modifiedDate.setDateTo(of(from(of(2017, 03, 25).atStartOfDay().toInstant(ZoneOffset.UTC))));
		searchProperties.setSearchModifiedDate(modifiedDate);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(4);
	}

	@Test
	public void shouldSearchAllEntries() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(5);
	}

	@Test
	public void shouldSearchEntriesWithRegexName() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchFileName searchFileName = new SearchFileName();
		searchFileName.setUseRegex(true);
		searchFileName.setContent(".*.log");
		searchProperties.setSearchFileName(searchFileName);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void shouldSearchEntriesWithNameContent() throws IOException {
		SearchProperties searchProperties = new SearchProperties();
		SearchFileName searchFileName = new SearchFileName();
		searchFileName.setUseRegex(false);
		searchFileName.setContent("spring.log");
		searchProperties.setSearchFileName(searchFileName);
		List<FileEntry> result = fileService.getAllFileEntries(Paths.get("D:\\logi"), searchProperties);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
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
}