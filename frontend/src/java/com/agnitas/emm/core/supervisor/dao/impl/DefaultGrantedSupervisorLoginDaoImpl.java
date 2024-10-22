package com.agnitas.emm.core.supervisor.dao.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem;
import com.agnitas.emm.core.supervisor.dao.GrantedSupervisorLoginDao;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.web.forms.PaginationForm;

import java.util.Date;

/**
 * Dummy implementation of {@link GrantedSupervisorLoginDao }
 */
public class DefaultGrantedSupervisorLoginDaoImpl implements GrantedSupervisorLoginDao {
    @Override
    public boolean isSupervisorLoginGranted(int supervisorID, Admin admin) {
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
    public PaginatedListImpl<SupervisorLoginPermissionTableItem> listActiveSupervisorLoginPermissions(PaginationForm form, int adminID) {
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
