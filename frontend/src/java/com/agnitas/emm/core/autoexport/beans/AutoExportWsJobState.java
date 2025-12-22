/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.autoexport.beans;

import java.util.Objects;

public class AutoExportWsJobState {
    private AutoExportJobStatus status;
    private String filename;

    public AutoExportWsJobState(AutoExportJobStatus status, final String filename) {
        this.status = Objects.requireNonNull(status, "status == null");

        this.filename = filename;
    }

    public AutoExportWsJobState(AutoExportJobStatus status) {
        this(status, null);
    }

    public AutoExportJobStatus getStatus() {
        return status;
    }

    public final String getFilename() {
    	return this.filename;
    }

}
