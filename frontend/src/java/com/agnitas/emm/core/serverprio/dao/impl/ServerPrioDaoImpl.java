/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverprio.dao.impl;

import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.serverprio.bean.ServerPrio;
import com.agnitas.emm.core.serverprio.dao.ServerPrioDao;
import com.agnitas.util.db.InsertStatementBuilder;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.DateRowMapper;

public final class ServerPrioDaoImpl extends BaseDaoImpl implements ServerPrioDao {
	
	@Override
	public boolean insertServerPrio(final ServerPrio serverPrio) {
		final InsertStatementBuilder builder = new InsertStatementBuilder("serverprio_tbl")
				.withPlaceholder("company_id", serverPrio.getCompanyID())
				.withPlaceholder("mailing_id", serverPrio.getMailingID())
				.withPlaceholder("priority", serverPrio.getPrio().orElse(null));
		
		serverPrio.getStartDate().ifPresent(date -> builder.withPlaceholder("start_date", date));
		serverPrio.getEndDate().ifPresent(date -> builder.withPlaceholder("end_date", date));
		
		final int inserted = update(builder.buildStatement(), builder.buildParameters());
		
		return inserted > 0;
	}

	@Override
	public boolean deleteServerPrioByMailingAndCompany(final int companyID, final int mailingID) {
		final String sql = "DELETE FROM serverprio_tbl WHERE company_id=? AND mailing_id=?";

		final int deleted = update(sql, companyID, mailingID);
		
		return deleted > 0;
	}

	@Override
	public List<ServerPrio> listServerPriosByMailingAndCompany(final int companyID, final int mailingID) {
		final String sql = "SELECT company_id, mailing_id, priority, start_date, end_date FROM serverprio_tbl WHERE company_id=? AND mailing_Id=?";
		
		return select(sql, new ServerPrioRowMapper(), companyID, mailingID);
	}

	@Override
	public Date getDeliveryPauseDate(final int companyId, final int mailingId) {
		final String sql = "SELECT start_date FROM serverprio_tbl WHERE (company_id = 0 OR company_id = ?) AND mailing_Id = ? AND priority = 0";

		final List<Date> list = select(sql, DateRowMapper.INSTANCE, companyId, mailingId);
		
		return list.isEmpty() 
				? null 
				: list.get(0);
	}
}
