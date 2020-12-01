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
import java.util.List;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.commons.ActivenessStatus;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;

public class UserFormDaoImpl extends PaginatedBaseDaoImpl implements UserFormDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormDaoImpl.class);

	private FormTrackableLinkDao trackableLinkDao;

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
		return select(logger, "SELECT form_id, formname FROM userform_tbl WHERE company_id = ? AND (startaction_id = ? OR endaction_id = ?) ORDER BY LOWER(formname)",
				(resultSet, i) -> new Tuple<>(resultSet.getInt("form_id"), resultSet.getString("formname")),
				companyID, actionID, actionID);
	}

    @Override
	public List<Tuple<Integer, String>> getImportNamesByActionID(int companyID, int actionID) {
		return select(logger, "SELECT id, shortname FROM import_profile_tbl WHERE company_id = ? AND action_for_new_recipients = ? ORDER BY LOWER(shortname)",
				(resultSet, i) -> new Tuple<>(resultSet.getInt("id"), resultSet.getString("shortname")),
				companyID, actionID);
	}

	@Override
	public UserForm getUserForm(int formID, @VelocityCheck int companyID) {
		String sql = "SELECT form_id, company_id, formName, description, success_template, error_template, success_mimetype, error_mimetype, startaction_id, endaction_id, success_url, error_url, success_use_url, error_use_url, active "
				+ " FROM userform_tbl WHERE form_id = ? AND company_id = ?";
		try {
			return selectObject(logger, sql, new UserForm_RowMapper(), formID, companyID);
		} catch (EmptyResultDataAccessException e) {
            logger.error("User form not found", e);
			return null;
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
				form.setTrackableLinks(trackableLinkDao.getUserFormTrackableLinks(form.getId(), companyID));
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
	@DaoUpdateReturnValueCheck
	public int storeUserForm(UserForm userForm) throws Exception {
		if (getUserForm(userForm.getId(), userForm.getCompanyID()) != null) {
			String sql = "UPDATE userform_tbl SET formname = ?, description = ?, success_template = ?, error_template = ?, success_mimetype = ?, error_mimetype = ?, startaction_id = ?, endaction_id = ?, success_url = ?, error_url = ?, success_use_url = ?, error_use_url = ?, active = ?, change_date = CURRENT_TIMESTAMP WHERE form_id = ? AND company_id = ?";
			update(logger, sql, userForm.getFormName(), userForm.getDescription(), userForm.getSuccessTemplate(), userForm.getErrorTemplate(), userForm.getSuccessMimetype(), userForm.getErrorMimetype(), userForm.getStartActionID(), userForm.getEndActionID(), userForm.getSuccessUrl(), userForm.getErrorUrl(), userForm.isSuccessUseUrl() ? 1 : 0, userForm.isErrorUseUrl() ? 1 : 0, userForm.isActive() ? 1 : 0, userForm.getId(), userForm.getCompanyID());
			// add/update trackable links
			if (userForm.getTrackableLinks() != null) {
				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					link.setFormID(userForm.getId());
					trackableLinkDao.saveUserFormTrackableLink(link.getFormID(), link.getCompanyID(), link);
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
					trackableLinkDao.saveUserFormTrackableLink(link.getFormID(), link.getCompanyID(), link);
				}
			}
		}
		
		ComTrackableUserFormLink dummyLink =
				trackableLinkDao.getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
		if (dummyLink == null) {
			dummyLink = new ComTrackableUserFormLinkImpl();
			dummyLink.setCompanyID(userForm.getCompanyID());
			dummyLink.setFormID(userForm.getId());
			dummyLink.setFullUrl("Form");
			trackableLinkDao.saveUserFormTrackableLink(dummyLink.getFormID(), dummyLink.getCompanyID(), dummyLink);
		}
		return userForm.getId();
	}
	
	@Override
	public int createUserForm(@VelocityCheck int companyId, UserForm userForm) {
    	int userFormId;
		if (isOracleDB()) {
			userFormId = selectInt(logger, "SELECT userform_tbl_seq.NEXTVAL FROM DUAL");
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
					+ "active) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			update(logger, sql,
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
					BooleanUtils.toInteger(userForm.isActive())
			);
		} else {
			userFormId = insertIntoAutoincrementMysqlTable(logger, "form_id",
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
							+ "active) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				companyId,
				userForm.getFormName(),
				userForm.getDescription(),
				userForm.getSuccessTemplate(), userForm.getErrorTemplate(),
				userForm.getSuccessMimetype(), userForm.getErrorMimetype(),
				userForm.getStartActionID(), userForm.getEndActionID(),
				userForm.getSuccessUrl(), userForm.getErrorUrl(),
				BooleanUtils.toInteger(userForm.isSuccessUseUrl()),
				BooleanUtils.toInteger(userForm.isErrorUseUrl()),
				BooleanUtils.toInteger(userForm.isActive())
			);
		}
		return userFormId;
	}
	
	@Override
	public void updateUserForm(@VelocityCheck int companyId, UserForm userForm) {
		String sql = "UPDATE userform_tbl SET formname = ?, description = ?, "
				+ "success_template = ?, error_template = ?, "
				+ "success_mimetype = ?, error_mimetype = ?, "
				+ "startaction_id = ?, endaction_id = ?, "
				+ "success_url = ?, error_url = ?, "
				+ "success_use_url = ?, error_use_url = ?, "
				+ "active = ?, "
				+ "change_date = CURRENT_TIMESTAMP WHERE form_id = ? AND company_id = ?";
		update(logger, sql, userForm.getFormName(), userForm.getDescription(),
					userForm.getSuccessTemplate(), userForm.getErrorTemplate(),
					userForm.getSuccessMimetype(), userForm.getErrorMimetype(),
					userForm.getStartActionID(), userForm.getEndActionID(),
					userForm.getSuccessUrl(), userForm.getErrorUrl(),
					BooleanUtils.toInteger(userForm.isSuccessUseUrl()),
					BooleanUtils.toInteger(userForm.isErrorUseUrl()),
					BooleanUtils.toInteger(userForm.isActive()),
					userForm.getId(), companyId);
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

	@Override
	public boolean isFormNameInUse(String formName, int formId, int companyId) {
		String query = "SELECT count(*) FROM userform_tbl WHERE company_id = ? AND formname = ? AND form_id != ?";
		int count = selectInt(logger, query, companyId, formName, formId);

		return count > 0;
	}

	@Override
	public PaginatedListImpl<UserForm> getUserFormsWithActionIdsNew(String sortColumn, String sortDirection,
																	int pageNumber, int pageSize, ActivenessStatus filter, @VelocityCheck int companyID) {

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

		if (filter != ActivenessStatus.NONE){
			query += " AND active = ?";
			params.add(BooleanUtils.toInteger(ActivenessStatus.ACTIVE == filter));
		}

		return selectPaginatedListWithSortClause(logger, query, sortClause, sortColumn, sortDirectionAscending,
				pageNumber, pageSize, new UserForm_LightWithActionIDs_RowMapper(), params.toArray());
	}
	
	@Override
	public boolean existsUserForm(int copmanyId, int userFormId) {
		return selectInt(logger, "SELECT COUNT(*) FROM userform_tbl WHERE company_id = ? AND form_id = ?", copmanyId, userFormId) > 0;
	}

	@Required
	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}
}
