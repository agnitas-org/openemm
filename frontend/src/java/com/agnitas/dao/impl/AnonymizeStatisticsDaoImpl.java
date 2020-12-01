/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.dao.AnonymizeStatisticsDao;

public class AnonymizeStatisticsDaoImpl extends BaseDaoImpl implements AnonymizeStatisticsDao {
	private static final transient Logger logger = Logger.getLogger(AnonymizeStatisticsDaoImpl.class);
	
	@Override
	public void anonymizeStatistics(final int companyID, final boolean anonymizeAll) throws Exception {
		
		final String trackingVetoSubSelect = anonymizeAll
				? "customer_id > 0 AND "
				: "customer_id in (SELECT customer_id FROM customer_" + companyID + "_tbl WHERE " + ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK + " = 1) AND ";
		
		final String trackingVetoClause = anonymizeAll 
				? ""
				: " AND " + ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK + " = 1 ";
		
		
		// Anonymize table onepixellog_<CID>_tbl
		update(logger, "UPDATE onepixellog_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());

		// Anonymize table onepixellog_device_<CID>_tbl
		update(logger, "UPDATE onepixellog_device_" + companyID + "_tbl SET customer_id = 0"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());

		// Anonymize table rdirlog_<CID>_tbl
		update(logger, "UPDATE rdirlog_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());
		
		// Anonymize table rdirlog_userform_<CID>_tbl
		update(logger, "UPDATE rdirlog_userform_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " (mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?) OR mailing_id IS NULL)",
			MailingContentType.advertising.name());

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
			// Anonymize table rdirlog_<CID>_tbl
			update(logger, "UPDATE rdirlog_" + companyID + "_val_num_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_alpha_tbl")) {
			// Anonymize table rdirlog_<CID>_tbl
			update(logger, "UPDATE rdirlog_" + companyID + "_val_alpha_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_ext_link_tbl")) {
			// Anonymize table rdirlog_<CID>_tbl
			update(logger, "UPDATE rdirlog_" + companyID + "_ext_link_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkTableAndColumnsExist(getDataSource(), "customer_" + companyID + "_tbl", "lastopen_date", "lastclick_date")) {
			// Anonymize lastclick_date and lastopen_date in customer_<CID>_tbl
			update(logger, "UPDATE customer_" + companyID + "_tbl SET lastopen_date = NULL, lastclick_date = NULL WHERE (lastopen_date IS NOT NULL OR lastclick_date IS NOT NULL)" + trackingVetoClause);
		}
	}
}
