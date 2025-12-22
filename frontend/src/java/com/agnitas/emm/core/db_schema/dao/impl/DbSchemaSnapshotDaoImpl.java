/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.dao.impl;

import java.util.List;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.db_schema.bean.DbColumnInfo;
import com.agnitas.emm.core.db_schema.dao.DbSchemaSnapshotDao;
import com.agnitas.util.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component("DbSchemaSnapshotDao")
public class DbSchemaSnapshotDaoImpl extends BaseDaoImpl implements DbSchemaSnapshotDao {

    private static final RowMapper<DbColumnInfo> DB_COLUMN_ROW_MAPPER = (rs, rowNum) -> {
        String length = rs.getString("length");
        return new DbColumnInfo(
                rs.getString("column_name"),
                rs.getString("data_type"),
                StringUtils.isBlank(length) ? null : Long.parseLong(length)
        );
    };

    public DbSchemaSnapshotDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    @Override
    public String getVendorName() {
        if (isPostgreSQL()) {
            return "postgresql";
        }

        return isOracleDB() ? "oracle" : "mariadb";
    }

    @Override
    public List<String> getTableNames() {
        String query;
        if (isOracleDB()) {
            query = """
                    SELECT table_name FROM all_tables
                    WHERE owner = (SELECT USER FROM dual) AND table_name NOT LIKE 'DR$%'
                    """;
        } else if (isPostgreSQL()) {
            query = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = CURRENT_SCHEMA()";
        } else {
            query = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = (SELECT SCHEMA())";
        }

        return select(query, StringRowMapper.INSTANCE);
    }

    @Override
    public List<DbColumnInfo> getTableColumns(String tableName) {
        String query;
        if (isOracleDB()) {
            query = """
                    SELECT column_name, data_type, data_length AS length
                    FROM all_tab_columns
                    WHERE owner = (SELECT USER FROM dual) AND table_name = UPPER(?)
                    """;
        } else if (isPostgreSQL()) {
            query = """
                    SELECT column_name, data_type, character_maximum_length AS length
                    FROM information_schema.COLUMNS
                    WHERE table_schema = CURRENT_SCHEMA() AND table_name = ?
                    """;
        } else {
            query = """
                    SELECT column_name, data_type, character_maximum_length AS length
                    FROM information_schema.COLUMNS
                    WHERE table_schema = (SELECT SCHEMA()) AND table_name = ?
                    """;
        }

        return select(query, DB_COLUMN_ROW_MAPPER, tableName);
    }

    @Override
    public boolean exists() {
        return selectInt("SELECT COUNT(*) FROM db_schema_snapshot_tbl") > 0;
    }

    @Override
    public boolean exists(String versionNumber) {
        return selectInt("SELECT COUNT(*) FROM db_schema_snapshot_tbl WHERE version_number = ?", versionNumber) > 0;
    }

    @Override
    public void save(String versionNumber, String schemaJson) {
        if (exists(versionNumber)) {
            update("UPDATE db_schema_snapshot_tbl SET schema_json = ? WHERE version_number = ?", schemaJson, versionNumber);
        } else {
            update("INSERT INTO db_schema_snapshot_tbl (version_number, schema_json) VALUES (?, ?)", versionNumber, schemaJson);
        }
    }

    @Override
    public Tuple<String, String> read() {
        return selectObjectDefaultNull(
                addRowLimit("SELECT version_number, schema_json FROM db_schema_snapshot_tbl ORDER BY version_number DESC", 1),
                (rs, i) -> Tuple.of(rs.getString("version_number"), rs.getString("schema_json"))
        );
    }
}
