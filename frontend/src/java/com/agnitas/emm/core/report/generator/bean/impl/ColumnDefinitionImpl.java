/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean.impl;

import java.util.Objects;

import com.agnitas.emm.core.report.generator.bean.ColumnDefinition;

public class ColumnDefinitionImpl implements ColumnDefinition {

    private String value;

    private String translationKey;

    private int width;

    public ColumnDefinitionImpl() {
    }

    public ColumnDefinitionImpl(String value, String translationKey, int width) {
        this.value = value;
        this.translationKey = translationKey;
        this.width = width;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnDefinitionImpl)) return false;
        ColumnDefinitionImpl that = (ColumnDefinitionImpl) o;
        return getWidth() == that.getWidth() &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getTranslationKey(), that.getTranslationKey());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getValue(), getTranslationKey(), getWidth());
    }
}
