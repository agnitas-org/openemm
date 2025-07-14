/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table.decorator;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public abstract class TableDecorator {

    protected Locale locale;

    public void init(Locale locale) {
        this.locale = locale;
    }

    public String getRowCssClass(Object element) {
        return "";
    }

    public Map<String, String> getRowAttributes(Object element) {
        return Collections.emptyMap();
    }
}
