/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.impl.DkimKeyEntry;
import com.agnitas.dao.ComDkimDao;

/**
 * DAO handler for DKIM-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComDkimDaoImpl extends BaseDaoImpl implements ComDkimDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComDkimDaoImpl.class);
	
	private ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public boolean existsDkimKeyForDomain(int companyID, String domainname) {
		if (companyID <= 0 || StringUtils.isEmpty(domainname)) {
			return false;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return false;
		} else {
			if (selectInt(logger, "SELECT COUNT(*) FROM dkim_key_tbl WHERE company_id = ? AND LOWER(domain) = LOWER(?) AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP)", companyID, domainname) > 0) {
				return true;
			} else {
				String mainDomainName = getDomainOfSubDomain(domainname);
				return selectInt(logger, "SELECT COUNT(*) FROM dkim_key_tbl WHERE company_id = ? AND LOWER(domain) = LOWER(?) AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP)", companyID, mainDomainName) > 0;
			}
		}
	}
	
	@Override
	public boolean deleteDkimKeyByCompany(int companyID) {
		if (companyID <= 0) {
			return false;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return false;
		} else {
			try {
				update(logger, "DELETE from dkim_key_tbl WHERE company_id = ?", companyID);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	private static String getDomainOfSubDomain(String domainName) {
        String[] domainParts = domainName.split("\\.");
		if (domainParts.length >= 2) {
			return domainParts[domainParts.length - 2] + "." + domainParts[domainParts.length - 1];
		} else {
			return domainName;
		}
    }

	@Override
	public DkimKeyEntry getDkimKeyForDomain(int companyID, String domainname, boolean allowNonMatchingFallback) {
		if (companyID <= 0 || StringUtils.isEmpty(domainname)) {
			return null;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "dkim_key_tbl")) {
			return null;
		} else {
			List<DkimKeyEntry> resultExactDomain = select(logger, "SELECT dkim_id, company_id, domain, selector, creation_date, timestamp, valid_start, valid_end, domain_key, domain_key_encrypted FROM dkim_key_tbl WHERE company_id = ? AND LOWER(domain) = LOWER(?) AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP) ORDER BY TIMESTAMP ASC", new DkimKeyEntry_RowMapper(), companyID, domainname);
			if (resultExactDomain.size() > 0) {
				return resultExactDomain.get(0);
			} else {
				String mainDomainName = getDomainOfSubDomain(domainname);
				List<DkimKeyEntry> resultMainDomain = select(logger, "SELECT dkim_id, company_id, domain, selector, creation_date, timestamp, valid_start, valid_end, domain_key, domain_key_encrypted FROM dkim_key_tbl WHERE company_id = ? AND LOWER(domain) = LOWER(?) AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP) ORDER BY TIMESTAMP ASC", new DkimKeyEntry_RowMapper(), companyID, mainDomainName);
				if (resultMainDomain.size() > 0) {
					return resultMainDomain.get(0);
				} else {
					List<DkimKeyEntry> resultMasterDomain = select(logger, "SELECT dkim_id, company_id, domain, selector, creation_date, timestamp, valid_start, valid_end, domain_key, domain_key_encrypted FROM dkim_key_tbl WHERE company_id = 1 AND LOWER(domain) = LOWER(?) AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP) ORDER BY TIMESTAMP ASC", new DkimKeyEntry_RowMapper(), mainDomainName);
					if (resultMasterDomain.size() > 0) {
						return resultMasterDomain.get(0);
					}
					
					if (allowNonMatchingFallback && configService.getBooleanValue(ConfigValue.DkimLocalActivation, companyID)) {
						List<DkimKeyEntry> resultLocal = select(logger, "SELECT dkim_id, company_id, domain, selector, creation_date, timestamp, valid_start, valid_end, domain_key, domain_key_encrypted FROM dkim_key_tbl WHERE company_id = ? AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP) ORDER BY TIMESTAMP ASC", new DkimKeyEntry_RowMapper(), companyID);
						if (resultLocal.size() > 0) {
							return resultLocal.get(0);
						}
					}
					
					if (allowNonMatchingFallback && configService.getBooleanValue(ConfigValue.DkimGlobalActivation, companyID)) {
						List<DkimKeyEntry> resultGlobal = select(logger, "SELECT dkim_id, company_id, domain, selector, creation_date, timestamp, valid_start, valid_end, domain_key, domain_key_encrypted FROM dkim_key_tbl WHERE company_id = 0 AND (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND (valid_end IS NULL OR valid_end >= CURRENT_TIMESTAMP) ORDER BY TIMESTAMP ASC", new DkimKeyEntry_RowMapper());
						if (resultGlobal.size() > 0) {
							return resultGlobal.get(0);
						}
					}
					
					return null;
				}
			}
		}
	}
	
    protected class DkimKeyEntry_RowMapper implements RowMapper<DkimKeyEntry> {
		@Override
		public DkimKeyEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			DkimKeyEntry dkimKeyEntry = new DkimKeyEntry();

			dkimKeyEntry.setDkimID(resultSet.getBigDecimal("dkim_id").intValue());
			dkimKeyEntry.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			dkimKeyEntry.setDomain(resultSet.getString("domain"));
			dkimKeyEntry.setSelector(resultSet.getString("selector"));
			dkimKeyEntry.setCreationDate(resultSet.getTimestamp("creation_date"));
			dkimKeyEntry.setChangeDate(resultSet.getTimestamp("timestamp"));
			dkimKeyEntry.setValidStartDate(resultSet.getTimestamp("valid_start"));
			dkimKeyEntry.setValidEndDate(resultSet.getTimestamp("valid_end"));
			dkimKeyEntry.setDomainKey(resultSet.getString("domain_key"));
			dkimKeyEntry.setDomainKeyEncrypted(resultSet.getString("domain_key_encrypted"));

			return dkimKeyEntry;
		}
	}
}
