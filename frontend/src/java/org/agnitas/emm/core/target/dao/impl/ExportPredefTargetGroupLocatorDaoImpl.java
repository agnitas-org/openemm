/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.dao.impl;

import javax.sql.DataSource;

import org.agnitas.emm.core.target.dao.ExportPredefTargetGroupLocatorDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Implementation of {@link ExportPredefTargetGroupLocatorDao}.
 */
public class ExportPredefTargetGroupLocatorDaoImpl implements ExportPredefTargetGroupLocatorDao {

	/**
	 * JDBC data source.
	 */
	private DataSource dataSource;
	
	@Override
	public boolean hasExportProfilesForTargetGroup(int targetGroupID, @VelocityCheck int companyID) {
		JdbcTemplate template = new JdbcTemplate(this.dataSource);
		
		int count = template.queryForObject("SELECT count(*) FROM export_predef_tbl WHERE company_id=? AND target_id=? AND deleted=0", Integer.class, companyID, targetGroupID);
		
		return count > 0;
	}

	// --------------------------------------------------- Dependency Injection
	/**
	 * Set JDBC data source.
	 * 
	 * @param dataSource JDBC data source
	 */
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
