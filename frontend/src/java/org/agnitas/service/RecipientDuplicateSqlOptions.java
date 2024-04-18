/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class RecipientDuplicateSqlOptions implements RecipientOptions {

    private boolean checkParenthesisBalance;
    private String sort;
    private String dir;
    private int listId;
    private int targetId;
    private int limitAccessTargetId;
    private List<String> userTypes;
    private int userStatus;
    private boolean userTypeEmpty;
    private boolean singleMode;
    private String searchFieldName;
    private int recipientId;
    private boolean caseSensitive;
    private Set<Integer> altgIds;

    public static Builder builder() {
        return new Builder();
    }

    public boolean isCheckParenthesisBalance() {
        return checkParenthesisBalance;
    }

    public String getSort() {
        return sort;
    }

    public String getDir() {
        return dir;
    }

    @Override
    public int getListId() {
        return listId;
    }

    @Override
	public int getTargetId() {
        return targetId;
    }

    @Override
    public int getAltgId() {
        return limitAccessTargetId;
    }
    
    @Override
    public Set<Integer> getAltgIds() {
        return altgIds;
    }

    @Override
	public List<String> getUserTypes() {
        return userTypes;
    }

    public String getSearchFieldName() {
        return searchFieldName;
    }

    @Override
	public int getUserStatus() {
        return userStatus;
    }

    @Override
    public boolean isUserTypeEmpty() {
        return userTypeEmpty;
    }

    public boolean isSingleMode() {
        return singleMode;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public static class Builder {
        private RecipientDuplicateSqlOptions options = new RecipientDuplicateSqlOptions();

        public RecipientDuplicateSqlOptions build() {
            if (StringUtils.isBlank(options.searchFieldName)) {
                options.searchFieldName = "email";
            }

            options.singleMode = options.recipientId > 0;
            RecipientDuplicateSqlOptions result = options;
            options = null;
            return result;
        }

        public Builder setListId(int listId) {
            options.listId = listId;
            return this;
        }

        public Builder setTargetId(int targetId) {
            options.targetId = targetId;
            return this;
        }

        public Builder setLimitAccessTargetId(int altgId) {
            options.limitAccessTargetId = altgId;
            return this;
        }
        
        public Builder setAltgIds(Set<Integer> altgIds) {
            options.altgIds = altgIds;
            return this;
        }

        public Builder setSort(String sort) {
            options.sort = sort;
            return this;
        }

        public Builder setDirection(String direction) {
            options.dir = direction;
            return this;
        }

        public Builder setUserTypes(List<String> userTypes) {
            options.userTypes = userTypes;
            return this;
        }

        public Builder setUserStatus(int userStatus) {
            options.userStatus = userStatus;
            return this;
        }

        public Builder setCheckParenthesisBalance(boolean checkParenthesisBalance) {
            options.checkParenthesisBalance = checkParenthesisBalance;
            return this;
        }

        public Builder setUserTypeEmpty(boolean userTypeEmpty) {
            options.userTypeEmpty = userTypeEmpty;
            return this;
        }

        public Builder setRecipientId(int recipientId) {
            options.recipientId = recipientId;
            return this;
        }

        public Builder setSearchFieldName(String searchField) {
            options.searchFieldName = searchField;
            return this;
        }

        public Builder setCaseSensitive(boolean caseSensitive) {
            options.caseSensitive = caseSensitive;
            return this;
        }
    }
}
