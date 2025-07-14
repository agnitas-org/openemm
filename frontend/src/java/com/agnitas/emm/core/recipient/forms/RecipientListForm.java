/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.forms;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecipientListForm extends RecipientListBaseForm {
    private static final String DEFAULT_FIELD = "email";

    private int filterMailinglistId;
    private int filterTargetId;
    private int filterAltgId;
    private int filterUserStatus;
    private List<String> filterUserTypes;
    private String searchFirstName;
    private Integer filterGender;
    private String searchLastName;
    private String searchEmail;
    private String searchQueryBuilderRules = "[]";
    private String eql;

    public RecipientListForm(Integer filterUserStatus) {
        this.filterUserStatus = filterUserStatus == null ? 0 : filterUserStatus;
    }

    public int getFilterMailinglistId() {
        return filterMailinglistId;
    }

    public void setFilterMailinglistId(int filterMailinglistId) {
        this.filterMailinglistId = filterMailinglistId;
    }

    public int getFilterTargetId() {
        return filterTargetId;
    }

    public void setFilterTargetId(int filterTargetId) {
        this.filterTargetId = filterTargetId;
    }

    public int getFilterAltgId() {
        return filterAltgId;
    }

    public void setFilterAltgId(int filterAltgId) {
        this.filterAltgId = filterAltgId;
    }

    public int getFilterUserStatus() {
        return filterUserStatus;
    }

    public void setFilterUserStatus(int filterUserStatus) {
        this.filterUserStatus = filterUserStatus;
    }

    public List<String> getFilterUserTypes() {
        return filterUserTypes;
    }

    public void setFilterUserTypes(List<String> filterUserTypes) {
        this.filterUserTypes = filterUserTypes;
    }

    public String getSearchFirstName() {
        return searchFirstName;
    }

    public void setSearchFirstName(String searchFirstName) {
        this.searchFirstName = searchFirstName;
    }

    public Integer getFilterGender() {
        return filterGender;
    }

    public void setFilterGender(Integer filterGender) {
        this.filterGender = filterGender;
    }

    public String getSearchLastName() {
        return searchLastName;
    }

    public void setSearchLastName(String searchLastName) {
        this.searchLastName = searchLastName;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public String getSearchQueryBuilderRules() {
        return searchQueryBuilderRules;
    }

    public void setSearchQueryBuilderRules(String searchQueryBuilderRules) {
        this.searchQueryBuilderRules = searchQueryBuilderRules;
    }

    public String getEql() {
        return eql;
    }

    public void setEql(String eql) {
        this.eql = eql;
    }

    @Override
    public boolean isDefaultColumn(String column) {
        return StringUtils.equalsIgnoreCase(DEFAULT_FIELD, column);
    }

    @Override
    public boolean isSelectedColumn(String column) {
        return selectedFields.contains(column);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("filterMailinglistId", filterMailinglistId);
        map.put("filterTargetId", filterTargetId);
        map.put("filterAltgId", filterAltgId);
        map.put("filterUserStatus", filterUserStatus);
        map.put("filterUserTypes", filterUserTypes);
        map.put("searchFirstName", searchFirstName);
        map.put("searchGender", filterGender);
        map.put("searchLastName", searchLastName);
        map.put("searchEmail", searchEmail);
        map.put("searchQueryBuilderRules", searchQueryBuilderRules);
        map.put("selectedFields", selectedFields);

        return map;
    }

    @Override
    public Object[] toArray() {
        // Each filter's name and value + all pagination parameters.
        List<Object> objects = new ArrayList<>(selectedFields.size() + 13);

        objects.add(getSort());
        objects.add(getOrder());
        objects.add(getPage());
        objects.add(getNumberOfRows());
        objects.add(filterMailinglistId);
        objects.add(filterTargetId);
        objects.add(filterUserStatus);
        objects.add(filterUserTypes);
        objects.add(searchFirstName);
        objects.add(filterGender);
        objects.add(searchLastName);
        objects.add(searchEmail);
        objects.add(searchQueryBuilderRules);
        objects.addAll(Collections.singletonList(selectedFields));

        return objects.toArray();
    }
}
