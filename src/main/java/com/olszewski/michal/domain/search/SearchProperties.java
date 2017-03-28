package com.olszewski.michal.domain.search;

import lombok.Data;

@Data
public class SearchProperties {
	private SearchModifiedDate searchModifiedDate;
	private SearchFileName searchFileName;
	private String fileContent;
	private Boolean recursive = false;

}
