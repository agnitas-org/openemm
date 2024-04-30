/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfileImportErrorRecipientQueryWorker implements Callable<PaginatedListImpl<Map<String, Object>>> {
    private static final transient Logger logger = LogManager.getLogger(ProfileImportErrorRecipientQueryWorker.class);
    
	private ImportRecipientsDao importRecipientsDao;
	private List<String> columns;
	private String sort;
	private String direction;
	private int previousFullListSize;
	private int page;
	private int rownums;
	private String temporaryErrorTableName;

	public ProfileImportErrorRecipientQueryWorker(ImportRecipientsDao importRecipientsDao, String temporaryErrorTableName, String sort, String direction, int page, int rownums, int previousFullListSize, List<String> columns) {
		this.importRecipientsDao = importRecipientsDao;
		this.temporaryErrorTableName = temporaryErrorTableName;
		this.sort = sort;
		this.direction = direction;
		this.page = page;
		this.rownums = rownums;
		this.previousFullListSize = previousFullListSize;
		this.columns = columns;
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> call() throws Exception {
		try {
			return importRecipientsDao.getInvalidRecipientList(temporaryErrorTableName, columns, sort, direction, page, rownums, previousFullListSize);
		} catch (Exception e) {
			logger.error("Error in ProfileImportErrorRecipientQueryWorker: " + e.getMessage(), e);
			return null;
		}
	}
}
