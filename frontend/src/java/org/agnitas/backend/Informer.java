/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;
import	org.agnitas.util.Log;
import	org.agnitas.backend.dao.AdminDAO;
import	org.agnitas.backend.dao.CompanyDAO;
import	org.agnitas.backend.dao.MailingDAO;
import	org.agnitas.backend.dao.RecipientDAO;
import	org.agnitas.dao.UserStatus;

public class Informer {
	private Data	data;
	private String	mailingName;
	private long	companyID;
	
	public Informer (Data data, String mailingName) {
		this.data = data;
		this.mailingName = mailingName;
		this.companyID = 1L;
	}
	
	synchronized public void send (Map <String, String> details) throws SQLException {
		List <AdminDAO.Admin>	admins = (new AdminDAO ())
			.getAdmins (data.dbase, data.company.id ())
			.stream ()
			.filter (a -> a.hasRole ("Administrator") && a.email () != null)
			.collect (ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		if ((admins == null) || (admins.size () == 0)) {
			data.logging (Log.INFO, "inform", "No administrators for " + data.company.id () + " found, no mails are sent");
		} else {
			CompanyDAO	companyDao = new CompanyDAO (data.dbase, companyID);

			if (companyDao.companyID () == 0) {
				data.logging (Log.WARNING, "inform", "unable to find company_id " + companyID + " in database");
			} else if (! "active".equals (companyDao.status ())) {
				data.logging (Log.WARNING, "inform", "company_id " + companyID + " is not active, but " + companyDao.status ());
			} else {
				long	mailingID = data.mailing.findMailingByName (mailingName, companyID);
				
				if (mailingID == 0) {
					data.logging (Log.INFO, "inform", "no mailing \"" + mailingName + "\" for company_id " + companyID + " found");
				} else {
					MailingDAO	mailing = new MailingDAO (data.dbase, mailingID);
					
					if (mailing.mailingID () == 0) {
						data.logging (Log.ERROR, "inform", "Failed to load mailingID " + mailingID + " (" + mailingName + ") for company_id " + companyID);
					} else {
						long	statusID = mailing.findMaildropStatusID (data.dbase, "E");
						
						if (statusID == 0L) {
							data.logging (Log.INFO, "inform", "mailing \"" + mailingName + "\" not active");
						} else {
							RecipientDAO		recipientDao = new RecipientDAO ();
							Map <String, String>	source = new HashMap <> ();
							
							source.put ("company_id", Long.toString (data.company.id ()));
							source.put ("company_name", data.company.name ());
							source.put ("mailing_id", Long.toString (data.mailing.id ()));
							source.put ("mailing_name", data.mailing.name ());
							if (details != null) {
								source.putAll (details);
							}
							for (AdminDAO.Admin admin : admins) {
								send (recipientDao, admin, mailing, statusID, source);
							}
						}
					}
				}
			}
		}
	}
	private void send (RecipientDAO recipientDao, AdminDAO.Admin admin, MailingDAO mailing, long statusID, Map <String, String> source) throws SQLException {
		long	customerID = recipientDao.findOrCreateRecipient (data.dbase, admin.email (), companyID, mailing.mailinglistID (), UserStatus.Suspend);
		
		if (customerID == 0L) {
			data.logging (Log.ERROR, "inform", "\"" + admin.email () + "\" not found and failed to create");
		} else {
			Map <String, Object>	opts = new HashMap <> ();
			List <Long>		userStatuses = new ArrayList <> ();
			Map <String, Object>	overwrite = new HashMap <> ();
			
			userStatuses.add ((long) UserStatus.Active.getStatusCode ());
			userStatuses.add ((long) UserStatus.Suspend.getStatusCode ());
			overwrite.put ("firstname", admin.firstname ());
			overwrite.put ("lastname", admin.lastname ());
			overwrite.put ("language", admin.language ());
			opts.put ("customer-id", customerID);
			opts.put ("user-status", userStatuses);
			opts.put ("overwrite", overwrite);
			if (source != null) {
				opts.put ("virtual", source);
			}
			try {
				Mailgun	mailgun = new MailgunImpl ();
				
				mailgun.initialize (Long.toString (statusID));
				mailgun.prepare (opts);
				mailgun.execute (opts);
				mailgun.done ();
			} catch (Exception e) {
				data.logging (Log.ERROR, "inform", "failed to send mailing " + mailing.mailingID () + " using maildropStatusID " + statusID + " to \"" + admin.email () + "\": " + e.toString ());
			}
		}
	}
}
