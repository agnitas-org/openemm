/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.agnitas.beans.Mailloop;
import org.agnitas.beans.MailloopEntry;
import org.agnitas.beans.impl.MailloopEntryImpl;
import org.agnitas.beans.impl.MailloopImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailloopDao;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComMailloopDaoImpl extends PaginatedBaseDaoImpl implements MailloopDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComMailloopDaoImpl.class);

	@Override
	public PaginatedListImpl<MailloopEntry> getPaginatedMailloopList(int companyId, String sortColumn, String direction, int pageNumber, int pageSize) {
		String selectStatement = "SELECT rid, description, shortname, filter_address FROM mailloop_tbl WHERE company_id = ?";

		return selectPaginatedList(logger, selectStatement,"mailloop_tbl",
				StringUtils.defaultIfEmpty(sortColumn, "shortname"),
				AgnUtils.sortingDirectionToBoolean(direction, true),
				pageNumber,
				pageSize,
				new MailloopEntry_RowMapper(),
				companyId);
	}

	@Override
	public Mailloop getMailloop(int mailloopId, @VelocityCheck int companyId) {
		if (mailloopId == 0 || companyId == 0) {
			return null;
		} else {
			return selectObjectDefaultNull(logger, "SELECT rid, company_id, description, shortname, forward, filter_address, forward_enable, "
					+ "ar_enable, timestamp, subscribe_enable, mailinglist_id, form_id, autoresponder_mailing_id, security_token "
					+ "FROM mailloop_tbl WHERE rid = ? AND company_id = ?", new Mailloop_RowMapper(), mailloopId, companyId);
		}
	}

	@Override
	public List<Mailloop> getMailloops(@VelocityCheck int companyId) {
		return select(logger, "SELECT rid, company_id, description, shortname, forward, filter_address, forward_enable, ar_enable, timestamp, "
				+ "subscribe_enable, mailinglist_id, form_id, autoresponder_mailing_id, security_token  FROM mailloop_tbl WHERE company_id = ?", 
				new Mailloop_RowMapper(), companyId);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int saveMailloop(Mailloop mailloop) {
		if (mailloop == null || mailloop.getCompanyID() == 0) {
			return 0;
		}
		
		Date now = new GregorianCalendar().getTime();
		mailloop.setChangedate(now);

		boolean isNew = mailloop.getId() == 0 || getMailloop(mailloop.getId(), mailloop.getCompanyID()) == null;
		if (isNew) {
			// Store new Mailloop in DB
			if (isOracleDB()) {
				int newId = selectInt(logger, "SELECT mailloop_tbl_seq.NEXTVAL FROM DUAL");
				String insertSql = "INSERT INTO mailloop_tbl (rid, company_id, description, shortname, forward, filter_address, forward_enable, ar_enable, timestamp, subscribe_enable, mailinglist_id, form_id, creation_date, autoresponder_mailing_id, security_token) VALUES (" + AgnUtils.repeatString("?", 15, ", ") + ")";
				int touchedLines = update(logger, insertSql,
						newId,
						mailloop.getCompanyID(),
						mailloop.getDescription(),
						mailloop.getShortname(),
						mailloop.getForwardEmail(),
						mailloop.getFilterEmail(),
						mailloop.isDoForward() ? 1 : 0,
						mailloop.isDoAutoresponder() ? 1 : 0,
						mailloop.getChangedate(),
						mailloop.isDoSubscribe() ? 1 : 0,
						mailloop.getMailinglistID(),
						mailloop.getUserformID(),
						now,
						mailloop.getAutoresponderMailingId(),
						mailloop.getSecurityToken());
				if (touchedLines == 1) {
					// Set the new ID
					mailloop.setId(newId);
				} else {
					throw new RuntimeException("Illegal insert result");
				}
			} else {
				String insertSql = "INSERT INTO mailloop_tbl (company_id, description, shortname, forward, filter_address, forward_enable, ar_enable, timestamp, subscribe_enable, mailinglist_id, form_id, creation_date, autoresponder_mailing_id, security_token) VALUES (" + AgnUtils.repeatString("?", 14, ", ") + ")";
				int newId = insertIntoAutoincrementMysqlTable(logger, "rid", insertSql,
						mailloop.getCompanyID(),
						mailloop.getDescription(),
						mailloop.getShortname(),
						mailloop.getForwardEmail(),
						mailloop.getFilterEmail(),
						mailloop.isDoForward() ? 1 : 0,
						mailloop.isDoAutoresponder() ? 1 : 0,
						mailloop.getChangedate(),
						mailloop.isDoSubscribe() ? 1 : 0,
						mailloop.getMailinglistID(),
						mailloop.getUserformID(),
						now,
						mailloop.getAutoresponderMailingId(),
						mailloop.getSecurityToken());
				mailloop.setId(newId);
			}
		} else {
			// Update Mailloop in DB
			String updateSql = "UPDATE mailloop_tbl SET description = ?, shortname = ?, forward = ?, filter_address = ?, forward_enable = ?, ar_enable = ?, timestamp = ?, subscribe_enable = ?, mailinglist_id = ?, form_id = ?, autoresponder_mailing_id = ?, security_token = ? WHERE rid = ? and company_id = ?";
			int touchedLines = update(logger, updateSql,
					mailloop.getDescription(),
					mailloop.getShortname(),
					mailloop.getForwardEmail(),
					mailloop.getFilterEmail(),
					mailloop.isDoForward() ? 1 : 0,
					mailloop.isDoAutoresponder() ? 1 : 0,
					mailloop.getChangedate(),
					mailloop.isDoSubscribe() ? 1 : 0,
					mailloop.getMailinglistID(),
					mailloop.getUserformID(),
					mailloop.getAutoresponderMailingId(),
					mailloop.getSecurityToken(),
					mailloop.getId(),
					mailloop.getCompanyID());
			
			if (touchedLines != 1) {
				throw new RuntimeException("Illegal update result");
			}
        }
		
		return mailloop.getId();
	}
	

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailloop(int mailloopId, @VelocityCheck int companyId) {
		int touchedLines = update(logger, "DELETE FROM mailloop_tbl WHERE rid = ? AND company_id = ?", mailloopId, companyId);
		return touchedLines > 0;
	}
	
	@Override
	public boolean deleteMailloopByCompany(@VelocityCheck int companyId) {
		update(logger, "DELETE FROM mailloop_tbl WHERE company_id = ?", companyId);
		return selectInt(logger, "SELECT COUNT(*) FROM mailloop_tbl WHERE company_id = ?", companyId) == 0;
	}
	

	protected class MailloopEntry_RowMapper implements RowMapper<MailloopEntry> {
		@Override
		public MailloopEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			int id = resultSet.getInt("rid");
			String description = resultSet.getString("description");
			String shortname = resultSet.getString("shortname");
			String filterEmail = resultSet.getString("filter_address");
			MailloopEntry readMailloopEntry = new MailloopEntryImpl(id, description, shortname, filterEmail);
			return readMailloopEntry;
		}
	}
	

	protected class Mailloop_RowMapper implements RowMapper<Mailloop> {
		@Override
		public Mailloop mapRow(ResultSet resultSet, int row) throws SQLException {
			Mailloop readMailloop = new MailloopImpl();
			
			readMailloop.setId(resultSet.getInt("rid"));
			readMailloop.setCompanyID(resultSet.getInt("company_id"));
			readMailloop.setDescription(resultSet.getString("description"));
			readMailloop.setShortname(resultSet.getString("shortname"));
			readMailloop.setForwardEmail(resultSet.getString("forward"));
			readMailloop.setFilterEmail(resultSet.getString("filter_address"));
			readMailloop.setDoForward(resultSet.getInt("forward_enable") > 0);
			readMailloop.setDoAutoresponder(resultSet.getInt("ar_enable") > 0);
			readMailloop.setChangedate(resultSet.getTimestamp("timestamp"));
			readMailloop.setDoSubscribe(resultSet.getInt("subscribe_enable") > 0);
			readMailloop.setMailinglistID(resultSet.getInt("mailinglist_id"));
			readMailloop.setUserformID(resultSet.getInt("form_id"));
			readMailloop.setAutoresonderMailingId(resultSet.getInt("autoresponder_mailing_id"));
			readMailloop.setSecurityToken(resultSet.getString("security_token"));
			
			return readMailloop;
		}
	}
	   
    @Override
    public boolean isMailingUsedInBounceFilterWithActiveAutoResponder(@VelocityCheck int companyId, int mailingId) {
		return selectInt(logger,
				"SELECT COUNT(*) FROM mailloop_tbl WHERE company_id = ? AND ar_enable=1 AND autoresponder_mailing_id = ?",
				companyId, mailingId)
				> 0;
    }
    
    @Override
    public List<MailloopEntry> getDependentBounceFiltersWithActiveAutoResponder(@VelocityCheck int companyId, int mailingId) {
		String query = "SELECT rid, description, shortname, filter_address FROM mailloop_tbl WHERE company_id = ? AND ar_enable=1 AND autoresponder_mailing_id = ?";
		return select(logger, query, new MailloopEntry_RowMapper(), companyId, mailingId);
	}
    
    @Override
    public boolean isAddressInUse(String filterAddress, boolean isNew) {
    	String query = "SELECT COUNT(*) FROM mailloop_tbl WHERE filter_address = ?";
    	if (isNew) {
    		return selectInt(logger, query, filterAddress) > 0;
    	} else {
    		return selectInt(logger, query, filterAddress) > 1;
    	}
    }
}
