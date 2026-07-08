/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.LinkProperty;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.emm.core.trackablelinks.dto.UserFormLinkStats;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.TrackableUserFormLinkImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.jdbc.core.RowMapper;

public class FormTrackableLinkDaoImpl extends BaseDaoImpl implements FormTrackableLinkDao {

	@Override
	public boolean existsDummyFormLink(int companyId, int userFormId) {
		String sql = "SELECT COUNT(url_id) FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ? AND LOWER(full_url) = 'form'";
		return selectInt(sql, companyId, userFormId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveUserFormTrackableLinks(int userFormId, int companyId, List<TrackableUserFormLink> trackableLinks) {
		for (TrackableUserFormLink link : trackableLinks) {
			saveUserFormTrackableLink(userFormId, companyId, link);
		}
	}

	@DaoUpdateReturnValueCheck
	@Override
	public int saveUserFormTrackableLink(int userFormId, int companyId, TrackableUserFormLink trackableLink) {
		int result = 0;
		if (trackableLink != null && userFormId >= 0 && companyId != 0) {
			String usage = getUsageCol();
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND full_url = ?";
			List<TrackableUserFormLink> existingLinks = select(sql, new TrackableUserFormLinkRowMapper(), userFormId, companyId, trackableLink.getFullUrl());
			if (!existingLinks.isEmpty()) {
				sql = "UPDATE rdir_url_userform_tbl SET action_id = ?, " + usage + " = ?, shortname = ?, deep_tracking = ? WHERE form_id = ? AND company_id = ? AND full_url = ?";
				update(sql, trackableLink.getActionID(), trackableLink.getUsage(), trackableLink.getShortname(), trackableLink.getDeepTracking(), userFormId, companyId, trackableLink.getFullUrl());

				if (existingLinks.size() == 1) {
					existingLinks.getFirst().setProperties(trackableLink.getProperties());
					storeUserFormTrackableLinkProperties(existingLinks.getFirst());

					result = existingLinks.getFirst().getId();
				} else {
					// TODO: This case should not occure so better throw an Exception
					for (TrackableUserFormLink existingLink : existingLinks) {
						existingLink.setProperties(trackableLink.getProperties());
						storeUserFormTrackableLinkProperties(existingLink);
					}

				}
			} else {
				if (isOracleDB()) {
					trackableLink.setId(selectInt("SELECT rdir_url_userform_tbl_seq.NEXTVAL FROM DUAL"));
					sql = "INSERT INTO rdir_url_userform_tbl (url_id, company_id, form_id, action_id, usage, full_url, shortname, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
					update(sql, trackableLink.getId(), companyId, userFormId, trackableLink.getActionID(), trackableLink.getUsage(), trackableLink.getFullUrl(), trackableLink.getShortname(), trackableLink.getDeepTracking());
				} else {
					String insertStatement = "INSERT INTO rdir_url_userform_tbl (company_id, form_id, action_id, %s, full_url, shortname, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?)".formatted(getUsageCol());

					Object[] paramsWithNext = new Object[7];
					paramsWithNext[0] = companyId;
					paramsWithNext[1] = userFormId;
					paramsWithNext[2] = trackableLink.getActionID();
					paramsWithNext[3] = trackableLink.getUsage();
					paramsWithNext[4] = trackableLink.getFullUrl();
					paramsWithNext[5] = trackableLink.getShortname();
					paramsWithNext[6] = trackableLink.getDeepTracking();

					int linkId = insert("url_id", insertStatement, paramsWithNext);
					trackableLink.setId(linkId);
				}

				storeUserFormTrackableLinkProperties(trackableLink);

				result = trackableLink.getId();
			}
		}
		return result;
	}

	private String getUsageCol() {
		return isOracleDB() || isPostgreSQL() ? "usage" : "`usage`";
	}

	@DaoUpdateReturnValueCheck
	private void storeUserFormTrackableLinkProperties(TrackableUserFormLink link) {
		update("DELETE FROM rdir_url_userform_param_tbl WHERE url_id = ?", link.getId());
		if (link.getProperties() != null) {
			String insertSql = "INSERT INTO rdir_url_userform_param_tbl (url_id, param_type, param_key, param_value) VALUES(?, ?, ?, ?)";
			List<Object[]> batchParameters = new ArrayList<>();
			for (LinkProperty property : link.getProperties()) {
				String propertyName = property.getPropertyName();
				if (propertyName == null) {
					propertyName = "";
				}
				String propertyValue = property.getPropertyValue();
				if (propertyValue == null) {
					propertyValue = "";
				}
				if (StringUtils.isNotEmpty(propertyName) || StringUtils.isNotEmpty(propertyValue)) {
					batchParameters.add(new Object[] { link.getId(), property.getPropertyType().toString(), propertyName, propertyValue });
				}
			}
			batchupdate(insertSql, batchParameters);
		}
	}

	@Override
	public TrackableUserFormLink getUserFormTrackableLink(int linkID) {
		logger.debug("getUserFormTrackableLink started - link id: {}", linkID);

		TrackableUserFormLink trackableUserFormLink;
		if (linkID > 0) {
			String usage = getUsageCol();
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE url_id = ?";

			trackableUserFormLink = selectObjectDefaultNull(sql, new FormTrackableLinkWithProperties(), linkID);
		} else {
			trackableUserFormLink = null;
		}

		logger.debug("getUserFormTrackableLink finished - link id: {}", linkID);

		return trackableUserFormLink;
	}

	@Override
	public TrackableUserFormLink getUserFormTrackableLink(int companyId, int formId, int linkId) {
		String usage = getUsageCol();
		String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id " +
				"FROM rdir_url_userform_tbl WHERE company_id =? AND form_id = ? AND url_id = ?";

		return selectObjectDefaultNull(sql, new FormTrackableLinkWithProperties(), companyId, formId, linkId);
	}

	@Override
	public TrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(int companyID, int formID) {
		String usage = getUsageCol();
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, deep_tracking FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ? AND LOWER(full_url) = 'form'";

		return selectObjectDefaultNull(sql, new TrackableUserFormLinkRowMapper(), companyID, formID);
	}

	@Override
	public Map<String, TrackableUserFormLink> getUserFormTrackableLinks(int formID, int companyID) {
		List<TrackableUserFormLink> list = getUserFormTrackableLinkList(formID, companyID);

		HashMap<String, TrackableUserFormLink> listTrackableUserFormLink = new HashMap<>();
		for (TrackableUserFormLink trackableUserFormLink : list) {
			listTrackableUserFormLink.put(trackableUserFormLink.getFullUrl(), trackableUserFormLink);
		}
		return listTrackableUserFormLink;
	}

	@Override
	public List<TrackableUserFormLink> getUserFormTrackableLinkList(int formID, int companyID) {
		String usage = getUsageCol();
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, deep_tracking " +
				" FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND LOWER(full_url) != 'form'";

		return select(sql, new FormTrackableLinkWithProperties(), formID, companyID);
	}

	private List<LinkProperty> getUserFormTrackableLinkProperties(int linkId) {
		String sql = "SELECT param_type, param_key, param_value FROM rdir_url_userform_param_tbl WHERE url_id = ? ORDER BY param_type, param_key, param_value";
		return select(sql, new TrackableUserFormLinkPropertyRowMapper(), linkId);
	}

	@Override
	public boolean logUserFormTrackableLinkClickInDB(TrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		if (logger.isDebugEnabled()) {
			logger.debug("logUserFormTrackableLinkClickInDB started - link id: {} customer id: {} remote address: {}", link.getId(), customerID, remoteAddr);
		}

		String sql = "INSERT INTO rdirlog_userform_" + link.getCompanyID() + "_tbl (form_id, customer_id, url_id, company_id, ip_adr, mailing_id, device_class_id, device_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			update(sql, link.getFormID(), customerID, link.getId(), link.getCompanyID(), remoteAddr, mailingID, deviceClass.getId(), deviceID, clientID);

			if (logger.isDebugEnabled()) {
				logger.debug("logUserFormTrackableLinkClickInDB finished - link id: {} customer id: {} remote address: {}", link.getId(), customerID, remoteAddr);
			}

			return true;
		} catch (Exception e) {
			logger.error("logUserFormTrackableLinkClickInDB finished with error - link id: {} customer id: {} remote address: {}", link.getId(), customerID, remoteAddr, e);
			return false;
		}
	}

	@Override
	public boolean logUserFormCallInDB(int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		try {
			int result = update(
				"INSERT INTO rdirlog_userform_" + companyID + "_tbl (company_id, form_id, url_id, mailing_id, customer_id, ip_adr, device_class_id, device_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				companyID, formID, linkID, mailingID, customerID, remoteAddr, deviceClass.getId(), deviceID, clientID);

			return result == 1;
		} catch (Exception e) {
			logger.error("logUserFormCallInDB finished with error - companyID: {} formID: {} linkID: {} customerID: {} remoteAddr: {}", companyID, formID, linkID, customerID, remoteAddr, e);
			return false;
		}
	}

	private static class TrackableUserFormLinkPropertyRowMapper implements RowMapper<LinkProperty> {
		@Override
		public LinkProperty mapRow(ResultSet resultSet, int row) throws SQLException {
			LinkProperty.PropertyType type = LinkProperty.PropertyType.parseString(resultSet.getString("param_type"));
			return new LinkProperty(type, resultSet.getString("param_key"), resultSet.getString("param_value"));
		}
	}

	private static class TrackableUserFormLinkRowMapper implements RowMapper<TrackableUserFormLink> {
		@Override
		public TrackableUserFormLink mapRow(ResultSet resultSet, int row) throws SQLException {
			TrackableUserFormLink trackableUserFormLink = new TrackableUserFormLinkImpl();
			trackableUserFormLink.setId(resultSet.getInt("url_id"));
			trackableUserFormLink.setCompanyID(resultSet.getInt("company_id"));
			trackableUserFormLink.setFormID(resultSet.getInt("form_id"));
			trackableUserFormLink.setActionID(resultSet.getInt("action_id"));
			trackableUserFormLink.setUsage(resultSet.getInt("usage"));
			trackableUserFormLink.setFullUrl(resultSet.getString("full_url"));
			trackableUserFormLink.setShortname(resultSet.getString("shortname"));
			trackableUserFormLink.setDeepTracking(resultSet.getInt("deep_tracking"));
			return trackableUserFormLink;
		}
	}

	private class FormTrackableLinkWithProperties extends TrackableUserFormLinkRowMapper {
		@Override
		public TrackableUserFormLink mapRow(ResultSet resultSet, int row) throws SQLException {
			TrackableUserFormLink link = super.mapRow(resultSet, row);
			link.setProperties(getUserFormTrackableLinkProperties(link.getId()));
			return link;
		}
	}

	@Override
	public List<UserFormLinkStats> getStats(int formId, int companyId, DateRange dateRange) {
		List<Object> params = new ArrayList<>();
		params.add(formId);
		String periodFilter = getPeriodFilter(dateRange, params);

		return select("""
                SELECT
                  COALESCE(url.shortname, url.full_url) AS url,
                  SUM(CASE WHEN (log.customer_id IS NOT NULL AND log.customer_id != 0) THEN 1 ELSE 0 END) AS clicks_gross,
                  COUNT(DISTINCT CASE WHEN (log.customer_id IS NOT NULL AND log.customer_id != 0)
                                      THEN log.customer_id END) AS clicks_net,
                  SUM(CASE WHEN (log.customer_id IS NULL OR log.customer_id = 0) THEN 1 ELSE 0 END) AS clicks_anonym
                FROM rdir_url_userform_tbl url
                  JOIN rdirlog_userform_%d_tbl log ON log.url_id = url.url_id
                WHERE url.form_id = ?
                  %s
                GROUP BY url.url_id, COALESCE(url.shortname, url.full_url)
            """.formatted(companyId, periodFilter), (rs, i) -> new UserFormLinkStats(
			rs.getString("url"),
			rs.getInt("clicks_gross"),
			rs.getInt("clicks_net"),
			rs.getInt("clicks_anonym")
		), params.toArray());
	}

	private static String getPeriodFilter(DateRange dateRange, List<Object> params) {
		if (dateRange.getFrom() == null || dateRange.getTo() == null) {
			return "";
		}
		params.add(dateRange.getFrom());
		params.add(DateUtils.addDays(dateRange.getTo(), 1));
		return " AND log.timestamp >= ? AND log.timestamp < ?";
	}
}
