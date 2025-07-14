/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.ArrayUtils;

public class MailingOverviewForm extends PaginationForm {

    private boolean numberOfRowsChanged;
    private boolean forTemplates;
    private boolean inEditColumnsMode;
    private boolean searchInName = true; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean searchInContent = true; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean searchInDescription = true; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private String searchQueryText; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private String filterSendDateBegin;
    private String filterSendDateEnd;
    private String filterCreationDateBegin;
    private String filterCreationDateEnd;
    private String filterPlanDateBegin;
    private String filterPlanDateEnd;
    private String filterChangeDateBegin;
    private String filterChangeDateEnd;
    private boolean useRecycleBin;
    private List<String> selectedFields = new ArrayList<>();
    private Set<MailingType> mailingTypes = new HashSet<>(Collections.singletonList(MailingType.NORMAL));
    private Set<MediaTypes> mediaTypes = new HashSet<>();
    private Set<MailingStatus> filterStatuses;
    private List<String> filterBadges;
    private List<Integer> filterMailingLists;
    private List<Integer> filterArchives;
    private String filterName;
    private String filterDescription;
    private String filterContent;

    public boolean isNumberOfRowsChanged() {
        return numberOfRowsChanged;
    }

    public void setNumberOfRowsChanged(boolean numberOfRowsChanged) {
        this.numberOfRowsChanged = numberOfRowsChanged;
    }

    public boolean isForTemplates() {
        return forTemplates;
    }

    public void setForTemplates(boolean forTemplates) {
        this.forTemplates = forTemplates;
    }

    public boolean isSearchInName() {
        return searchInName;
    }

    public void setSearchInName(boolean searchInName) {
        this.searchInName = searchInName;
    }

    public boolean isSearchInContent() {
        return searchInContent;
    }

    public void setSearchInContent(boolean searchInContent) {
        this.searchInContent = searchInContent;
    }

    public boolean isSearchInDescription() {
        return searchInDescription;
    }

    public void setSearchInDescription(boolean searchInDescription) {
        this.searchInDescription = searchInDescription;
    }

    public String getSearchQueryText() {
        return searchQueryText;
    }

    public void setSearchQueryText(String searchQueryText) {
        this.searchQueryText = searchQueryText;
    }

    public String getFilterSendDateBegin() {
        return filterSendDateBegin;
    }

    public void setFilterSendDateBegin(String filterSendDateBegin) {
        this.filterSendDateBegin = filterSendDateBegin;
    }

    public String getFilterSendDateEnd() {
        return filterSendDateEnd;
    }

    public void setFilterSendDateEnd(String filterSendDateEnd) {
        this.filterSendDateEnd = filterSendDateEnd;
    }

    public String getFilterCreationDateBegin() {
        return filterCreationDateBegin;
    }

    public void setFilterCreationDateBegin(String filterCreationDateBegin) {
        this.filterCreationDateBegin = filterCreationDateBegin;
    }

    public String getFilterCreationDateEnd() {
        return filterCreationDateEnd;
    }

    public void setFilterCreationDateEnd(String filterCreationDateEnd) {
        this.filterCreationDateEnd = filterCreationDateEnd;
    }

    public String getFilterPlanDateBegin() {
        return filterPlanDateBegin;
    }

    public void setFilterPlanDateBegin(String filterPlanDateBegin) {
        this.filterPlanDateBegin = filterPlanDateBegin;
    }

    public String getFilterPlanDateEnd() {
        return filterPlanDateEnd;
    }

    public void setFilterPlanDateEnd(String filterPlanDateEnd) {
        this.filterPlanDateEnd = filterPlanDateEnd;
    }

    public String getFilterChangeDateBegin() {
        return filterChangeDateBegin;
    }

    public void setFilterChangeDateBegin(String filterChangeDateBegin) {
        this.filterChangeDateBegin = filterChangeDateBegin;
    }

