/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	org.agnitas.backend.DBase;

/**
 * Accesses all recipient related data beside the main query
 */
public class RecipientDAO {
	public long findAdminOrTestRecipientForMailinglist (DBase dbase, long companyID, long mailinglistID) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			Long	rc = dbase.queryLong (with.jdbc (),
						      "SELECT cust.customer_id " +
						      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
						      "WHERE bind.mailinglist_id = :mailinglistID AND bind.user_type IN ('A', 'T') " + (dbase.isOracle () ? "AND rownum = 1 " : "LIMIT 1 "),
						      "mailinglistID", mailinglistID);
			return rc == null ? 0 : rc;
		}
	}

	public long findAdminOrTestRecipient (DBase dbase, long companyID) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			Long	rc = dbase.queryLong (with.jdbc (),
						      "SELECT cust.customer_id " +
						      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
						      "WHERE bind.user_type IN ('A', 'T') " + (dbase.isOracle () ? "AND rownum = 1" : "LIMIT 1"));
			return rc == null ? 0 : rc;
		}
	}
	
	public long findRecipient (DBase dbase, long companyID) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			Long	rc = dbase.queryLong (with.jdbc (),
						      "SELECT cust.customer_id " +
						      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
						       (dbase.isOracle () ? "WHERE rownum = 1" : "LIMIT 1"));
			return rc == null ? 0 : rc;
		}
	}
}
