/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import static com.agnitas.emm.core.maildrop.MaildropStatus.ACTION_BASED;
import static com.agnitas.emm.core.maildrop.MaildropStatus.ADMIN;
import static com.agnitas.emm.core.maildrop.MaildropStatus.DATE_BASED;
import static com.agnitas.emm.core.maildrop.MaildropStatus.ON_DEMAND;
import static com.agnitas.emm.core.maildrop.MaildropStatus.TEST;
import static com.agnitas.emm.core.maildrop.MaildropStatus.WORLD;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.agnitas.backend.dao.MaildropStatusDAO;
import org.agnitas.backend.exceptions.CancelException;
import org.agnitas.util.Const;
import org.agnitas.util.Log;

public class MaildropStatus {
	/**
	 * refrence to global configuration
	 */
	private Data data;
	/**
	 * maildrop status table status_id for this status id
	 */
	private long id;
	/**
	 * database representaion of this entry
	 */
	private MaildropStatusDAO maildrop;
	/**
	 * maildrop status table status_field
	 */
	private String statusField;
	/**
	 * maildrop status table senddate
	 */
	private Timestamp sendDate;
	/**
	 * maildrop status table max_recipients
	 */
	private long limitReceivers;
	/**
	 * maildrop status table admin_test_target_id
	 */
	private long adminTestTargetID;
	/**
	 * resulting Target for test target id, if set
	 */
	private Target adminTestTargetSQL;
	/**
	 * maildrop status table optimized_mail_generation
	 */
	private String optimizeMailGeneration;
	/**
	 * maildrop status table selected_test_recipients
	 */
	private boolean selectedTestRecipients;
	/**
	 * generic send date handling
	 */
	public MaildropStatus(Data nData) {
		data = nData;
	}

	public MaildropStatus done() {
		return null;
	}

	public boolean exists() {
		return maildrop != null;
	}

	public long id() {
		return id;
	}

	public void id(long nId) throws SQLException {
		if (id != nId) {
			id = nId;
			statusField = null;
			sendDate = null;
			maildrop = new MaildropStatusDAO(data.dbase, id);
			if (maildrop.statusID() == 0L) {
				maildrop = null;
			} else {
				statusField(maildrop.statusField());
				sendDate(maildrop.sendDate());
			}
		}
	}

	public String statusField() {
		return statusField;
	}

	public void statusField(String nStatusField) {
		statusField = nStatusField;
	}

	public Timestamp sendDate() {
		return sendDate;
	}

	public void sendDate(Timestamp nSendDate) {
		sendDate = nSendDate;
	}

	public long limitReceivers() {
		return limitReceivers;
	}

	public String optimizeMailGeneration() {
		return optimizeMailGeneration;
	}

	public boolean selectedTestRecipients() {
		return selectedTestRecipients;
	}

	public Date genericSendDate() {
		try {
			return (exists() ? maildrop : new MaildropStatusDAO(data.dbase, 0, data.mailing.id())).realSendDate();
		} catch (SQLException e) {
			data.logging(Log.ERROR, "senddate", "Failed to query real send date: " + e.toString());
			return new Date();
		}
	}

