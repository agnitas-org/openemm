/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.service;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.messages.Message;

public class FormImportResult {
	public static Builder builder() {return new Builder();}
	
	private int userFormID;
	private String userFormName;
	private boolean isSuccess = false;
	private Map<String, Object[]> warnings = new HashMap<>();
	private Map<String, Object[]> errors = new HashMap<>();

	protected FormImportResult() {}
	
	public int getUserFormID() {
		return userFormID;
	}

	public String getUserFormName() {
		return userFormName;
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
	
    public static FormImportResult error(String errorMsgCode) {
        return FormImportResult.error(Message.of(errorMsgCode));
    }
    
    public static FormImportResult error(Message error) {
        Builder builder = FormImportResult.builder();
        builder.addErrors(Map.of(error.getCode(), error.getArguments()));
        builder.setSuccess(false);
        return builder.build();
    }
    
	public static class Builder {
		private FormImportResult options = new FormImportResult();
		private Map<String, Object[]> errors = new HashMap<>();
		private Map<String, Object[]> warnings = new HashMap<>();

		private Builder() {}
		
		public Builder setUserFormID(int userFormID) {
			options.userFormID = userFormID;
			return this;
		}

		public Builder setUserFormName(String userFormName) {
			options.userFormName = userFormName;
			return this;
		}

		public Builder setSuccess(boolean isSuccess) {
			options.isSuccess = isSuccess;
			return this;
		}
        
        public Builder addWarnings(Map<String, Object[]> warningsToAdd) {
        	this.warnings.putAll(warningsToAdd);
            return this;
        }
		
		public Builder addWarning(String warningKey) {
			this.warnings.put(warningKey, null);
			return this;
		}
		
		public Builder addWarning(String warningKey, Object... values) {
			this.warnings.put(warningKey, values);
			return this;
		}
        
        public Builder addErrors(Map<String, Object[]> errorsToAdd) {
        	this.errors.putAll(errorsToAdd);
            return this;
        }
		
		public Builder addError(String errorKey) {
			this.errors.put(errorKey, null);
			return this;
		}
		
		public Builder addError(String errorKey, Object... values) {
			this.errors.put(errorKey, values);
			return this;
		}
		
		
		public FormImportResult build() {
			FormImportResult result = options;
			result.warnings = warnings;
			result.errors = errors;
			warnings = null;
			errors = null;
			options = null;
			return result;
		}
	}
}
