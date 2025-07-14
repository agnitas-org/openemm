/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.autoexport.beans;

public enum AutoExportJobStatus {
    QUEUED("Queued", 1),
    RUNNING("Running", 2),
    TRANSFERRING("Transferring", 3),
    DONE("Done", 4),
    FAILED("Failed", 5);

    private String description;
    private int code;

    AutoExportJobStatus(String description, final int code) {
        this.description = description;
        this.code = code;
    }
    
    public static AutoExportJobStatus fromCode(final int code) {
    	for(final AutoExportJobStatus st : values()) {
    		if(st.code == code) {
    			return st;
    		}
    	}
    	
    	return null;
    }

    public int getIntValue() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

}
