/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.util.SortDirection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;

@JsonPropertyOrder({ "page", "content" })
public class PaginatedList<T> {

	@JsonProperty("list")
	private final List<T> partialList;
	@JsonIgnore
	private int fullListSize;
	@JsonIgnore
	private int pageSize;
	@JsonIgnore
	private int pageNumber = 1;
	@JsonIgnore
	private String sortCriterion;
	@JsonIgnore
	private SortDirection sortDirection = SortDirection.ASCENDING; // DESC or ASC
	@JsonIgnore
	private long notFilteredFullListSize = -1; // stores total count of entries when UI filters were set

	public PaginatedList() {
		this.partialList = new ArrayList<>();
	}

	public PaginatedList(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, String sortDirection) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;

		if ((StringUtils.isBlank(sortDirection) || "ASC".equalsIgnoreCase(sortDirection.trim()) || "ASCENDING".equalsIgnoreCase(sortDirection.trim()))) {
			this.sortDirection = SortDirection.ASCENDING;
		} else if ("DESC".equalsIgnoreCase(sortDirection.trim()) || "DESCENDING".equalsIgnoreCase(sortDirection.trim())) {
			this.sortDirection =  SortDirection.DESCENDING;
		} else {
			throw new IllegalArgumentException("Invalid sorting direction");
		}
	}

	public PaginatedList(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, boolean sortedAscending) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortedAscending ? SortDirection.ASCENDING : SortDirection.DESCENDING;
	}

	public PaginatedList(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, SortDirection sortDirection, long notFilteredFullListSize) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortDirection == null ? SortDirection.ASCENDING : sortDirection;
		this.notFilteredFullListSize = notFilteredFullListSize;
	}

	public static <T> PaginatedList<T> of(List<T> fullList, int page, int size) {
		int totalCount = fullList.size();
		int start = Math.min((page - 1) * size, totalCount);
		int end = Math.min(start + size, totalCount);
		List<T> partialList = isEmpty(fullList)
				? emptyList()
				: fullList.subList(start, end);
		return new PaginatedList<>(
				partialList,
				totalCount,
				size,
				page,
				"",
				false
		);
	}

	public static <T> PaginatedList<T> empty() {
		return PaginatedList.of(emptyList(), 0, 0);
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public List<T> getList() {
		return partialList;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * The sort criterion can be set after the creation of the object, to fit the camelcase sort criterion defined in JSPs
	 */
	public void setSortCriterion(String sortCriterion) {
		this.sortCriterion = sortCriterion; 
	}

	public String getSortCriterion() {
		return sortCriterion; 
	}

	public SortDirection getSortDirection() {
		return sortDirection;
	}

	public int getPageSize() {
		return pageSize;
	}

	public long getNotFilteredFullListSize() {
		return notFilteredFullListSize;
	}

	public void setNotFilteredFullListSize(long notFilteredFullListSize) {
		this.notFilteredFullListSize = notFilteredFullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setSortDirection(SortDirection sortDirection) {
		this.sortDirection = sortDirection;
	}

	@JsonProperty("page")
	public Map<String, Object> getPageMeta() {
		Map<String, Object> page = new HashMap<>(); // keep modifiable
		page.put("number", pageNumber);
		page.put("size", pageSize);
		page.put("totalCount", fullListSize);
		return page;
	}
}
