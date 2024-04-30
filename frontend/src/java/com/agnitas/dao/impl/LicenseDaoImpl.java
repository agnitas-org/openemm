/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.LicenseDao;

public class LicenseDaoImpl extends BaseDaoImpl implements LicenseDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(LicenseDaoImpl.class);
	
	@Override
	public boolean hasLicenseData() throws Exception {
		return selectInt(logger, "SELECT COUNT(*) FROM license_tbl WHERE name = ? AND data IS NOT NULL", "LicenseData") > 0;
	}

	@Override
	public byte[] getLicenseData() throws Exception {
		ByteArrayOutputStream interimStream = new ByteArrayOutputStream();
		writeBlobInStream(logger, "SELECT data FROM license_tbl WHERE name = ?", interimStream, "LicenseData");
		return interimStream.toByteArray();
	}

	@Override
	public void storeLicense(byte[] licenseData, byte[] licenseSignatureData, Date licenseDate) throws Exception {
		synchronized (this) {
			if (licenseDate != null) {
				try {
					update(logger, "DELETE FROM license_tbl WHERE change_date < ? OR change_date IS NULL", licenseDate);
					
					if (selectInt(logger, "SELECT COUNT(*) FROM license_tbl") == 0) {
						update(logger, "INSERT INTO license_tbl (name, change_date) VALUES (?, ?)", "LicenseData", licenseDate);
						updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
						
						if (licenseSignatureData != null) {
							// OpenEMM has no license signature
							update(logger, "INSERT INTO license_tbl (name, change_date) VALUES (?, ?)", "LicenseSignature", licenseDate);
							updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
						}
					}
				} catch (Exception e) {
					logger.error("Error storing license data", e);
					throw e;
				}
			} else {
				try {
					int touchedLines = update(logger, "UPDATE license_tbl SET change_date = CURRENT_TIMESTAMP WHERE name = ?", "LicenseData");
					if (touchedLines > 0) {
						updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
					} else {
						update(logger, "INSERT INTO license_tbl (name, change_date) VALUES (?, CURRENT_TIMESTAMP)", "LicenseData");
						updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseData, "LicenseData");
					}
				} catch (Exception e) {
					logger.error("Error storing license data", e);
					throw e;
				}
	
				if (licenseSignatureData != null) {
					// OpenEMM has no license signature
					try {
						int touchedLines = update(logger, "UPDATE license_tbl SET change_date = CURRENT_TIMESTAMP WHERE name = ?", "LicenseSignature");
						if (touchedLines > 0) {
							updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
						} else {
							update(logger, "INSERT INTO license_tbl (name, change_date) VALUES (?, CURRENT_TIMESTAMP)", "LicenseSignature");
							updateBlob(logger, "UPDATE license_tbl SET data = ? WHERE name = ?", licenseSignatureData, "LicenseSignature");
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
	public byte[] getLicenseSignatureData() throws Exception {
		try {
			ByteArrayOutputStream interimStream = new ByteArrayOutputStream();
			writeBlobInStream(logger, "SELECT data FROM license_tbl WHERE name = ?", interimStream, "LicenseSignature");
			return interimStream.toByteArray();
		} catch (Exception e) {
			logger.error("Error reading license data", e);
			
			return null;
		}
	}

	@Override
	public int getHighestAccessLimitingMailinglistsPerCompany() {
		if (isDisabledMailingListsSupported()) {
			return selectIntWithDefaultValue(logger, "SELECT MAX(amount) FROM (SELECT company_id, COUNT(DISTINCT mailinglist_id) as amount FROM disabled_mailinglist_tbl GROUP BY company_id) subsel", 0);
		} else {
			return 0;
		}
	}

	@Override
	public int getHighestAccessLimitingTargetgroupsPerCompany() {
		if (isAccessLimitingTargetgroupsSupported()) {
			return selectIntWithDefaultValue(logger, "SELECT MAX(amount) FROM (SELECT company_id, COUNT(*) as amount FROM dyn_target_tbl WHERE is_access_limiting = 1 GROUP BY company_id) subsel", 0);
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfAccessLimitingMailinglists(int companyID) {
		if (isDisabledMailingListsSupported()) {
			return selectIntWithDefaultValue(logger, "SELECT COUNT(DISTINCT mailinglist_id) FROM disabled_mailinglist_tbl WHERE company_id = ?", 0, companyID);
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfAccessLimitingTargetgroups(int companyID) {
		if (isAccessLimitingTargetgroupsSupported()) {
			return selectIntWithDefaultValue(logger, "SELECT COUNT(*) FROM disabled_mailinglist_tbl WHERE dyn_target_tbl WHERE is_access_limiting = 1 AND company_id = ?", 0, companyID);
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfCompanyReferenceTables(int companyID) {
		return selectInt(logger, "SELECT count(*) FROM reference_tbl WHERE company_id = ? AND deleted = 0", companyID);
	}
}
