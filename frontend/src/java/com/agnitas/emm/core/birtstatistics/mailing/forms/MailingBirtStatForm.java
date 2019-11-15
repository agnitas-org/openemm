/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang.ArrayUtils;

public class MailingBirtStatForm extends PaginationForm {

    private int action = -1;

    private int mailingID;

    private int previousMailingID = -1;

    private String[] availableTargets;

    private String shortname;

    private String searchQueryText;

    private boolean searchNameChecked;

    private boolean searchDescriptionChecked;

    private String[] additionalFields = ArrayUtils.EMPTY_STRING_ARRAY;

    private int[] filteredMailingLists = ArrayUtils.EMPTY_INT_ARRAY;

    private int[] filteredTargetGroups = ArrayUtils.EMPTY_INT_ARRAY;

    public boolean isSearchNameChecked() {
        return searchNameChecked;
    }

    public void setSearchNameChecked(boolean searchNameChecked) {
        this.searchNameChecked = searchNameChecked;
    }

    public boolean isSearchDescriptionChecked() {
        return searchDescriptionChecked;
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

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public int getPreviousMailingID() {
        return previousMailingID;
    }

    public void setPreviousMailingID(int previousMailingID) {
        this.previousMailingID = previousMailingID;
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

        return new HashSet<>(Arrays.asList(additionalFields));
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
}
