/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.LinkProperty;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;

public class FormTrackableLinkDaoImpl extends BaseDaoImpl implements FormTrackableLinkDao {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(FormTrackableLinkDaoImpl.class);

	@Override
	public boolean existsDummyFormLink(int companyId, int userFormId) {
		String sql = "SELECT COUNT(url_id) FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ? AND LOWER(full_url) = 'form'";
		return selectInt(logger, sql, companyId, userFormId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveUserFormTrackableLinks(int userFormId, int companyId, List<ComTrackableUserFormLink> trackableLinks) {
		for (ComTrackableUserFormLink link : trackableLinks) {
			saveUserFormTrackableLink(userFormId, companyId, link);
		}
	}

	@DaoUpdateReturnValueCheck
	@Override
	public int saveUserFormTrackableLink(int userFormId, int companyId, ComTrackableUserFormLink trackableLink) {
		int result = 0;
		if (trackableLink != null && userFormId >= 0 && companyId != 0) {
			String usage = isOracleDB() ? "usage" : "`usage`";
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND full_url = ?";
			List<ComTrackableUserFormLink> existingLinks = select(logger, sql, new ComTrackableUserFormLink_RowMapper(), userFormId, companyId, trackableLink.getFullUrl());
			if (existingLinks.size() > 0) {
				sql = "UPDATE rdir_url_userform_tbl SET action_id = ?, " + usage + " = ?, shortname = ?, deep_tracking = ? WHERE form_id = ? AND company_id = ? AND full_url = ?";
				update(logger, sql, trackableLink.getActionID(), trackableLink.getUsage(), trackableLink.getShortname(), trackableLink.getDeepTracking(), userFormId, companyId, trackableLink.getFullUrl());

				if (existingLinks.size() == 1) {
					existingLinks.get(0).setProperties(trackableLink.getProperties());
					storeUserFormTrackableLinkProperties(existingLinks.get(0));

					result = existingLinks.get(0).getId();
				} else {
					// TODO: This case should not occure so better throw an Exception
					for (ComTrackableUserFormLink existingLink : existingLinks) {
						existingLink.setProperties(trackableLink.getProperties());
						storeUserFormTrackableLinkProperties(existingLink);
					}

				}
			} else {
				if (isOracleDB()) {
					trackableLink.setId(selectInt(logger, "SELECT rdir_url_userform_tbl_seq.NEXTVAL FROM DUAL"));
					sql = "INSERT INTO rdir_url_userform_tbl (url_id, company_id, form_id, action_id, usage, full_url, shortname, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
					update(logger, sql, trackableLink.getId(), companyId, userFormId, trackableLink.getActionID(), trackableLink.getUsage(), trackableLink.getFullUrl(), trackableLink.getShortname(), trackableLink.getDeepTracking());
				} else {
					String insertStatement = "INSERT INTO rdir_url_userform_tbl (company_id, form_id, action_id, `usage`, full_url, shortname, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?)";

					Object[] paramsWithNext = new Object[7];
					paramsWithNext[0] = companyId;
					paramsWithNext[1] = userFormId;
					paramsWithNext[2] = trackableLink.getActionID();
					paramsWithNext[3] = trackableLink.getUsage();
					paramsWithNext[4] = trackableLink.getFullUrl();
					paramsWithNext[5] = trackableLink.getShortname();
					paramsWithNext[6] = trackableLink.getDeepTracking();

					int linkId = insertIntoAutoincrementMysqlTable(logger, "url_id", insertStatement, paramsWithNext);
					trackableLink.setId(linkId);
				}

				storeUserFormTrackableLinkProperties(trackableLink);

				result = trackableLink.getId();
			}
		}
		return result;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void storeUserFormTrackableLinkProperties(ComTrackableUserFormLink link) {
		update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id = ?", link.getId());
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
			batchupdate(logger, insertSql, batchParameters);
		}
	}

	@Override
	public ComTrackableUserFormLink getUserFormTrackableLink(int linkID) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("getUserFormTrackableLink started - link id: " + linkID);
		}

		ComTrackableUserFormLink trackableUserFormLink;
		if (linkID > 0) {
			String usage = isOracleDB() ? "usage" : "`usage`";
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE url_id = ?";

			List<ComTrackableUserFormLink> existingLinks = select(logger, sql, new FormTrackableLinkWithProperties(), linkID);
			if (existingLinks == null || existingLinks.size() == 0) {
				trackableUserFormLink =  null;
			} else if (existingLinks.size() == 1) {
				trackableUserFormLink = existingLinks.get(0);
			} else {
				throw new Exception("Invalid number of userformlinks found");
			}
		} else {
			trackableUserFormLink = null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("getUserFormTrackableLink finished - link id: " + linkID);
		}

		return trackableUserFormLink;
	}

	@Override
	public ComTrackableUserFormLink getUserFormTrackableLink(int companyId, int formId, int linkId) {
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, deep_tracking, company_id " +
				"FROM rdir_url_userform_tbl WHERE company_id =? AND form_id = ? AND url_id = ?";

		return selectObjectDefaultNull(logger, sql, new FormTrackableLinkWithProperties(), companyId, formId, linkId);
	}

