package com.agnitas.emm.core.supervisor.dao.impl;

import java.util.List;

import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.common.SupervisorSortCriterion;
import com.agnitas.emm.core.supervisor.dao.ComSupervisorDao;
import com.agnitas.emm.core.supervisor.form.SupervisorOverviewFilter;
import com.agnitas.emm.util.SortDirection;

/**
 * Dummy implementation of {@link ComSupervisorDao }
 */
public class DefaultSupervisorDaoImpl implements ComSupervisorDao {
	 @Override
    public Supervisor getSupervisor(String supervisorName, String password) throws SupervisorException {
        return null;
    }
    
    @Override
    public List<Supervisor> listAllSupervisors(SupervisorSortCriterion criterion, SortDirection direction, SupervisorOverviewFilter filter) throws SupervisorException {
        return null;
    }
    
    @Override
    public Supervisor getSupervisor(int id) throws SupervisorException {
        return null;
    }
    
    @Override
    public void setSupervisorPassword(int id, String password) throws SupervisorException {
		// nothing to do
    }
    
    @Override
    public boolean isCurrentPassword(int id, String pwd) throws SupervisorException {
        return false;
    }
    
    @Override
    public Supervisor getSupervisor(String supervisorName) {
        return null;
    }
    
    @Override
    public int getNumberOfSupervisors() {
        return 0;
    }
    
    @Override
    public int createSupervisor(Supervisor supervisor) {
        return 0;
    }
    
    @Override
    public List<Integer> getAllowedCompanyIDs(int supervisorId) {
        return null;
    }
    
    @Override
    public void setAllowedCompanyIds(int id, List<Integer> allowedCompanyIds) throws SupervisorException {
		// nothing to do
    }
    
    @Override
    public Supervisor updateSupervisor(Supervisor supervisor) {
        return null;
    }
    
    @Override
    public boolean logSupervisorLogin(int supervisorId, int companyId) {
        return false;
    }
    
    @Override
    public void cleanupUnusedSupervisorBindings(int daysBeforeInactive) {
		// nothing to do
    }
    
    @Override
    public boolean deleteSupervisor(int supervisorId) {
        return false;
    }

	@Override
	public boolean existsSupervisor(String supervisorName) {
		return false;
	}

	@Override
	public boolean deleteSupervisorPermissionByCompany(int companyID) {
		return true;
	}
}
