/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.sql.SQLException;
import java.util.List;

import com.agnitas.backend.dao.DkimDAO;
import com.agnitas.util.Log;
import com.agnitas.util.Str;

/**
 * Read DKIM information from database to forward them to
 * xmlback for mail generation
 */
public class Dkim {
	private Data data;
	private DkimDAO dkimDAO;

	/**
	 * Constructor
	 *
	 * @param data the global configuration
	 */
	public Dkim(Data nData) throws SQLException {
		data = nData;
		dkimDAO = new DkimDAO (data.dbase, data.company.id ());
	}
	
	public List <DkimDAO.DKIM> dkims () {
		return dkimDAO.dkims ();
	}

	/**
	 * scan the stored DKIM keys to match the domain part
	 * of the given email and store the related information
	 * as part of the company info data.
	 *
	 * @param email the email to lookup the dkim key for
	 * @return true if a dkim key was found, false otherwise
	 */
	public boolean check (EMail email) {
		DkimDAO.DKIM	dkim = dkimDAO.find (email,
						     Str.atob (data.company.info ("dkim-local-key"), false),
						     Str.atob (data.company.info ("dkim-global-key"), false));
		if (dkim == null) {
			data.logging (Log.INFO, "dkim", "No DKIM key found for email " + email);
			return false;
		}
		data.logging (Log.INFO, "dkim", "Use " + dkim + " for email " + email);

		data.company.infoAdd("_dkim_domain", dkim.domain());
		data.company.infoAdd("_dkim_selector", dkim.selector());
		data.company.infoAdd("_dkim_key", dkim.key());
		if (dkim.ident()) {
			data.company.infoAdd("_dkim_ident", email.pure_puny);
		}
		String dkimDebug = data.company.info("dkim-debug", data.mailing.id());
		if (dkimDebug != null) {
			data.company.infoAdd("_dkim_z", dkimDebug);
		}
		if (dkim.reportEnabled ()) {
			data.company.infoAdd("_dkim_report", "true");
		}
		return true;
	}
}