	/**
	 * Write all mailinglist related settings to logfile
	 */
	public void logSettings() {
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.id = " + id);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.statusField = " + statusField);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.sendDate = " + sendDate);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.limitReceivers = " + limitReceivers);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.adminTestTargetID = " + adminTestTargetID);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.adminTestTargetSQL = " + (adminTestTargetSQL == null ? "*unset*" : adminTestTargetSQL.getSQL(true)));
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.optimizeMailGeneration = " + (optimizeMailGeneration == null ? "*unset*" : optimizeMailGeneration));
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.selectedTestRecipients = " + selectedTestRecipients);
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.dependsOnAutoImportID = " + (exists () ? maildrop.dependsOnAutoImportID () : "*unset*"));
		data.logging(Log.DEBUG, "init", "\tmaildropStatus.autoImportOK = " + (exists () ? maildrop.autoImportOK () : "*unset*"));
	}

	/**
	 * Configure from external resource
	 *
	 * @param cfg the configuration
	 */
	public void configure(Config cfg) {
		// nothing to do
	}

	/**
	 * Retrieves all company realted information from available resources
	 */
	public void retrieveInformation() throws Exception {
		if (!exists()) {
			throw new Exception("No entry for statusID " + id + " in maildrop status table found");
		}
		if (maildrop.genStatus() != 1) {
			throw new Exception("Generation state is not 1, but " + maildrop.genStatus());
		}
		if (maildrop.dependsOnAutoImportID () && (! maildrop.autoImportOK ())) {
			if (isAdminMailing () || isTestMailing () || isWorldMailing ()) {
				try {
					setGenerationStatus (1, 3, true);
				} catch (Exception e) {
					data.logging (Log.ERROR, "retrieve", "Failed to update gneration status: " + e.toString ());
				}
			}
			if ((! isCampaignMailing ()) && (! isPreviewMailing ()) && (! isVerificationMailing ())) {
				try {
					updateGenerationChange ();
					data.trigger ("import-dependency", id);
				} catch (Exception e) {
					data.logging (Log.ERROR, "retrieve", "Failed to update generation change: " + e.toString ());
				}
				throw new CancelException ("dependency on auto import(s) is not fullfilled");
			}
		}

		for (int retry = 0; retry < 2; ++retry) {
			if (isAdminMailing() || isTestMailing() || isWorldMailing() || isRuleMailing() || isOnDemandMailing()) {
				try {
					setGenerationStatus(1, 2);
					break;
				} catch (Exception e) {
					data.logging (retry == 0 ? Log.INFO : Log.ERROR, "retrieve", "Failed to update generation status from 1 to 2: " + e.toString ());
					if (maildrop.genStatus () != 2) {
						throw new Exception ("After failing setting generation status from 1 to 2 it is now " + maildrop.genStatus ());
					}
				}
			}
		}
		data.company.id (maildrop.companyID ());
		data.mailing.id (maildrop.mailingID ());
		statusField (maildrop.statusField ());
		sendDate (maildrop.sendDate ());
		limitReceivers = maildrop.maxRecipients ();
		adminTestTargetID = maildrop.adminTestTargetID ();
		optimizeMailGeneration = maildrop.optimizeMailGeneration ();
		selectedTestRecipients = maildrop.selectedTestRecipients ();
		if ((optimizeMailGeneration != null) &&
		    (! optimizeMailGeneration.equals (Const.OptimizedMailGeneration.ID_DAY)) &&
		    (! optimizeMailGeneration.equals (Const.OptimizedMailGeneration.ID_24H))) {
			data.logging (Log.WARNING, "retrieve", "Invalid value for maildrop status table optimize_mail_generation found: " + optimizeMailGeneration);
			optimizeMailGeneration = null;
		}
		if (adminTestTargetID > 0) {
			adminTestTargetSQL = data.targetExpression.getTarget(adminTestTargetID, false);
		}
		if (maildrop.blockSize() > 0) {
			data.mailing.blockSize(maildrop.blockSize());
		}
		data.mailing.stepping(maildrop.step());
	}

	/**
	 * if this is a admin mail
	 *
	 * @return true, if admin mail
	 */
	public boolean isAdminMailing() {
		return ADMIN.getCodeString().equals(statusField);
	}

	/**
	 * if this is a test mail
	 *
	 * @return true, if test mail
	 */
	public boolean isTestMailing() {
		return TEST.getCodeString().equals(statusField);
	}

	/**
	 * if this is a campaign mail
	 *
	 * @return true, if campaign mail
	 */
	public boolean isCampaignMailing() {
		return ACTION_BASED.getCodeString().equals(statusField);
	}

	/**
	 * if this is a date based mailing
	 *
	 * @return true, if its date based
	 */
	public boolean isRuleMailing() {
		return DATE_BASED.getCodeString().equals(statusField);
	}

	/**
	 * if this an on demand mailing
	 *
	 * @return true, if this is on demand
	 */
	public boolean isOnDemandMailing() {
		return ON_DEMAND.getCodeString().equals(statusField);
	}

	/**
	 * if this is a world mail
	 *
	 * @return true, if world mail
	 */
	public boolean isWorldMailing() {
		return WORLD.getCodeString().equals(statusField);
	}

	/**
	 * if this is a preview
	 *
	 * @return true, if preview
	 */
	public boolean isPreviewMailing() {
		return statusField.equals("P");
	}

	/**
	 * if this is a verification (provider preview)
	 *
	 * @return true, if verification
	 */
	public boolean isVerificationMailing() {
		return statusField.equals("V");
	}

	/**
	 * return SQL fragment, if there is a limitiation for admin or test mailing
	 *
	 * @return the sql fragment, if available, else null
	 */
	public String getAdminTestSQL() {
		return adminTestTargetSQL != null ? adminTestTargetSQL.getSQL(true) : null;
	}

	public boolean optimizeForDay() {
		return optimizeMailGeneration != null && optimizeMailGeneration.equals(Const.OptimizedMailGeneration.ID_DAY);
	}

	public boolean optimizeFor24h() {
		return optimizeMailGeneration != null && optimizeMailGeneration.equals(Const.OptimizedMailGeneration.ID_24H);
	}

	public long findSmallestStatusIDForWorldMailing(long mailingID) throws SQLException {
		return MaildropStatusDAO.findSmallestStatusIDForWorldMailing(data.dbase, mailingID);
	}

	public long findLargestStatusIDForWorldMailing(long mailingID) throws SQLException {
		return MaildropStatusDAO.findLargestStatusIDForWorldMailing(data.dbase, mailingID);
	}

	/**
	 * modify the genetation status for this maildrop entry
	 *
	 * @param fromStatus the source status the entry must have (0 means any value)
	 * @param toStatus   the new status to be set
	 */
	public void setGenerationStatus(int fromStatus, int toStatus, boolean forceSetProcessedBy) throws Exception {
		if (exists()) {
			if (!maildrop.updateGenStatus(data.dbase, fromStatus, toStatus, forceSetProcessedBy)) {
				throw new Exception("failed to update genstatus for " + id);
			}
		} else {
			throw new Exception("status entry for id " + id + " does not exist");
		}
	}
	public void setGenerationStatus(int fromStatus, int toStatus) throws Exception {
		setGenerationStatus (fromStatus, toStatus, false);
	}
	private void updateGenerationChange () throws Exception {
		if (exists ()) {
			if (! maildrop.updateGenChange (data.dbase)) {
				throw new Exception ("failed to update genchange for " + id);
			}
		} else {
			throw new Exception ("status entry for id " + id + " does not exist");
		}
	}

	public void removeEntry() throws Exception {
		if (exists()) {
			if (!maildrop.remove(data.dbase)) {
				data.logging(Log.WARNING, "remove", "Tried to remove non existing entry in maildrop status table for " + id);
			}
			maildrop = null;
		}
	}
}
