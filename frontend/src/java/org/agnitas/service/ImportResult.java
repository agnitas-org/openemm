/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ImportResult {
    public static Builder builder() {return new Builder();}
    
    private int mailingID;
    private boolean isTemplate;
    private int gridTemplateID;
    private boolean isSuccess = false;
    private Set<String> warningKeys = new HashSet<>();
    private Set<String> errorKeys = new HashSet<>();

    protected ImportResult() {}

    public int getMailingID() {
        return mailingID;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public Set<String> getWarningKeys() {
        return warningKeys;
    }
    
    public Set<String> getErrorKeys() {
        return errorKeys;
    }
    
    public boolean isSuccess() {
        return isSuccess;
    }
    
    public int getGridTemplateID() {
        return gridTemplateID;
    }
    
    public static class Builder {
        private ImportResult options = new ImportResult();
        private Set<String> errorKeys = new HashSet<>();
        private Set<String> warningKeys = new HashSet<>();

        private Builder() {}
        
        public ImportResult.Builder setSuccess(boolean isSuccess) {
            options.isSuccess = isSuccess;
            return this;
        }
        
        public ImportResult.Builder addWarningKeys(String... warningKeys) {
            this.warningKeys.addAll(Arrays.asList(warningKeys));
            return this;
        }
        
        public ImportResult.Builder addWarningKeys(Set<String> warningKeys) {
            this.warningKeys.addAll(warningKeys);
            return this;
        }
        
        public ImportResult.Builder setErrorKeys(String... errorKeys) {
            this.errorKeys.addAll(Arrays.asList(errorKeys));
            return this;
        }
        
        public ImportResult.Builder setErrorKeys(Set<String> errorKeys) {
            this.errorKeys.addAll(errorKeys);
            return this;
        }

        public ImportResult.Builder setMailingID(int mailingID) {
            options.mailingID = mailingID;
            return this;
        }
        
        public ImportResult.Builder setGridTemplateID(int gridTemplateID) {
            options.gridTemplateID = gridTemplateID;
            return this;
        }
        
        public ImportResult.Builder setIsTemplate(boolean isTemplate) {
            options.isTemplate = isTemplate;
            return this;
        }

        public ImportResult build() {
            ImportResult result = options;
            result.warningKeys = warningKeys;
            result.errorKeys = errorKeys;
            warningKeys = null;
            errorKeys = null;
            options = null;
            return result;
        }
    }
}
