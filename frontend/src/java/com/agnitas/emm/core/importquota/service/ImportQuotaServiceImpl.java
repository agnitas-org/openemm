/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.importquota.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportException;

import com.agnitas.emm.core.importquota.common.ImportSize;
import com.agnitas.emm.core.importquota.common.ImportType;
import com.agnitas.emm.core.importquota.dao.ImportQuotaDao;

public final class ImportQuotaServiceImpl implements ImportQuotaRegisterService, ImportQuotaCheckService {
	
	private final ImportQuotaDao quotaDao;
	private final ConfigService configService;
	
	public ImportQuotaServiceImpl(final ImportQuotaDao quotaDao, final ConfigService configService) {
		this.quotaDao = Objects.requireNonNull(quotaDao, "quota dao");
		this.configService = Objects.requireNonNull(configService, "config service");
	}

	@Override
	public final void registerImportSize(final int companyID, final int importID, final ImportType importType, final int linesCount) {
		if(importQuotaEnabled(companyID)) {
			if(linesCount > 0) {
				this.quotaDao.saveImportSize(companyID, importID, ZonedDateTime.now(), importType, linesCount);
			}
		}
	}

	@Override
	public final void checkImportQuota(final int companyID, final int linesToImport) throws ImportException {
		if(importQuotaEnabled(companyID)) {
			final long linesAfterImport = totalImportLinesCount(companyID, linesToImport);
			
			final long importLineQuota = configService.getIntegerValue(ConfigValue.ImportQuota, companyID);
			
			if(linesAfterImport > importLineQuota)
				throw new ImportException(false, "error.import.lineQuotaExceeded", importLineQuota);
		}
	} 
	
	@Override
	public boolean checkWarningLimitReached(final int companyID, final int importedLines) {
		if(importQuotaEnabled(companyID)) {
			final long linesAfterImport = totalImportLinesCount(companyID, importedLines);
			
			final long importLineQuota = configService.getIntegerValue(ConfigValue.ImportQuotaWarning, companyID);
			
			return linesAfterImport > importLineQuota;
		} else {
			return false;
		}
	}

	@Override
	public long getImportLimit(int companyID) {
		return this.configService.getLongValue(ConfigValue.ImportQuota, companyID);
	}

	@Override
	public final long totalImportLinesCount(final int companyID, final int linesToImport) {
		final LocalDate today = LocalDate.now();
		final LocalTime midnight = LocalTime.of(0, 0);
		final ZonedDateTime start = ZonedDateTime.of(LocalDateTime.of(today, midnight), ZoneId.systemDefault());
		final ZonedDateTime end = start.plusDays(1);
		
		final long currentLineCount = totalImportsLineCount(companyID, start, end);
		final long linesAfterImport = currentLineCount + linesToImport;
		
		return linesAfterImport;
	}

	private long totalImportsLineCount(final int companyID, final ZonedDateTime from, final ZonedDateTime untilExclusive) {
		final List<ImportSize> importSizeList = this.quotaDao.listImportSized(companyID, from, untilExclusive);
		
		return importSizeList.stream().mapToLong(size -> size.getLineCount()).sum();
	}

	private final boolean importQuotaEnabled(final int companyID) {
		return configService.getBooleanValue(ConfigValue.ImportQuotaEnabled, companyID);
	}
}
