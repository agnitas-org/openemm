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

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.departments.service.DepartmentService;
import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;

public final class ComUserSelfServiceShowSupervisorLoginPermissionAction extends StrutsActionBase {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComUserSelfServiceShowSupervisorLoginPermissionAction.class);

	private ConfigService configService;
	private SupervisorLoginPermissionService supervisorLoginPermissionService;
	private DepartmentService departmentService;
	
	@Override
	public final ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		final ComAdmin admin = AgnUtils.getAdmin(request);

        if (admin.getSupervisor() == null && this.configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID())) {
        	request.setAttribute("SHOW_SUPERVISOR_PERMISSION_MANAGEMENT", true);
        	request.setAttribute("DEPARTMENT_LIST", this.departmentService.listAllDepartments());
        	request.setAttribute("LOCALE_DATE_PATTERN", admin.getDateFormat().toPattern());
        	request.setAttribute("ACTIVE_LOGIN_PERMISSIONS", this.supervisorLoginPermissionService.listActiveSupervisorLoginPermissions(admin));
        	AgnUtils.setAdminDateTimeFormatPatterns(request);
        }
		
		return mapping.findForward("view");
	}

	@Required
	public final void setConfigService(final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "ConfigService cannot be null");
	}

	@Required
	public final void setSupervisorLoginPermissionService(final SupervisorLoginPermissionService service) {
		this.supervisorLoginPermissionService = Objects.requireNonNull(service, "Supervisor service cannot be null");
	}
	
	@Required
	public final void setDepartmentService(final DepartmentService service) {
		this.departmentService = Objects.requireNonNull(service, "DepartmentService is null");
	}
	

}
