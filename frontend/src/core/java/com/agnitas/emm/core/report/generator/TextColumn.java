/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

// todo: implement validator
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TextColumn {

    /**
     * Represents how many characters will be filled by the column in text editor.
     *
     * In case of width wasn't set up the width of column will be max length of content.
     *
     * @return count of characters.
     */
    int width() default 0;

    /**
     * Key for translation column name.
     *
     * In case of it wasn't set up column name will be either {@link TextColumn#defaultValue()}
     * or the name of mapped method or the name of mapped field.
     *
     * @return key for translation of the column name.
     */
    String translationKey() default StringUtils.EMPTY;


    /**
     * Name of column.
     *
     * In case of it wasn't set up column name will be either the name of mapped method
     * or the name of mapped field.
     *
     * @return column name.
     */
    String defaultValue() default StringUtils.EMPTY;

    /**
     * Key for making order of columns or for displaying columns with the same name of method/field
     * from different implementation of one interface.
     * Also it allows to display different fields/methods as one column.
     *
     * In case of it wasn't set up key will be either the name of mapped method
     * or the name of mapped field.
     *
     * @return key of column.
     */
    String key() default StringUtils.EMPTY;
}
