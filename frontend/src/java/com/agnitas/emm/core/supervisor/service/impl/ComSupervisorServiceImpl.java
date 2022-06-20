/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.service.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.SpringPasswordCheckHandler;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.password.ComPasswordCheck;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorSortCriterion;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.common.UnknownSupervisorIdException;
import com.agnitas.emm.core.supervisor.dao.ComSupervisorDao;
import com.agnitas.emm.core.supervisor.service.ComSupervisorService;
import com.agnitas.emm.util.SortDirection;
import com.agnitas.web.mvc.Popups;

/**
 * Implementation of {@link ComSupervisorService}.
 */
public class ComSupervisorServiceImpl implements ComSupervisorService {

	private static final int ALL_COMPANIES_ID = 0;

	private ConfigService configService;
	
	/** DAO for accessing supervisor data. */
	private ComSupervisorDao supervisorDao;
	
	private ComPasswordCheck passwordCheck;

	@Override
	public List<Supervisor> listSupervisors(SupervisorSortCriterion criterion0, SortDirection direction0) throws SupervisorException {
		SupervisorSortCriterion criterion = criterion0 != null ? criterion0 : SupervisorSortCriterion.SUPERVISOR_NAME;
		SortDirection direction = direction0 != null ? direction0 : SortDirection.ASCENDING;
		return supervisorDao.listAllSupervisors(criterion, direction);
	}

	@Override
	public Supervisor getSupervisor(int id) throws SupervisorException {
		return supervisorDao.getSupervisor(id);
	}

	@Override
	public void setSupervisorPassword(int id, String password) throws SupervisorException {
		supervisorDao.setSupervisorPassword(id, password);
	}

	@Override
    public boolean save(Supervisor supervisor, String password, List<Integer> allowedCompanyIdsOrNull, Popups popups) {
	    Objects.requireNonNull(supervisor, "supervisor == null");

        try {
            if (supervisor.getId() > 0) {
                // Password and allowed companies (bindings) can only be changed for an existing supervisor.
                if (StringUtils.isNotEmpty(password)) {
                    if (validatePassword(password, supervisor, popups)) {
                        supervisorDao.setSupervisorPassword(supervisor.getId(), password);
                    } else {
                        return false;
                    }
                }
                supervisorDao.updateSupervisor(supervisor);
            } else {
                if (StringUtils.isBlank(supervisor.getSupervisorName())) {
                    popups.field("supervisorName", "error.name.too.short");
                    return false;
                }

                if (StringUtils.isBlank(supervisor.getFullName())) {
                    popups.field("fullName", "error.name.too.short");
                    return false;
                }

                if (StringUtils.isBlank(supervisor.getEmail()) || !AgnUtils.isEmailValid(supervisor.getEmail())) {
                    popups.field("email", "error.invalid.email");
                    return false;
                }

                if (StringUtils.isEmpty(password)) {
                    popups.field("password", "error.password.missing");
                    return false;
                }

                if (validatePassword(password, popups)) {
                    supervisor.setId(supervisorDao.createSupervisor(supervisor));
                    supervisorDao.setSupervisorPassword(supervisor.getId(), password);
                } else {
                    return false;
                }
            }

            if(allowedCompanyIdsOrNull != null) {
            	supervisorDao.setAllowedCompanyIds(supervisor.getId(), allowedCompanyIdsOrNull);
            }
        } catch (UnknownSupervisorIdException e) {
            popups.alert("supervisor.error.not.exists");		// TODO This is UI-related code. Let this method throw exceptions (remove boolean return value) and move exception handling to SupervisorController
            return false;
        } catch (SupervisorException e) {
            return false;
        }

        return true;
    }

