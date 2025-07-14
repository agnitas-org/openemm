/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.form;

import com.agnitas.emm.core.dashboard.enums.DashboardMode;
import com.agnitas.web.forms.PaginationForm;

public class DashboardForm extends PaginationForm {
    private static final int ROWNUMS = 20;
    
    public DashboardForm() {
        setNumberOfRows(ROWNUMS);
    }
    
    private int lastSentMailingId; //TODO check usage and remove after EMMGUI-953 has been successfully tested
    private DashboardMode mode;

    public int getLastSentMailingId() {
        return lastSentMailingId;
    }

    public void setLastSentMailingId(int lastSentMailingId) {
        this.lastSentMailingId = lastSentMailingId;
    }

    public DashboardMode getMode() {
        return mode;
    }

    public void setMode(DashboardMode mode) {
        this.mode = mode;
    }
}
