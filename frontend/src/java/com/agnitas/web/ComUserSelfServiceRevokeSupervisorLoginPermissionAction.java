/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;
import com.agnitas.web.forms.SupervisorRevokeLoginPermissionForm;

public final class ComUserSelfServiceRevokeSupervisorLoginPermissionAction extends StrutsActionBase {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUserSelfServiceRevokeSupervisorLoginPermissionAction.class);
	
	private SupervisorLoginPermissionService supervisorLoginPermissionService;

	@Override
	public final ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final ComAdmin admin = AgnUtils.getAdmin(request);
		final SupervisorRevokeLoginPermissionForm revokePermissionForm = (SupervisorRevokeLoginPermissionForm) form;

		// Check, that there is no supervisor attached to the admin (= admin with supervisor login)
		if(admin.getSupervisor() != null) {
			logger.warn(String.format("Attempt to grant supervisor login permission for EMM user '%s' (ID %d, company %d), logged in with supervisor '%s' (%d)", admin.getUsername(), admin.getAdminID(), admin.getCompanyID(), admin.getSupervisor().getSupervisorName(), admin.getSupervisor().getId()));
			
			final ActionMessages errors = new ActionMessages();
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.revokeLoginPermissionBySupervisor"));
			
			saveErrors(request, errors);
		} else {
			try {
				supervisorLoginPermissionService.revokeSupervisorLoginPermission(admin.getAdminID(), revokePermissionForm.getPermissionID());

				logToUal(admin, revokePermissionForm.getPermissionID());

				final ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("settings.supervisor.loginPermissionRevoked"));
				saveMessages(request, messages);
			} catch(final UnknownSupervisorLoginPermissionException e) {
				final ActionMessages errors = new ActionMessages();
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.settings.supervisor.revokingLoginPermission"));
				saveErrors(request, errors);
				
				logger.error(String.format("Error revoking login permission %d for admin %d", revokePermissionForm.getPermissionID(), admin.getAdminID()), e);
			}
		}
				
		return mapping.findForward("view");
	}
	
	private final Department departmentOrNullForLoginPermission(final int loginPermission) throws UnknownSupervisorLoginPermissionException, UnknownDepartmentIdException {
		final Department department = this.supervisorLoginPermissionService.getDepartmentForLoginPermission(loginPermission);
		
		return department;
	}
	
	private final void logToUal(final ComAdmin admin, final int permissionID) {
		try {
			final Department department = this.departmentOrNullForLoginPermission(permissionID);
			
			final String msg = department != null 
					? String.format(
							"User '%s' (%d) revoked login permission of department '%s'", 
							admin.getUsername(), 
							admin.getAdminID(), 
							department.getSlug()
							)
					: String.format(
							"User '%s' (%d) revoked login permission of ALL departments", 
							admin.getUsername(), 
							admin.getAdminID()
							);
			
	        writeUserActivityLog(admin, "grant login permission", msg, logger);
		} catch(final UnknownSupervisorLoginPermissionException | UnknownDepartmentIdException e) {
			final String msg = String.format(
					"User '%s' (%d) revoked login permission of unknown department",
					admin.getUsername(), 
					admin.getAdminID());
			
	        writeUserActivityLog(admin, "revoked login permission", msg, logger);
		}
	}
	

	@Required
	public final void setSupervisorLoginPermissionService(final SupervisorLoginPermissionService service) {
		this.supervisorLoginPermissionService = Objects.requireNonNull(service, "SupervisorLoginPermissionService cannot be null");
	}

}
