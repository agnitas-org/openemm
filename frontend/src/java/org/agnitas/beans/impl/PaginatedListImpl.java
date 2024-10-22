/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.apache.commons.lang3.StringUtils;
import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

import java.util.ArrayList;
import java.util.List;

public class PaginatedListImpl<T> implements PaginatedList {

	private final List<T> partialList;
	private int fullListSize;
	private int pageSize;
	private int pageNumber = 1;
	private String sortCriterion;
	private SortOrderEnum sortDirection = SortOrderEnum.ASCENDING; // DESC or ASC
	private long notFilteredFullListSize = -1; // stores total count of entries when UI filters were set

	public PaginatedListImpl() {
		this.partialList = new ArrayList<>();
	}

	public PaginatedListImpl(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, String sortDirection) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;
		
		if ((StringUtils.isBlank(sortDirection) || "ASC".equalsIgnoreCase(sortDirection.trim()) || "ASCENDING".equalsIgnoreCase(sortDirection.trim()))) {
			this.sortDirection = SortOrderEnum.ASCENDING;
		} else if ("DESC".equalsIgnoreCase(sortDirection.trim()) || "DESCENDING".equalsIgnoreCase(sortDirection.trim())) {
			this.sortDirection =  SortOrderEnum.DESCENDING;
		} else {
			throw new RuntimeException("Invalid sorting direction");
		}
	}

	public PaginatedListImpl(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, boolean sortedAscending) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortedAscending ? SortOrderEnum.ASCENDING : SortOrderEnum.DESCENDING;
	}

	public PaginatedListImpl(List<T> partialList, int fullListSize, int pageSize, int pageNumber, String sortCriterion, SortOrderEnum sortDirection, long notFilteredFullListSize) {
		super();
		this.partialList = partialList;
		this.fullListSize = fullListSize;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortDirection == null ? SortOrderEnum.ASCENDING : sortDirection;
		this.notFilteredFullListSize = notFilteredFullListSize;
	}

	@Override
	public int getFullListSize() {
		return fullListSize;
	}

	@Override
	public List<T> getList() {
		return partialList;
	}

	@Override
	public int getObjectsPerPage() {
		return pageSize;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public String getSearchId()  {
		return null;
	}

	/**
	 * The sort criterion can be set after the creation of the object, to fit the camelcase sort criterion defined in JSPs
	 */
	public void setSortCriterion(String sortCriterion) {
		this.sortCriterion = sortCriterion; 
	}

	@Override
	public String getSortCriterion() {
		return sortCriterion; 
	}

	@Override
	public SortOrderEnum getSortDirection() {
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
