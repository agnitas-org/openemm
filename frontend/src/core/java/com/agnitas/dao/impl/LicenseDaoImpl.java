/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Date;

import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.impl.mapper.BlobRowMapper;
import com.agnitas.emm.core.commons.util.ConfigService;

public class LicenseDaoImpl extends BaseDaoImpl implements LicenseDao {

	@Override
	public boolean hasLicenseData() {
		return selectInt("SELECT COUNT(*) FROM license_tbl WHERE name = ? AND data IS NOT NULL", "LicenseData") > 0;
	}

	@Override
	public byte[] getLicenseData() {
		return selectObjectDefaultNull("SELECT data FROM license_tbl WHERE name = ?", BlobRowMapper.INSTANCE, "LicenseData");
	}

	@Override
	public void storeLicense(byte[] licenseData, byte[] licenseSignatureData, Date licenseDate) {
		synchronized (this) {
			if (licenseDate != null) {
				try {
					update("DELETE FROM license_tbl WHERE change_date < ? OR change_date IS NULL", licenseDate);
					
					if (selectInt("SELECT COUNT(*) FROM license_tbl") == 0) {
						update("INSERT INTO license_tbl (name, change_date) VALUES (?, ?)", "LicenseData", licenseDate);
						updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
						
						if (licenseSignatureData != null) {
							// OpenEMM has no license signature
							update("INSERT INTO license_tbl (name, change_date) VALUES (?, ?)", "LicenseSignature", licenseDate);
							updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
						}
					}
				} catch (Exception e) {
					logger.error("Error storing license data", e);
					throw e;
				}
			} else {
				try {
					int touchedLines = update("UPDATE license_tbl SET change_date = CURRENT_TIMESTAMP WHERE name = ?", "LicenseData");
					if (touchedLines > 0) {
						updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
					} else {
						update("INSERT INTO license_tbl (name, change_date) VALUES (?, CURRENT_TIMESTAMP)", "LicenseData");
						updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
					}
				} catch (Exception e) {
					logger.error("Error storing license data", e);
					throw e;
				}
	
				if (licenseSignatureData != null) {
					// OpenEMM has no license signature
					try {
						int touchedLines = update("UPDATE license_tbl SET change_date = CURRENT_TIMESTAMP WHERE name = ?", "LicenseSignature");
						if (touchedLines > 0) {
							updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
						} else {
							update("INSERT INTO license_tbl (name, change_date) VALUES (?, CURRENT_TIMESTAMP)", "LicenseSignature");
							updateBlob("UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
						}
					} catch (Exception e) {
						logger.error("Error storing license signature data", e);
						throw e;
					}
				}
			}
		}
	}

	@Override
	public byte[] getLicenseSignatureData() {
		return selectObjectDefaultNull("SELECT data FROM license_tbl WHERE name = ?", BlobRowMapper.INSTANCE, "LicenseSignature");
	}

	@Override
	public int getHighestAccessLimitingTargetgroupsPerCompany() {
		if (ConfigService.getInstance().isAccessLimitingTargetgroupsSupported()) {
			return selectIntWithDefaultValue("SELECT MAX(amount) FROM (SELECT company_id, COUNT(*) as amount FROM dyn_target_tbl WHERE is_access_limiting = 1 GROUP BY company_id) subsel", 0);
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfAccessLimitingMailinglists(int companyID) {
		if (ConfigService.getInstance().isDisabledMailingListsSupported()) {
			return selectIntWithDefaultValue("SELECT COUNT(DISTINCT mailinglist_id) FROM disabled_mailinglist_tbl WHERE company_id = ?", 0, companyID);
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfCompanyReferenceTables(int companyID) {
		return selectInt("SELECT COUNT(*) FROM reference_tbl WHERE company_id = ? AND deleted = 0", companyID);
	}
}
