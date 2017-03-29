package com.olszewski.michal.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.io.FileUtils;


@Data
public class FileEntry {
	private String filename;
	private Path filePath;
	@JsonIgnore
	private Instant modified;
	@JsonIgnore
	private FileType fileType;
	@JsonIgnore
	private long size;

	@JsonIgnore
	public String getCustomSize() {
		return FileUtils.byteCountToDisplaySize(size);
	}

	@JsonIgnore
	public String getCustomModified() {
		DateTimeFormatter formatter =
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
						.withLocale(Locale.UK)
						.withZone(ZoneId.systemDefault());
		return formatter.format(modified);
	}

	public String getFilename() {
		return filename;
	}

	public String getFilePath() {
		if (filePath != null)
			return filePath.toString();
		return filename;
	}

	@JsonIgnore
	public Path getPath() {
		return filePath;
	}
}
