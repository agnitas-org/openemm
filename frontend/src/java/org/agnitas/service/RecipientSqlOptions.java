/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.Objects;

import org.agnitas.web.RecipientForm;

public class RecipientSqlOptions implements RecipientOptions {
    
    private boolean checkParenthesisBalance;
    private String sort;
    private String dir;
    private int listId;
    private int targetId;
    private int accessLimitTargetId;
    private String targetEQL;
    private String queryBuilderRules;
    private String userType;
    private String searchFirstName;
    private String searchLastName;
    private String searchEmail;
    private int userStatus;
    private boolean useAdvancedSearch;
    private RecipientForm form;

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
        return accessLimitTargetId;
    }

    public String getTargetEQL() {
        return targetEQL;
    }

    public String getQueryBuilderRules() {
        return queryBuilderRules;
    }

    @Override
	public String getUserType() {
        return userType;
    }
    
    public String getSearchFirstName() {
        return searchFirstName;
    }
    
    public String getSearchLastName() {
        return searchLastName;
    }
    
    public String getSearchEmail() {
        return searchEmail;
    }
    
    @Override
	public int getUserStatus() {
        return userStatus;
    }
    
    public boolean isUseAdvancedSearch() {
        return useAdvancedSearch;
    }
    
    public RecipientForm getForm() {
        return form;
    }
    
    @Override
	public boolean isUserTypeEmpty() {
        return false;
    }
    
    public static class Builder {
        private RecipientSqlOptions options = new RecipientSqlOptions();
    
        public RecipientSqlOptions build() {
            RecipientSqlOptions result = options;
            options = null;
            return result;
        }
    
        public Builder setUseAdvancedSearch(boolean userAdvancedSearch, RecipientForm form) {
            if (userAdvancedSearch) {
                options.form = Objects.requireNonNull(form);
            }
            options.useAdvancedSearch = userAdvancedSearch;
            
            return this;
        }
    
        public Builder setListId(int listId) {
            options.listId = listId;
            return this;
        }
    
        public Builder setTargetId(int targetId) {
            options.targetId = targetId;
            return this;
        }
    
        public Builder setSearchFirstName(String searchFirstName) {
            options.searchFirstName = searchFirstName;
            return this;
        }
    
        public Builder setSearchLastName(String searchLastName) {
            options.searchLastName = searchLastName;
            return this;
        }
    
        public Builder setSearchEmail(String searchEmail) {
            options.searchEmail = searchEmail;
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
    
        public Builder setUserType(String userType) {
            options.userType = userType;
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

        public Builder setLimitAccessTargetId(int accessLimitTargetId) {
            options.accessLimitTargetId = accessLimitTargetId;
            return this;
        }

        public Builder setTargetEQL(String targetEQL) {
            options.targetEQL = targetEQL;
            return this;
        }

        public Builder setQueryBuilderRules(String queryBuilderRules) {
            options.queryBuilderRules = queryBuilderRules;
            return this;
        }
    }
}
