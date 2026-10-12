/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.logon.service.impl;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

import java.util.List;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.LicenseType;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.logon.service.AdminSessionService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.security.sessionbinding.web.service.SessionBindingService;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service
public class AdminSessionServiceImpl implements AdminSessionService {

    private final LogonService logonService;
    private final SessionBindingService sessionBindingService;
    private final ConfigService configService;

    public AdminSessionServiceImpl(
            LogonService logonService,
            SessionBindingService sessionBindingService,
            ConfigService configService
    ) {
        this.logonService = logonService;
        this.sessionBindingService = sessionBindingService;
        this.configService = configService;
    }

    @Override
    public void fillSession(
            Admin admin,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request context bound to current thread — #fillSession must be called within an active HTTP request");
        }

        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin, SCOPE_SESSION);
        attributes.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, logonService.getPreferences(admin), SCOPE_SESSION);

        sessionBindingService.bindSession(request, response);
        logonService.updateSessionsLanguagesAttributes(admin);

        attributes.setAttribute("emmLayoutBase", logonService.getEmmLayoutBase(admin), SCOPE_SESSION);
        attributes.setAttribute("userName", defaultString(admin.getUsername()), SCOPE_SESSION);
        attributes.setAttribute("firstName", defaultString(admin.getFirstName()), SCOPE_SESSION);
        attributes.setAttribute("fullName", admin.getFullname(), SCOPE_SESSION);

        Optional.ofNullable(admin.getSupervisor())
                .ifPresent(supervisor -> {
                    attributes.setAttribute("supervisorName", supervisor.getFullName(), SCOPE_SESSION);
                    attributes.setAttribute("supervisorUsername", supervisor.getSupervisorName(), SCOPE_SESSION);
                });

        attributes.setAttribute("companyShortName", admin.getCompany().getShortname(), SCOPE_SESSION);
        attributes.setAttribute("companyID", admin.getCompany().getId(), SCOPE_SESSION);
        attributes.setAttribute("adminTimezone", admin.getAdminTimezone(), SCOPE_SESSION);

        if (shouldExposeVersionNumber()) {
            attributes.setAttribute("versionNumber", configService.getValue(ConfigValue.ApplicationVersion), SCOPE_SESSION);
        }
    }

    private boolean shouldExposeVersionNumber() {
        if (!configService.getBooleanValue(ConfigValue.IsLiveInstance)) {
            return true;
        }

        LicenseType licenseType = configService.getLicenseType();
        if (List.of(LicenseType.Inhouse, LicenseType.OpenEMM, LicenseType.OpenEMM_Plus).contains(licenseType)) {
            return true;
        }

        return configService.getIntegerValue(ConfigValue.System_Licence) == 0;
    }

}