	@Override
	public ComTrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(int companyID, int formID) throws Exception {
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, deep_tracking FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ? AND LOWER(full_url) = 'form'";

		List<ComTrackableUserFormLink> list = select(logger, sql, new ComTrackableUserFormLink_RowMapper(), companyID, formID);

		if  (list == null || list.size() > 1) {
			throw new Exception("Unexpected result in getDummyUserFormTrackableLinkForStatisticCount");
		} else if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public Map<String, ComTrackableUserFormLink> getUserFormTrackableLinks(int formID, int companyID) {
		List<ComTrackableUserFormLink> list = getUserFormTrackableLinkList(formID, companyID);

		HashMap<String, ComTrackableUserFormLink> listTrackableUserFormLink = new HashMap<>();
		for (ComTrackableUserFormLink trackableUserFormLink : list) {
			listTrackableUserFormLink.put(trackableUserFormLink.getFullUrl(), trackableUserFormLink);
		}
		return listTrackableUserFormLink;
	}

	@Override
	public List<ComTrackableUserFormLink> getUserFormTrackableLinkList(int formID, int companyID) {
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, deep_tracking " +
				" FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND LOWER(full_url) != 'form'";

		return select(logger, sql, new FormTrackableLinkWithProperties(), formID, companyID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteUserFormTrackableLink(int linkID, int companyID) {
		if (logger.isDebugEnabled()) {
			logger.debug("deleteUserFormTrackableLink started - link id: " + linkID + " company id: " + companyID);
		}

		try {
			update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_userform_tbl WHERE company_id = ? AND url_id = ?)", companyID, linkID);

			int deletedEntries = update(logger, "DELETE FROM rdir_url_userform_tbl WHERE url_id = ? AND company_id = ?", linkID, companyID);
			if (logger.isDebugEnabled()) {
				logger.debug("deleteUserFormTrackableLink finished - link id: " + linkID + " company id: " + companyID);
			}

			return deletedEntries == 1;
		} catch (Exception e) {
			logger.error("deleteUserFormTrackableLink finished with error - link id: " + linkID + " company id: " + companyID, e);

			return false;
		}
	}

	private List<LinkProperty> getUserFormTrackableLinkProperties(int linkId) {
		String sql = "SELECT param_type, param_key, param_value FROM rdir_url_userform_param_tbl WHERE url_id = ? ORDER BY param_type, param_key, param_value";
		return select(logger, sql, new ComTrackableUserFormLinkProperty_RowMapper(), linkId);
	}

	@Override
	public boolean logUserFormTrackableLinkClickInDB(ComTrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		if (logger.isDebugEnabled()) {
			logger.debug("logUserFormTrackableLinkClickInDB started - link id: " + link.getId() + " customer id: " + customerID + " remote address: " + remoteAddr);
		}

		String sql = "INSERT INTO rdirlog_userform_" + link.getCompanyID() + "_tbl (form_id, customer_id, url_id, company_id, ip_adr, mailing_id, device_class_id, device_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			update(logger, sql, link.getFormID(), customerID, link.getId(), link.getCompanyID(), remoteAddr, mailingID, deviceClass.getId(), deviceID, clientID);

			if (logger.isDebugEnabled()) {
				logger.debug("logUserFormTrackableLinkClickInDB finished - link id: " + link.getId() + " customer id: " + customerID + " remote address: " + remoteAddr);
			}

			return true;
		} catch (Exception e) {
			logger.error("logUserFormTrackableLinkClickInDB finished with error - link id: " + link.getId() + " customer id: " + customerID + " remote address: " + remoteAddr, e);

			return false;
		}
	}

	@Override
	public boolean logUserFormCallInDB(int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		try {
			int result = update(logger,
				"INSERT INTO rdirlog_userform_" + companyID + "_tbl (company_id, form_id, url_id, mailing_id, customer_id, ip_adr, device_class_id, device_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				companyID, formID, linkID, mailingID, customerID, remoteAddr, deviceClass.getId(), deviceID, clientID);

			return result == 1;
		} catch (Exception e) {
			logger.error("logUserFormCallInDB finished with error - companyID: " + companyID + " formID: " + formID + " linkID: " + linkID + " customerID: " + customerID + " remoteAddr: " + remoteAddr, e);

			return false;
		}
	}

	private static class ComTrackableUserFormLinkProperty_RowMapper implements RowMapper<LinkProperty> {
		@Override
		public LinkProperty mapRow(ResultSet resultSet, int row) throws SQLException {
			LinkProperty.PropertyType type;
			try {
				type = LinkProperty.PropertyType.parseString(resultSet.getString("param_type"));
			} catch (Exception e) {
				throw new SQLException("Error when reading properties param_type", e);
			}
			return new LinkProperty(type, resultSet.getString("param_key"), resultSet.getString("param_value"));
		}
	}

	private class ComTrackableUserFormLink_RowMapper implements RowMapper<ComTrackableUserFormLink> {
		@Override
		public ComTrackableUserFormLink mapRow(ResultSet resultSet, int row) throws SQLException {
			ComTrackableUserFormLink trackableUserFormLink = new ComTrackableUserFormLinkImpl();
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

	private class FormTrackableLinkWithProperties extends ComTrackableUserFormLink_RowMapper {
		@Override
		public ComTrackableUserFormLink mapRow(ResultSet resultSet, int row) throws SQLException {
			ComTrackableUserFormLink link = super.mapRow(resultSet, row);
			link.setProperties(getUserFormTrackableLinkProperties(link.getId()));
			return link;
		}
	}
}
