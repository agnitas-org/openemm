/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table.decorator.impl;

import java.util.Collections;
import java.util.Map;

import com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm;
import com.agnitas.messages.I18nString;
import com.agnitas.taglib.table.decorator.TableDecorator;

public class TrackableLinkListDecorator extends TableDecorator {

    @Override
    public String getRowCssClass(Object element) {
        return ((TrackableLinkForm) element).isDeleted() ? "disabled" : "";
    }

    @Override
    public Map<String, String> getRowAttributes(Object element) {
        if (!((TrackableLinkForm) element).isDeleted()) {
            return Collections.emptyMap();
        }

        return Map.of("data-tooltip", I18nString.getLocaleString("target.Deleted", locale));
    }
}
