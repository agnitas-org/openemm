/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;

import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MailingStatatisticListForm extends PaginationForm {

    private int mailingID;

    private String[] availableTargets;

    private String shortname;

    private String searchQueryText; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean searchNameChecked; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean searchDescriptionChecked; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean inEditColumnsMode;
    private String filterSendDateBegin;
    private String filterSendDateEnd;

    private String filterName;
    private String filterDescription;

    private String[] additionalFields = ArrayUtils.EMPTY_STRING_ARRAY;

    private int[] filteredMailingLists = ArrayUtils.EMPTY_INT_ARRAY;

    private int[] filteredTargetGroups = ArrayUtils.EMPTY_INT_ARRAY; // TODO: remove after EMMGUI-714 will be finished and old design will removed

    public boolean isSearchNameChecked() {
        return searchNameChecked;
    }

    public void setSearchNameChecked(boolean searchNameChecked) {
        this.searchNameChecked = searchNameChecked;
    }

    public boolean isSearchDescriptionChecked() {
        return searchDescriptionChecked;
    }

    public boolean isInEditColumnsMode() {
        return inEditColumnsMode;
    }

    public void setInEditColumnsMode(boolean inEditColumnsMode) {
        this.inEditColumnsMode = inEditColumnsMode;
    }

    public void setSearchDescriptionChecked(boolean searchDescriptionChecked) {
        this.searchDescriptionChecked = searchDescriptionChecked;
    }

    public String getSearchQueryText() {
        return searchQueryText;
    }

    public void setSearchQueryText(String searchQueryText) {
        this.searchQueryText = searchQueryText;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public String[] getAvailableTargets() {
        return availableTargets;
    }

    public void setAvailableTargets(String[] availableTargets) {
        this.availableTargets = availableTargets;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String[] getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(String[] additionalFields) {
        this.additionalFields = additionalFields;
    }

    public Set<String> getAdditionalFieldsSet() {
        if (ArrayUtils.isEmpty(additionalFields)) {
            return Collections.emptySet();
        }

        return Set.of(additionalFields);
    }

    public List<Integer> getFilteredMailingListsAsList() {
        if (ArrayUtils.isEmpty(filteredMailingLists)) {
            return Collections.emptyList();
        }

        return Arrays.stream(filteredMailingLists)
                .boxed()
                .collect(Collectors.toList());
    }

    public int[] getFilteredMailingLists() {
        return filteredMailingLists;
    }

    public void setFilteredMailingLists(int[] filteredMailingLists) {
        this.filteredMailingLists = filteredMailingLists;
    }

    public List<Integer> getFilteredTargetGroupsAsList() {
        if (ArrayUtils.isEmpty(filteredTargetGroups)) {
            return Collections.emptyList();
        }

        return Arrays.stream(filteredTargetGroups)
                .boxed()
                .collect(Collectors.toList());
    }

    public int[] getFilteredTargetGroups() {
        return filteredTargetGroups;
    }

    public void setFilteredTargetGroups(int[] filteredTargetGroups) {
        this.filteredTargetGroups = filteredTargetGroups;
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
}
