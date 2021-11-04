/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.dao.LicenseDao;

public class LicenseDaoImpl extends BaseDaoImpl implements LicenseDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LicenseDaoImpl.class);
	
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
}
