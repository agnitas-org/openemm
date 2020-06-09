/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractBaseRowMapper<T> implements RowMapper<T> {

    private final String columnPrefix;

    public AbstractBaseRowMapper() {
        this.columnPrefix = StringUtils.EMPTY;
    }

    public AbstractBaseRowMapper(final String columnPrefix) {
        this.columnPrefix = StringUtils.defaultString(columnPrefix, StringUtils.EMPTY);
    }

    public String getColumnPrefix() {
        return columnPrefix;
    }

    protected <R> R getValue(final String fieldName, final ResultSetFunction<R> function) throws SQLException {
        return function.apply(columnPrefix + fieldName);
    }

    protected Set<String> getColumnNamesLowerCase(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnsCount = metaData.getColumnCount();
        final Set<String> resultColumnNames = new HashSet<>();
        for (int i = 1; i <= columnsCount; ++i) {
            resultColumnNames.add(metaData.getColumnName(i).toLowerCase());
        }
        return resultColumnNames;
    }

    @FunctionalInterface
    protected interface ResultSetFunction<R> {
        R apply(String t) throws SQLException;
    }
}
