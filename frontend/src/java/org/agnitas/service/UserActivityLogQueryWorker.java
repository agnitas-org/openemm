/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.LoggedUserAction;

import com.agnitas.beans.Admin;

public class UserActivityLogQueryWorker implements Callable<PaginatedListImpl<LoggedUserAction>> {
	private UserActivityLogService userActivityLogService;
	private Admin admin;
	private int pageNumber;
	private int rownums;
	private String sort;
	private String direction;
	private List<AdminEntry> admins;
	private String username;
	private int action;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String description;


	public UserActivityLogQueryWorker(UserActivityLogService userActivityLogService, Admin admin, int pageNumber, int rownums, String username, int action, LocalDate fromDate, LocalDate toDate, String description, String sort, String direction, List<AdminEntry> admins) {
		this.userActivityLogService = userActivityLogService;
		this.admin = admin;
		this.pageNumber = pageNumber;
		this.rownums = rownums;
		this.sort = sort;
		this.direction = direction;
		this.admins = admins;
		this.username = username;
		this.action = action;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.description = description;
	}

	@Override
	public PaginatedListImpl<LoggedUserAction> call() throws Exception {
		return userActivityLogService.getUserActivityLogByFilter(admin, username, action, fromDate, toDate, description, pageNumber, rownums, sort, direction, admins);
	}
}
