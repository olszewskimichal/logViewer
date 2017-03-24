package com.olszewski.michal.builders;

import java.time.Instant;

import com.olszewski.michal.domain.FileEntry;
import com.olszewski.michal.domain.FileType;

public class FileEntryBuilder {
	private FileEntry entry = new FileEntry();

	public FileEntryBuilder withName(String name) {
		entry.setFilename(name);
		return this;
	}

	public FileEntryBuilder withSize(Long size) {
		entry.setSize(size);
		return this;
	}

	public FileEntryBuilder withType(FileType type) {
		entry.setFileType(type);
		return this;
	}

	public FileEntryBuilder withModified(Instant modified) {
		entry.setModified(modified);
		return this;
	}

	public FileEntry build() {
		return entry;
	}
}
