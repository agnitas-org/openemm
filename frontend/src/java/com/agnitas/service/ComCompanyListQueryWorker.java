/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.company.bean.CompanyEntry;

/**
 * wrapper for a long sql query. It will be used for asynchronous tasks
 */
public class ComCompanyListQueryWorker implements Callable<PaginatedListImpl<CompanyEntry>>, Serializable {
	private static final long serialVersionUID = -3047853895576634885L;

	private ComCompanyDao companyDao;
	private String sort;
	private String direction;
	private int page;
	private int rownums;
	private int companyID;

	public ComCompanyListQueryWorker(ComCompanyDao dao, @VelocityCheck int companyID, String sort, String direction, int page, int rownums) {
		this.companyDao = dao;
		this.sort = sort;
		this.direction = direction;
		this.page = page;
		this.rownums = rownums;
		this.companyID = companyID;
	}

	@Override
	public PaginatedListImpl<CompanyEntry> call() throws Exception {
		return companyDao.getCompanyList(companyID, sort, direction, page, rownums);
	}
}
