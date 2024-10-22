/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.AdminGroupDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;
import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.AdminGroupImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * DAO handler for AdminGroup-Objects
 */
public class AdminGroupDaoImpl extends PaginatedBaseDaoImpl implements AdminGroupDao  {
	
	private static final Logger logger = LogManager.getLogger(AdminGroupDaoImpl.class);
    private static final String ADMIN_GROUP_TBL = "admin_group_tbl";
    private static final String SHORTNAME_COL = "shortname";

    /** DAO for accessing company data. */
	protected ComCompanyDao companyDao;

    /**
     * Set DAO for accessing company data.
     * 
     * @param companyDao DAO for accessing company data
     */
    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

	@Override
	public AdminGroup getAdminGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor) {
		return selectObjectDefaultNull(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE admin_group_id = ? AND deleted = 0", new AdminGroupRowMapper(companyToLimitPremiumPermissionsFor), adminGroupID);
	}

    @Override
    public AdminGroup getUserGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor) {
        return selectObjectDefaultNull(logger, "SELECT ag.admin_group_id, ag.company_id, ag.shortname, ag.description,"
                        + " (SELECT c.shortname FROM company_tbl c WHERE c.company_id = ag.company_id) AS company_name"
                        + " FROM admin_group_tbl ag WHERE ag.admin_group_id = ? AND ag.deleted = 0",
                new AdminGroupRowMapper(companyToLimitPremiumPermissionsFor), adminGroupID);
    }

	private AdminGroup getAdminGroup(int groupID, int companyToLimitPremiumPermissionsFor, Stack<Integer> cycleDetectionGroupIds) {
		if (cycleDetectionGroupIds == null) {
			cycleDetectionGroupIds = new Stack<>();
		}
		cycleDetectionGroupIds.push(groupID);
		
		AdminGroup group = selectObjectDefaultNull(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE admin_group_id = ?", new AdminGroupRowMapper(companyToLimitPremiumPermissionsFor, cycleDetectionGroupIds), groupID);
		cycleDetectionGroupIds.pop();
		return group;
	}
    
    @Override
	public List<AdminGroup> getAdminGroupsByCompanyId(int companyId) {
		return getAdminGroupsByCompanyId(companyId, false);
	}

	@Override
	public List<AdminGroup> getAdminGroupsByCompanyId(int companyId, boolean includeDeleted) {
        return select(logger,
			"SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE company_id = ?"
				+ (includeDeleted ? "" : " AND deleted = 0")
				+ " ORDER BY admin_group_id",
			new AdminGroupRowMapper(companyId), companyId);
    }
    
    @Override
    public List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyId, List<Integer> additionalAdminGroupIds) {
    	if (companyId == 1) {
    		return select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE deleted = 0 ORDER BY admin_group_id", new AdminGroupRowMapper(companyId));
    	} else {
    		String additionalAdminGroupIdsPart = "";
    		if (additionalAdminGroupIds != null && !additionalAdminGroupIds.isEmpty()) {
    			additionalAdminGroupIdsPart = " OR admin_group_id IN (" + StringUtils.join(additionalAdminGroupIds, ", ") + ")";
    		}
    		return select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE deleted = 0 AND (company_id = ? OR company_id = (SELECT creator_company_id FROM company_tbl WHERE company_id = ?))" + additionalAdminGroupIdsPart + " ORDER BY admin_group_id", new AdminGroupRowMapper(companyId), companyId, companyId);
    	}
	}

    @Override
    public PaginatedListImpl<UserGroupDto> getAdminGroupsByCompanyIdInclCreator(UserGroupOverviewFilter filter) {
        StringBuilder sql =  new StringBuilder("SELECT ag.admin_group_id, ag.company_id," +
                " CONCAT(CONCAT(CONCAT(c.shortname, ' ('), c.company_id), ')') AS company_descr," +
                " ag.shortname ag_shortname, ag.description" +
                " FROM admin_group_tbl ag LEFT JOIN company_tbl c ON ag.company_id = c.company_id");
        List<Object> params = applyOverviewFilter(filter, sql);

        String sortCol = filter.getSortOrDefault(SHORTNAME_COL);
        String sortClause = tryGetOverviewSortClause(sortCol, filter.ascending());

		PaginatedListImpl<UserGroupDto> list = selectPaginatedListWithSortClause(logger, sql.toString(), sortClause, sortCol,
				filter.ascending(), filter.getPage(), filter.getNumberOfRows(),
				new UserGroupOverviewRowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCurrentAdminId(), filter.getCurrentAdminCompanyId(), filter.isShowDeleted()));
		}

		return list;
    }

    private String tryGetOverviewSortClause(String sortCol, boolean asc) {
        try {
            return getOverviewSortClause(sortCol, asc);
        } catch (Exception e) {
            return "";
        }
    }

    private String getOverviewSortClause(String sortCol, boolean asc) throws Exception {
        DbColumnType columnDataType = DbUtilities.getColumnDataType(getDataSource(), ADMIN_GROUP_TBL, sortCol);
        boolean lowerSearch = columnDataType != null
                && columnDataType.getSimpleDataType() == DbColumnType.SimpleDataType.Characters;
        sortCol = SHORTNAME_COL.equals(sortCol) ? "ag_" + SHORTNAME_COL : sortCol;
        return (lowerSearch ? "ORDER BY LOWER(" + sortCol + ")" : "ORDER BY " + sortCol) + (asc ? " ASC" : " DESC");
    }

    private List<Object> applyOverviewFilter(UserGroupOverviewFilter filter, StringBuilder sql) {
		List<Object> params = applyRequiredOverviewFilter(sql, filter.getCurrentAdminId(), filter.getCurrentAdminCompanyId(), filter.isShowDeleted());

		if (StringUtils.isNotBlank(filter.getGroupName())) {
            sql.append(getPartialSearchFilterWithAnd("ag.shortname"));
            params.add(filter.getGroupName());
        }
        if (StringUtils.isNotBlank(filter.getDescription())) {
            sql.append(getPartialSearchFilterWithAnd("ag.description"));
            params.add(filter.getDescription());
        }
        if (filter.getClientId() != null && filter.getClientId() > 0) {
            sql.append(" AND ag.company_id = ?");
            params.add(filter.getClientId());
        }
        if (StringUtils.isNotBlank(filter.getClientName())) {
            sql.append(getPartialSearchFilterWithAnd("c.shortname"));
            params.add(filter.getClientName());
        }
        return params;
    }

	private int getTotalUnfilteredCountForOverview(int adminId, int companyId, boolean deleted) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM admin_group_tbl ag");
		List<Object> params = applyRequiredOverviewFilter(query, adminId, companyId, deleted);

		return selectIntWithDefaultValue(logger, query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int adminId, int companyId, boolean deleted) {
		List<Object> params = new ArrayList<>();
		if (adminId == 1) {
			query.append(" WHERE 1=1 AND deleted = ?");
		} else {
			query.append(" WHERE (ag.company_id = ? OR ag.company_id = (SELECT creator_company_id FROM company_tbl WHERE company_id = ?)) AND deleted = ?");
			params.add(companyId);
			params.add(companyId);
		}
		params.add(BooleanUtils.toInteger(deleted));
		return params;
	}

    @Override
	// TODO: EMMGUI-714: remove when old design will be removed
    public PaginatedListImpl<AdminGroup> getAdminGroupsByCompanyIdInclCreator(int companyId, int adminId, String sortColumn, String sortDirection, int pageNumber, int pageSize) {
    	if (StringUtils.isBlank(sortColumn)) {
    		sortColumn = SHORTNAME_COL;
        }
		
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		
		String selectStatement;
    	if (adminId == 1) {
    		selectStatement = "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE deleted = 0";
    		return selectPaginatedList(logger, selectStatement, ADMIN_GROUP_TBL, sortColumn, sortDirectionAscending, pageNumber, pageSize, new AdminGroupRowMapper(companyId));
    	} else {
    		selectStatement = "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl"
    			+ " WHERE deleted = 0 AND (company_id = ? OR company_id = (SELECT creator_company_id FROM company_tbl WHERE company_id = ?))";
    		return selectPaginatedList(logger, selectStatement, ADMIN_GROUP_TBL, sortColumn, sortDirectionAscending, pageNumber, pageSize, new AdminGroupRowMapper(companyId), companyId, companyId);
    	}
    }
	
	@Override
	public int saveAdminGroup(AdminGroup adminGroup) throws Exception {
		if (adminGroup == null || adminGroup.getCompanyID() == 0) {
			return -1;
		} else if (checkGroupCycle(adminGroup.getGroupID(), adminGroup.getParentGroupIds())) {
			throw new Exception("Group member cycle detected");
		}
		
		int groupId = adminGroup.getGroupID();
		boolean successful;
		if (groupId <= 0 || !exists(adminGroup.getCompanyID(), groupId)) {
			if (isOracleDB()) {
				// Insert new AdminGroup into DB
				groupId = selectInt(logger, "SELECT admin_group_tbl_seq.nextval FROM DUAL");
				int touchedLines = update(logger, "INSERT INTO admin_group_tbl (admin_group_id, company_id, shortname, description) VALUES (?, ?, ?, ?)",
					groupId,
					adminGroup.getCompanyID(),
					adminGroup.getShortname(),
					adminGroup.getDescription());
				
				successful = touchedLines == 1;
			} else {
				groupId = insertIntoAutoincrementMysqlTable(logger, "admin_group_id", "INSERT INTO admin_group_tbl (company_id, shortname, description) VALUES (?, ?, ?)", adminGroup.getCompanyID(), adminGroup.getShortname(), adminGroup.getDescription());
				successful = groupId > 0;
			}
		} else {
			// Update group in DB
			int touchedLines = update(logger, "UPDATE admin_group_tbl SET shortname = ?, description = ?, change_date = CURRENT_TIMESTAMP WHERE admin_group_id = ? AND company_id = ?",
				adminGroup.getShortname(),
				adminGroup.getDescription(),
				adminGroup.getGroupID(),
				adminGroup.getCompanyID());

			successful = touchedLines == 1;
		}
		
		if (!successful) {
			throw new Exception("Illegal insert result");
		}
		
		saveAdminGroupPermissions(groupId, adminGroup.getGroupPermissions());
		saveAdminGroupParentGroups(groupId, adminGroup.getParentGroups());
		
		adminGroup.setGroupID(groupId);
		
		return groupId;
	}

	private void saveAdminGroupPermissions(int adminGroupId, Set<Permission> permissions) {
		update(logger, "DELETE FROM admin_group_permission_tbl WHERE admin_group_id = ?", adminGroupId);
		
		// write permissions (when inserting new group, the deletion of the old permissions was done after group_tbl update before)
		if (CollectionUtils.isNotEmpty(permissions)) {
			List<Object[]> parameterList = new ArrayList<>();
			for (Permission permission : permissions) {
				parameterList.add(new Object[] { adminGroupId, permission.getTokenString() });
			}
			batchupdate(logger, "INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) VALUES (?, ?)", parameterList);
		}
	}
	
	private void saveAdminGroupParentGroups(int adminGroupId, Set<AdminGroup> parentGroups) {
		update(logger, "DELETE FROM group_to_group_tbl WHERE admin_group_id = ?", adminGroupId);
		
		if (parentGroups != null && CollectionUtils.isNotEmpty(parentGroups)) {
			List<Object[]> parameterList = new ArrayList<>();
            for (AdminGroup group : parentGroups) {
				parameterList.add(new Object[] { adminGroupId, group.getGroupID() });
            }
            batchupdate(logger, "INSERT INTO group_to_group_tbl (admin_group_id, member_of_admin_group_id) VALUES (?, ?)", parameterList);
		}
	}
	
	public boolean exists(int companyID, int groupID) throws Exception {
		String sql = "SELECT COUNT(*) FROM admin_group_tbl WHERE company_id = ? AND deleted = 0 AND admin_group_id = ?";
		int numberOfFoundGroups = selectInt(logger, sql, companyID, groupID);
		if (numberOfFoundGroups > 1) {
			throw new Exception("Invald number of groups found for groupid: " + companyID + "/" + groupID);
		} else {
			return numberOfFoundGroups == 1;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int delete(int companyId, int adminGroupId) {
    	if (companyId > 0) {
    		update(logger, "DELETE FROM group_to_group_tbl WHERE admin_group_id = ?", adminGroupId);
    		update(logger, "DELETE FROM admin_group_permission_tbl WHERE admin_group_id = ?", adminGroupId);
    		return update(logger, "DELETE FROM admin_group_tbl WHERE admin_group_id = ? AND company_id = ?", adminGroupId, companyId);
    	} else {
    		//don't allow deletion of default groups
    		return 0;
    	}
    }

	@Override
	public void markDeleted(int groupId, int companyId) {
		update(logger, "UPDATE admin_group_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP"
				+ " WHERE admin_group_id = ? AND company_id = ?", groupId, companyId);
	}

    @Override
    public boolean adminGroupExists(int companyId, String groupname) {
		String sql = "SELECT COUNT(*) FROM admin_group_tbl WHERE (company_id = ? or company_id = 1) AND deleted = 0 AND shortname = ?";
		return selectInt(logger, sql, companyId, groupname) > 0;
	}

    private class UserGroupOverviewRowMapper implements RowMapper<UserGroupDto> {
        @Override
        public UserGroupDto mapRow(ResultSet resultSet, int row) throws SQLException {
            UserGroupDto group = new UserGroupDto();
            group.setUserGroupId(resultSet.getInt("admin_group_id"));
            group.setShortname(resultSet.getString("ag_shortname"));
            group.setCompanyId(resultSet.getInt("company_id"));
            group.setCompanyDescr(resultSet.getString("company_descr"));
            group.setDescription(resultSet.getString("description"));
            return group;
        }
    }

    private class AdminGroupRowMapper implements RowMapper<AdminGroup> {
    	private int companyID;
    	private Stack<Integer> cycleDetectionGroupIds = null;
    	
    	/**
    	 * Overrides allowed companyPermissions of this group, because it may be some default group of company id 1 or other parent company
    	 * 
    	 * @param companyID
    	 */
    	public AdminGroupRowMapper(int companyID) {
    		this.companyID = companyID;
    	}
    	
    	public AdminGroupRowMapper(int companyID, Stack<Integer> cycleDetectionGroupIds) {
    		this.companyID = companyID;
    		this.cycleDetectionGroupIds = cycleDetectionGroupIds;
    	}
    	
		@Override
		public AdminGroup mapRow(ResultSet resultSet, int row) throws SQLException {
			AdminGroup group = new AdminGroupImpl();
			
			group.setGroupID((resultSet.getInt("admin_group_id")));
			group.setCompanyID((resultSet.getInt("company_id")));
			group.setShortname(resultSet.getString(SHORTNAME_COL));
			group.setDescription(resultSet.getString("description"));
			group.setGroupPermissions(Permission.fromTokens(getGroupPermissionsTokens(group.getGroupID())));
            if (DbUtilities.resultsetHasColumn(resultSet, "company_name")) {
                group.setCompanyName(resultSet.getString("company_name"));
            }
			
			Set<Permission> companyPermissions = companyDao.getCompanyPermissions(companyID);
			group.setCompanyPermissions(companyPermissions);
			
			Set<AdminGroup> parentGroups = new HashSet<>();
			for (int parentGroupId : getParentGroupIds(group.getGroupID())) {
				if (cycleDetectionGroupIds != null && cycleDetectionGroupIds.contains(parentGroupId)) {
					throw new SQLException("Group member cycle detected: " + parentGroupId);
				}
				AdminGroup adminGroup = getAdminGroup(parentGroupId, group.getCompanyID(), cycleDetectionGroupIds);
				parentGroups.add(adminGroup);
			}
			
			group.setParentGroups(parentGroups);
			
			return group;
		}
	}

	@Override
	public List<String> getAdminsOfGroup(int companyId, int groupId) {
		return select(logger, "SELECT adm.username FROM admin_tbl adm WHERE adm.company_id = ? AND EXISTS (SELECT 1 FROM admin_to_group_tbl grp WHERE grp.admin_id = adm.admin_id AND grp.admin_group_id = ?)", StringRowMapper.INSTANCE, companyId, groupId);
	}

	@Override
	public List<String> getGroupNamesUsingGroup(int companyId, int groupId) {
		return select(logger, "SELECT grp1.shortname FROM admin_group_tbl grp1 WHERE grp1.company_id = ? AND EXISTS (SELECT 1 FROM group_to_group_tbl grp2 WHERE grp2.admin_group_id = grp1.admin_group_id AND grp2.member_of_admin_group_id = ?)", StringRowMapper.INSTANCE, companyId, groupId);
	}
	
	@Override
    public List<AdminGroup> getAdminGroupsByAdminID(int companyID, int adminId) {
    	return select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE admin_group_id IN (SELECT admin_group_id FROM admin_to_group_tbl WHERE admin_id = ? AND deleted = 0)", new AdminGroupRowMapper(companyID), adminId);
	}
    
    @Override
    public Set<String> getGroupPermissionsTokens(int adminGroupId) {
    	Set<String> returnSet = new HashSet<>();
    	
    	returnSet.addAll(select(logger, "SELECT permission_name FROM admin_group_permission_tbl WHERE admin_group_id = ?", StringRowMapper.INSTANCE, adminGroupId));
        
        returnSet.addAll(getParentGroupsPermissionTokens(adminGroupId));
        
        return returnSet;
    }
    
    @Override
    public List<Integer> getParentGroupIds(int adminGroupId) {
        return select(logger, "SELECT member_of_admin_group_id FROM group_to_group_tbl WHERE admin_group_id = ? ORDER BY member_of_admin_group_id", IntegerRowMapper.INSTANCE, adminGroupId);
    }
    
    @Override
    public Set<String> getParentGroupsPermissionTokens(int adminGroupId) {
    	Set<String> returnSet = new HashSet<>();
    	
        for (int parentGroupId : getParentGroupIds(adminGroupId)) {
        	returnSet.addAll(select(logger, "SELECT permission_name FROM admin_group_permission_tbl WHERE admin_group_id = ?", StringRowMapper.INSTANCE, parentGroupId));
        }
        
        return returnSet;
    }

	@Override
	public AdminGroup getAdminGroupByName(String adminGroupName, int companyToLimitPremiumPermissionsFor) {
		try {
			List<AdminGroup> groups = select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE (company_id = 1 OR company_id = ?) AND deleted = 0 AND shortname = ?", new AdminGroupRowMapper(companyToLimitPremiumPermissionsFor), companyToLimitPremiumPermissionsFor, adminGroupName);
			if (groups.size() > 0) {
				return groups.get(0);
			} else {
				// No Group found
				return null;
			}
		} catch (DataAccessException e) {
			// No Group found
			return null;
		}
	}

	private boolean checkGroupCycle(int groupID, List<Integer> parentGroupIds) {
		Set<Integer> summedUpParentGroupIds = new HashSet<>();
		Set<Integer> groupIdsToCheck = new HashSet<>(parentGroupIds);
		while (groupIdsToCheck.size() > 0) {
			int nextGroupIdToCheck = groupIdsToCheck.iterator().next();
			summedUpParentGroupIds.add(nextGroupIdToCheck);
			groupIdsToCheck.remove(nextGroupIdToCheck);
			for (int parentGroupId : getParentGroupIds(nextGroupIdToCheck)) {
				if (!summedUpParentGroupIds.contains(parentGroupId)) {
					groupIdsToCheck.add(parentGroupId);
				}
			}
		}
		return summedUpParentGroupIds.contains(groupID);
	}

	@Override
	public void restore(Set<Integer> ids, int companyId) {
		update(logger,
			"UPDATE admin_group_tbl SET deleted = 0, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND "
				+ makeBulkInClauseForInteger("admin_group_id", ids), companyId);
	}

	@Override
	public List<Integer> getMarkedAsDeletedBefore(Date date, int companyId) {
		return select(logger,
			"SELECT admin_group_id FROM admin_group_tbl WHERE company_id = ? AND deleted = 1 AND change_date < ?",
			IntegerRowMapper.INSTANCE, companyId, date);
	}
}
