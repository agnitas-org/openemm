/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.impl.CampaignImpl;
import com.agnitas.beans.impl.MailingBaseImpl;
import com.agnitas.beans.impl.MailinglistImpl;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class CampaignDaoImpl extends PaginatedBaseDaoImpl implements CampaignDao {

	private static final List<String> SORTABLE_FIELDS = Arrays.asList("campaign_id", "shortname", "description");

	@Override
	public Campaign getCampaign(int campaignID, int companyID) {
		if (campaignID == 0) {
			return null;
		}
		
		String query = "SELECT campaign_id, company_id, shortname, description FROM campaign_tbl WHERE campaign_id = ? AND company_id = ?";
		return selectObjectDefaultNull(query, new CampaignRowMapper(), campaignID, companyID);
	}
	
	@Override
	public List<Campaign> getCampaigns(int companyID) {
		String query = "SELECT campaign_id, company_id, shortname, description FROM campaign_tbl WHERE company_id = ?";
		return select(query, new CampaignRowMapper(), companyID);
	}

	@Override
	public PaginatedList<MailingBase> getCampaignMailings(int campaignID, PaginationForm form, Admin admin) {
        List<Object> params = new ArrayList<>();
        params.add(MaildropStatus.WORLD.getCodeString());
        params.add(admin.getCompanyID());
        params.add(campaignID);
		String sql = "SELECT a.mailing_id, a.shortname AS mailing_name, a.description AS mailing_description, b.shortname AS listname, (SELECT min(c.timestamp)"
				+ " FROM mailing_account_tbl c WHERE a.mailing_id = c.mailing_id AND c.status_field = ?) AS senddate FROM mailing_tbl a, mailinglist_tbl b WHERE a.company_id = ?"
				+ " AND a.campaign_id = ? AND b.deleted = 0 AND a.deleted = 0 AND a.is_template = 0 AND a.mailinglist_id = b.mailinglist_id " + 
                getTargetRestrictions(admin, params, "a");

		String sortClause = "ORDER BY senddate DESC, mailing_id DESC";
		if (StringUtils.isNotBlank(form.getSort())) {
			if (!form.getSort().equals("senddate")) {
				sortClause = "ORDER BY " + form.getSort();
			} else {
				sortClause = "ORDER BY LOWER(" + form.getSort() + ")";
			}

			sortClause += " " + (form.ascending() ? "asc" : "desc");
		}

		return selectPaginatedListWithSortClause(sql, sortClause, form, new ArchiveMailingRowMapper(), params.toArray());
	}

	@Override
	public boolean isContainMailings(int campaignId, Admin admin) {
		return selectInt("SELECT COUNT(*) FROM mailing_tbl WHERE campaign_id = ? AND company_id = ? AND deleted = 0 ",
				campaignId, admin.getCompanyID()) > 0;
	}

	@Override
	public boolean isDefinedForAutoOptimization(int campaignId, Admin admin) {
		return selectInt("SELECT COUNT(*) FROM auto_optimization_tbl WHERE campaign_id = ? AND company_id = ? AND deleted = 0", campaignId, admin.getCompanyID()) > 0;
	}
	
    protected String getTargetRestrictions(Admin admin, List<Object> queryParams, String tableAlias) {
        return StringUtils.EMPTY;
    }

	@Override
	public List<Campaign> getCampaignList(int companyID, String sort, int order) {
		if(!SORTABLE_FIELDS.contains(sort)) {
			sort = "shortname";
		}
		String orderString = (sort.equalsIgnoreCase("campaign_id") ? sort : "LOWER(" + sort + ")") + " " + (order == 2 ? "DESC" : "ASC");
		String sqlStatement = "SELECT campaign_id, shortname, description FROM campaign_tbl WHERE company_id = ? ORDER BY " + orderString;
		List<Map<String, Object>> tmpList = select(sqlStatement, companyID);

		List<Campaign> result = new ArrayList<>();
		for (Map<String, Object> row : tmpList) {

			Campaign campaign = new CampaignImpl();
			campaign.setId(((Number) row.get("CAMPAIGN_ID")).intValue());
			campaign.setShortname((String) row.get("SHORTNAME"));
			campaign.setDescription((String) row.get("DESCRIPTION"));
			result.add(campaign);

		}
		return result;
	}

	@Override
	public PaginatedList<Campaign> getOverview(int companyId, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) {
		String query = "SELECT campaign_id, shortname, description, company_id FROM campaign_tbl WHERE company_id = ?";
		return selectPaginatedList(query, "campaign_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new CampaignRowMapper(), companyId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int save(Campaign campaign) {
		if (campaign.getId() == 0) {
			if (isOracleDB()) {
				int newID = selectInt("SELECT campaign_tbl_seq.NEXTVAL FROM DUAL");
				if (newID == 0) {
					newID = 1;
				}
				update("INSERT INTO campaign_tbl (campaign_id, company_id, shortname,  description ) VALUES (?, ?, ?, ? )", newID, campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription());
				campaign.setId(newID);
			} else {
				int newID = insert("campaign_id", "INSERT INTO campaign_tbl (company_id, shortname,  description ) VALUES (?, ?, ? )", campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription());
				campaign.setId(newID);
			}
		} else {
			String query = "UPDATE campaign_tbl SET company_id = ?, shortname = ? , description = ?  WHERE campaign_id = ?";
			update(query, campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription(), campaign.getId());
		}
		return campaign.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(Campaign campaign) {
		String deleteQuery = "DELETE FROM campaign_tbl WHERE campaign_id = ? AND company_id = ?";
        return update(deleteQuery, campaign.getId(), campaign.getCompanyID()) > 0;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteByCompanyID(int companyID) {
		String deleteQuery = "DELETE FROM campaign_tbl WHERE company_id = ?";
		return update(deleteQuery, companyID);
	}

	private static class CampaignRowMapper implements RowMapper<Campaign> {
		@Override
		public Campaign mapRow(ResultSet resultSet, int row) throws SQLException {

			Campaign campaign = new CampaignImpl();
			campaign.setId(resultSet.getBigDecimal("campaign_id").intValue());
			campaign.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			campaign.setDescription(resultSet.getString("description"));
			campaign.setShortname(resultSet.getString("shortname"));

			return campaign;
		}
	}

	private static class ArchiveMailingRowMapper implements RowMapper<MailingBase> {
		@Override
		public MailingBase mapRow(ResultSet resultSet, int row) throws SQLException {
			MailingBase newBean = new MailingBaseImpl();
			newBean.setId(resultSet.getInt("mailing_id"));
			newBean.setShortname(resultSet.getString("mailing_name"));
			newBean.setDescription(resultSet.getString("mailing_description"));
			newBean.setSenddate(resultSet.getTimestamp("senddate"));

			Mailinglist mailinglist = new MailinglistImpl();
			mailinglist.setShortname(resultSet.getString("listname"));
			newBean.setMailinglist(mailinglist);

			return newBean;
		}
	}
	
	@Override
	public List<Integer> getSampleCampaignIDs(int companyID) {
		return select("SELECT campaign_id FROM campaign_tbl WHERE company_id = ? AND (LOWER(shortname) LIKE '%sample%' OR LOWER(shortname) LIKE '%example%' OR LOWER(shortname) LIKE '%muster%' OR LOWER(shortname) LIKE '%beispiel%')",
			IntegerRowMapper.INSTANCE, companyID);
	}

}
