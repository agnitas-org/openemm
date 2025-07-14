/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import com.agnitas.beans.ColumnMapping;
import org.apache.commons.lang3.StringUtils;

public class ColumnMappingImpl implements ColumnMapping {
	protected int id;

    protected int profileId;

    protected String fileColumn;

    protected String databaseColumn;

    protected boolean mandatory = false;

    protected boolean encrypted = false;

    protected String defaultValue;
    
    protected String format;
    
    private boolean keyColumn = false;

    @Override
	public int getId() {
        return id;
    }

    @Override
	public void setId(int id) {
        this.id = id;
    }

    @Override
	public int getProfileId() {
        return profileId;
    }

    @Override
	public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @Override
	public String getFileColumn() {
        return fileColumn;
    }

    @Override
	public void setFileColumn(String fileColumn) {
        this.fileColumn = fileColumn;
    }

    @Override
	public String getDatabaseColumn() {
        return databaseColumn;
    }

    @Override
	public void setDatabaseColumn(String databaseColumn) {
        this.databaseColumn = databaseColumn;
    }

    @Override
	public boolean isMandatory() {
        return mandatory;
    }

    @Override
	public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
	public boolean isEncrypted() {
        return encrypted;
    }

    @Override
	public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    @Override
	public String getDefaultValue() {
        return defaultValue;
    }

    @Override
	public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }    

    @Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public void setKeyColumn(boolean keyColumn) {
		this.keyColumn = keyColumn;
	}

	@Override
	public boolean isKeyColumn() {
		return keyColumn;
	}

	@Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	
    	if (fileColumn != null) {
    		result.append("file = \"").append(fileColumn).append("\"");
    	} else {
    		result.append("file = null");
    	}
    	
    	if (databaseColumn != null) {
    		result.append(", database = \"").append(databaseColumn).append("\"");
    	} else {
    		result.append(", database = null");
    	}
    	
    	if (keyColumn) {
    		result.append(", keycolumn");
    	}
    	
    	if (StringUtils.isNotEmpty(defaultValue)) {
    		result.append(", default = \"").append(defaultValue).append("\"");
    	}
    	
    	if (StringUtils.isNotEmpty(format)) {
    		result.append(", format = \"").append(format).append("\"");
    	}
    	
    	if (mandatory) {
    		result.append(", mandatory = ").append(mandatory);
    	}
    	
    	if (encrypted) {
    		result.append(", encrypted = ").append(encrypted);
    	}
    	
		return result.toString();
    }
}
