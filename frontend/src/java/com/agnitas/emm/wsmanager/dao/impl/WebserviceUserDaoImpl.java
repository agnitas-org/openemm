/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserOverviewFilter;
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
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implementation of {@link WebserviceUserDao} interface.
 */
public class WebserviceUserDaoImpl extends PaginatedBaseDaoImpl implements WebserviceUserDao {

	private static final String COMPANY_ID_COLUMN = "company_id";
	private static final String DEFAULT_DATA_SOURCE_ID_COLUMN = "default_data_source_id";
	private static final String ACTIVE_COLUMN = "active";
	private static final String USERNAME_COLUMN = "username";
	private static final String COMPANY_NAME_COLUMN = "company_name";
	private static final String DEFAULT_SORT_COLUMN = USERNAME_COLUMN;
	private static final String LIST_ERROR = "Error listing webservice user";
	private static final String WS_USER_TBL_NAME = "webservice_user_tbl";

	private static final List<String> SORTABLE_COLUMNS = List.of(
		COMPANY_ID_COLUMN,
		DEFAULT_DATA_SOURCE_ID_COLUMN,
		ACTIVE_COLUMN);

	private static final RowMapper<WebserviceUserImpl> USER_ROWMAPPER = (rs, i) -> {
		WebserviceUserImpl user = new WebserviceUserImpl();
		user.setCompanyID(rs.getInt(COMPANY_ID_COLUMN));
		user.setDefaultDatasourceID(rs.getInt(DEFAULT_DATA_SOURCE_ID_COLUMN));
		user.setUsername(rs.getString(USERNAME_COLUMN));
		user.setActive(rs.getBoolean(ACTIVE_COLUMN));
		user.setContactEmail(rs.getString("contact_email"));
		return user;
	};

	private static final RowMapper<WebserviceUserListItem> LIST_ITEM_ROWMAPPER = (rs, i) -> {
		WebserviceUserListItemImpl user = new WebserviceUserListItemImpl();
		user.setCompanyID(rs.getInt(COMPANY_ID_COLUMN));
		user.setDefaultDatasourceID(rs.getInt(DEFAULT_DATA_SOURCE_ID_COLUMN));
		user.setUsername(rs.getString(USERNAME_COLUMN));
		user.setActive(rs.getBoolean(ACTIVE_COLUMN));

		if (DbUtilities.resultsetHasColumn(rs, COMPANY_NAME_COLUMN)) {
			user.setClientName(rs.getString(COMPANY_NAME_COLUMN));
		}

		return user;
	};

