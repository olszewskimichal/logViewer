package com.olszewski.michal.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SearchResult {
	private FileEntry entry;
	private List<String> result = new ArrayList<>();
}
