/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.agnitas.dao.ImportLoggerDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ImportLoggerDaoImpl extends BaseDaoImpl implements ImportLoggerDao {
	@Override
	@DaoUpdateReturnValueCheck
    public void log(@VelocityCheck final int companyId, final int adminId, final int datasource_id, final int importedLines, final String statistics, final String profile) {
        if (companyId <= 0) {
        	return;
        }
		String sql = null;
        if (isOracleDB()) {
            sql = "INSERT INTO import_log_tbl " +
                    "(log_id, company_id, admin_id, datasource_id, imported_lines, statistics, profile) " +
                    "VALUES(import_log_tbl_seq.nextval,?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO import_log_tbl " +
                    "(company_id, admin_id, datasource_id, imported_lines, statistics, profile) " +
                    "VALUES(?, ?, ?, ?, ?, ?)";
        }
        final JdbcTemplate template = new JdbcTemplate(getDataSource());
        final String updateSql = sql;
        template.update(new PreparedStatementCreator() {
            @Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                final PreparedStatement statement = connection.prepareStatement(updateSql);
                statement.setInt(1, companyId);
                statement.setInt(2, adminId);
                statement.setInt(3, datasource_id);
                statement.setInt(4, importedLines);
                statement.setString(5, statistics);
                statement.setString(6, profile);
                return statement;
            }
        });
    }
}
