/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.TrackableUserFormLinkImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class UserFormDaoImpl extends PaginatedBaseDaoImpl implements UserFormDao {

	private FormTrackableLinkDao trackableLinkDao;

	@Override
	public List<UserForm> overview(int companyID) {
		return getUserForms(companyID, true);
	}

	@Override
	public List<UserForm> getUserForms(int companyID) {
		return getUserForms(companyID, false);
	}

    private List<UserForm> getUserForms(int companyID, boolean includeDeleted) {
    	List<UserForm> forms = select(
				"SELECT form_id, company_id, formname, description, creation_date, change_date, active, startaction_id, endaction_id, deleted " +
				"FROM userform_tbl WHERE company_id = ? " +
					(includeDeleted ? "" : " AND deleted = 0") +
					" ORDER BY lower(formname)",
				new UserForm_Light_RowMapper(), companyID);
    	return new ArrayList<>(forms);
    }

    @Override
	public List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID) {
		return select("SELECT form_id, formname FROM userform_tbl WHERE company_id = ? AND deleted = 0 AND (startaction_id = ? OR endaction_id = ?) ORDER BY LOWER(formname)",
				(resultSet, i) -> new Tuple<>(resultSet.getInt("form_id"), resultSet.getString("formname")),
				companyID, actionID, actionID);
	}

	@Override
	public UserForm getUserForm(int formID, int companyID) {
		String sql = "SELECT form_id, company_id, formName, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active, success_builder_json, error_builder_json, creation_date, change_date "
				+ " FROM userform_tbl WHERE form_id = ? AND company_id = ? AND deleted = 0";
		return selectObjectDefaultNull(sql, new UserForm_RowMapper(), formID, companyID);
	}

	@Override
	public UserForm getUserFormByName(String name, int companyID) {
		if (name == null || companyID == 0) {
			return null;
		} else {
			String sql = "SELECT form_id, company_id, formName, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active, success_builder_json, error_builder_json, creation_date, change_date FROM userform_tbl WHERE formname = ? AND company_id = ? AND deleted = 0";

			List<UserForm> userFormList = select(sql, new UserForm_RowMapper(), name, companyID);
			if (userFormList == null || userFormList.isEmpty()) {
				return null;
			} else if (userFormList.size() > 1) {
				throw new IllegalStateException("Invalid number of UserForm items found by name " + name);
			} else {
				UserForm form = userFormList.get(0);
				form.setTrackableLinks(trackableLinkDao.getUserFormTrackableLinks(form.getId(), companyID));
				return form;
			}
		}
	}

	@Override
	public String getUserFormName(int formId, int companyId) {
		if (formId > 0 && companyId > 0) {
			String sql = "SELECT formName FROM userform_tbl WHERE form_id = ? AND company_id = ? AND deleted = 0";
			return selectObjectDefaultNull(sql, (rs, index) -> rs.getString("formName"), formId, companyId);
		} else {
			return null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int storeUserForm(UserForm userForm) {
		if (getUserForm(userForm.getId(), userForm.getCompanyID()) != null) {
			userForm.setChangeDate(new Date());
			String sql = "UPDATE userform_tbl SET formname = ?, description = ?, success_template = ?, error_template = ?, success_mimetype = ?, error_mimetype = ?, startaction_id = ?, endaction_id = ?, success_url = ?, error_url = ?, success_use_url = ?, error_use_url = ?, active = ?, change_date = ? WHERE form_id = ? AND company_id = ?";
			update(sql, userForm.getFormName(), userForm.getDescription(), userForm.getSuccessTemplate(), userForm.getErrorTemplate(), userForm.getSuccessMimetype(), userForm.getErrorMimetype(), userForm.getStartActionID(), userForm.getEndActionID(), userForm.getSuccessUrl(), userForm.getErrorUrl(), userForm.isSuccessUseUrl() ? 1 : 0, userForm.isErrorUseUrl() ? 1 : 0, userForm.isActive() ? 1 : 0, userForm.getChangeDate(), userForm.getId(), userForm.getCompanyID());
			// add/update trackable links
			if (userForm.getTrackableLinks() != null) {
				for (TrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					link.setFormID(userForm.getId());
					trackableLinkDao.saveUserFormTrackableLink(link.getFormID(), link.getCompanyID(), link);
				}
			}
		} else {
			if (isFormNameInUse(userForm.getFormName(), userForm.getId(), userForm.getCompanyID())) {
				throw new RuntimeException("name of userform is already in use");
			}

			userForm.setCreationDate(new Date());
			userForm.setChangeDate(new Date());
			
			if (isOracleDB()) {
				userForm.setId(selectInt("SELECT userform_tbl_seq.NEXTVAL FROM DUAL"));
				String sql = "INSERT INTO userform_tbl (form_id, company_id, formname, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active, creation_date, change_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				update(sql, userForm.getId(), userForm.getCompanyID(), userForm.getFormName(), userForm.getDescription(), userForm.getSuccessTemplate(), userForm.getErrorTemplate(), userForm.getSuccessMimetype(), userForm.getErrorMimetype(), userForm.getStartActionID(), userForm.getEndActionID(), userForm.getSuccessUrl(), userForm.getErrorUrl(), userForm.isSuccessUseUrl() ? 1 : 0, userForm.isErrorUseUrl() ? 1 : 0, userForm.isActive() ? 1 : 0, userForm.getCreationDate(), userForm.getChangeDate());
			} else {
				int targetID = insertIntoAutoincrementMysqlTable("form_id", "INSERT INTO userform_tbl (company_id, formname, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active, creation_date, change_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
					userForm.isActive() ? 1 : 0,
					userForm.getCreationDate(),
					userForm.getChangeDate()
				);
				userForm.setId(targetID);
			}

			// add trackable links
			if (userForm.getTrackableLinks() != null) {
				for (TrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					link.setFormID(userForm.getId());
					trackableLinkDao.saveUserFormTrackableLink(link.getFormID(), link.getCompanyID(), link);
				}
			}
		}
		
		TrackableUserFormLink dummyLink =
				trackableLinkDao.getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
		if (dummyLink == null) {
			dummyLink = new TrackableUserFormLinkImpl();
			dummyLink.setCompanyID(userForm.getCompanyID());
			dummyLink.setFormID(userForm.getId());
			dummyLink.setFullUrl("Form");
			trackableLinkDao.saveUserFormTrackableLink(dummyLink.getFormID(), dummyLink.getCompanyID(), dummyLink);
		}
		return userForm.getId();
	}
	
	@Override
	public int createUserForm(int companyId, UserForm userForm) {
		userForm.setCreationDate(new Date());
		userForm.setChangeDate(new Date());
		
    	int userFormId;
		if (isOracleDB()) {
			userFormId = selectInt("SELECT userform_tbl_seq.NEXTVAL FROM DUAL");
			String sql = "INSERT INTO userform_tbl ("
					+ "form_id, "
					+ "company_id, "
					+ "formname, "
					+ "description, "
					+ "success_template, error_template, "
					+ "success_mimetype, error_mimetype, "
					+ "startaction_id, endaction_id, "
					+ "success_url, error_url, "
					+ "success_use_url, "
					+ "error_use_url, "
					+ "active, "
					+ "success_builder_json, error_builder_json, "
					+ "creation_date, change_date) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			update(sql,
					userFormId,
					companyId,
					userForm.getFormName(),
					userForm.getDescription(),
					userForm.getSuccessTemplate(), userForm.getErrorTemplate(),
					userForm.getSuccessMimetype(), userForm.getErrorMimetype(),
					userForm.getStartActionID(), userForm.getEndActionID(),
					userForm.getSuccessUrl(), userForm.getErrorUrl(),
					BooleanUtils.toInteger(userForm.isSuccessUseUrl()),
					BooleanUtils.toInteger(userForm.isErrorUseUrl()),
					BooleanUtils.toInteger(userForm.isActive()),
					userForm.getSuccessFormBuilderJson(), userForm.getErrorFormBuilderJson(),
					userForm.getCreationDate(), userForm.getChangeDate()
			);
		} else {
			userFormId = insertIntoAutoincrementMysqlTable("form_id",
					"INSERT INTO userform_tbl ("
							+ "company_id, "
							+ "formname, "
							+ "description, "
							+ "success_template, error_template, "
							+ "success_mimetype, error_mimetype, "
							+ "startaction_id, endaction_id, "
							+ "success_url, error_url, "
							+ "success_use_url, "
							+ "error_use_url, "
							+ "active, "
							+ "success_builder_json, error_builder_json, "
							+ "creation_date, change_date) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				companyId,
				userForm.getFormName(),
				userForm.getDescription(),
				userForm.getSuccessTemplate(), userForm.getErrorTemplate(),
				userForm.getSuccessMimetype(), userForm.getErrorMimetype(),
				userForm.getStartActionID(), userForm.getEndActionID(),
				userForm.getSuccessUrl(), userForm.getErrorUrl(),
				BooleanUtils.toInteger(userForm.isSuccessUseUrl()),
				BooleanUtils.toInteger(userForm.isErrorUseUrl()),
				BooleanUtils.toInteger(userForm.isActive()),
				userForm.getSuccessFormBuilderJson(), userForm.getErrorFormBuilderJson(),
				userForm.getCreationDate(), userForm.getChangeDate()
			);
		}
		return userFormId;
	}
	
	@Override
	public void updateUserForm(int companyId, UserForm userForm) {
		userForm.setChangeDate(new Date());
		
		String sql = "UPDATE userform_tbl SET formname = ?, description = ?, "
				+ "success_template = ?, error_template = ?, "
				+ "success_mimetype = ?, error_mimetype = ?, "
				+ "startaction_id = ?, endaction_id = ?, "
				+ "success_url = ?, error_url = ?, "
				+ "success_use_url = ?, error_use_url = ?, "
				+ "active = ?, "
				+ "success_builder_json = ?, error_builder_json = ?, "
				+ "change_date = ? WHERE form_id = ? AND company_id = ?";
		update(sql, userForm.getFormName(), userForm.getDescription(),
					userForm.getSuccessTemplate(), userForm.getErrorTemplate(),
					userForm.getSuccessMimetype(), userForm.getErrorMimetype(),
					userForm.getStartActionID(), userForm.getEndActionID(),
					userForm.getSuccessUrl(), userForm.getErrorUrl(),
					BooleanUtils.toInteger(userForm.isSuccessUseUrl()),
					BooleanUtils.toInteger(userForm.isErrorUseUrl()),
					BooleanUtils.toInteger(userForm.isActive()),
					userForm.getSuccessFormBuilderJson(), userForm.getErrorFormBuilderJson(),
					userForm.getChangeDate(),
					userForm.getId(), companyId);
	}
	
	@Override
	public int updateActiveness(int companyId, Collection<Integer> formIds, boolean isActive) {
		if (CollectionUtils.isEmpty(formIds) || companyId <= 0) {
			return 0;
		}

    	String query = "UPDATE userform_tbl SET active = ?, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND"
				+ makeBulkInClauseForInteger("form_id", formIds);

    	return update(query, BooleanUtils.toInteger(isActive), companyId);
	}

	@Override
	public List<UserForm> getByIds(int companyId, Collection<Integer> formIds) {
		if (CollectionUtils.isEmpty(formIds) || companyId <= 0) {
			return Collections.emptyList();
		}

		String statement = "SELECT form_id, company_id, formname, description, creation_date, change_date, active, startaction_id, endaction_id" +
				" FROM userform_tbl" +
				String.format(" WHERE company_id = ? AND deleted = 0 AND form_id IN (%s)", StringUtils.join(formIds, ','));

		return select(statement, new UserForm_Light_RowMapper(), companyId);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteUserForm(int formID, int companyID) {
		if (formID == 0 || companyID == 0) {
			return false;
		} else {
			try {
				update("DELETE FROM rdir_url_userform_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ?)", companyID, formID);
				update("DELETE FROM rdir_url_userform_tbl WHERE company_id = ? AND form_id = ?", companyID, formID);
				int deletedEntries = update("DELETE FROM userform_tbl WHERE company_id = ? AND form_id = ?", companyID, formID);
				return deletedEntries == 1;
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public void markDeleted(int formId, int companyId) {
		update("UPDATE userform_tbl SET active = 0, deleted = 1, change_date = CURRENT_TIMESTAMP"
			+ " WHERE form_id = ? AND company_id = ?", formId, companyId);
	}

	@Override
	public void restore(Set<Integer> ids, int companyId) {
		update("UPDATE userform_tbl SET deleted = 0, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND "
			+ makeBulkInClauseForInteger("form_id", ids), companyId);
	}

	@Override
	public List<Integer> getMarkedAsDeletedBefore(Date date, int companyId) {
		return select(
			"SELECT form_id FROM userform_tbl WHERE company_id = ? AND deleted = 1 AND change_date < ?",
			IntegerRowMapper.INSTANCE, companyId, date);
	}

	@Override
	public boolean deleteUserFormByCompany(int companyID) {
		if (companyID == 0) {
			return false;
		} else {
			try {
				update("DELETE FROM rdir_url_userform_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_userform_tbl WHERE company_id = ?)", companyID);
				update("DELETE FROM rdir_url_userform_tbl WHERE company_id = ?", companyID);
				update("DELETE FROM userform_tbl WHERE company_id = ?", companyID);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	private static class UserForm_Light_RowMapper implements RowMapper<UserForm> {
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
			readUserForm.setStartActionID(resultSet.getInt("startaction_id"));
			readUserForm.setEndActionID(resultSet.getInt("endaction_id"));
			if (DbUtilities.resultsetHasColumn(resultSet, "deleted")) {
				readUserForm.setDeleted(resultSet.getBoolean("deleted"));
			}
			return readUserForm;
		}
	}

	private static class UserForm_RowMapper implements RowMapper<UserForm> {
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
			readUserForm.setSuccessFormBuilderJson(resultSet.getString("success_builder_json"));
			readUserForm.setErrorFormBuilderJson(resultSet.getString("error_builder_json"));
			readUserForm.setCreationDate(resultSet.getTimestamp("creation_date"));
			readUserForm.setChangeDate(resultSet.getTimestamp("change_Date"));
			return readUserForm;
		}
	}

	@Override
	public boolean isFormNameInUse(String formName, int formId, int companyId) {
		String query = "SELECT COUNT(*) FROM userform_tbl WHERE company_id = ? AND formname = ? AND form_id != ?";
		int count = selectInt(query, companyId, formName, formId);

		return count > 0;
	}

	@Override
	public boolean existsUserForm(int companyId, int userFormId) {
		return selectInt("SELECT COUNT(*) FROM userform_tbl WHERE company_id = ? AND deleted = 0 AND form_id = ?", companyId, userFormId) > 0;
	}

	@Override
	public boolean isActive(int formId) {
		return selectIntWithDefaultValue("SELECT active FROM userform_tbl WHERE form_id = ? AND deleted = 0", 0, formId) > 0;
	}

	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}
}
