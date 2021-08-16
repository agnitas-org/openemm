/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.wsmanager.common.UnknownWebserviceUsernameException;
import com.agnitas.emm.wsmanager.common.WebserviceUser;
import com.agnitas.emm.wsmanager.common.WebserviceUserCredential;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.common.WebserviceUserListItem;
import com.agnitas.emm.wsmanager.common.impl.WebserviceUserImpl;
import com.agnitas.emm.wsmanager.common.impl.WebserviceUserListItemImpl;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDaoException;
import com.agnitas.emm.wsmanager.service.WebserviceUserAlreadyExistsException;

/**
 * Implementation of {@link WebserviceUserDao} interface.
 */
public class WebserviceUserDaoImpl extends PaginatedBaseDaoImpl implements WebserviceUserDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(WebserviceUserDaoImpl.class);

	/** Row mapper for list items. */
	private static final WebserviceUserListItemRowMapper LIST_ITEM_ROWMAPPER = new WebserviceUserListItemRowMapper();

	/** Row mapper for webservice user. */
	private static final WebserviceUserRowMapper USER_ROWMAPPER = new WebserviceUserRowMapper();

	private static final String WS_USER_TBL_NAME = "webservice_user_tbl";
	
	private static final List<String> SORTABLE_COLUMNS = Arrays.asList("company_id", "default_data_source_id", "active");
	
	private static final String DEFAULT_SORT_COLUMN = "username";

	@Override
	public PaginatedListImpl<WebserviceUserListItem> getWebserviceUserList(int companyID, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException {
		if(!SORTABLE_COLUMNS.contains(sortColumn)) {
			sortColumn = DEFAULT_SORT_COLUMN;
		}

		try {
			return selectPaginatedList(logger, "SELECT username, company_id, default_data_source_id, active FROM webservice_user_tbl WHERE company_id = ?", WS_USER_TBL_NAME, sortColumn, sortDirectionAscending, pageNumber, pageSize, LIST_ITEM_ROWMAPPER, companyID);
		} catch (Exception e) {
			logger.error("Error listing webservice user", e);
			throw new WebserviceUserDaoException("Error listing webservice user", e);
		}
	}
	
	@Override
	public PaginatedListImpl<WebserviceUserListItem> getWebserviceUserMasterList(String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException {
		if(!SORTABLE_COLUMNS.contains(sortColumn)) {
			sortColumn = DEFAULT_SORT_COLUMN;
		}

		try {
			return selectPaginatedList(logger, "SELECT username, company_id, default_data_source_id, active FROM webservice_user_tbl", WS_USER_TBL_NAME, sortColumn, sortDirectionAscending, pageNumber, pageSize, LIST_ITEM_ROWMAPPER);
		} catch (Exception e) {
			logger.error("Error listing webservice user", e);
			throw new WebserviceUserDaoException("Error listing webservice user", e);
		}
	}

	@Override
	public WebserviceUser getWebserviceUser(final String username) throws WebserviceUserException, WebserviceUserDaoException {
		WebserviceUser webserviceUser = selectObjectDefaultNull(logger, "SELECT * FROM webservice_user_tbl WHERE username = ?", USER_ROWMAPPER, username);
		
		if (webserviceUser != null) {
			webserviceUser.setGrantedPermissions(loadGrantedPermissions(username));
			webserviceUser.setGrantedPermissionGroupIDs(loadGrantedPermissionGroups(username));
			return webserviceUser;
		} else {
			throw new UnknownWebserviceUsernameException(username);
		}
	}

	@Override
	public void updateUser(final WebserviceUser user) throws WebserviceUserDaoException, WebserviceUserException {
		try {
			int count = update(logger, "UPDATE webservice_user_tbl SET active = ?, contact_info = ?, contact_email = ? WHERE username = ?", user.isActive(), user.getContact(), user.getContactEmail(), user.getUsername());
			if (count == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Unknown webservice user '" + user.getUsername() + "'");
				}
				throw new UnknownWebserviceUsernameException(user.getUsername());
			}
		} catch (Exception e) {
			logger.error("Error updating webservice user '" + user.getUsername() + "*'", e);
			throw new WebserviceUserDaoException("Error updating webservice user '" + user.getUsername() + "*'", e);
		}
	}

	@Override
	public void updatePasswordHash(final String username, final String passwordHash) throws WebserviceUserException, WebserviceUserDaoException {
		try {
			int rows = update(logger, "UPDATE webservice_user_tbl SET password_encrypted = ? WHERE username = ?", passwordHash, username);
			if (rows == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Unknown webservice user '" + username + "'");
				}
				throw new UnknownWebserviceUsernameException(username);
			}
		} catch (Exception e) {
			logger.error("Error updating password hash for webservice user '" + username + "'", e);
			throw new WebserviceUserDaoException("Error updating password hash for webservice user '" + username + "'", e);
		}
	}

	@Override
	public boolean webserviceUserExists(final String username) {
		int count = selectInt(logger, "SELECT COUNT(*) FROM webservice_user_tbl WHERE username = ?", username);
		return count > 0;
	}

	@Override
	public void createWebserviceUser(WebserviceUserCredential user, int dataSourceId, int bulkSizeLimit) throws WebserviceUserException, WebserviceUserDaoException {
		String username = user.getUsername();
		if (webserviceUserExists(username)) {
			throw new WebserviceUserAlreadyExistsException(username);
		}

		try {
			update(logger, "INSERT INTO webservice_user_tbl (username, company_id, default_data_source_id, bulk_size_limit, password_encrypted, active, contact_email) VALUES (?,?,?,?,?,1,?)",
					username, user.getCompanyID(), user.getDefaultDatasourceID(), bulkSizeLimit, user.getPasswordHash(), user.getContactEmail());
		} catch(Exception e) {
			logger.error("Error creating new webservice user", e);

			throw new WebserviceUserDaoException("Error creating new webservice user", e);
		}
	}
	
	public static class WebserviceUserRowMapper implements RowMapper<WebserviceUser> {

		@Override
		public WebserviceUser mapRow(ResultSet resultSet, int row) throws SQLException {
			WebserviceUserImpl user = new WebserviceUserImpl();
			
			user.setCompanyID(resultSet.getInt("company_id"));
			user.setDefaultDatasourceID(resultSet.getInt("default_data_source_id"));
			user.setUsername(resultSet.getString("username"));
			user.setActive(resultSet.getBoolean("active"));
			user.setContact(resultSet.getString("contact_info"));
			user.setContactEmail(resultSet.getString("contact_email"));
			
			return user;
		}
	
	}

	public static class WebserviceUserListItemRowMapper implements RowMapper<WebserviceUserListItem> {
	
		@Override
		public WebserviceUserListItem mapRow(ResultSet resultSet, int row) throws SQLException {
			WebserviceUserListItemImpl user = new WebserviceUserListItemImpl();
	
			user.setCompanyID(resultSet.getInt("company_id"));
			user.setDefaultDatasourceID(resultSet.getInt("default_data_source_id"));
			user.setUsername(resultSet.getString("username"));
			user.setActive(resultSet.getBoolean("active"));
	
			return user;
		}
	
	}

	@Override
	public int getNumberOfWebserviceUsers() {
		return selectInt(logger, "SELECT COUNT(*) FROM webservice_user_tbl");
	}
	
	private final Set<String> loadGrantedPermissions(final String username) {
		final String sql = "SELECT endpoint FROM webservice_permission_tbl WHERE username=?";
		
		return new HashSet<>(select(logger, sql, new StringRowMapper(), username));
	}
	
	private final Set<Integer> loadGrantedPermissionGroups(final String username) {
		final String sql = "SELECT group_ref FROM webservice_user_group_tbl WHERE username=?";
		
		return new HashSet<>(select(logger, sql, new IntegerRowMapper(), username));
	}

	@Override
	public final void saveGrantedPermissionsAndGroups(final WebserviceUser user) {
		saveGrantedPermissions(user);
		saveGrantedPermissionGroups(user);
	}
	
	private final void saveGrantedPermissions(final WebserviceUser user) {
		final String deletePermissions = "DELETE FROM webservice_permission_tbl WHERE username=?";
		this.update(logger, deletePermissions, user.getUsername());
		
		final String insertPermission = "INSERT INTO webservice_permission_tbl (username, endpoint) VALUES (?,?)";
		for(final String permission : user.getGrantedPermissions()) {
			this.update(logger, insertPermission, user.getUsername(), permission);
		}
	}
	
	private final void saveGrantedPermissionGroups(final WebserviceUser user) {
		final String deleteGroupsSql = "DELETE FROM webservice_user_group_tbl WHERE username=?";
		this.update(logger, deleteGroupsSql, user.getUsername());
		
		final String insertGroups = "INSERT INTO webservice_user_group_tbl (username, group_ref) VALUES (?,?)";
		for(final int groupID : user.getGrantedPermissionGroupIDs()) {
			this.update(logger, insertGroups, user.getUsername(), groupID);
		}
	}

	@Override
	public final void updateLastLoginDate(final String username, final ZonedDateTime loginDate) {
		final String sql = "UPDATE webservice_user_tbl SET last_login_date=? WHERE username=?";
		
		update(logger, sql, Date.from(loginDate.toInstant()), username);
	}
}
