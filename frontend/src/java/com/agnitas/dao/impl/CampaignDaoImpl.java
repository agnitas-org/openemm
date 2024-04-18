/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.factory.CampaignStatEntryFactory;
import org.agnitas.beans.impl.MailingBaseImpl;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.stat.CampaignStatEntry;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.CampaignStats;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.impl.CampaignImpl;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.ComRevenueDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.messages.I18nString;

public class CampaignDaoImpl extends PaginatedBaseDaoImpl implements CampaignDao {

	private static final Logger logger = LogManager.getLogger(CampaignDaoImpl.class);
	
	protected CampaignStatEntryFactory campaignStatEntryFactory;
	
	private static final List<String> SORTABLE_FIELDS = Arrays.asList("campaign_id", "shortname", "description");

	@Override
	public Campaign getCampaign(int campaignID, int companyID) {
		if (campaignID == 0) {
			return null;
		}
		
		String query = "SELECT campaign_id, company_id, shortname, description FROM campaign_tbl WHERE campaign_id = ? AND company_id = ?";
		return selectObjectDefaultNull(logger, query, new ComCampaignRowMapper(), campaignID, companyID);
	}
	
	@Override
	public List<Campaign> getCampaigns(int companyID) {
		String query = "SELECT campaign_id, company_id, shortname, description FROM campaign_tbl WHERE company_id = ?";
		return select(logger, query, new ComCampaignRowMapper(), companyID);
	}
	
	@Override
	public CampaignStats getStats(boolean useMailtracking, Locale aLocale, List<Integer> mailingIDs, Campaign campaign, String mailingSelection, int targetID, ComTargetDao targetDao, ComRevenueDao revenueDao) {
		CampaignStats stats = new CampaignImpl().getCampaignStats();
		String uniqueStr = "";
		ComTarget aTarget = null;
		StringBuffer mailIDs = null;
		boolean isFirst = true;
		String csv = "";

		// aLocale

		csv = "\"" + I18nString.getLocaleString("statistic.CampaignStats", aLocale) + "\"\r\n\r\n";

		if (mailingIDs != null) {
			for (int tmpInt : mailingIDs) {
				if (mailIDs == null) {
					mailIDs = new StringBuffer();
				}
				if (isFirst) {
					mailIDs.append(tmpInt);
					isFirst = false;
				} else {
					mailIDs.append(", ");
					mailIDs.append(tmpInt);
				}
			}
		}

		if (mailIDs != null) {
			mailingSelection = mailIDs.toString();
		} else {
			mailingSelection = null;
		}

		// * * * * * * * * * * * *
		// * LOAD TARGET GROUP *
		// * * * * * * * * * * * *

		// woher kommt die targetID - vorher nicht deklariert!!!
		if (targetID != 0) {
			aTarget = targetDao.getTarget(targetID, campaign.getCompanyID());
			csv += "\"" + I18nString.getLocaleString("Target", aLocale) + "\";\"" + aTarget.getTargetName() + "\"\r\n\r\n";
		} else {
			csv += "\"" + I18nString.getLocaleString("Target", aLocale) + "\";\"" + I18nString.getLocaleString("statistic.all_subscribers", aLocale) + "\"\r\n\r\n";
		}

		// * * * * * * * * * *
		// * SET NETTO SQL *
		// * * * * * * * * * *
		if (campaign.isNetto()) {
			uniqueStr = "distinct ";
			csv += "\"" + I18nString.getLocaleString("Unique_Clicks", aLocale) + "\"\r\n\r\n";
		}

		stats = loadMailingNames(stats, campaign, mailingSelection);
		stats = loadClicks(stats, campaign, uniqueStr, mailingSelection);
		stats = loadOpenedMails(stats, campaign, aTarget, useMailtracking, mailingSelection);
		stats = loadOptout(stats, campaign, aTarget, useMailtracking, mailingSelection);
		stats = loadRevenues(stats, revenueDao, campaign.getCompanyID(), targetID);

		// get mailing_id's from Hashtable
		// loop over every mailing_id
		for (int aktMailingID : stats.getMailingData().keySet()) {
			int aktBounces = 0;

			stats = loadBounces(stats, campaign, aTarget, useMailtracking, aktMailingID, aktBounces);

			// T O T A L S E N T M A I L S *

			// * * * * * * * * * *
			// case mail_tracking:
			if (useMailtracking) {
				stats = loadTotalSentMailtracking(stats, campaign, aTarget, aktMailingID);
			} else {
				// * * * * * * * * * * * *
				// case no_mail_tracking:
				// look for world mailing:
				stats = loadTotalSent(stats, campaign, aTarget, aktMailingID);
			}
		}

		// look for max values and set them to 0 if lower 0:
		if (stats.getMaxClicks() < 0) {
			stats.setMaxClicks(0);
		}
		if (stats.getMaxBounces() < 0) {
			stats.setMaxBounces(0);
		}
		if (stats.getMaxOpened() < 0) {
			stats.setMaxOpened(0);
		}
		if (stats.getMaxOptouts() < 0) {
			stats.setMaxOptouts(0);
		}
		if (stats.getMaxSubscribers() < 0) {
			stats.setMaxSubscribers(0);
		}

		campaign.setCsvfile(csv);

		return stats;
	}
	