	@Override
	public PaginatedList<WebserviceUserListItem> getWebserviceUserList(int companyID, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException {
		if(!SORTABLE_COLUMNS.contains(sortColumn)) {
			sortColumn = DEFAULT_SORT_COLUMN;
		}

		try {
			return selectPaginatedList("SELECT username, company_id, default_data_source_id, active FROM webservice_user_tbl WHERE company_id = ?", WS_USER_TBL_NAME, sortColumn, sortDirectionAscending, pageNumber, pageSize, LIST_ITEM_ROWMAPPER, companyID);
		} catch (Exception e) {
			logger.error(LIST_ERROR, e);
			throw new WebserviceUserDaoException(LIST_ERROR, e);
		}
	}

	@Override
	public PaginatedList<WebserviceUserListItem> getWebserviceUserList(WebserviceUserOverviewFilter filter) throws WebserviceUserDaoException {
		String sortColumn = filter.getSortOrDefault(DEFAULT_SORT_COLUMN);

		StringBuilder query = new StringBuilder("SELECT w.username, w.company_id, w.default_data_source_id, w.active, c.shortname AS company_name FROM webservice_user_tbl w JOIN company_tbl c ON c.company_id = w.company_id");
		List<Object> params = applyOverviewFilter(filter, query);

		try {
			PaginatedList<WebserviceUserListItem> list;
			if (sortColumn.equals(COMPANY_NAME_COLUMN)) {
				String sortClause = "ORDER BY LOWER(c.shortname) " + (filter.ascending() ? "ASC" : "DESC");
				list = selectPaginatedListWithSortClause(query.toString(), sortClause, sortColumn, filter.ascending(),
						filter.getPage(), filter.getNumberOfRows(), LIST_ITEM_ROWMAPPER, params.toArray());
			} else {
				list = selectPaginatedList(query.toString(), WS_USER_TBL_NAME, sortColumn, filter.ascending(), filter.getPage(),
						filter.getNumberOfRows(), LIST_ITEM_ROWMAPPER, params.toArray());
			}

			if (filter.isUiFiltersSet()) {
				list.setNotFilteredFullListSize(selectInt("SELECT COUNT(*) FROM webservice_user_tbl w JOIN company_tbl c ON c.company_id = w.company_id"));
			}

			return list;
		} catch (Exception e) {
			logger.error(LIST_ERROR, e);
			throw new WebserviceUserDaoException(LIST_ERROR, e);
		}
	}

	private List<Object> applyOverviewFilter(WebserviceUserOverviewFilter filter, StringBuilder query) {
		List<Object> params = new ArrayList<>();

		if (filter.getCompanyId() != null) {
			query.append(" WHERE w.company_id = ?");
			params.add(filter.getCompanyId());
		} else {
			query.append(" WHERE 1=1");
		}

		if (filter.getStatus() != null) {
			query.append(" AND w.active = ?");
			params.add(filter.getStatus());
		}

		if (StringUtils.isNotBlank(filter.getUsername())) {
			query.append(getPartialSearchFilterWithAnd("w.username"));
			params.add(filter.getUsername());
		}

		if (filter.getDefaultDataSourceId() != null) {
			query.append(" AND w.default_data_source_id = ?");
			params.add(filter.getDefaultDataSourceId());
		}

		return params;
	}

	@Override
	public PaginatedList<WebserviceUserListItem> getWebserviceUserMasterList(String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException {
		if(!SORTABLE_COLUMNS.contains(sortColumn)) {
			sortColumn = DEFAULT_SORT_COLUMN;
		}

		try {
			return selectPaginatedList("SELECT username, company_id, default_data_source_id, active FROM webservice_user_tbl", WS_USER_TBL_NAME, sortColumn, sortDirectionAscending, pageNumber, pageSize, LIST_ITEM_ROWMAPPER);
		} catch (Exception e) {
			logger.error(LIST_ERROR, e);
			throw new WebserviceUserDaoException(LIST_ERROR, e);
		}
	}

	@Override
	public WebserviceUser getWebserviceUser(String username) throws WebserviceUserException {
		WebserviceUser webserviceUser = selectObjectDefaultNull("SELECT * FROM webservice_user_tbl WHERE username = ?", USER_ROWMAPPER, username);
		
		if (webserviceUser != null) {
			webserviceUser.setGrantedPermissions(loadGrantedPermissions(username));
			webserviceUser.setGrantedPermissionGroupIDs(loadGrantedPermissionGroups(username));
			return webserviceUser;
		} else {
			throw new UnknownWebserviceUsernameException(username);
		}
	}

	@Override
	public void updateUser(final WebserviceUser user) throws WebserviceUserDaoException {
		try {
			int count = update("UPDATE webservice_user_tbl SET active = ?, contact_email = ? WHERE username = ?", user.isActive() ? 1 : 0, user.getContactEmail(), user.getUsername());
			if (count == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Unknown webservice user '{}'", user.getUsername());
				}
				throw new UnknownWebserviceUsernameException(user.getUsername());
			}
		} catch (Exception e) {
			logger.error("Error updating webservice user '{}*'", user.getUsername(), e);
			throw new WebserviceUserDaoException("Error updating webservice user '" + user.getUsername() + "*'", e);
		}
	}

	@Override
	public void updatePasswordHash(final String username, final String passwordHash) throws WebserviceUserDaoException {
		try {
			int rows = update("UPDATE webservice_user_tbl SET password_encrypted = ? WHERE username = ?", passwordHash, username);
			if (rows == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Unknown webservice user '{}'", username);
				}
				throw new UnknownWebserviceUsernameException(username);
			}
		} catch (Exception e) {
			logger.error("Error updating password hash for webservice user '{}'", username, e);
			throw new WebserviceUserDaoException("Error updating password hash for webservice user '" + username + "'", e);
		}
	}

