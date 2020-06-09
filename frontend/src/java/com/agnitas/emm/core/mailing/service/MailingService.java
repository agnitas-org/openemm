/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface MailingService {
	int addMailing(MailingModel model) throws MailinglistNotExistException;
	
	int addMailingFromTemplate(MailingModel model);

	Mailing getMailing(MailingModel model);

	Mailing getMailing(final int companyID, final int mailingID);

	void updateMailing(MailingModel model, List<UserAction> userActions) throws MailinglistException;

	String getMailingStatus(MailingModel model);

    void deleteMailing(MailingModel model);
	
	List<Mailing> getMailings(MailingModel model);
	
	List<Mailing> getMailingsForMLID(MailingModel model) throws MailinglistException;

	void sendMailing(MailingModel model, List<UserAction> userActions) throws Exception;

	MaildropEntry addMaildropEntry(MailingModel model, List<UserAction> userActions) throws Exception;
	
	/**
	 * Returns the number of minutes, a mailing is generated before delivery.
	 * 
	 * @param companyID companyID
	 * 
	 * @return number of minutes
	 */
	int getMailGenerationMinutes(@VelocityCheck int companyID);

	/**
	 * Checks, if given mailing is already world-sent or scheduled for world-send.
	 * 
	 * @param mailingID mailing ID
	 * @param companyID company ID of mailing
	 * 
	 * @return {@code true}, if mailing is world sent
	 * 
	 * @throws MailingNotExistException if mailing ID is unknown
	 */
	boolean isMailingWorldSent(int mailingID, @VelocityCheck int companyID) throws MailingNotExistException;

	boolean isActiveIntervalMailing(final int mailingID);
	
    List<LightweightMailing> getAllMailingNames(@VelocityCheck ComAdmin admin);

    List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID) throws MailingNotExistException;

    List<Mailing> getDuplicateMailing(List<WorkflowIcon> icons, @VelocityCheck int companyId);

	/**
	 * All the new mailings (created since GWUA-3991) require text version so user is prevented from sending such mailing.
	 * For old mailings a warning message is sufficient.
	 * See GWUA-3991 for more details.
	 */
    boolean isTextVersionRequired(@VelocityCheck int companyId, int mailingId);

	boolean switchStatusmailOnErrorOnly(int companyID, int mailingId, boolean statusmailOnErrorOnly);
	
	List<LightweightMailing> listAllActionBasedMailingsForMailinglist(final int companyID, final int mailinglistID);

	LightweightMailing getLightweightMailing(final int companyID, final int mailingId) throws MailingNotExistException;

	List<TargetLight> listTargetGroupsOfMailing(final int companyID, final int mailingID) throws MailingNotExistException;

	boolean tryToLock(ComAdmin admin, int mailingId);
	
	boolean isDeliveryComplete(final int companyID, final int mailingID);
	boolean isDeliveryComplete(final LightweightMailing mailing);

	void updateStatus(int companyID, int mailingID, String string);

	List<Integer> listFollowupMailingIds(int companyID, int mailingID, boolean includeUnscheduled);
    
    boolean generateMailingTextContentFromHtml(ComAdmin admin, int mailingId) throws Exception;
}
