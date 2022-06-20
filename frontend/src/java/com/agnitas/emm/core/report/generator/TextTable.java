/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TextTable {

    /**
     * Represents the order of columns.
     * Contains enumeration of column keys, method names or field names.
     *
     * @return array of column's order keys.
     */
    String[] order() default {};


    /**
     * Key for translation column name.
     *
     * In case of it wasn't set up table title will be either {@link TextTable#defaultTitle()}
     * or without title.
     *
     * @return key for translation of title.
     */
    String translationKey() default StringUtils.EMPTY;

    /**
     * Title of table.
     *
     * In case of it wasn't set up table title will be either the translated key
     * or will be without any title at all.
     *
     * @return default title of table.
     */
    String defaultTitle() default StringUtils.EMPTY;
}