	@Override
	public PaginatedListImpl<MailingBase> getCampaignMailings(int campaignID, PaginationForm form, Admin admin) {
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

		return selectPaginatedListWithSortClause(logger, sql, sortClause, form.getSort(), form.ascending(), form.getPage(), form.getNumberOfRows(), new ArchiveMailingRowMapper(), params.toArray());
	}

	@Override
	public boolean isContainMailings(int campaignId, Admin admin) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE campaign_id = ? AND company_id = ? AND deleted = 0 ",
				campaignId, admin.getCompanyID()) > 0;
	}

	@Override
	public boolean isDefinedForAutoOptimization(int campaignId, Admin admin) {
		return selectInt(logger, "SELECT COUNT(*) FROM auto_optimization_tbl WHERE campaign_id = ? AND company_id = ? AND deleted = 0", campaignId, admin.getCompanyID()) > 0;
	}
	
    protected String getTargetRestrictions(Admin admin, List<Object> queryParams, String tableAlias) {
        return StringUtils.EMPTY;
    }

	@Override
	public List<Campaign> getCampaignList(int companyID, String sort, int order) {
		if(!SORTABLE_FIELDS.contains(sort)) {
			sort = "shortname";
		}
		String orderString = "LOWER(" + sort + ") " + (order == 2 ? "DESC" : "ASC");
		String sqlStatement = "SELECT campaign_id, shortname, description FROM campaign_tbl WHERE company_id = ? ORDER BY " + orderString;
		List<Map<String, Object>> tmpList = select(logger, sqlStatement, companyID);

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
	public PaginatedListImpl<Campaign> getOverview(int companyId, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) {
		String query = "SELECT campaign_id, shortname, description, company_id FROM campaign_tbl WHERE company_id = ?";
		return selectPaginatedList(logger, query, "campaign_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new ComCampaignRowMapper(), companyId);
	}

	@Override
	public List<Map<String, Object>> getMailingNames(Campaign campaign, String mailingSelection) {
		try {
			List<Map<String, Object>> list;
			if (mailingSelection != null) {
				list = select(logger, "SELECT mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND mailing_id IN (" + mailingSelection + ") ORDER BY mailing_id DESC", campaign.getCompanyID());
			} else {
				list = select(logger, "SELECT mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND campaign_id = ? AND deleted = 0 AND is_template = 0 ORDER BY mailing_id DESC", campaign.getCompanyID(), campaign.getId());
			}
		
			return list;
		} catch (Exception e) {
			return null;
		}
	}

	private CampaignStats loadMailingNames(CampaignStats stats, Campaign campaign, String mailingSelection) {
		List<Map<String, Object>> mailingNames = getMailingNames(campaign, mailingSelection);
		for (Map<String, Object> map : mailingNames) {
			int id = ((Number) map.get("mailing_id")).intValue();
			CampaignStatEntry aktEntry = campaignStatEntryFactory.newCampaignStatEntry();
			aktEntry.setShortname((String) map.get("shortname"));
			if (map.get("description") != null) {
				aktEntry.setName((String) map.get("description"));
			} else {
				aktEntry.setName(" ");
			}
			stats.getMailingData().put(id, aktEntry);
		}
		return stats;
	}

	private CampaignStats loadClicks(CampaignStats stats, Campaign campaign, String uniqueStr, String mailingSelection) {
		String totalClicksQuery = "SELECT rdir.mailing_id AS mailing_id, COUNT(" + uniqueStr + " rdir.customer_id) AS amount"
			+ " FROM rdirlog_" + campaign.getCompanyID() + "_tbl rdir, rdir_url_tbl url"
			+ " WHERE rdir.mailing_id IN (" + mailingSelection + ") AND rdir.url_id = url.url_id"
			+ " GROUP BY rdir.mailing_id";

		List<Map<String, Object>> list = select(logger, totalClicksQuery);
		for (Map<String, Object> map : list) {
			int mailingID = ((Number) map.get("mailing_id")).intValue();
			int clicks = ((Number) map.get("amount")).intValue();

			// get CampaignStatEntry...
			CampaignStatEntry aktEntry = stats.getMailingData().get(mailingID);
			// ...fill in total clicks...
			aktEntry.setClicks(clicks);
			// ...put it back...
			stats.getMailingData().put(mailingID, aktEntry);
			// ...and add value to global value:
			stats.setClicks(stats.getClicks() + clicks);
			// look for max. value
			if (clicks > stats.getMaxClicks()) {
				stats.setMaxClicks(clicks);
			}
		}
			
		return stats;
	}

	private CampaignStats loadOpenedMails(CampaignStats stats, Campaign campaign, ComTarget aTarget, boolean useMailtracking, String mailingSelection) {
		CampaignStatEntry aktEntry = null;
		String onePixelQueryByCust = "SELECT onepix.MAILING_ID AS MAILING_ID, count(onepix.customer_id) AS amount FROM onepixellog_" + campaign.getCompanyID() + "_tbl onepix";
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			onePixelQueryByCust += ", customer_" + campaign.getCompanyID() + "_tbl cust";
			if (aTarget.getTargetSQL().contains("bind.")) {
				onePixelQueryByCust += ", customer_" + campaign.getCompanyID() + "_binding_tbl bind";
			}
		}

		onePixelQueryByCust += " WHERE onepix.mailing_id IN (" + mailingSelection + ")";
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			onePixelQueryByCust += " AND ((" + aTarget.getTargetSQL() + ") AND cust.customer_id = onepix.customer_id)";
			if (aTarget.getTargetSQL().contains("bind.")) {
				onePixelQueryByCust += " AND cust.customer_id = bind.customer_id";
			}
		}
		onePixelQueryByCust += " GROUP BY onepix.mailing_id";

		List<Map<String, Object>> list = select(logger, onePixelQueryByCust);
		for (Map<String, Object> map : list) {
			int mailingID = ((Number) map.get("mailing_id")).intValue();
			int opened = ((Number) map.get("amount")).intValue();

			// get CampaignStatEntry...
			aktEntry = stats.getMailingData().get(mailingID);
			// ...fill in opened mails...
			aktEntry.setOpened(opened);
			// ...put it back...
			stats.getMailingData().put(mailingID, aktEntry);
			// ...and add value to global value:
			stats.setOpened(stats.getOpened() + opened);
			// check for max. value:
			if (opened > stats.getMaxOpened()) {
				stats.setMaxOpened(opened);
			}
		}
		
		return stats;
	}

	private CampaignStats loadOptout(CampaignStats stats, Campaign campaign, ComTarget aTarget, boolean useMailtracking, String mailingSelection) {
		CampaignStatEntry aktEntry = null;
		String optoutQuery = "SELECT bind.exit_mailing_id AS mailing_id, COUNT(bind.customer_id) AS amount FROM customer_" + campaign.getCompanyID() + "_binding_tbl bind";
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			optoutQuery += ", customer_" + campaign.getCompanyID() + "_tbl cust";
		}
		optoutQuery += " WHERE bind.exit_mailing_id IN (" + mailingSelection + ")";
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			optoutQuery += " AND ((" + aTarget.getTargetSQL() + ") AND cust.customer_id = bind.customer_id)";
		}
		optoutQuery += " AND bind.user_status IN (" + UserStatus.AdminOut.getStatusCode() + ", " + UserStatus.UserOut.getStatusCode() + ") GROUP BY bind.exit_mailing_id";

		List<Map<String, Object>> list = select(logger, optoutQuery);
		for (Map<String, Object> map : list) {
			int mailingID = ((Number) map.get("mailing_id")).intValue();
			int optouts = ((Number) map.get("amount")).intValue();

			// get CampaignStatEntry...
			aktEntry = stats.getMailingData().get(mailingID);
			// ...fill in optouts...
			aktEntry.setOptouts(optouts);
			// ...put it back...
			stats.getMailingData().put(mailingID, aktEntry);
			// ...and add value to global value:
			stats.setOptouts(stats.getOptouts() + optouts);
			// check for max. value:
			if (optouts > stats.getMaxOptouts()) {
				stats.setMaxOptouts(optouts);
			}
		}
		
		return stats;
	}

	private CampaignStats loadBounces(CampaignStats stats, Campaign campaign, ComTarget aTarget, boolean useMailtracking, int aktMailingID, int aktBounces) {
		CampaignStatEntry aktEntry = null;
		String bounceQuery = "SELECT bind.mailinglist_id AS mailinglist_id, COUNT(bind.customer_id) AS amount FROM customer_" + campaign.getCompanyID() + "_binding_tbl bind";
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			bounceQuery += ", customer_" + campaign.getCompanyID() + "_tbl cust";
		}
		bounceQuery += " WHERE bind.exit_mailing_id = " + aktMailingID;
		if (useMailtracking && aTarget != null && aTarget.getId() != 0) {
			bounceQuery += " AND ((" + aTarget.getTargetSQL() + ") AND cust.customer_id = bind.customer_id)";
		}
		bounceQuery += " AND bind.user_status = " + UserStatus.Bounce.getStatusCode() + " GROUP BY bind.mailinglist_id";

		// get entry...
		aktEntry = stats.getMailingData().get(aktMailingID);
		List<Map<String, Object>> list = select(logger, bounceQuery);
		for (Map<String, Object> map : list) {
			int bounces = ((Number) map.get("amount")).intValue();

			if (bounces > aktBounces) {
				aktBounces = bounces;
			}
		}
		// ...set value...
		aktEntry.setBounces(aktBounces);
		// ...put it back...
		stats.getMailingData().put(aktMailingID, aktEntry);
		// ...and add value to global value:
		stats.setBounces(stats.getBounces() + aktBounces);
		// check for max. value:
		if (aktBounces > stats.getMaxBounces()) {
			stats.setMaxBounces(aktBounces);
		}
		
		return stats;
	}

	private CampaignStats loadTotalSentMailtracking(CampaignStats stats, Campaign campaign, ComTarget aTarget, int aktMailingID) {
		String mailtrackQuery = "SELECT COUNT(DISTINCT succ.customer_id) FROM success_" + campaign.getCompanyID() + "_tbl succ";
		if (aTarget != null && aTarget.getId() != 0) {
			mailtrackQuery += ", customer_" + campaign.getCompanyID() + "_tbl cust";
			if (aTarget.getTargetSQL().contains("bind.")) {
				mailtrackQuery += ", customer_" + campaign.getCompanyID() + "_binding_tbl bind";
			}
		}
		mailtrackQuery += " WHERE succ.mailing_id = ?";
		if (aTarget != null && aTarget.getId() != 0) {
			mailtrackQuery += " AND ((" + aTarget.getTargetSQL() + ") AND cust.customer_id = succ.customer_id)";
			if (aTarget.getTargetSQL().contains("bind.")) {
				mailtrackQuery += " AND cust.customer_id = bind.customer_id";
			}
		}
		
		int subscribers = selectInt(logger, mailtrackQuery, aktMailingID);
		// get CampaignStatEntry...
		CampaignStatEntry aktEntry = stats.getMailingData().get(aktMailingID);
		// ...fill in subscribers...
		aktEntry.setTotalMails(subscribers);
		// ...write it back...
		stats.getMailingData().put(aktMailingID, aktEntry);
		// ... and add value to global value:
		stats.setSubscribers(stats.getSubscribers() + subscribers);
		// check for max. value:
		if (subscribers > stats.getMaxSubscribers()) {
			stats.setMaxSubscribers(subscribers);
		}
		return stats;
	}

	private CampaignStats loadTotalSent(CampaignStats stats, Campaign campaign, ComTarget aTarget, int aktMailingID) {
		long totalAdmMails = 0;
		long totalMails = 0;
		String SentMailsQuery = "SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = ? AND company_id = ? AND status_field IN (?, ?, ?)";

		totalMails = selectInt(logger, SentMailsQuery, aktMailingID, campaign.getCompanyID(), MaildropStatus.WORLD.getCodeString(), MaildropStatus.ACTION_BASED.getCodeString(), MaildropStatus.DATE_BASED.getCodeString());

		// look for admin or test mailing only:
		String sentAdmMailsQuery = "";
		if (isOracleDB()) {
			sentAdmMailsQuery = "SELECT MAX(SUM(no_of_mailings)) AS amount FROM mailing_account_tbl WHERE mailing_id = ? AND company_id = ? AND status_field IN (?, ?, ?) GROUP BY timestamp";
		} else {
			sentAdmMailsQuery = "SELECT SUM(no_of_mailings) AS amount FROM mailing_account_tbl WHERE mailing_id = ? AND company_id = ? AND status_field IN (?, ?, ?) GROUP BY timestamp ORDER BY amount DESC LIMIT 1";
		}

		totalAdmMails = selectInt(logger, sentAdmMailsQuery, aktMailingID, campaign.getCompanyID(), UserType.Admin.getTypeCode(), UserType.TestUser.getTypeCode(), UserType.TestVIP.getTypeCode());

		// take the bigger value for displaying:
		// get CampaignStatEntry...
		CampaignStatEntry aktEntry = stats.getMailingData().get(aktMailingID);
		// ...fill in subscribers...
		if (totalAdmMails > totalMails) {
			aktEntry.setTotalMails((int) totalAdmMails);
			// add value to global value:
			stats.setSubscribers(stats.getSubscribers() + (int) totalAdmMails);
			// check for max. value:
			if (totalAdmMails > stats.getMaxSubscribers()) {
				stats.setMaxSubscribers((int) totalAdmMails);
			}

		} else {
			aktEntry.setTotalMails((int) totalMails);
			// add value to global value:
			stats.setSubscribers(stats.getSubscribers() + (int) totalMails);
			// check for max. value:
			if (totalMails > stats.getMaxSubscribers()) {
				stats.setMaxSubscribers((int) totalMails);
			}

		}
		// ...and write it back:
		stats.getMailingData().put(aktMailingID, aktEntry);
		return stats;
	}

	protected CampaignStats loadRevenues(CampaignStats stats, ComRevenueDao revenueDao, int companyId, int targetId) {
		Map<Integer, Double> revenueTable = null;
		LinkedList<Integer> mailingIds = new LinkedList<>();

		// Loop over all mailings. e contains the MailingID. Reason: Revenue Dao
		// wants a LinkedList with
		// Mailing IDs. mailingData contains CampaignStatEntrys.
		mailingIds.addAll(stats.getMailingData().keySet());

		// get the revenues.
		revenueTable = revenueDao.getRevenue(companyId, mailingIds, targetId);
		// set the revenue-hash
		stats.setRevenues(revenueTable);
		// set the biggest-revenue
		stats.setBiggestRevenue(getBiggestRev(revenueTable));
		// set the total-revenue
		stats.setTotalRevenue(getSumRev(revenueTable));
		return stats;
	}

	// calculates the biggest Revenue Value from the Hashmap.
	private double getBiggestRev(Map<Integer, Double> in_Revenues) {
		double biggestRev = 0.0;
		double tmpRev = 0.0;

		// loop over all Revenues and get the biggest one.
		for(Map.Entry<Integer, Double> entry : in_Revenues.entrySet()) {
			tmpRev = entry.getValue().doubleValue();
			if (tmpRev > biggestRev) {
				biggestRev = tmpRev;
			}
		}
		return biggestRev;
	}

	// calculates the sum of all revenues of this campaign
	private double getSumRev(Map<Integer, Double> in_Revenues) {
		double returnValue = 0.0;
		// loop over all Revenues and sum it up.
		for(Map.Entry<Integer, Double> entry : in_Revenues.entrySet()) {
			returnValue += entry.getValue().doubleValue();
		}
		return returnValue;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int save(Campaign campaign) {
		if (campaign.getId() == 0) {
			if (isOracleDB()) {
				int newID = selectInt(logger, "SELECT campaign_tbl_seq.NEXTVAL FROM DUAL");
				if (newID == 0) {
					newID = 1;
				}
				update(logger, "INSERT INTO campaign_tbl (campaign_id, company_id, shortname,  description ) VALUES (?, ?, ?, ? )", newID, campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription());
				campaign.setId(newID);
			} else {
				int newID = insertIntoAutoincrementMysqlTable(logger, "campaign_id", "INSERT INTO campaign_tbl (company_id, shortname,  description ) VALUES (?, ?, ? )", campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription());
				campaign.setId(newID);
			}
		} else {
			String query = "UPDATE campaign_tbl SET company_id = ?, shortname = ? , description = ?  WHERE campaign_id = ?";
			update(logger, query, campaign.getCompanyID(), campaign.getShortname(), campaign.getDescription(), campaign.getId());
		}
		return campaign.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(Campaign campaign) {
		String deleteQuery = "DELETE FROM campaign_tbl WHERE campaign_id = ? AND company_id = ?";
		int numberofRows = update(logger, deleteQuery, campaign.getId(), campaign.getCompanyID());
		return numberofRows > 0;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteByCompanyID(int companyID) {
		String deleteQuery = "DELETE FROM campaign_tbl WHERE company_id = ?";
		return update(logger, deleteQuery, companyID);
	}

	private class ComCampaignRowMapper implements RowMapper<Campaign> {
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

	private class ArchiveMailingRowMapper implements RowMapper<MailingBase> {
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
	
	public void setCampaignStatEntryFactory(CampaignStatEntryFactory campaignStatEntryFactory) {
		this.campaignStatEntryFactory = campaignStatEntryFactory;
	}
	
	@Override
	public List<Integer> getSampleCampaignIDs(int companyID) {
		return select(logger, "SELECT campaign_id FROM campaign_tbl WHERE company_id = ? AND (LOWER(shortname) LIKE '%sample%' OR LOWER(shortname) LIKE '%example%' OR LOWER(shortname) LIKE '%muster%' OR LOWER(shortname) LIKE '%beispiel%')",
			IntegerRowMapper.INSTANCE, companyID);
	}
}
