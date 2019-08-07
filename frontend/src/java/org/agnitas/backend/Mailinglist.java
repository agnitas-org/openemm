/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	org.agnitas.backend.dao.MailinglistDAO;
import	org.agnitas.util.Config;
import	org.agnitas.util.Log;

/**
 * Thes class keeps track of all mailinglist related
 * configurations
 */
public class Mailinglist {
	/** refrence to global configuration				*/
	private Data		data;
	/** the mailinglist table mailinglist_id			*/
	private long		id;
	/** the name of the mailinglist					*/
	private String		name;
	/** the mailinglist version of the RDIR domain to use		*/
	private String		rdirDomain;

	public Mailinglist (Data nData) {
		data = nData;
	}
	
	public Mailinglist done () {
		return null;
	}
	
	public long id () {
		return id;
	}
	public void id (long nId) {
		if (id != nId) {
			id = nId;
			name = null;
			rdirDomain = null;
		}
	}
	
	public String name () {
		return name;
	}
	public void name (String nName) {
		name = nName;
	}
	
	public String rdirDomain () {
		return rdirDomain;
	}
	public void rdirDomain (String nRdirDomain) {
		rdirDomain = nRdirDomain;
	}

	/**
	 * Write all mailinglist related settings to logfile
	 */
	public void logSettings () {
		data.logging (Log.DEBUG, "init", "\tmailinglist.id = " + id);
		if (name != null) {
			data.logging (Log.DEBUG, "init", "\tmailinglist.name = " + name);
		}
		if (rdirDomain != null) {
			data.logging (Log.DEBUG, "init", "\tmailinglist.rdirDomain = " + rdirDomain);
		}
	}

	/**
	 * Configure from external resource
	 * 
	 * @param cfg the configuration
	 */
	public void configure (Config cfg) {
	}

	/**
	 * Retrieves all company realted information from available resources
	 */
	public void retrieveInformation () throws Exception {
		MailinglistDAO	mailinglist = new MailinglistDAO (data.dbase, id);
		
		if (mailinglist.mailinglistID () == 0L) {
			throw new Exception ("No entry for mailinglistID " + id + " in mailinglist table found");
		}
		name (mailinglist.shortName ());
		rdirDomain (mailinglist.rdirDomain ());
	}
}
