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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.AdminGroupImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.Permission;

/**
 * DAO handler for AdminGroup-Objects
 */
public class ComAdminGroupDaoImpl extends PaginatedBaseDaoImpl implements ComAdminGroupDao  {
	
	private static final transient Logger logger = Logger.getLogger(ComAdminGroupDaoImpl.class);
	
	private final AdminGroupRowMapper adminGroupRowMapper = new AdminGroupRowMapper();
    
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
	public AdminGroup getAdminGroup(int groupID, int companyToLimitPremiumPermissionsFor) {
		try {
			List<AdminGroup> groups = select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE admin_group_id = ?", new AdminGroupRowMapperWithOtherCompanyId(companyToLimitPremiumPermissionsFor), groupID);
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
    
    @Override
	public List<AdminGroup> getAdminGroupsByCompanyId(@VelocityCheck int companyId) {
		List<AdminGroup> groupList = select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE company_id = ?", adminGroupRowMapper, companyId);
		return groupList;
    }
    
    @Override
    public List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(@VelocityCheck int companyId) {
    	if (companyId == 1) {
    		return select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl", adminGroupRowMapper);
    	} else {
    		return select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE company_id = ? OR company_id = (SELECT creator_company_id FROM company_tbl WHERE company_id = ?) ", adminGroupRowMapper, companyId, companyId);
    	}
	}
    
    @Override
    public PaginatedListImpl<AdminGroup> getAdminGroupsByCompanyIdInclCreator(@VelocityCheck int companyId, int adminId, String sortColumn, String sortDirection, int pageNumber, int pageSize) {
    	if (StringUtils.isBlank(sortColumn)) {
    		sortColumn = "shortname";
        }
		
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		
		String selectStatement;
    	if (adminId == 1) {
    		selectStatement = "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl";
    		return selectPaginatedList(logger, selectStatement, "admin_group_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, adminGroupRowMapper);
    	} else {
    		selectStatement = "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl"
    			+ " WHERE (company_id = ? OR company_id = (SELECT creator_company_id FROM company_tbl WHERE company_id = ?))";
    		return selectPaginatedList(logger, selectStatement, "admin_group_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, adminGroupRowMapper, companyId, companyId);
    	}
    }
	
	@Override
	public int saveAdminGroup(AdminGroup adminGroup) throws Exception {
		if(adminGroup == null || adminGroup.getCompanyID() == 0) {
			return -1;
		}
		
		int groupId = adminGroup.getGroupID();
		boolean successful;
		if(groupId <= 0 || !exists(adminGroup.getCompanyID(), groupId)) {
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
			int touchedLines = update(logger, "UPDATE admin_group_tbl SET shortname = ?, description = ? WHERE admin_group_id = ? AND company_id = ?",
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
		
		return groupId;
	}
	
	private void saveAdminGroupPermissions(int adminGroupId, Set<Permission> permissions) {
		update(logger, "DELETE FROM admin_group_permission_tbl WHERE admin_group_id = ?", adminGroupId);
		
		// write permissions (when inserting new group, the deletion of the old permissions was done after group_tbl update before)
		if (CollectionUtils.isNotEmpty(permissions)) {
			List<Object[]> parameterList = new ArrayList<>();
			for (Permission permission : permissions) {
				parameterList.add(new Object[] {adminGroupId, permission.getTokenString()});
			}
			batchupdate(logger, "INSERT INTO admin_group_permission_tbl (admin_group_id, security_token) VALUES (?, ?)", parameterList);
		}
	}
	
	public boolean exists(int companyID, int groupID) throws Exception {
		String sql = "SELECT COUNT(*) FROM admin_group_tbl WHERE company_id = ? AND admin_group_id = ?";
		int numberOfFoundGroups = selectInt(logger, sql, companyID, groupID);
		if (numberOfFoundGroups > 1) {
			throw new Exception("Invald number of groups found for groupid: " + companyID + "/" + groupID);
		} else {
			return numberOfFoundGroups == 1;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int delete(@VelocityCheck int companyId, int adminGroupId) {
    	if (companyId > 0) {
    		update(logger, "DELETE FROM admin_group_permission_tbl WHERE admin_group_id = ?", adminGroupId);
    		return update(logger, "DELETE FROM admin_group_tbl WHERE admin_group_id = ? AND company_id = ?", adminGroupId, companyId);
    	} else {
    		//don't allow deletion of default groups
    		return 0;
    	}
    }
    
    @Override
    public int adminGroupExists(@VelocityCheck int companyId, String groupname) {
		String sql = "SELECT admin_group_id FROM admin_group_tbl WHERE (company_id = ? or company_id = 1) AND shortname = ?";
		return selectInt(logger, sql, companyId, groupname);
	}
    
    private class AdminGroupRowMapper implements RowMapper<AdminGroup> {
		@Override
		public AdminGroup mapRow(ResultSet resultSet, int row) throws SQLException {
			AdminGroup group = new AdminGroupImpl();
			
			group.setGroupID((resultSet.getInt("admin_group_id")));
			group.setCompanyID((resultSet.getInt("company_id")));
			group.setShortname(resultSet.getString("shortname"));
			group.setDescription(resultSet.getString("description"));
			group.setGroupPermissions(Permission.fromTokens(getGroupPermissionsTokens(group.getGroupID())));
			
			Set<Permission> companyPermissions = companyDao.getCompanyPermissions(group.getCompanyID());
			group.setCompanyPermissions(companyPermissions);
			
			return group;
		}
	}
    
    private class AdminGroupRowMapperWithOtherCompanyId implements RowMapper<AdminGroup> {
    	private int companyID;
    	
    	/**
    	 * Overrides allowed companyPermissions of this group, because it may be some default group of company id 1 or other parent company
    	 * 
    	 * @param companyID
    	 */
    	public AdminGroupRowMapperWithOtherCompanyId(int companyID) {
    		this.companyID = companyID;
    	}
    	
		@Override
		public AdminGroup mapRow(ResultSet resultSet, int row) throws SQLException {
			AdminGroup group = new AdminGroupImpl();
			
			group.setGroupID((resultSet.getInt("admin_group_id")));
			group.setCompanyID((resultSet.getInt("company_id")));
			group.setShortname(resultSet.getString("shortname"));
			group.setDescription(resultSet.getString("description"));
			group.setGroupPermissions(Permission.fromTokens(getGroupPermissionsTokens(group.getGroupID())));
			
			Set<Permission> companyPermissions = companyDao.getCompanyPermissions(companyID);
			group.setCompanyPermissions(companyPermissions);
			
			return group;
		}
	}

	@Override
	public List<String> getAdminsOfGroup(@VelocityCheck int companyId, int groupId) {
		return select(logger, "SELECT username FROM admin_tbl WHERE company_id = ? AND admin_group_id = ?", new StringRowMapper(), companyId, groupId);
	}
	
	@Override
    public List<AdminGroup> getAdminGroupByAdminID(int adminId) {
    	return select(logger, "SELECT g.admin_group_id, g.company_id, shortname, description FROM admin_group_tbl g, admin_tbl admin WHERE g.admin_group_id = admin.admin_group_id AND admin.admin_id = ?", adminGroupRowMapper, adminId);
	}
    
    @Override
    public Set<String> getGroupPermissionsTokens(int adminGroupId) {
        List<String> tokensList = select(logger, "SELECT security_token FROM admin_group_permission_tbl WHERE admin_group_id = ?",
                (resultSet1, i) -> resultSet1.getString("security_token"),
                adminGroupId);
        
        return new HashSet<>(tokensList);
    }

	@Override
	public AdminGroup getAdminGroupByName(String adminGroupName, int companyToLimitPremiumPermissionsFor) {
		try {
			List<AdminGroup> groups = select(logger, "SELECT admin_group_id, company_id, shortname, description FROM admin_group_tbl WHERE (company_id = 1 OR company_id = ?) AND shortname = ?", new AdminGroupRowMapperWithOtherCompanyId(companyToLimitPremiumPermissionsFor), companyToLimitPremiumPermissionsFor, adminGroupName);
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
}
