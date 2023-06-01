/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.Map;

import org.agnitas.backend.DBase;

/**
 * Accesses all mailinglist relevant information from the database
 * from the table mailinglist_tbl
 */
public class MailinglistDAO {
	private long mailinglistID;
	private String shortName;
	private String rdirDomain;
	private boolean frequencyCounterEnabled;

	public MailinglistDAO(DBase dbase, long forMailinglistID) throws SQLException {
		Map<String, Object> row;

		try (DBase.With with = dbase.with ()) {
			row = dbase.querys (with.cursor (),
					    "SELECT * " +
					    "FROM mailinglist_tbl " +
					    "WHERE (deleted IS NULL OR deleted = 0) AND mailinglist_id = :mailinglistID",
					    "mailinglistID", forMailinglistID);
			if (row != null) {
				mailinglistID = dbase.asLong (row.get ("mailinglist_id"));
				shortName = dbase.asString (row.get ("shortname"));
				rdirDomain = dbase.asString (row.get ("rdir_domain"), true);
				if (row.containsKey ("freq_counter_enabled")) {
					frequencyCounterEnabled = dbase.asInt (row.get ("freq_counter_enabled")) == 1;
				}
			} else {
				mailinglistID = 0L;
			}
		}
	}

	public long mailinglistID() {
		return mailinglistID;
	}

	public String shortName() {
		return shortName;
	}

	public String rdirDomain() {
		return rdirDomain;
	}

	public boolean frequencyCounterEnabled() {
		return frequencyCounterEnabled;
	}
}
