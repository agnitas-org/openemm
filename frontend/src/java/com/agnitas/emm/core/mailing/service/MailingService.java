/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.bean.LightweightMailingWithMailingList;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.service.ServiceResult;
import org.springframework.context.ApplicationContext;

public interface MailingService {
	
	int addMailing(MailingModel model);
	
	int addMailingFromTemplate(MailingModel model);

	Mailing getMailing(MailingModel model);

	Mailing getMailing(final int companyID, final int mailingID);

    int getFollowUpFor(int mailingId);
	
	void updateMailing(MailingModel model, List<UserAction> userActions);

	MailingStatus getMailingStatus(MailingModel model);

    void deleteMailing(MailingModel model);
	
	List<Mailing> getMailings(MailingModel model);

	boolean exists(int mailingID, int companyID);

	List<Mailing> listMailings(final int companyId, final ListMailingFilter filter);

	List<Mailing> getMailingsForMLID(MailingModel model);

	void sendMailing(MailingModel model, List<UserAction> userActions);

	boolean isMissingNecessaryTargetGroup(Mailing mailing);

	boolean isFollowupMailingDateBeforeDate(Mailing mailing, Date boundDate);

	boolean isMailingLocked(Mailing mailing);

	MaildropEntry addMaildropEntry(MailingModel model, List<UserAction> userActions);
	
	/**
	 * Returns the number of minutes, a mailing is generated before delivery.
	 * 
	 * @param companyID companyID
	 * 
	 * @return number of minutes
	 */
	int getMailGenerationMinutes(int companyID);

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
	boolean isMailingWorldSent(int mailingID, int companyID);

	boolean isActiveIntervalMailing(int mailingId);

	boolean isActiveIntervalMailing(int mailingID, int companyId);

	int copyMailing(int mailingId, int companyId, String newMailingNamePrefix);

	int copyMailing(int newCompanyId, int mailinglistID, int fromCompanyID, int fromMailingID, boolean isTemplate);

	List<LightweightMailing> getAllMailingNames(Admin admin);

    List<MailingComponent> getMailingComponents(int mailingID, int companyID);

    List<Mailing> getDuplicateMailing(List<WorkflowIcon> icons, int companyId);

	/**
	 * All the new mailings (created since GWUA-3991) require text version so user is prevented from sending such mailing.
	 * For old mailings a warning message is sufficient.
	 * See GWUA-3991 for more details.
	 */
    boolean isTextVersionRequired(int companyId, int mailingId);

	boolean resumeDateBasedSending(int companyId, int mailingId);

	boolean saveSecuritySettings(int companyId, int mailingId, MailingSendSecurityOptions options);

	boolean switchStatusmailOnErrorOnly(int companyID, int mailingId, boolean statusmailOnErrorOnly);

	Map<Integer, List<LightweightMailingWithMailingList>> findActionBasedMailingsForMailinglists(List<Mailinglist> mailinglists, int companyId);

	List<LightweightMailingWithMailingList> listAllActionBasedMailingsForMailinglist(final int companyID, final int mailinglistID);

	LightweightMailing getLightweightMailing(int companyID, int mailingId);

	List<TargetLight> listTargetGroupsOfMailing(int companyID, int mailingID);

	boolean containsInvalidTargetGroups(int companyID, int mailingId);

	String getTargetExpression(final int companyId, final int mailingId);

	boolean tryToLock(Admin admin, int mailingId);

	Admin getMailingLockingAdmin(int mailingId, int companyId);

	boolean isDeliveryComplete(final int companyID, final int mailingID);

	boolean isDeliveryComplete(final LightweightMailing mailing);

	void updateStatus(int companyID, int mailingID, MailingStatus status);

	List<Integer> listFollowupMailingIds(int companyID, int mailingID, boolean includeUnscheduled);
    
    boolean generateMailingTextContentFromHtml(Admin admin, int mailingId) throws Exception;

	List<LightweightMailing> getLightweightMailings(Admin admin);

	List<MailingBase> getTemplatesWithPreview(MailingTemplateSelectionFilter filter, Admin admin);

	Map<Integer, String> getTemplateNames(Admin admin);

	List<MailingBase> getMailingsByStatusE(int companyId);

    List<LightweightMailing> getUnsetMailingsForRootTemplate(int companyId, int templateId);

	boolean isThresholdClearanceExceeded(int companyId, int mailingId);

	int saveMailingWithNewContent(Mailing mailing) throws Exception;

	int saveMailingWithNewContent(Mailing mailing, Admin admin) throws Exception;

	int saveMailingWithNewContent(Mailing mailing, boolean errorTolerant, boolean removeUnusedContent) throws Exception;

	int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks);

    List<UserAction> deleteMailing(int mailingId, Admin admin);

    void updateMailingsWithDynamicTemplate(Mailing mailing, ApplicationContext applicationContext);

	Date getMailingLastSendDate(int mailingId);

    boolean isBaseMailingTrackingDataAvailable(int baseMailingId, Admin admin);

    boolean isDynamicTemplateCheckboxVisible(Mailing mailing);

    boolean hasMediaType(int mailingId, MediaTypes type, int companyId);

	String getMailingName(int mailingId, int companyId);

	boolean isMailingTargetsHaveConjunction(Admin admin, Mailing mailing);

	List<Integer> findTargetDependentMailings(int targetGroupId, int companyId);

	List<Integer> filterNotSentMailings(List<Integer> mailings);

    boolean isBasicFullTextSearchSupported();

    boolean isContentFullTextSearchSupported();

    ServiceResult<PaginatedList<Map<String, Object>>> getOverview(Admin admin, MailingsListProperties props);

    List<Map<String, String>> listTriggers(int mailingId, int companyId);

    ServiceResult<List<Mailing>> getMailingsForDeletion(Collection<Integer> mailingIds, Admin admin);

    ServiceResult<List<UserAction>> bulkDelete(Collection<Integer> ids, Admin admin);

    void bulkRestore(Collection<Integer> mailingIds, Admin admin);

    void restoreMailing(int mailingId, Admin admin);

    boolean isApproved(int mailingId, int companyId);

    void removeApproval(int mailingId, Admin admin);

	void writeRemoveApprovalLog(int mailingId, Admin admin);

    Mailing getMailing(int mailingId, int companyId, boolean includeDependencies);

    Map<Integer, Mediatype> getMediatypes(int mailingId, int companyId);

    boolean isDateBasedMailingWasSentToday(int mailingId);

	void allowDateBasedMailingResending(int mailingId);

	MailingStatus getMailingStatus(int companyID, int id);

	boolean hasMailingStatus(int mailingId, MailingStatus status, int companyID);

	boolean saveMailingDescriptiveData(Mailing mailing);

	List<LightweightMailing> getMailingsUsingEmmAction(int actionId, int companyID);

    void clearPlanDate(int mailingId, int companyId);

	String getEmailParameter(int mailingID);

	String getMailingRdirDomain(int mailingID, int companyID);

	MailingType getMailingType(int mailingID);

	boolean isSettingsReadonly(Admin admin, int mailingId);

	boolean isSettingsReadonly(Admin admin, boolean isTemplate);

	boolean isMarkedAsDeleted(int mailingId, int companyID);

	String getJoinedContent(Mailing mailing);

	boolean isTemplate(int mailingId, int companyId);

    Map<Integer, String> getMailingNames(Admin admin);
}
