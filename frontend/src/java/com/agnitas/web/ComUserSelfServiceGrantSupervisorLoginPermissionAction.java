/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.service.DepartmentService;
import com.agnitas.emm.core.departments.util.DepartmentI18n;
import com.agnitas.emm.core.supervisor.common.UnknownSupervisorIdException;
import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;
import com.agnitas.util.OneOf;
import com.agnitas.web.forms.SupervisorGrantLoginPermissionForm;

public final class ComUserSelfServiceGrantSupervisorLoginPermissionAction extends StrutsActionBase {
	
	/** ID representing selection "all departments". */
	public static final int ALL_DEPARTMENTS_ID = -1;
	
	public static final String UNLIMITED_LOGIN_PERMISSION = "UNLIMITED";
	public static final String LIMITED_LOGIN_PERMISSION = "LIMITED";


	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUserSelfServiceGrantSupervisorLoginPermissionAction.class);
	
	private SupervisorLoginPermissionService supervisorLoginPermissionService;
	private DepartmentService departmentService;

	@Override
	public final ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final ComAdmin admin = AgnUtils.getAdmin(request);
		final SupervisorGrantLoginPermissionForm loginPermissionForm = (SupervisorGrantLoginPermissionForm) form;

		// Check, that there is no supervisor attached to the admin (= admin with supervisor login)
		if(admin.getSupervisor() != null) {
			logger.warn(String.format("Attempt to grant supervisor login permission for EMM user '%s' (ID %d, company %d), logged in with supervisor '%s' (%d)", admin.getUsername(), admin.getAdminID(), admin.getCompanyID(), admin.getSupervisor().getSupervisorName(), admin.getSupervisor().getId()));
			
			final ActionMessages errors = new ActionMessages();
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.grantLoginPermissionBySupervisor"));
			
			saveErrors(request, errors);
		} else {
			final ActionMessages errors = new ActionMessages();
			
			checkAndCorrectFormData(loginPermissionForm, errors);
			
			if(errors.isEmpty()) {
				final ActionMessages messages = new ActionMessages();

				if(UNLIMITED_LOGIN_PERMISSION.equals(loginPermissionForm.getLimit())) {
					grantUnlimitedLoginPermission(loginPermissionForm, admin, errors, messages);
				} else {
					grantLimitedLoginPermission(loginPermissionForm, admin, errors, messages);
				}
				
				if(!messages.isEmpty()) {
					saveMessages(request, messages);
				}
			}
			
			if(!errors.isEmpty()) {
				saveErrors(request, errors);
			}
		}
				
		return mapping.findForward("view");
	}
	
	private final void grantLimitedLoginPermission(final SupervisorGrantLoginPermissionForm loginPermissionForm, final ComAdmin admin, final ActionMessages errors, final ActionMessages messages) {
		final SimpleDateFormat dateFormat = admin.getDateFormat();
        
        try {
	        final Date expireDateFromForm = dateFormat.parse(loginPermissionForm.getExpireDateLocalized());
	        
	        // As long as the user can only specify a date, the permission is valid including this day, so we have to add 1 day to achieve this.
	        final GregorianCalendar cal = new GregorianCalendar();
	        cal.setTime(expireDateFromForm);
	        cal.add(Calendar.DAY_OF_MONTH, 1);
	        final Date expireDate = cal.getTime();
	        
	        try {
	        	if(loginPermissionForm.getDepartmentID() > 0) {
	        		final Department department = this.departmentService.getDepartmentByID(loginPermissionForm.getDepartmentID());
	        		
			        supervisorLoginPermissionService.grantLoginPermissionToDepartment(admin, loginPermissionForm.getDepartmentID(), expireDate);
			        
			        logToUal(admin, department, expireDateFromForm);
			        
			        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("settings.supervisor.loginGranted", DepartmentI18n.translateDepartmentName(department, admin.getLocale()), department.getId()));
	        	} else if(loginPermissionForm.getDepartmentID() == ComUserSelfServiceGrantSupervisorLoginPermissionAction.ALL_DEPARTMENTS_ID) {
	        		this.supervisorLoginPermissionService.grantLoginPermissionToAllDepartments(admin, expireDate);
			        
			        logToUal(admin, null, expireDateFromForm);
			        
			        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("settings.supervisor.loginGrantedToAll"));
	        	}

		        // Reset form values, to avoid that user accidentally grants permission
				loginPermissionForm.clearData();
	        } catch(final UnknownSupervisorIdException e) {
	        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("supervisor.error.unknown_id"));
	        } catch(final Exception e) {
	        	logger.error(String.format("Error granting limited login permission to department %d (all if 0)", loginPermissionForm.getDepartmentID()), e);
	        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("supervisor.error.general"));
	        }
        } catch(final ParseException e) {
        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.date.format"));
        }
	}
	
	private final void grantUnlimitedLoginPermission(final SupervisorGrantLoginPermissionForm loginPermissionForm, final ComAdmin admin, final ActionMessages errors, final ActionMessages messages) {
		try {
        	if(loginPermissionForm.getDepartmentID() > 0) {
		        final Department department = this.departmentService.getDepartmentByID(loginPermissionForm.getDepartmentID());
		        this.supervisorLoginPermissionService.grantUnlimitedLoginPermissionToDepartment(admin, department.getId());
		        
		        logToUal(admin, department, null);
		        
		        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("settings.supervisor.loginGranted.unlimited", DepartmentI18n.translateDepartmentName(department, admin.getLocale()), department.getId()));
        	} else if(loginPermissionForm.getDepartmentID() == ComUserSelfServiceGrantSupervisorLoginPermissionAction.ALL_DEPARTMENTS_ID) {
        		supervisorLoginPermissionService.grantUnlimitedLoginPermissionToAllDepartments(admin);
		        
		        logToUal(admin, null, null);
		        
		        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("settings.supervisor.loginGrantedToAll.unlimited"));
        	}

	        // Reset form values, to avoid that user accidentally grants permission
			loginPermissionForm.clearData();
        } catch(final UnknownSupervisorIdException e) {
        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("supervisor.error.unknown_id"));
        } catch(final Exception e) {
        	logger.error(String.format("Error granting unlimited login permission to department %d (all if 0)", loginPermissionForm.getDepartmentID()), e);
        	// TODO Why the f#@* is the only generic error message "An error occured" related to webservices????
        	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("supervisor.error.general"));
        }
	}

	private final void checkAndCorrectFormData(final SupervisorGrantLoginPermissionForm loginPermissionForm, final ActionMessages errors) {
		// Correct data
		if(loginPermissionForm.getDepartmentID() < 0 && !OneOf.oneIntOf(loginPermissionForm.getDepartmentID(), ALL_DEPARTMENTS_ID)) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Received invalid department ID %d. Changed it to 0", loginPermissionForm.getDepartmentID()));
			}
			
			loginPermissionForm.setDepartmentID(0);
		}
		
		// Check data
		if(loginPermissionForm.getDepartmentID() == 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.noDepartmentSelected"));
		}
		
		if(!OneOf.oneObjectOf(loginPermissionForm.getLimit(), "LIMITED", "UNLIMITED")) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.noLoginLimitSelected"));
		}
		
		if(LIMITED_LOGIN_PERMISSION.equals(loginPermissionForm.getLimit()) && StringUtils.isEmpty(loginPermissionForm.getExpireDateLocalized()) ) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.noExpireDate"));
		}
	}
	
	private final void logToUal(final ComAdmin admin, final Department department, final Date expireDate) {
		if(expireDate != null) {
			final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
			final String formattedDate = dateFormat.format(expireDate);
			
			final String msg = department != null
					? String.format(
							"User '%s' (%d) granted login permission to department '%s' until including %s",
							admin.getUsername(),
							admin.getAdminID(),
							department.getSlug(),
							formattedDate
							)
					: String.format(
							"User '%s' (%d) granted login permission to ALL departments until including %s",
							admin.getUsername(),
							admin.getAdminID(),
							formattedDate
							)
							;
			
	        writeUserActivityLog(admin, "grant login permission", msg, logger);
		} else {
			final String msg = department != null
					? String.format(
							"User '%s' (%d) granted UNLIMITED login permission to department '%s'",
							admin.getUsername(),
							admin.getAdminID(),
							department.getSlug()
							)
					: String.format(
							"User '%s' (%d) granted UNLIMITED login permission to ALL departments",
							admin.getUsername(),
							admin.getAdminID()
							)
							;
			
	        writeUserActivityLog(admin, "grant login permission", msg, logger);
		}
	}

	@Required
	public final void setSupervisorLoginPermissionService(final SupervisorLoginPermissionService service) {
		this.supervisorLoginPermissionService = Objects.requireNonNull(service, "SupervisorLoginPermissionServicecannot be null");
	}

	@Required
	public final void setDepartmentService(final DepartmentService service) {
		this.departmentService = Objects.requireNonNull(service, "DepartmentService is null");
	}
}