    public String getFilterChangeDateEnd() {
        return filterChangeDateEnd;
    }

    public void setFilterChangeDateEnd(String filterChangeDateEnd) {
        this.filterChangeDateEnd = filterChangeDateEnd;
    }

    public Set<MailingType> getMailingTypes() {
        return mailingTypes;
    }

    public void setMailingTypes(Set<MailingType> mailingTypes) {
        this.mailingTypes = mailingTypes;
    }

    public Set<MediaTypes> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(Set<MediaTypes> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        this.selectedFields = selectedFields;
    }

    public Set<MailingStatus> getFilterStatuses() {
        return filterStatuses;
    }

    public void setFilterStatuses(Set<MailingStatus> filterStatuses) {
        this.filterStatuses = filterStatuses;
    }

    public List<String> getFilterBadges() {
        return filterBadges;
    }

    public void setFilterBadges(List<String> filterBadges) {
        this.filterBadges = filterBadges;
    }

    public List<Integer> getFilterMailingLists() {
        return filterMailingLists;
    }

    public void setFilterMailingLists(List<Integer> filterMailingLists) {
        this.filterMailingLists = filterMailingLists;
    }

    public List<Integer> getFilterArchives() {
        return filterArchives;
    }

    public void setFilterArchives(List<Integer> filterArchives) {
        this.filterArchives = filterArchives;
    }

    public boolean isUseRecycleBin() {
        return useRecycleBin;
    }

    public void setUseRecycleBin(boolean useRecycleBin) {
        this.useRecycleBin = useRecycleBin;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterDescription() {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }

    public String getFilterContent() {
        return filterContent;
    }

    public void setFilterContent(String filterContent) {
        this.filterContent = filterContent;
    }

    public boolean isInEditColumnsMode() {
        return inEditColumnsMode;
    }

    public void setInEditColumnsMode(boolean inEditColumnsMode) {
        this.inEditColumnsMode = inEditColumnsMode;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("page", getPage());
        map.put("forTemplates", forTemplates);
        map.put("inEditColumnsMode", inEditColumnsMode);
        map.put("searchInName", searchInName);
        map.put("searchInContent", searchInContent);
        map.put("searchInDescription", searchInDescription);
        map.put("mailingTypes", mailingTypes);
        map.put("mediaTypes", mediaTypes);
        map.put("selectedFields", selectedFields);
        map.put("searchQueryText", searchQueryText);
        map.put("filterSendDateBegin", filterSendDateBegin);
        map.put("filterSendDateEnd", filterSendDateEnd);
        map.put("filterCreationDateBegin", filterCreationDateBegin);
        map.put("filterCreationDateEnd", filterCreationDateEnd);
        map.put("filterChangeDateBegin", filterChangeDateBegin);
        map.put("filterChangeDateEnd", filterChangeDateEnd);
        map.put("filterStatuses", filterStatuses);
        map.put("filterBadges", filterBadges);
        map.put("filterMailingLists", filterMailingLists);
        map.put("filterArchives", filterArchives);
        map.put("useRecycleBin", useRecycleBin);
        map.put("filterName", filterName);
        map.put("filterDescription", filterDescription);
        map.put("filterContent", filterContent);
        return map;
    }

    @Override
    public Object[] toArray() {
        return ArrayUtils.addAll(Arrays.asList(
                forTemplates,
                inEditColumnsMode,
                searchInName,
                searchInContent,
                searchInDescription,
                mailingTypes,
                mediaTypes,
                selectedFields,
                searchQueryText,
                filterSendDateBegin,
                filterSendDateEnd,
                filterCreationDateBegin,
                filterCreationDateEnd,
                filterChangeDateBegin,
                filterChangeDateEnd,
                filterStatuses,
                filterBadges,
                filterMailingLists,
                filterArchives,
                useRecycleBin,
                filterName,
                filterDescription,
                filterContent
        ).toArray(), super.toArray());
    }
}