	@Override
	public boolean webserviceUserExists(final String username) {
		int count = selectInt("SELECT COUNT(*) FROM webservice_user_tbl WHERE username = ?", username);
		return count > 0;
	}

	@Override
	public void createWebserviceUser(WebserviceUserCredential user, int dataSourceId, int bulkSizeLimit) throws WebserviceUserException, WebserviceUserDaoException {
		String username = user.getUsername();
		if (webserviceUserExists(username)) {
			throw new WebserviceUserAlreadyExistsException(username);
		}

		try {
			update("INSERT INTO webservice_user_tbl (username, company_id, default_data_source_id, bulk_size_limit, password_encrypted, active, contact_email) VALUES (?, ?, ?, ?, ?, 1, ?)",
					username, user.getCompanyID(), user.getDefaultDatasourceID(), bulkSizeLimit, user.getPasswordHash(), user.getContactEmail());
		} catch(Exception e) {
			logger.error("Error creating new webservice user", e);

			throw new WebserviceUserDaoException("Error creating new webservice user", e);
		}
	}

	@Override
	public int getNumberOfWebserviceUsers(int companyID) {
		if (companyID > 0) {
			return selectInt("SELECT COUNT(*) FROM webservice_user_tbl WHERE company_id = ?", companyID);
		} else {
			return selectInt("SELECT COUNT(*) FROM webservice_user_tbl");
		}
	}
	
	private Set<String> loadGrantedPermissions(final String username) {
		final String sql = "SELECT endpoint FROM webservice_permission_tbl WHERE username=?";
		
		return new HashSet<>(select(sql, StringRowMapper.INSTANCE, username));
	}
	
	private Set<Integer> loadGrantedPermissionGroups(final String username) {
		final String sql = "SELECT group_ref FROM webservice_user_group_tbl WHERE username=?";
		
		return new HashSet<>(select(sql, IntegerRowMapper.INSTANCE, username));
	}

	@Override
	public final void saveGrantedPermissionsAndGroups(final WebserviceUser user) {
		saveGrantedPermissions(user);
		saveGrantedPermissionGroups(user);
	}
	
	private void saveGrantedPermissions(final WebserviceUser user) {
		final String deletePermissions = "DELETE FROM webservice_permission_tbl WHERE username=?";
		this.update(deletePermissions, user.getUsername());
		
		final String insertPermission = "INSERT INTO webservice_permission_tbl (username, endpoint) VALUES (?,?)";
		for(final String permission : user.getGrantedPermissions()) {
			this.update(insertPermission, user.getUsername(), permission);
		}
	}
	
	private void saveGrantedPermissionGroups(final WebserviceUser user) {
		final String deleteGroupsSql = "DELETE FROM webservice_user_group_tbl WHERE username=?";
		this.update(deleteGroupsSql, user.getUsername());
		
		final String insertGroups = "INSERT INTO webservice_user_group_tbl (username, group_ref) VALUES (?,?)";
		for(final int groupID : user.getGrantedPermissionGroupIDs()) {
			this.update(insertGroups, user.getUsername(), groupID);
		}
	}

	@Override
	public boolean deleteWebserviceUser(String username) {
		update("DELETE FROM webservice_user_group_tbl WHERE username = ?", username);
		update("DELETE FROM webservice_permission_tbl WHERE username = ?", username);
		return update("DELETE FROM webservice_user_tbl WHERE username = ?", username) == 1;
	}

	@Override
	public List<String> getUsernames() {
		String query = "SELECT wsadm.username FROM webservice_user_tbl wsadm, company_tbl comp WHERE wsadm.company_id = comp.company_id";
		return select(query, StringRowMapper.INSTANCE);
	}

	@Override
	public List<String> getUsernames(int companyId) {
		String query = "SELECT wsadm.username FROM webservice_user_tbl wsadm, company_tbl comp WHERE wsadm.company_id = comp.company_id AND wsadm.company_id = ?";
		return select(query, StringRowMapper.INSTANCE, companyId);
	}
}
