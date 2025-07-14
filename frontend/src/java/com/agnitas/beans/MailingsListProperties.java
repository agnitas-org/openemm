/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MailingsListProperties {

    private String types;
    private String mediaTypes;
    private boolean isTemplate;
    private String searchQuery; // TODO: remove after EMMGUI-714 will be finished and old design will removed. Note: if not necessary for mailing statistics search
    private boolean searchName; // TODO: remove after EMMGUI-714 will be finished and old design will removed. Note: if not necessary for mailing statistics search
    private boolean searchDescription; // TODO: remove after EMMGUI-714 will be finished and old design will removed. Note: if not necessary for mailing statistics search
    private boolean searchContent; // TODO: remove after EMMGUI-714 will be finished and old design will removed. Note: if not necessary for mailing statistics search
    private boolean isRedesignedUiUsed; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private String searchNameStr;
    private String searchDescriptionStr;
    private String searchContentStr;
    private List<String> statuses;
    private List<String> badge;
    private List<Integer> mailingLists;
    private List<Integer> archives;
    private List<Integer> targetGroups;
    private Date sendDateBegin;
    private Date sendDateEnd;
    private Date creationDateBegin;
    private Date creationDateEnd;
    private Date planDateBegin;
    private Date planDateEnd;
    private Date changeDateBegin;
    private Date changeDateEnd;
    private String sort;
    private String direction;
    private int page;
    private int rownums;
    private Boolean isGrid;
    private boolean useRecycleBin;
    private boolean isMailingStatisticsOverview;
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

    public Date getPlanDateBegin() {
        return planDateBegin;
    }

    public void setPlanDateBegin(Date planDateBegin) {
        this.planDateBegin = planDateBegin;
    }

    public Date getPlanDateEnd() {
        return planDateEnd;
    }

    public void setPlanDateEnd(Date planDateEnd) {
        this.planDateEnd = planDateEnd;
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

    public boolean isUseRecycleBin() {
        return useRecycleBin;
    }

    public void setUseRecycleBin(boolean useRecycleBin) {
        this.useRecycleBin = useRecycleBin;
    }

    public String getSearchNameStr() {
        return searchNameStr;
    }

    public void setSearchNameStr(String searchNameStr) {
        this.searchNameStr = searchNameStr;
    }

    public String getSearchDescriptionStr() {
        return searchDescriptionStr;
    }

    public void setSearchDescriptionStr(String searchDescriptionStr) {
        this.searchDescriptionStr = searchDescriptionStr;
    }

    public String getSearchContentStr() {
        return searchContentStr;
    }

    public void setSearchContentStr(String searchContentStr) {
        this.searchContentStr = searchContentStr;
    }

    public boolean isRedesignedUiUsed() {
        return isRedesignedUiUsed;
    }

    public void setRedesignedUiUsed(boolean redesignedUiUsed) {
        isRedesignedUiUsed = redesignedUiUsed;
    }

    public void setMailingStatisticsOverview(boolean mailingStatisticsOverview) {
        isMailingStatisticsOverview = mailingStatisticsOverview;
    }

    public boolean isMailingStatisticsOverview() {
        return isMailingStatisticsOverview;
    }

    public boolean isUiFiltersSet() {
        return (!isMailingStatisticsOverview && isNotBlank(types)) || isNotBlank(mediaTypes) || isNotBlank(searchNameStr) || isNotBlank(searchDescriptionStr)
                || isNotBlank(searchContentStr) || (!isMailingStatisticsOverview && isNotEmpty(statuses)) || isNotEmpty(badge) || isNotEmpty(mailingLists)
                || isNotEmpty(archives) || isNotEmpty(targetGroups) || sendDateBegin != null || sendDateEnd != null
                || creationDateBegin != null || creationDateEnd != null || planDateBegin != null || planDateEnd != null
                || changeDateBegin != null || changeDateEnd != null || isGrid != null;
    }
}
