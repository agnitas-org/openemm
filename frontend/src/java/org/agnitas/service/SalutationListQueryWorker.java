/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.concurrent.Callable;

import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.ComTitleDao;

/**
 * wrapper for a long sql query. It will be used for asynchronous tasks
 */
public class SalutationListQueryWorker implements Callable<PaginatedListImpl<SalutationEntry>> {
	private ComTitleDao titleDao;
	private String sort;
	private String direction;
	private int page;
	private int rownums;
	private int companyID;

    public SalutationListQueryWorker(ComTitleDao dao, @VelocityCheck int companyID, String sort, String direction, int page, int rownums ) {
		this.titleDao = dao;
		this.sort = sort;
		this.direction = direction;
		this.page = page;
		this.rownums = rownums;
		this.companyID = companyID;
	}

    @Override
	public PaginatedListImpl<SalutationEntry> call() throws Exception {
	   return titleDao.getSalutationList(companyID, sort, direction, page, rownums); 
	}
}
