/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.emm.core.bounce.util.BounceUtils;
import com.agnitas.beans.Mailloop;
import com.agnitas.beans.MailloopEntry;
import com.agnitas.beans.impl.MailloopEntryImpl;
import com.agnitas.beans.impl.MailloopImpl;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.MailloopDao;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MailloopDaoImpl extends PaginatedBaseDaoImpl implements MailloopDao {
	
    private static final String FILTER_ADDRESS_COL = "filter_address";
    private static final String DESCRIPTION_COL = "description";
    private static final String SHORTNAME_COL = "shortname";

    @Override
	public PaginatedListImpl<MailloopEntry> getPaginatedMailloopList(int companyId, String sortColumn, String direction, int pageNumber, int pageSize) {
		String selectStatement = "SELECT rid, description, shortname, filter_address FROM mailloop_tbl WHERE company_id = ?";

		return selectPaginatedList(selectStatement,"mailloop_tbl",
				StringUtils.defaultIfEmpty(sortColumn, SHORTNAME_COL),
				AgnUtils.sortingDirectionToBoolean(direction, true),
				pageNumber,
				pageSize,
				new MailloopEntry_RowMapper(),
				companyId);
	}

    @Override
   	public PaginatedListImpl<BounceFilterDto> getPaginatedMailloopList(BounceFilterListForm filter) {
        StringBuilder sql = new StringBuilder("SELECT rid, description, shortname, ")
                .append(getFilterAddressOverviewQuery(filter.getCompanyDomain())).append(" filter_address")
                .append(" FROM mailloop_tbl ");
        List<Object> params = applyOverviewFilter(filter, sql);

		PaginatedListImpl<BounceFilterDto> list = selectPaginatedList(sql.toString(), "mailloop_tbl",
				filter.getSortOrDefault(SHORTNAME_COL), filter.ascending(), filter.getPage(),
				filter.getNumberOfRows(), new BounceFilterDtoRowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
		}

		return list;
   	}

    private static String getFilterAddressOverviewQuery(String companyDomain) {
        return String.format("COALESCE(NULLIF(filter_address, ''), CONCAT(CONCAT('%s', rid), '%s'))",
                BounceUtils.EMAIL_PREFIX + "_", "@" + companyDomain);
    }

    private List<Object> applyOverviewFilter(BounceFilterListForm filter, StringBuilder sql) {
        List<Object> params = applyRequiredOverviewFilter(sql, filter.getCompanyId());

		if (StringUtils.isNotBlank(filter.getName())) {
            sql.append(getPartialSearchFilterWithAnd(SHORTNAME_COL));
            params.add(filter.getName());
        }
        if (StringUtils.isNotBlank(filter.getDescription())) {
            sql.append(getPartialSearchFilterWithAnd(DESCRIPTION_COL));
            params.add(filter.getDescription());
        }
        if (StringUtils.isNotBlank(filter.getFilterAddress())) {
            sql.append(getPartialSearchFilterWithAnd(getFilterAddressOverviewQuery(filter.getCompanyDomain())));
            params.add(filter.getFilterAddress());
        }
        return params;
    }

	private int getTotalUnfilteredCountForOverview(int companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM mailloop_tbl");
		List<Object> params = applyRequiredOverviewFilter(query, companyId);

		return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int companyId) {
		query.append(" WHERE company_id = ?");
		return new ArrayList<>(List.of(companyId));
	}

	@Override
	public Mailloop getMailloop(int mailloopId, int companyId) {
		if (mailloopId == 0 || companyId == 0) {
			return null;
		} else {
			return selectObjectDefaultNull("SELECT rid, company_id, description, shortname, forward, filter_address, forward_enable, "
					+ "ar_enable, timestamp, subscribe_enable, mailinglist_id, form_id, autoresponder_mailing_id, security_token "
					+ "FROM mailloop_tbl WHERE rid = ? AND company_id = ?", new Mailloop_RowMapper(), mailloopId, companyId);
		}
	}

	@Override
	public List<Mailloop> getMailloops(int companyId) {
		return select("SELECT rid, company_id, description, shortname, forward, filter_address, forward_enable, ar_enable, timestamp, "
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
				int newId = selectInt("SELECT mailloop_tbl_seq.NEXTVAL FROM DUAL");
				String insertSql = "INSERT INTO mailloop_tbl (rid, company_id, description, shortname, forward, filter_address, forward_enable, ar_enable, timestamp, subscribe_enable, mailinglist_id, form_id, creation_date, autoresponder_mailing_id, security_token) VALUES (" + AgnUtils.repeatString("?", 15, ", ") + ")";
				int touchedLines = update(insertSql,
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
				int newId = insertIntoAutoincrementMysqlTable("rid", insertSql,
						mailloop.getCompanyID(),
						mailloop.getDescription(),
						mailloop.getShortname(),
						mailloop.getForwardEmail(),
						StringUtils.isBlank(mailloop.getFilterEmail()) ? null : mailloop.getFilterEmail(),
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
			int touchedLines = update(updateSql,
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
	public boolean deleteMailloop(int mailloopId, int companyId) {
		int touchedLines = update("DELETE FROM mailloop_tbl WHERE rid = ? AND company_id = ?", mailloopId, companyId);
		return touchedLines > 0;
	}
	
	@Override
	public boolean deleteMailloopByCompany(int companyId) {
		update("DELETE FROM mailloop_tbl WHERE company_id = ?", companyId);
		return selectInt("SELECT COUNT(*) FROM mailloop_tbl WHERE company_id = ?", companyId) == 0;
	}

	protected class MailloopEntry_RowMapper implements RowMapper<MailloopEntry> {
		@Override
		public MailloopEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			int id = resultSet.getInt("rid");
			String description = resultSet.getString(DESCRIPTION_COL);
			String shortname = resultSet.getString(SHORTNAME_COL);
			String filterEmail = resultSet.getString(FILTER_ADDRESS_COL);
			MailloopEntry readMailloopEntry = new MailloopEntryImpl(id, description, shortname, filterEmail);
			return readMailloopEntry;
		}
	}

    private static class BounceFilterDtoRowMapper implements RowMapper<BounceFilterDto> {
   		@Override
   		public BounceFilterDto mapRow(ResultSet resultSet, int row) throws SQLException {
            BounceFilterDto dto = new BounceFilterDto();
            dto.setId(resultSet.getInt("rid"));
            dto.setShortName(resultSet.getString(SHORTNAME_COL));
            dto.setFilterEmail(resultSet.getString(FILTER_ADDRESS_COL));
            dto.setDescription(resultSet.getString(DESCRIPTION_COL));
   			return dto;
   		}
   	}

	protected class Mailloop_RowMapper implements RowMapper<Mailloop> {
		@Override
		public Mailloop mapRow(ResultSet resultSet, int row) throws SQLException {
			Mailloop readMailloop = new MailloopImpl();
			
			readMailloop.setId(resultSet.getInt("rid"));
			readMailloop.setCompanyID(resultSet.getInt("company_id"));
			readMailloop.setDescription(resultSet.getString(DESCRIPTION_COL));
			readMailloop.setShortname(resultSet.getString(SHORTNAME_COL));
			readMailloop.setForwardEmail(resultSet.getString("forward"));
			readMailloop.setFilterEmail(resultSet.getString(FILTER_ADDRESS_COL));
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
    public boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId) {
		return selectInt(
				"SELECT COUNT(*) FROM mailloop_tbl WHERE company_id = ? AND ar_enable=1 AND autoresponder_mailing_id = ?",
				companyId, mailingId)
				> 0;
    }
    
    @Override
    public List<MailloopEntry> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId) {
		String query = "SELECT rid, description, shortname, filter_address FROM mailloop_tbl WHERE company_id = ? AND ar_enable=1 AND autoresponder_mailing_id = ?";
		return select(query, new MailloopEntry_RowMapper(), companyId, mailingId);
	}

    @Override
    public boolean isAddressInUse(String filterAddress) {
        return selectInt("SELECT COUNT(*) FROM mailloop_tbl WHERE filter_address = ?", filterAddress) > 0;
    }
}
