/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.userform.service.UserFormFilter;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.UserFormDao;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;

public class UserFormDaoImpl extends PaginatedBaseDaoImpl implements UserFormDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormDaoImpl.class);

    @Override
    public List<UserForm> getUserForms(@VelocityCheck int companyID) {
    	List<UserForm> comList = select(logger,
				"SELECT form_id, company_id, formname, description, creation_date, change_date, active  " +
				"FROM userform_tbl WHERE company_id = ? ORDER BY lower(formname)",
				new UserForm_Light_RowMapper(), companyID);
    	return new ArrayList<>(comList);
    }

    @Override
	public List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID) {
		return select(logger, "SELECT form_id, formname FROM userform_tbl WHERE company_id = ? AND (startaction_id = ? OR endaction_id = ?) ORDER BY  LOWER(formname)",
				(resultSet, i) -> new Tuple<>(resultSet.getInt("form_id"), resultSet.getString("formname")),
				companyID, actionID, actionID);
	}
    
	@Override
	public UserForm getUserForm(int formID, @VelocityCheck int companyID) throws Exception {
		if (formID == 0 || companyID == 0) {
			return null;
		} else {
			String sql = "SELECT form_id, company_id, formName, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active "
					+ " FROM userform_tbl WHERE form_id = ? AND company_id = ?";
			List<UserForm> userFormList = select(logger, sql, new UserForm_RowMapper(), formID, companyID);
			if (userFormList == null || userFormList.size() < 1) {
				return null;
			} else if (userFormList.size() > 1) {
				throw new Exception("Invalid number of UserForm items found by id " + formID);
			} else {
				UserForm form = userFormList.get(0);
				form.setTrackableLinks(getUserFormTrackableLinks(formID, companyID));
				return form;
			}
		}
	}

	@Override
	public UserForm getUserFormByName(String name, @VelocityCheck int companyID) throws Exception {
		if (name == null || companyID == 0) {
			return null;
		} else {
			String sql = "SELECT form_id, company_id, formName, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active FROM userform_tbl WHERE formname = ? AND company_id = ?";

			List<UserForm> userFormList = select(logger, sql, new UserForm_RowMapper(), name, companyID);
			if (userFormList == null || userFormList.size() < 1) {
				return null;
			} else if (userFormList.size() > 1) {
				throw new Exception("Invalid number of UserForm items found by name " + name);
			} else {
				UserForm form = userFormList.get(0);
				form.setTrackableLinks(getUserFormTrackableLinks(form.getId(), companyID));
				return form;
			}
		}
	}

	@Override
	public String getUserFormName(int formId, @VelocityCheck int companyId) {
		if (formId > 0 && companyId > 0) {
			String sql = "SELECT formName FROM userform_tbl WHERE form_id = ? AND company_id = ?";
			return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("formName"), formId, companyId);
		} else {
			return null;
		}
	}

	@Override
	public Map<String, ComTrackableUserFormLink> getUserFormTrackableLinks(int formID, @VelocityCheck int companyID) {
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, relevance, deep_tracking FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND LOWER(full_url) != 'form'";

		List<ComTrackableUserFormLink> list = select(logger, sql, new ComTrackableUserFormLink_RowMapper(), formID, companyID);

		HashMap<String, ComTrackableUserFormLink> listTrackableUserFormLink = new HashMap<>();
		for (ComTrackableUserFormLink trackableUserFormLink : list) {
			List<LinkProperty> linkProperties = getUserFormTrackableLinkProperties(trackableUserFormLink);
			trackableUserFormLink.setProperties(linkProperties);
			
			listTrackableUserFormLink.put(trackableUserFormLink.getFullUrl(), trackableUserFormLink);
		}
		return listTrackableUserFormLink;
	}

	@Override
	public ComTrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(@VelocityCheck int companyID, int formID) throws Exception {
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "SELECT company_id, form_id, url_id, action_id, " + usage + ", full_url, shortname, relevance, deep_tracking FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ? AND LOWER(full_url) = 'form'";

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
	@DaoUpdateReturnValueCheck
	public int storeUserForm(UserForm userForm) throws Exception {
		if (getUserForm(userForm.getId(), userForm.getCompanyID()) != null) {
			String sql = "UPDATE userform_tbl SET formname = ?, description = ?, success_template = ?, error_template = ?, success_mimetype = ?, error_mimetype = ?, startaction_id = ?, endaction_id = ?, success_url = ?, error_url = ?, success_use_url = ?, error_use_url = ?, active = ?, change_date = CURRENT_TIMESTAMP WHERE form_id = ? AND company_id = ?";
			update(logger, sql, userForm.getFormName(), userForm.getDescription(), userForm.getSuccessTemplate(), userForm.getErrorTemplate(), userForm.getSuccessMimetype(), userForm.getErrorMimetype(), userForm.getStartActionID(), userForm.getEndActionID(), userForm.getSuccessUrl(), userForm.getErrorUrl(), userForm.isSuccessUseUrl() ? 1 : 0, userForm.isErrorUseUrl() ? 1 : 0, userForm.isActive() ? 1 : 0, userForm.getId(), userForm.getCompanyID());
			// add/update trackable links
			if (userForm.getTrackableLinks() != null) {
				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					link.setFormID(userForm.getId());
					storeUserFormTrackableLink(link);
				}
			}
		} else {
			if (isFormNameInUse(userForm.getFormName(), userForm.getId(), userForm.getCompanyID())) {
				throw new Exception("name of userform is already in use");
			}
			if (isOracleDB()) {
				userForm.setId(selectInt(logger, "SELECT userform_tbl_seq.NEXTVAL FROM DUAL"));
				String sql = "INSERT INTO userform_tbl (form_id, company_id, formname, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				update(logger, sql, userForm.getId(), userForm.getCompanyID(), userForm.getFormName(), userForm.getDescription(), userForm.getSuccessTemplate(), userForm.getErrorTemplate(), userForm.getSuccessMimetype(), userForm.getErrorMimetype(), userForm.getStartActionID(), userForm.getEndActionID(), userForm.getSuccessUrl(), userForm.getErrorUrl(), userForm.isSuccessUseUrl() ? 1 : 0, userForm.isErrorUseUrl() ? 1 : 0, userForm.isActive() ? 1 : 0);
			} else {
				int targetID = insertIntoAutoincrementMysqlTable(logger, "form_id", "INSERT INTO userform_tbl (company_id, formname, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					userForm.getCompanyID(),
					userForm.getFormName(),
					userForm.getDescription(),
					userForm.getSuccessTemplate(),
					userForm.getErrorTemplate(),
					userForm.getSuccessMimetype(),
					userForm.getErrorMimetype(),
					userForm.getStartActionID(),
					userForm.getEndActionID(),
					userForm.getSuccessUrl(),
					userForm.getErrorUrl(),
					userForm.isSuccessUseUrl() ? 1 : 0,
					userForm.isErrorUseUrl() ? 1 : 0,
					userForm.isActive() ? 1 : 0
				);
				userForm.setId(targetID);
			}

			// add trackable links
			if (userForm.getTrackableLinks() != null) {
				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					link.setFormID(userForm.getId());
					storeUserFormTrackableLink(link);
				}
			}
		}
		
		ComTrackableUserFormLink userFormStaticsticCountDummyLink = getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
		if (userFormStaticsticCountDummyLink == null) {
			userFormStaticsticCountDummyLink = new ComTrackableUserFormLinkImpl();
			userFormStaticsticCountDummyLink.setCompanyID(userForm.getCompanyID());
			userFormStaticsticCountDummyLink.setFormID(userForm.getId());
			userFormStaticsticCountDummyLink.setFullUrl("Form");
			storeUserFormTrackableLink(userFormStaticsticCountDummyLink);
		}
		return userForm.getId();
	}

	@Override
	public int updateActiveness(@VelocityCheck int companyId, Collection<Integer> formIds, boolean isActive) {
		if (CollectionUtils.isEmpty(formIds) || companyId <= 0) {
			return 0;
		}

    	String ids = StringUtils.join(formIds, ", ");
    	String query = String.format("UPDATE userform_tbl SET active = ? WHERE company_id = ? AND form_id IN (%s)", ids);

    	return update(logger, query, BooleanUtils.toInteger(isActive), companyId);
	}

	@Override
	public List<UserForm> getByIds(@VelocityCheck int companyId, Collection<Integer> formIds) {
		if (CollectionUtils.isEmpty(formIds) || companyId <= 0) {
			return Collections.emptyList();
		}

		String statement = "SELECT form_id, company_id, formname, description, creation_date, change_date, active" +
				" FROM userform_tbl" +
				String.format(" WHERE company_id = ? AND form_id IN (%s)", StringUtils.join(formIds, ','));

		return select(logger, statement, new UserForm_Light_RowMapper(), companyId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int storeUserFormTrackableLink(ComTrackableUserFormLink link) {
		if (link == null || link.getCompanyID() == 0) {
			return 0;
		} else {
			String usage = isOracleDB() ? "usage" : "`usage`";
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, relevance, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE form_id = ? AND company_id = ? AND full_url = ?";
			List<ComTrackableUserFormLink> existingLinks = select(logger, sql, new ComTrackableUserFormLink_RowMapper(), link.getFormID(), link.getCompanyID(), link.getFullUrl());
			if (existingLinks.size() > 0) {
				sql = "UPDATE rdir_url_userform_tbl SET action_id = ?, " + usage + " = ?, shortname = ?, relevance = ?, deep_tracking = ? WHERE form_id = ? AND company_id = ? AND full_url = ?";
				update(logger, sql, link.getActionID(), link.getUsage(), link.getShortname(), link.getRelevance(), link.getDeepTracking(), link.getFormID(), link.getCompanyID(), link.getFullUrl());

				if (existingLinks.size() == 1) {
					existingLinks.get(0).setProperties(link.getProperties());
					storeUserFormTrackableLinkProperties(existingLinks.get(0));
					
					return existingLinks.get(0).getId();
				} else {
					// TODO: This case should not occure so better throw an Exception
					for (ComTrackableUserFormLink existingLink : existingLinks) {
						existingLink.setProperties(link.getProperties());
						storeUserFormTrackableLinkProperties(existingLink);
					}
					
					return 0;
				}
			} else {
				if (isOracleDB()) {
					link.setId(selectInt(logger, "SELECT rdir_url_userform_tbl_seq.NEXTVAL FROM DUAL"));
					sql = "INSERT INTO rdir_url_userform_tbl (url_id, company_id, form_id, action_id, usage, full_url, shortname, relevance, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
					update(logger, sql, link.getId(), link.getCompanyID(), link.getFormID(), link.getActionID(), link.getUsage(), link.getFullUrl(), link.getShortname(), link.getRelevance(), link.getDeepTracking());
				} else {
					String insertStatement = "INSERT INTO rdir_url_userform_tbl (company_id, form_id, action_id, `usage`, full_url, shortname, relevance, deep_tracking) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

					Object[] paramsWithNext = new Object[8];
					paramsWithNext[0] = link.getCompanyID();
					paramsWithNext[1] = link.getFormID();
					paramsWithNext[2] = link.getActionID();
					paramsWithNext[3] = link.getUsage();
					paramsWithNext[4] = link.getFullUrl();
					paramsWithNext[5] = link.getShortname();
					paramsWithNext[6] = link.getRelevance();
					paramsWithNext[7] = link.getDeepTracking();
		
					int targetID = insertIntoAutoincrementMysqlTable(logger, "url_id", insertStatement, paramsWithNext);
					link.setId(targetID);
				}
				
				storeUserFormTrackableLinkProperties(link);
				
				return link.getId();
			}
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
			String sql = "SELECT url_id, form_id, action_id, " + usage + ", full_url, shortname, relevance, deep_tracking, company_id FROM rdir_url_userform_tbl WHERE url_id = ?";

			List<ComTrackableUserFormLink> existingLinks = select(logger, sql, new ComTrackableUserFormLink_RowMapper(), linkID);
			if (existingLinks == null || existingLinks.size() == 0) {
				trackableUserFormLink =  null;
			} else if (existingLinks.size() == 1) {
				trackableUserFormLink = existingLinks.get(0);
				List<LinkProperty> linkProperties = getUserFormTrackableLinkProperties(existingLinks.get(0));
				existingLinks.get(0).setProperties(linkProperties);
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
	@DaoUpdateReturnValueCheck
	public boolean deleteUserForm(int formID, @VelocityCheck int companyID) {
		if (formID == 0 || companyID == 0) {
			return false;
		} else {
			try {
				update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ?)", companyID, formID);
				update(logger, "DELETE FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ?", companyID, formID);
				int deletedEntries = update(logger, "DELETE FROM userform_tbl WHERE company_id = ? AND form_id = ?", companyID, formID);
				return deletedEntries == 1;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	@Override
	public boolean deleteUserFormByCompany(@VelocityCheck int companyID) {
		if (companyID == 0) {
			return false;
		} else {
			try {
				update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_userform_tbl WHERE company_id = ?)", companyID);
				update(logger, "DELETE FROM rdir_url_userform_tbl WHERE company_id = ?", companyID);
				update(logger, "DELETE FROM userform_tbl WHERE company_id = ?", companyID);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteUserFormTrackableLink(int linkID, @VelocityCheck int companyID) {
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
	
	/**
	 * Logs a click for trackable link in rdir_log_userform_tbl
	 * 
	 * @param link
	 *            the link which was clicked.
	 * @param customerID
	 *            the id of the recipient who clicked the link.
	 * @param remoteAddr
	 *            the ip address of the recipient.
	 * @return True on success.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean logUserFormTrackableLinkClickInDB(ComTrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass device_class, int device_id, int clientID) {
		if (logger.isDebugEnabled()) {
			logger.debug("logUserFormTrackableLinkClickInDB started - link id: " + link.getId() + " customer id: " + customerID + " remote address: " + remoteAddr);
		}
	
		String sql = "INSERT INTO rdirlog_userform_" + link.getCompanyID() + "_tbl (form_id, customer_id, url_id, company_id, ip_adr, mailing_id, device_class_id, device_id, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			update(logger, sql, link.getFormID(), customerID, link.getId(), link.getCompanyID(), remoteAddr, mailingID, device_class.getId(), device_id, clientID);
			
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
	@DaoUpdateReturnValueCheck
	public boolean logUserFormCallInDB(@VelocityCheck int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
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

	@Override
	public List<LinkProperty> getUserFormTrackableLinkProperties(ComTrackableUserFormLink trackableUserFormLink) {
		String sql = "SELECT param_type, param_key, param_value FROM rdir_url_userform_param_tbl WHERE url_id = ? ORDER BY param_type, param_key, param_value";
		List<LinkProperty> linkProperties = select(logger, sql, new ComTrackableUserFormLinkProperty_RowMapper(), trackableUserFormLink.getId());
		return linkProperties;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteUserFormTrackableLinkProperties(int linkID) {
		update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id = ?", linkID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void storeUserFormTrackableLinkProperties(ComTrackableUserFormLink link) {
		update(logger, "DELETE FROM rdir_url_userform_param_tbl WHERE url_id = ?", link.getId());
		if (link.getProperties() != null) {
			String insertSql = "INSERT INTO rdir_url_userform_param_tbl (url_id, param_type, param_key, param_value) VALUES(?, ?, ?, ?)";
			for (LinkProperty property : link.getProperties()) {
				update(logger, insertSql, link.getId(), property.getPropertyType().toString(), property.getPropertyName(), property.getPropertyValue());
			}
		}
	}

	private class UserForm_Light_RowMapper implements RowMapper<UserForm> {
		@Override
		public UserForm mapRow(ResultSet resultSet, int row) throws SQLException {
			UserForm readUserForm = new UserFormImpl();
			readUserForm.setId(resultSet.getInt("form_id"));
			readUserForm.setCompanyID(resultSet.getInt("company_id"));
			readUserForm.setFormName(resultSet.getString("formname"));
			readUserForm.setDescription(resultSet.getString("description"));
			readUserForm.setCreationDate(resultSet.getTimestamp("creation_date"));
			readUserForm.setChangeDate(resultSet.getTimestamp("change_date"));
			readUserForm.setActive(resultSet.getBoolean("active"));
			return readUserForm;
		}
	}

	private class UserForm_LightWithActionIDs_RowMapper extends UserForm_Light_RowMapper {
		@Override
		public UserForm mapRow(ResultSet resultSet, int row) throws SQLException {
			UserForm readUserForm = super.mapRow(resultSet, row);
			readUserForm.setStartActionID(resultSet.getInt("startaction_id"));
			readUserForm.setEndActionID(resultSet.getInt("endaction_id"));
			return readUserForm;
		}
	}

	private class UserForm_RowMapper implements RowMapper<UserForm> {
		@Override
		public UserForm mapRow(ResultSet resultSet, int row) throws SQLException {
			UserForm readUserForm = new UserFormImpl();
			readUserForm.setId(resultSet.getInt("form_id"));
			readUserForm.setCompanyID(resultSet.getInt("company_id"));
			readUserForm.setFormName(resultSet.getString("formname"));
			readUserForm.setDescription(resultSet.getString("description"));
			readUserForm.setSuccessTemplate(resultSet.getString("success_template"));
			readUserForm.setErrorTemplate(resultSet.getString("error_template"));
			readUserForm.setSuccessMimetype(resultSet.getString("success_mimetype"));
			readUserForm.setErrorMimetype(resultSet.getString("error_mimetype"));
			readUserForm.setStartActionID(resultSet.getInt("startaction_id"));
			readUserForm.setEndActionID(resultSet.getInt("endaction_id"));
			readUserForm.setSuccessUrl(resultSet.getString("success_url"));
			readUserForm.setErrorUrl(resultSet.getString("error_url"));
			readUserForm.setSuccessUseUrl(resultSet.getInt("success_use_url") != 0);
			readUserForm.setErrorUseUrl(resultSet.getInt("error_use_url") != 0);
			readUserForm.setActive(resultSet.getBoolean("active"));
			return readUserForm;
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
			trackableUserFormLink.setRelevance(resultSet.getInt("relevance"));
			trackableUserFormLink.setDeepTracking(resultSet.getInt("deep_tracking"));
			return trackableUserFormLink;
		}
	}

	private class ComTrackableUserFormLinkProperty_RowMapper implements RowMapper<LinkProperty> {
		@Override
		public LinkProperty mapRow(ResultSet resultSet, int row) throws SQLException {
			PropertyType type;
			try {
				type = PropertyType.parseString(resultSet.getString("param_type"));
			} catch (Exception e) {
				throw new SQLException("Error when reading properties param_type", e);
			}
			LinkProperty readProperty = new LinkProperty(type, resultSet.getString("param_key"), resultSet.getString("param_value"));
			return readProperty;
		}
	}

	@Override
	public boolean isFormNameInUse(String formName, int formId, int companyId) {
		String query = "SELECT count(*) FROM userform_tbl WHERE company_id = ? AND formname = ? AND form_id != ?";
		int count = selectInt(logger, query, companyId, formName, formId);

		return count > 0;
	}

	@Override
	public PaginatedListImpl<UserForm> getUserFormsWithActionIDs(String sortColumn, String sortDirection, int pageNumber,
																 int pageSize, Boolean activenessFilter, @VelocityCheck int companyID) {

		String sortClause;
		if (StringUtils.isBlank(sortColumn)) {
			sortClause = "ORDER BY LOWER(formname)";
		} else if ("changedate".equalsIgnoreCase(sortColumn)) {
			sortClause = "ORDER BY change_date";
		} else if ("creationdate".equalsIgnoreCase(sortColumn)) {
			sortClause = "ORDER BY creation_date";
		} else {
			sortClause = "ORDER BY " + sortColumn;
		}
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		sortClause += (sortDirectionAscending ? " ASC" : " DESC");

		List<Object> params = new ArrayList<>();
		params.add(companyID);

		String query = "SELECT form_id, company_id, formname, description, creation_date, change_date, startaction_id, endaction_id, active " +
				"FROM userform_tbl WHERE company_id = ?";

		if (Objects.nonNull(activenessFilter)){
			query += " AND active = ?";
			params.add(BooleanUtils.toInteger(activenessFilter));
		}

		return selectPaginatedListWithSortClause(logger, query, sortClause, sortColumn, sortDirectionAscending,
				pageNumber, pageSize, new UserForm_LightWithActionIDs_RowMapper(), params.toArray());
	}
	
	@Override
	public PaginatedListImpl<UserForm> getUserFormsWithActionIdsNew(String sortColumn, String sortDirection,
			int pageNumber, int pageSize, UserFormFilter filter, @VelocityCheck int companyID) {

		String sortClause;
		if (StringUtils.isBlank(sortColumn)) {
			sortClause = "ORDER BY LOWER(formname)";
		} else if ("changedate".equalsIgnoreCase(sortColumn)) {
			sortClause = "ORDER BY change_date";
		} else if ("creationdate".equalsIgnoreCase(sortColumn)) {
			sortClause = "ORDER BY creation_date";
		} else {
			sortClause = "ORDER BY " + sortColumn;
		}
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		sortClause += (sortDirectionAscending ? " ASC" : " DESC");

		List<Object> params = new ArrayList<>();
		params.add(companyID);

		String query = "SELECT form_id, company_id, formname, description, creation_date, change_date, startaction_id, endaction_id, active " +
				"FROM userform_tbl WHERE company_id = ?";

		if (filter != UserFormFilter.NONE){
			query += " AND active = ?";
			params.add(BooleanUtils.toInteger(UserFormFilter.ACTIVE == filter));
		}

		return selectPaginatedListWithSortClause(logger, query, sortClause, sortColumn, sortDirectionAscending,
				pageNumber, pageSize, new UserForm_LightWithActionIDs_RowMapper(), params.toArray());
	}
}
