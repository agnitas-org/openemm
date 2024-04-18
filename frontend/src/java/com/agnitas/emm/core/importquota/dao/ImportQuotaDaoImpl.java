/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.importquota.dao;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.importquota.common.ImportSize;
import com.agnitas.emm.core.importquota.common.ImportType;

public final class ImportQuotaDaoImpl extends BaseDaoImpl implements ImportQuotaDao {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ImportQuotaDaoImpl.class);

	@Override
	public final void saveImportSize(final int companyID, final int importID, final ZonedDateTime timestamp, final ImportType importType, final int linesCount) {
		final String sql = "INSERT INTO import_size_tbl (company_ref, import_ref, import_type, timestamp, lines_count) VALUES (?,?,?,?,?)";
		
		this.update(LOGGER, sql, companyID, importID, importType.name(), Date.from(timestamp.toInstant()), linesCount);
	}

	@Override
	public final List<ImportSize> listImportSized(final int companyID, final ZonedDateTime from, final ZonedDateTime untilExclusive) {
		final String sql = "SELECT company_ref, import_ref, import_type, timestamp, lines_count FROM import_size_tbl WHERE company_ref=? AND timestamp BETWEEN ? AND ?";
		
		return this.select(LOGGER, sql, ImportSizeRowMapper.INSTANCE, companyID, Date.from(from.toInstant()), Date.from(untilExclusive.toInstant()));
	}

	@Override
	public void clearAllImportData(int companyId) {
		final String sql = "DELETE FROM import_size_tbl WHERE company_ref=?";

		this.update(LOGGER, sql, companyId);
	}

}
