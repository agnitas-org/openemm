/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.backend.DBase;
import org.agnitas.util.Log;

/**
 * Accesses all recipient related data beside the main query
 */
public class RecipientDAO {
	public long findRecipientForPreview(DBase dbase, long companyID, long mailinglistID, long[] targetIDs) throws SQLException {
		try (DBase.With with = dbase.with()) {
			Long rc;
			String limit = (dbase.isOracle() ? " AND rownum = 1 " : " LIMIT 1 ");

			if ((targetIDs != null) && (targetIDs.length > 0)) {
				List<String> targets = new ArrayList<>();

				for (long targetID : targetIDs) {
					if (targetID > 0) {
						String	sql = dbase.queryString (with.cursor (),
										 "SELECT target_sql " +
										 "FROM dyn_target_tbl " +
										 "WHERE target_id = :targetID",
										 "targetID", targetID);
						if (sql != null) {
							targets.add(sql);
						}
					}
				}
				if (targets.size() > 0) {
					String targetExpression = targets.stream().reduce((s, e) -> s + " AND (" + e + ")").orElse("");

					rc = dbase.queryLong (with.cursor (),
							      "SELECT cust.customer_id " +
							      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
							      "WHERE bind.mailinglist_id = :mailinglistID AND bind.user_type IN ('A', 'T') AND (" + targetExpression + ")" + limit,
							      "mailinglistID", mailinglistID);
				
					if ((rc != null) && (rc > 0L)) {
						dbase.logging(Log.DEBUG, "recipient", "Found test recipient " + rc + " on mailinglist " + mailinglistID + " using " + targetExpression);
						return rc;
					}
					rc = dbase.queryLong (with.cursor (),
							      "SELECT cust.customer_id " +
							      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
							      "WHERE bind.user_type IN ('A', 'T') AND (" + targetExpression + ")" + limit);
					if ((rc != null) && (rc > 0L)) {
						dbase.logging(Log.DEBUG, "recipient", "Found test recipient " + rc + " using " + targetExpression);
						return rc;
					}
					rc = dbase.queryLong (with.cursor (),
							      "SELECT cust.customer_id " +
							      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
							      "WHERE bind.mailinglist_id = :mailinglistID AND (" + targetExpression + ")" + limit,
							      "mailinglistID", mailinglistID);
					if ((rc != null) && (rc > 0L)) {
						dbase.logging(Log.DEBUG, "recipient", "Found recipient " + rc + " on mailinglist " + mailinglistID + " using " + targetExpression);
						return rc;
					}
					rc = dbase.queryLong (with.cursor (),
							      "SELECT cust.customer_id " +
							      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
							      "WHERE " + targetExpression + limit);
					if ((rc != null) && (rc > 0L)) {
						dbase.logging(Log.DEBUG, "recipient", "Found recipient " + rc + " using " + targetExpression);
						return rc;
					}
				}
			}
			rc = dbase.queryLong (with.cursor (),
					      "SELECT cust.customer_id " +
					      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
					      "WHERE bind.mailinglist_id = :mailinglistID AND bind.user_type IN ('A', 'T')" + limit,
					      "mailinglistID", mailinglistID);
			if ((rc != null) && (rc > 0L)) {
				dbase.logging(Log.DEBUG, "recipient", "Found test recipient " + rc + " on mailinglist " + mailinglistID);
				return rc;
			}
			rc = dbase.queryLong (with.cursor (),
					      "SELECT cust.customer_id " +
					      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
					      "WHERE bind.user_type IN ('A', 'T')" + limit);
			if ((rc != null) && (rc > 0L)) {
				dbase.logging(Log.DEBUG, "recipient", "Found test recipient " + rc);
				return rc;
			}
			rc = dbase.queryLong (with.cursor (),
					      "SELECT cust.customer_id " +
					      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id " +
					      "WHERE bind.mailinglist_id = :mailinglistID" + limit,
					      "mailinglistID", mailinglistID);
			if ((rc != null) && (rc > 0L)) {
				dbase.logging(Log.DEBUG, "recipient", "Found recipient " + rc + " on mailinglist " + mailinglistID);
				return rc;
			}
			rc = dbase.queryLong (with.cursor (),
					      "SELECT cust.customer_id " +
					      "FROM customer_" + companyID + "_tbl cust INNER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id" + limit);
			if ((rc != null) && (rc > 0L)) {
				dbase.logging(Log.DEBUG, "recipient", "Found recipient " + rc);
				return rc;
			}
			rc = dbase.queryLong (with.cursor (),
					      "SELECT cust.customer_id " +
					      "FROM customer_" + companyID + "_tbl cust LEFT OUTER JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id" + limit);
			if ((rc != null) && (rc > 0L)) {
				dbase.logging(Log.DEBUG, "recipient", "Found recipient " + rc + " without binding");
				return rc;
			}
		}
		return 0L;
	}
}
