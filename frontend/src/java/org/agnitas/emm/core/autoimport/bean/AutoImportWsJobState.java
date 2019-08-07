/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.bean;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.agnitas.emm.core.autoimport.service.AutoImportJobStatus;

public class AutoImportWsJobState {
    private AutoImportJobStatus status;
    private Map<String, String> statusMap;

    public AutoImportWsJobState(AutoImportJobStatus status, Map<String, String> statusMap) {
        this.status = Objects.requireNonNull(status, "status == null");

        if (statusMap == null) {
            this.statusMap = Collections.emptyMap();
        } else {
            this.statusMap = Collections.unmodifiableMap(statusMap);
        }
    }

    public AutoImportWsJobState(AutoImportJobStatus status) {
        this(status, null);
    }

    public AutoImportJobStatus getStatus() {
        return status;
    }

    public Map<String, String> getStatusMap() {
        return statusMap;
    }
}
