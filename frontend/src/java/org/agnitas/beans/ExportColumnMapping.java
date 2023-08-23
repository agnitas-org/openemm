/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Objects;

public class ExportColumnMapping {

	private int id;
	private String dbColumn;
	private String fileColumn;
	private String defaultValue;
	private boolean encrypted;
	
	public ExportColumnMapping() {
	}
	
    public ExportColumnMapping(String dbColumn) {
        this.dbColumn = dbColumn;
    }
	
	public ExportColumnMapping(String dbColumn, String fileColumn, String defaultValue, boolean encrypted) {
		this.dbColumn = dbColumn;
		this.fileColumn = fileColumn;
		this.defaultValue = defaultValue;
		this.encrypted = encrypted;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDbColumn() {
		return dbColumn;
	}
	
	public void setDbColumn(String dbColumn) {
		this.dbColumn = dbColumn;
	}
	
	public String getFileColumn() {
		return fileColumn;
	}
	
	public void setFileColumn(String fileColumn) {
		this.fileColumn = fileColumn;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}
	
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

    @Override
    public int hashCode() {
        return Objects.hash(id, dbColumn, fileColumn, defaultValue, encrypted);
    }
}
