/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.MailingContentType;
import com.agnitas.dao.AnonymizeStatisticsDao;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.util.DbUtilities;

public class AnonymizeStatisticsDaoImpl extends BaseDaoImpl implements AnonymizeStatisticsDao {
	
	@Override
	public void anonymizeStatistics(final int companyID, final boolean anonymizeAll) {
		final String trackingVetoSubSelect = anonymizeAll
				? "customer_id > 0 AND "
				: "customer_id in (SELECT customer_id FROM customer_" + companyID + "_tbl WHERE " + RecipientStandardField.DoNotTrack.getColumnName() + " = 1) AND ";
		
		final String trackingVetoClause = anonymizeAll 
				? ""
				: " AND " + RecipientStandardField.DoNotTrack.getColumnName() + " = 1 ";
		
		
		// Anonymize table onepixellog_<CID>_tbl
		update("UPDATE onepixellog_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());

		// Anonymize table onepixellog_device_<CID>_tbl
		update("UPDATE onepixellog_device_" + companyID + "_tbl SET customer_id = 0"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());

		// Anonymize table rdirlog_<CID>_tbl
		update("UPDATE rdirlog_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
			MailingContentType.advertising.name());
		
		// Anonymize table rdirlog_userform_<CID>_tbl
		update("UPDATE rdirlog_userform_" + companyID + "_tbl SET customer_id = 0, ip_adr = NULL"
			+ " WHERE " +  trackingVetoSubSelect
			+ " (mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?) OR mailing_id IS NULL)",
			MailingContentType.advertising.name());

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
			// Anonymize table rdirlog_<CID>_val_num_tbl
			update("UPDATE rdirlog_" + companyID + "_val_num_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_alpha_tbl")) {
			// Anonymize table rdirlog_<CID>_val_alpha_tbl
			update("UPDATE rdirlog_" + companyID + "_val_alpha_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_ext_link_tbl")) {
			// Anonymize table rdirlog_<CID>_ext_link_tbl
			update("UPDATE rdirlog_" + companyID + "_ext_link_tbl SET customer_id = 0, ip_adr = NULL"
				+ " WHERE " +  trackingVetoSubSelect
				+ " mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE content_type IS NULL OR content_type = ?)",
				MailingContentType.advertising.name());
		}

		if (DbUtilities.checkTableAndColumnsExist(getDataSource(), "customer_" + companyID + "_tbl", "lastopen_date", "lastclick_date")) {
			// Anonymize lastclick_date and lastopen_date in customer_<CID>_tbl
			update("UPDATE customer_" + companyID + "_tbl SET lastopen_date = NULL, lastclick_date = NULL WHERE (lastopen_date IS NOT NULL OR lastclick_date IS NOT NULL)" + trackingVetoClause);
		}
	}
}
