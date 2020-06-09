/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.service;

import java.util.HashMap;
import java.util.Map;

public class ImportResult {
	public static Builder builder() {return new Builder();}
	
	private int mailingID;
	private boolean isTemplate;
	private int gridTemplateID;
	private boolean isSuccess = false;
	private Map<String, Object[]> warnings = new HashMap<>();
	private Map<String, Object[]> errors = new HashMap<>();

	protected ImportResult() {}

	public int getMailingID() {
		return mailingID;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public Map<String, Object[]> getWarnings() {
		return warnings;
	}
	
	public Map<String, Object[]> getErrors() {
		return errors;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public int getGridTemplateID() {
		return gridTemplateID;
	}
	
	public static class Builder {
		private ImportResult options = new ImportResult();
		private Map<String, Object[]> errors = new HashMap<>();
		private Map<String, Object[]> warnings = new HashMap<>();

		private Builder() {}
		
		public ImportResult.Builder setSuccess(boolean isSuccess) {
			options.isSuccess = isSuccess;
			return this;
		}
        
        public ImportResult.Builder addWarnings(Map<String, Object[]> warningsToAdd) {
        	this.warnings.putAll(warningsToAdd);
            return this;
        }
		
		public ImportResult.Builder addWarning(String warningKey) {
			this.warnings.put(warningKey, null);
			return this;
		}
		
		public ImportResult.Builder addWarning(String warningKey, Object... values) {
			this.warnings.put(warningKey, values);
			return this;
		}
        
        public ImportResult.Builder addErrors(Map<String, Object[]> errorsToAdd) {
        	this.errors.putAll(errorsToAdd);
            return this;
        }
		
		public ImportResult.Builder addError(String errorKey) {
			this.errors.put(errorKey, null);
			return this;
		}
		
		public ImportResult.Builder addError(String errorKey, Object... values) {
			this.errors.put(errorKey, values);
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
			result.warnings = warnings;
			result.errors = errors;
			warnings = null;
			errors = null;
			options = null;
			return result;
		}
	}
}
