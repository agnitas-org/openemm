/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.service;

import static java.text.MessageFormat.format;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.useractivitylog.dao.LoggedUserAction;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.dao.SoapUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("UserActivityLogService")
public class DbUserActivityLogServiceImpl implements UserActivityLogService {

	private final UserActivityLogDao generalUserActivityLogDao;
	private final RestfulUserActivityLogDao restfulUserActivityLogDao;
	private final SoapUserActivityLogDao soapUserActivityLogDao;
	private final AdminService adminService;
	private final WebserviceUserService webserviceUserService;

	public DbUserActivityLogServiceImpl(UserActivityLogDao generalUserActivityLogDao, RestfulUserActivityLogDao restfulUserActivityLogDao,
                                        SoapUserActivityLogDao soapUserActivityLogDao, AdminService adminService,
										WebserviceUserService webserviceUserService) {
		this.generalUserActivityLogDao = generalUserActivityLogDao;
		this.restfulUserActivityLogDao = restfulUserActivityLogDao;
		this.soapUserActivityLogDao = soapUserActivityLogDao;
        this.adminService = adminService;
        this.webserviceUserService = webserviceUserService;
    }

	@Override
	public void deleteSoapActivity(Set<String> usernames) {
		soapUserActivityLogDao.deleteByUsernames(usernames);
	}

	@Override
	public void deleteActivity(List<Admin> admins) {
		Map<Boolean, List<Admin>> adminsMap = admins.stream()
				.collect(Collectors.partitioningBy(Admin::isRestful));

        Map<Integer, Set<String>> restfulUsernames = groupUsernamesByCompany(adminsMap.get(true));
        Map<Integer, Set<String>> guiUsernames = groupUsernamesByCompany(adminsMap.get(false));

        restfulUsernames.forEach((companyId, usernames) ->
                restfulUserActivityLogDao.deleteByUsernames(usernames, companyId));

        guiUsernames.forEach((companyId, usernames) ->
                generalUserActivityLogDao.deleteByUsernames(usernames, companyId));
	}

	private Map<Integer, Set<String>> groupUsernamesByCompany(List<Admin> admins) {
		return admins.stream().
				collect(Collectors.groupingBy(
						Admin::getCompanyID,
						Collectors.mapping(Admin::getUsername, Collectors.toSet()))
				);
	}

	@Override
	public Set<String> getAvailableUsernames(Admin admin, UserType userType) {
		boolean masterLogAllowed = admin.permissionAllowed(Permission.MASTERLOG_SHOW);
		boolean adminLogAllowed = admin.permissionAllowed(Permission.ADMINLOG_SHOW);

		if (!masterLogAllowed && !adminLogAllowed) {
			return Collections.emptySet();
		}

		Integer companyId = masterLogAllowed ? null : admin.getCompanyID();

		List<String> usernames;

		switch (userType) {
			case GUI -> {
				usernames = generalUserActivityLogDao.getDistinctUsernames(companyId);
				usernames.addAll(adminService.getGuiUsernames(companyId));
			}
			case REST -> {
				usernames = restfulUserActivityLogDao.getDistinctUsernames(companyId);
				usernames.addAll(adminService.getRestfulUsernames(companyId));
			}
			case SOAP -> {
				usernames = soapUserActivityLogDao.getDistinctUsernames(companyId);
				usernames.addAll(webserviceUserService.getUsernames(companyId));
			}
			default -> usernames = Collections.emptyList();
		}

		return usernames.stream()
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	@Override
	public PaginatedList<LoggedUserAction> getUserActivityLogByFilter(UserActivityLogFilter filter, Admin admin) {
		addRequiredFilterRestrictions(filter, admin);

		if (StringUtils.isBlank(filter.getSort())) {
			filter.setSort("logtime");
		}

		return generalUserActivityLogDao.getUserActivityEntries(filter);
	}

	@Override
	public PaginatedList<RestfulUserActivityAction> getRestfulUserActivityLogByFilter(RestfulUserActivityLogFilter filter, Admin admin) {
		addRequiredFilterRestrictions(filter, admin);

		if (StringUtils.isBlank(filter.getSort())) {
			filter.setSort("timestamp");
		}

		return restfulUserActivityLogDao.getUserActivityEntries(filter);
	}

	@Override
	public PaginatedList<SoapUserActivityAction> getSoapUserActivityLogByFilter(SoapUserActivityLogFilter filter, Admin admin) {
		addRequiredFilterRestrictions(filter, admin);

		if (StringUtils.isBlank(filter.getSort())) {
			filter.setSort("timestamp");
		}

		return soapUserActivityLogDao.getUserActivityEntries(filter);
	}

	@Override
	public SqlPreparedStatementManager prepareSqlStatementForDownload(UserActivityLogFilterBase filter, UserType userType, Admin admin) {
		addRequiredFilterRestrictions(filter, admin);

		if (UserType.REST.equals(userType)) {
			return restfulUserActivityLogDao.prepareSqlStatementForEntriesRetrieving(((RestfulUserActivityLogFilter) filter));
		}

		if (UserType.SOAP.equals(userType)) {
			return soapUserActivityLogDao.prepareSqlStatementForEntriesRetrieving(((SoapUserActivityLogFilter) filter));
		}

        return generalUserActivityLogDao.prepareSqlStatementForEntriesRetrieving((UserActivityLogFilter) filter);
	}

	private void addRequiredFilterRestrictions(UserActivityLogFilterBase filter, Admin admin) {
		if (admin.permissionAllowed(Permission.MASTERLOG_SHOW)) {
			return;
		}

		filter.setCompanyId(admin.getCompanyID());

		if (!admin.permissionAllowed(Permission.ADMINLOG_SHOW)) {
			filter.setUsername(admin.getUsername());
		}
	}

	@Override
	public void writeUserActivityLog(Admin admin, String action, String description) {
		generalUserActivityLogDao.writeUserActivityLog(admin, action, description);
		generalUserActivityLogDao.addAdminUseOfFeature(admin, action.trim(), new Date());
	}

	@Override
	public void writeUserActivityLog(Admin admin, UserAction action) {
		writeUserActivityLog(admin, action.getAction(), action.getDescription());
	}

    @Override
    public void writeUserActivityLog(Admin admin, String action, String description, Logger callerLog) {
        try {
            this.writeUserActivityLog(admin,action,description);
        } catch (Exception e) {
            callerLog.error(format("Error writing ActivityLog: {0}", e.getMessage()), e);
            callerLog.info("Userlog: {} {} {}", admin.getUsername(), action, description);
        }
    }

    @Override
	public void writeUserActivityLog(Admin admin, UserAction action, Logger callerLog) {
		if (action == null) {
			callerLog.error("Can't write UAL because action is null.");
			return;
		}
		writeUserActivityLog(admin, action.getAction(), action.getDescription(), callerLog);
	}

}
