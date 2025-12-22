/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.util.SortDirection;
import org.apache.commons.lang3.StringUtils;

public class PaginatedList<T> {

	private final List<T> partialList;
	private int fullListSize;
	private int pageSize;
	private int pageNumber = 1;
	private String sortCriterion;
	private SortDirection sortDirection = SortDirection.ASCENDING; // DESC or ASC
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
}
