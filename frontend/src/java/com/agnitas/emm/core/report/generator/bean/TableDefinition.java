/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean;

import java.util.List;

import com.agnitas.emm.core.report.generator.TextTable;

public interface TableDefinition {

    String getTitleTranslationKey();

    void setTitleTranslationKey(String titleTranslationKey);

    String getDefaultTitle();

    void setDefaultTitle(String defaultTitle);

    /**
     * Represent value specified in {@link TextTable#order()}
     * Also maybe set of fields/methods names if {@link TextTable#order()} wasn't setted up.
     *
     * @return set of unique keys of columns in necessary order.
     */
    List<String> getOrder();

    void setOrder(List<String> order);
}
