/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean.impl;

import java.util.List;

import com.agnitas.emm.core.report.generator.bean.TableDefinition;

public class TableDefinitionImpl implements TableDefinition {

    private String titleTranslationKey;

    private String defaultTitle;

    private List<String> order;

    public TableDefinitionImpl() {
    }

    public TableDefinitionImpl(String titleTranslationKey, String defaultTitle, List<String> order) {
        this.titleTranslationKey = titleTranslationKey;
        this.defaultTitle = defaultTitle;
        this.order = order;
    }

    @Override
    public String getTitleTranslationKey() {
        return titleTranslationKey;
    }

    @Override
    public void setTitleTranslationKey(String titleTranslationKey) {
        this.titleTranslationKey = titleTranslationKey;
    }

    @Override
    public String getDefaultTitle() {
        return defaultTitle;
    }

    @Override
    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    @Override
    public List<String> getOrder() {
        return order;
    }

    @Override
    public void setOrder(List<String> order) {
        this.order = order;
    }
}
