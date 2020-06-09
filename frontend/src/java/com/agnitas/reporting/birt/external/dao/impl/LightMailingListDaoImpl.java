/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.dao.LightMailingListDao;

public class LightMailingListDaoImpl extends BaseDaoImpl implements LightMailingListDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LightMailingListDaoImpl.class);

	public LightMailingListDaoImpl(DataSource dataSource) {
		setDataSource(dataSource);
	}

	@Override
	public LightMailingList getMailingList(int mailingListID, @VelocityCheck int companyID) {
		return selectObjectDefaultNull(logger, "SELECT mailinglist_id, shortname, company_id FROM mailinglist_tbl WHERE mailinglist_id = ? AND company_id = ?", new LightMailingListRowMapper(), mailingListID, companyID);
	}

	@Override
	public List<LightMailingList> getMailingLists(List<Integer> mailingListIDs, @VelocityCheck int companyID) {
		if (mailingListIDs == null || mailingListIDs.size() < 1) {
			return null;
		} else {
			return select(logger, "SELECT mailinglist_id, shortname, company_id FROM mailinglist_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailingListIDs, ", ") + ") AND company_id = ?", new LightMailingListRowMapper(), companyID);
		}
	}
	
    protected class LightMailingListRowMapper implements RowMapper<LightMailingList> {
		@Override
		public LightMailingList mapRow(ResultSet resultSet, int row) throws SQLException {
			LightMailingList readItem = new LightMailingList();
			readItem.setMailingListId(resultSet.getInt("mailinglist_id"));
			readItem.setShortname(resultSet.getString("shortname"));
			readItem.setCompanyId(resultSet.getInt("company_id"));
			return readItem;
		}
	}
}
