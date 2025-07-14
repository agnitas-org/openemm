/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.form;

import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.util.DbColumnType;
import com.agnitas.web.forms.FormSearchParams;

public class ProfileFieldFormSearchParams implements FormSearchParams<ProfileFieldForm> {

    private String fieldName;
    private String dbFieldName;
    private String description;
    private DbColumnType.SimpleDataType type;
    private ProfileFieldMode mode;
    private Boolean historized;

    @Override
    public void storeParams(ProfileFieldForm form) {
        this.fieldName = form.getFilterFieldName();
        this.dbFieldName = form.getFilterDbFieldName();
        this.type = form.getFilterType();
        this.mode = form.getFilterMode();
        this.description = form.getFilterDescription();
        this.historized = form.getHistorized();
    }

    @Override
    public void restoreParams(ProfileFieldForm form) {
        form.setFilterFieldName(this.fieldName);
        form.setFilterDbFieldName(this.dbFieldName);
        form.setFilterType(this.type);
        form.setFilterMode(this.mode);
        form.setFilterDescription(this.description);
        form.setHistorized(this.historized);
    }

    @Override
    public void resetParams() {
        fieldName = null;
        dbFieldName = null;
        description = null;
        type = null;
        mode = null;
        historized = null;
    }
}
