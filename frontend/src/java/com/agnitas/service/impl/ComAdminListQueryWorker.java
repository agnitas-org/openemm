/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.concurrent.Callable;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.admin.service.AdminService;

/**
 * wrapper for a long sql query. It will be used for asynchronous tasks
 */
public class ComAdminListQueryWorker implements Callable<PaginatedListImpl<AdminEntry>> {
    private AdminService adminService;
    private String sort;
    private String direction;
    private int page;
    private int rownums;
    private int companyID;
    private String searchFirstName;
    private String searchLastName;
    private String searchEmail;
    private String searchCompany;
    private Integer filterCompanyId;
    private Integer filterAdminGroupId;
    private Integer filterMailinglistId;
    private String filterLanguage;

    public ComAdminListQueryWorker(AdminService adminService, @VelocityCheck int companyID, String searchFirstName, String searchLastName, String searchEmail,
                                   String searchCompany, Integer filterCompanyId, Integer filterAdminGroupId, Integer filterMailinglistId, String filterLanguage,
                                   String sort, String direction, int page, int rownums) {
        this.adminService = adminService;
        this.sort = sort;
        this.direction = direction;
        this.page = page;
        this.rownums = rownums;
        this.companyID = companyID;
        this.searchFirstName = searchFirstName;
        this.searchLastName = searchLastName;
        this.searchEmail = searchEmail;
        this.searchCompany = searchCompany;
        this.filterCompanyId = filterCompanyId;
        this.filterAdminGroupId = filterAdminGroupId;
        this.filterMailinglistId = filterMailinglistId;
        this.filterLanguage = filterLanguage;
    }

    @Override
	public PaginatedListImpl<AdminEntry> call() throws Exception {
        return adminService.getAdminList(companyID, searchFirstName, searchLastName, searchEmail, searchCompany,
                filterCompanyId, filterAdminGroupId, filterMailinglistId, filterLanguage, sort, direction, page, rownums);
    }
}
