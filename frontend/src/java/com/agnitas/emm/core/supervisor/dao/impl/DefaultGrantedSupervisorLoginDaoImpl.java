package com.agnitas.emm.core.supervisor.dao.impl;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem;
import com.agnitas.emm.core.supervisor.dao.GrantedSupervisorLoginDao;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;

/**
 * Dummy implementation of {@link GrantedSupervisorLoginDao}
 */
public class DefaultGrantedSupervisorLoginDaoImpl implements GrantedSupervisorLoginDao {
    @Override
    public boolean isSupervisorLoginGranted(int supervisorID, ComAdmin admin) {
        return false;
    }
    
    @Override
    public void grantSupervisorLoginToDepartment(int adminID, int departmentID, Date expireDate) {
    	// default implementation
    }
    
    @Override
    public void grantSupervisorLoginToAllDepartments(int adminID, Date expireDate) {
    	// default implementation
    }
    
    @Override
    public List<SupervisorLoginPermissionTableItem> listActiveSupervisorLoginPermissions(int adminID) {
        return null;
    }
    
    @Override
    public void deleteOldGrants(int expireDays) {
    	// default implementation
    }
    
    @Override
    public void revokeSupervisorLoginPermission(int adminID, int permissionID) throws UnknownSupervisorLoginPermissionException {
    	// default implementation
    }
    
    @Override
    public Integer getDepartmentIdForLoginPermission(int permissionID) throws UnknownSupervisorLoginPermissionException {
        return null;
    }
}