	@Override
	public PasswordState getPasswordState(Supervisor supervisor) {
		int passwordExpireDays = configService.getIntegerValue(ConfigValue.SupervisorPasswordExpireDays, 0);
		int passwordExpireNotificationDays = configService.getIntegerValue(ConfigValue.SupervisorPasswordExpireNotificationDays, 0);
		
		Date currentDate = (new GregorianCalendar()).getTime();
		Date expireWarningDate = computeDate(supervisor.getLastPasswordChangeDate(), passwordExpireDays - passwordExpireNotificationDays);
		Date expiredDate = computePasswordExpireDate(supervisor);
		
		if (currentDate.compareTo(expireWarningDate) < 0) {
			return PasswordState.VALID;
		} else if (currentDate.compareTo( expiredDate) < 0) {
			return PasswordState.EXPIRING;
		} else {
			Date finalExpiredDate = computePasswordFinalExpireDate(supervisor);
			if (currentDate.compareTo(finalExpiredDate) < 0) {
				return PasswordState.EXPIRED;
			} else {
				return PasswordState.EXPIRED_LOCKED;
			}
		}
			
	}

	@Override
	public Date computePasswordExpireDate(Supervisor supervisor) {
		int passwordExpireDays = configService.getIntegerValue(ConfigValue.SupervisorPasswordExpireDays, 0);
		return computeDate(supervisor.getLastPasswordChangeDate(), passwordExpireDays);
	}

	@Override
	public Date computePasswordFinalExpireDate(Supervisor supervisor) {
		int passwordExpireDays = configService.getIntegerValue(ConfigValue.SupervisorPasswordExpireDays, 0);
		int passwordFinalExpireNotificationDays = configService.getIntegerValue(ConfigValue.SupervisorPasswordFinalExpirationDays, 0);
		return computeDate(supervisor.getLastPasswordChangeDate(), passwordExpireDays + passwordFinalExpireNotificationDays);
	}

	@Override
	public boolean isCurrentPassword(Supervisor supervisor, String pwd) throws SupervisorException {
		return this.supervisorDao.isCurrentPassword(supervisor.getId(), pwd);
	}

	@Override
	public List<Integer> getAllowedCompanyIds(int supervisorId) {
		try {
			final Supervisor supervisor = supervisorDao.getSupervisor(supervisorId);
			List<Integer> ids = supervisorDao.getAllowedCompanyIDs(supervisorId);
			
			if(supervisor.getDepartment() != null && !supervisor.getDepartment().isSupervisorBindingToCompany0Allowed()) {
				ids.remove(Integer.valueOf(ALL_COMPANIES_ID)); // Boxing to Integer needed, otherwise we will remove item at index
			}
	
			if (ids.contains(ALL_COMPANIES_ID)) {
				return Collections.singletonList(ALL_COMPANIES_ID);
			}
	
			return ids;
		} catch(final SupervisorException e) {
			return Collections.emptyList();
		}
	}
	
	@Override
	public boolean deleteSupervisor(int id) {
		return supervisorDao.deleteSupervisor(id);
	}

	/**
	 * Computes date a given number of days in the future.
	 * 
	 * @param referenceDate reference date
	 * @param dayOffset offset in days
	 * 
	 * @return computed date
	 */
	private static Date computeDate(Date referenceDate, int dayOffset) {
		GregorianCalendar thresholdDate = new GregorianCalendar();
		thresholdDate.setTime(referenceDate);
		thresholdDate.add(Calendar.DAY_OF_MONTH, dayOffset);
		return thresholdDate.getTime();
	}

	private boolean validatePassword(String password, Popups popups) {
        PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups);
        return passwordCheck.checkSupervisorPassword(password, handler);
    }

	private boolean validatePassword(String password, Supervisor supervisor, Popups popups) throws SupervisorException {
        PasswordCheckHandler handler = new SpringPasswordCheckHandler(popups);
        return passwordCheck.checkSupervisorPassword(password, supervisor, handler);
    }

	// ------------------------------------------------------------------ Dependency Injection
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	/**
	 * Set DAO for accessing supervisor data.
	 * 
	 * @param dao DAO for accessing supervisor data
	 */
	@Required
	public void setSupervisorDao(ComSupervisorDao dao) {
		this.supervisorDao = dao;
	}

	@Required
	public void setPasswordCheck(ComPasswordCheck passwordCheck) {
		this.passwordCheck = passwordCheck;
	}
}
