/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MailingsListProperties {
    private String types;
    private String mediaTypes;
    private boolean isTemplate;
    private String searchQuery;
    private boolean searchName;
    private boolean searchDescription;
    private boolean searchContent;
    private List<String> statuses;
    private List<String> badge;
    private List<Integer> mailingLists;
    private List<Integer> archives;
    private List<Integer> targetGroups;
    private Date sendDateBegin;
    private Date sendDateEnd;
    private Date creationDateBegin;
    private Date creationDateEnd;
    private Date changeDateBegin;
    private Date changeDateEnd;
    private String sort;
    private String direction;
    private int page;
    private int rownums;
    private boolean includeTargetGroups;
    private Boolean isGrid;
    private Set<String> additionalColumns = new HashSet<>();

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(String mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public boolean isSearchName() {
        return searchName;
    }

    public void setSearchName(boolean searchName) {
        this.searchName = searchName;
    }

    public boolean isSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(boolean searchDescription) {
        this.searchDescription = searchDescription;
    }

    public boolean isSearchContent() {
        return searchContent;
    }

    public void setSearchContent(boolean searchContent) {
        this.searchContent = searchContent;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public List<String> getBadge() {
        return badge;
    }

    public void setBadge(List<String> badge) {
        this.badge = badge;
    }

    public List<Integer> getMailingLists() {
        return mailingLists;
    }

    public void setMailingLists(List<Integer> mailingLists) {
        this.mailingLists = mailingLists;
    }

    public List<Integer> getArchives() {
        return archives;
    }

    public void setArchives(List<Integer> archives) {
        this.archives = archives;
    }

    public Date getSendDateBegin() {
        return sendDateBegin;
    }

    public void setSendDateBegin(Date sendDateBegin) {
        this.sendDateBegin = sendDateBegin;
    }

    public Date getSendDateEnd() {
        return sendDateEnd;
    }

    public void setSendDateEnd(Date sendDateEnd) {
        this.sendDateEnd = sendDateEnd;
    }

    public Date getCreationDateBegin() {
        return creationDateBegin;
    }

    public void setCreationDateBegin(Date creationDateBegin) {
        this.creationDateBegin = creationDateBegin;
    }

    public Date getCreationDateEnd() {
        return creationDateEnd;
    }

    public void setCreationDateEnd(Date creationDateEnd) {
        this.creationDateEnd = creationDateEnd;
    }

    public Date getChangeDateBegin() {
        return changeDateBegin;
    }

    public void setChangeDateBegin(Date changeDateBegin) {
        this.changeDateBegin = changeDateBegin;
    }

    public Date getChangeDateEnd() {
        return changeDateEnd;
    }

    public void setChangeDateEnd(Date changeDateEnd) {
        this.changeDateEnd = changeDateEnd;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRownums() {
        return rownums;
    }

    public void setRownums(int rownums) {
        this.rownums = rownums;
    }

    public boolean isIncludeTargetGroups() {
        return includeTargetGroups;
    }

    public void setIncludeTargetGroups(boolean includeTargetGroups) {
        this.includeTargetGroups = includeTargetGroups;
    }

    public List<Integer> getTargetGroups() {
        return targetGroups;
    }

    public void setTargetGroups(List<Integer> targetGroups) {
        this.targetGroups = targetGroups;
    }

    public Set<String> getAdditionalColumns() {
        return additionalColumns;
    }

    public void setAdditionalColumns(Set<String> additionalColumns) {
        this.additionalColumns = additionalColumns;
    }

    public Boolean getGrid() {
        return isGrid;
    }

    public void setGrid(Boolean grid) {
        isGrid = grid;
    }
}
