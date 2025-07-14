/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.beans.Mailing;

public class ImportResult {
	public static Builder builder() {return new Builder();}
	
	private int mailingID;
	private boolean isTemplate;
	private int gridTemplateID;
	private boolean isSuccess = false;
    private boolean needsAltg;
    private Mailing importedMailing;
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

    public Mailing getImportedMailing() {
        return importedMailing;
    }

    public boolean isNeedsAltg() {
        return needsAltg;
    }

    public void setNeedsAltg(boolean needsAltg) {
        this.needsAltg = needsAltg;
    }

	public static class Builder {
		private ImportResult options = new ImportResult();

		private Builder() {}
		
		public ImportResult.Builder setSuccess(boolean isSuccess) {
			options.isSuccess = isSuccess;
			return this;
		}
        
        public ImportResult.Builder addWarnings(Map<String, Object[]> warningsToAdd) {
        	options.warnings.putAll(warningsToAdd);
            return this;
        }
		
		public ImportResult.Builder addWarning(String warningKey) {
			options.warnings.put(warningKey, null);
			return this;
		}
		
		public ImportResult.Builder addWarning(String warningKey, Object... values) {
			options.warnings.put(warningKey, values);
			return this;
		}
        
        public ImportResult.Builder addErrors(Map<String, Object[]> errorsToAdd) {
        	options.errors.putAll(errorsToAdd);
            return this;
        }
		
		public ImportResult.Builder addError(String errorKey) {
			options.errors.put(errorKey, null);
			return this;
		}
		
		public ImportResult.Builder addError(String errorKey, Object... values) {
			options.errors.put(errorKey, values);
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
		
		public ImportResult.Builder setNeedsAltg(boolean needsAltg) {
			options.needsAltg = needsAltg;
			return this;
		}

        public ImportResult.Builder setImportedMailing(Mailing importedMailing) {
            options.importedMailing = importedMailing;
            return this;
        }

		public ImportResult build() {
			ImportResult result = options;
			
			options = null;
			
			return result;
		}
	}
}
