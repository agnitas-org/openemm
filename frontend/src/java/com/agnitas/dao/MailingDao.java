/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.RdirMailingData;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonFilter;
import com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.MailingDto;
import com.agnitas.emm.core.mailing.dao.MailingDaoOptions;
import com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingSendStatus;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.common.MailingStatus;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList;
import org.agnitas.emm.core.mailing.beans.MailingArchiveEntry;
import com.agnitas.util.FulltextSearchInvalidQueryException;

public interface MailingDao {

    Mailing getMailing(int mailingId, int companyId, boolean includeDependencies);

    Mailing getMailingWithDeletedDynTags(int mailingID, int companyID);
	
	boolean updateStatus(int companyID, int mailingID, MailingStatus mailingStatus, Date sendDate);
	
	// returns all Mailings in a linked List.
	List<Integer> getAllMailings(int companyID);
	// returns the given amount of mailings
	List<Mailing> getMailings(int companyID, int adminId, int count, String mailingStatus, boolean takeMailsForPeriod);

	/**
	 * Loads list of non-deleted mailings/templates of certain company.
	 * Depending on the conditions in the given filter, other tables can be taken into account.
	 * 
	 * @param companyId	            Id of the company
	 * @param template	        true - load templates, false - load mailings
	 * @param filter			filter for mailings
	 * 
	 * @return List of Mailing objects
	 */
	List<Mailing> listMailings(final int companyId, final boolean template, final ListMailingFilter filter);

	List<Map<String, Object>> getMailings(int companyId, String commaSeparatedMailingIds);
	
	boolean hasEmail(int companyId, int mailingId);

	boolean hasMediaType(int companyId, int mailingId, MediaTypes type);

	boolean saveStatusmailRecipients(int mailingID, String statusmailRecipients);

	boolean saveStatusmailOnErrorOnly(int companyID, int mailingID, boolean statusmailOnErrorOnly);

	@DaoUpdateReturnValueCheck
	boolean saveSecuritySettings(int companyId, int mailingId, MailingSendSecurityOptions options);

	/**
     * Get mailings which is set as the followup to given one,
     *
     * @param mailingID - target mailing id
     * @param includeUnscheduled - w@Override
	hether the unscheduled followup mailings will be included.
     * @return - list of followup mailings id. Is not recursive.
     */
    List<Integer> getFollowupMailings(int mailingID, int companyID, boolean includeUnscheduled);

	/**
	 * The last send-date of a mailing. The state 'W' will be preferred even if there a later send-dates with state 'A' or 'T' !
	 * @return - the send-date , null if the mailing has not been sent yet
	 */
	Timestamp getLastSendDate(int mailingID) ;

    int saveUndoMailing(int mailingId, int adminId);

	/**
	 * returns the type of a Followup Mailing as String.
	 * The String can be fount in the mailing-class (eg. FollowUpType.TYPE_FOLLOWUP_CLICKER)
	 * if no followup is found, null is the returnvalue!
	 */
	String getFollowUpType(int mailingID);

    int getCompanyIdForMailingId(int mailingId);
	
	RdirMailingData getRdirMailingData(int mailingId);
	
	/**
	 * returns the base mailing for the given one.
	 */
	String getFollowUpFor(int mailingID);
	
    PaginatedListImpl<Map<String, Object>> getDashboardMailingList(Admin admin, String sort, String direction, int rownums);

    /**
     * Get the last n sent world mailings in descend order
	 * @param number number of mailings you want to achieve
	 * @return a list of hashmaps. Currently there are the keys mailingid (int) and shortname (string) available
     */
    List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int number);

    Date getSendDate(int companyId, int mailingId);
    
    
    /**
     * this method returns the mailing-ID for the last sent world mailing.
     */
    int getLastSentMailing(int companyID, int customerID);
    
    /**
	 * This method returns the mailing ID for a already sent world-mailing with the given companyID and
	 * (if given) the mailingListID. If no mailingListID is given (null or "0") it will be ignored.
	 */
    int getLastSentWorldMailingByCompanyAndMailinglist(int companyID, int mailingListID);

    List<MailingBase> getPredefinedMailingsForReports(int companyId, int number, int filterType, int filterValue, MailingType mailingType, String orderKey, int targetId, Set<Integer> adminAltgIds);

	List<MailingBase> getSentWorldMailingsForReports(int companyID, int number, int targetId, Set<Integer> targetIds);

	List<MailingBase> getPredefinedNormalMailingsForReports(int companyId, Date from, Date to, int filterType, int filterValue, String orderKey, int targetId, Set<Integer> adminAltgIds);

	List<MailingDto> getSentAndScheduled(MailingDaoOptions opts, Admin admin);

	List<Map<String, Object>> getSentAndScheduled(Admin admin, Date startDate, Date endDate, int limit);

    List<MailingPopoverInfo> getMailingsCalendarInfo(Set<Integer> mailingIds, Admin admin);

    List<Map<String, Object>> getSentAndScheduledLight(Admin admin, Date startDate, Date endDate);

	List<ScheduledMailing> getScheduledMailings(Admin admin, Date startDate, Date endDate);

    List<MailingDto> getPlannedMailings(MailingDaoOptions opts, Admin admin);

	List<Map<String, Object>> getPlannedMailings(Admin admin, Date startDate, Date endDate, int limit);

    List<Map<String, Object>> getPlannedMailingsLight(Admin admin, Date startDate, Date endDate);

	Map<Integer, Integer> getOpeners(int companyId, List<Integer> mailingsId);

	/**
	 * Calculate a number of an openers for each mailingId.
	 *
	 * @param companyId an identifier of a company.
	 * @param mailingsId a collections of mailing identifiers.
	 * @param currentRecipientsOnly whether ({@code true}) or not ({@code false}) exclude recipients who are not present
	 *                                 in a current mailing list of a mailing (notice that unsubscribed customers
	 *                                 like "Opt-Out by Admin" are considered present anyway).
     * @return a map of an openers count (mailingId -> openersCount)
     */
	Map<Integer, Integer> getOpeners(int companyId, Collection<Integer> mailingsId, boolean currentRecipientsOnly);

    Map<Integer, Integer> getClickers(int companyId, List<Integer> mailingsId);

	/**
	 * Calculate a number of a clickers for each mailingId.
	 *
	 * @param companyId an identifier of a company.
	 * @param mailingsId a collections of mailing identifiers.
	 * @param currentRecipientsOnly whether ({@code true}) or not ({@code false}) exclude recipients who are not present
	 *                                 in a current mailing list of a mailing (notice that unsubscribed customers
	 *                                 like "Opt-Out by Admin" are considered present anyway).
	 * @return a map of a clickers count (mailingId -> clickersCount)
	 */
	Map<Integer, Integer> getClickers(int companyId, Collection<Integer> mailingsId, boolean currentRecipientsOnly);

	PaginatedListImpl<Map<String, Object>> getUnsentMailings(Admin admin, int rownums);

	PaginatedListImpl<Map<String, Object>> getPlannedMailings(Admin admin, int rownums);

	List<LightweightMailing> getMailingNames(Admin admin);

    List<Integer> getClassicTemplatesByName(String name, int companyId);

    List<LightweightMailing> getAllMailingsSorted(Admin admin, String sortFiled, String sortDirection);

    List<LightweightMailing> getMailingsDateSorted(Admin admin);

	List<Map<String, Object>> getMailingsNamesByStatus(Admin admin, List<MailingType> mailingTypes,
			String workStatus, String mailingStatus,
			boolean takeMailsForPeriod, String sort, String order);

	List<Integer> findTargetDependentMailings(int targetGroupId, int companyId);

	List<Integer> filterNotSentMailings(List<Integer> mailings);

    boolean deleteMailingsByCompanyIDReally(int companyID);

    List<Integer> getBirtReportMailingsToSend(int companyID, int reportId, Date startDate, Date endDate, int filterId, int filterValue);

    Map<String, Object> getMailingWithWorkStatus(int mailingId, int companyId);

	List<MailingDto> getUnsentMailings(Admin admin, boolean planned);

    // Todo EMMGUI-953 check usage and remove after
	List<CalendarUnsentMailing> getNotSentMailings(Admin admin, boolean planned);

    boolean usedInCampaignManager(int mailingId);

	/**
	 * Check if a mailing referenced by {@code mailingId} is being used in a running (status is either "active" or "testing") workflow.
	 *
	 * @param mailingId an identifier of a mailing to be checked.
	 * @param companyId an identifier of a company that owns a referenced mailing.
	 * @return {@code true} if there's running workflow using referenced mailing or {@code false} otherwise.
	 */
    boolean usedInRunningWorkflow(int mailingId, int companyId);

    int getWorkflowId(int mailingId);

	int getWorkflowId(int mailingId, int companyId);

    void cleanTestDataInSuccessTbl(int mailingId, int companyId);

    MailingStatus getStatus(int companyID, int mailingID);
    
	List<Integer> getMailingIdsForIntervalSend();

	List<LightweightMailing> getLightweightMailings(int companyID, int targetId);

	List<Integer> getSampleMailingIDs();
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final  int companyId);
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final  int companyId, boolean includeInactive);

    String getMailingName(int mailingId, int companyId);

    Map<Integer, String> getMailingNames(Collection<Integer> mailingIds, int companyId);

	List<MailingArchiveEntry> listMailingArchive(int campaignId, DateRange sendDate, Integer countLimit, int companyId);

	int getAnyMailingIdForCompany(int companyID);

	void cleanTestDataInMailtrackTbl(int mailingId, int companyId);

	/**
	 * Retrieve an identifier of mailinglist (see {@link Mailing#getMailinglistID()}) assigned to referenced mailing.
	 *
	 * @param mailingId an identifier of the mailing whose mailinglistId should be retrieved.
	 * @param companyId an identifier of a company that owns referenced mailing.
	 * @return identifier of assigned mailinglist or 0.
	 */
	int getMailinglistId(int mailingId, int companyId);

	/**
	 * Retrieve target expression assigned to the referenced mailing.
	 *
	 * @param mailingId an identifier of the mailing whose target expression should be retrieved.
	 * @param companyId an identifier of a company that owns referenced mailing.
	 * @return target expression
	 * or {@code null} (if mailing doesn't exist)
	 * or empty string (if mailing is there but target expression is not assigned).
	 */
	String getTargetExpression(int mailingId, int companyId);

	/**
	 * Retrieve target expression and (optional) splitId assigned to the referenced mailing.
	 *
	 * @param mailingId an identifier of the mailing whose target expression should be retrieved.
	 * @param companyId an identifier of a company that owns referenced mailing.
	 * @param appendListSplit whether ({@code true}) or not ({@code false}) splitId should be retrieved as part of target expression.
	 * @return target expression including (if appendListSplit = true) splitId
	 * or {@code null} (if mailing doesn't exist)
	 * or empty string (if mailing is there but requested values are not assigned).
	 */
	String getTargetExpression(int mailingId, int companyId, boolean appendListSplit);

    Map<Integer, String> getTargetExpressions(int companyId, Set<Integer> mailingIds);

    Map<Integer, Set<Integer>> getTargetsUsedInContent(int companyId, Set<Integer> mailingIds);

    /**
	 * Set a target expression to a mailing.
	 *
	 * @param mailingId an identifier of a mailing to be updated.
	 * @param companyId an identifier of current user's company.
	 * @param targetExpression a new target expression.
	 * @return {@code true} if succeeded or {@code false} if mailing doesn't exist (or marked as deleted).
	 * @throws TooManyTargetGroupsInMailingException if a target expression is too long.
	 */
	boolean setTargetExpression(int mailingId, int companyId, String targetExpression) throws TooManyTargetGroupsInMailingException;

	boolean isActiveIntervalMailing(final int mailingID);

	boolean deleteAccountSumEntriesByCompany(int companyID);
	
	boolean isAdvertisingContentType(int companyId, int mailingId);

	/**
	 * See GWUA-3991.
	 */
    boolean isTextVersionRequired(int companyId, int mailingId);

	Date getMailingSendDate(int companyID, int mailingID);

	List<LightweightMailingWithMailingList> listAllActionBasedMailingsForMailinglists(Set<Integer> mailinglistsIds, int companyID);

	LightweightMailing getLightweightMailing(int companyId, int mailingId);

	boolean tryToLock(int mailingId, int adminId, int companyId, long duration, TimeUnit durationUnit);

	String getEmailSubject(int companyID, int mailingID);

	MailingContentType getMailingContentType(int companyId, int mailingId);
	
    int getMailingLockingAdminId(int mailingId, int companyId);

	List<LightweightMailing> getUnsetMailingsForRootTemplate(int companyId, int templateId);

	boolean isActiveIntervalMailing(int companyID, int mailingID);

	boolean resumeDateBasedSending(int mailingId);

	boolean isThresholdClearanceExceeded(int mailingId);

	void removeApproval(int mailingID, int companyID);

	boolean isApproved(int mailingId, int companyId);

	void restoreMailings(Collection<Integer> mailingIds, int companyID);

	List<Map<String, Object>> getMailingsMarkedAsDeleted(int companyID, Date deletedMailingExpire);

	void deleteOutdatedMailingData(int companyID, int mailingID);

	void markMailingAsDataDeleted(int mailingID);

	boolean isDateBasedMailingWasSentToday(int mailingId);

    void allowDateBasedMailingResending(int mailingId);

	boolean saveMailingDescriptiveData(Mailing mailing);

    List<LightweightMailing> getMailingsUsingEmmAction(int actionId, int companyID);

	List<LightweightMailing> getMailingTemplates(int companyID);

	void deleteMailtrackDataForMailing(int companyID, int mailingID);

	/**
	 * Checks whether a mailing was sent at least once and whether one was sent more than 'expire-mailtrack' (taken from company_info_tbl) days ago
	 *
	 * @param mailingID Id of the mailing in database
	 * @param companyID Id of the company that created a mailing
	 * @return an instance of {@link com.agnitas.beans.impl.MailingSendStatusImpl}
	 */
	MailingSendStatus getMailingSendStatus(int mailingID, int companyID);

	/**
	 * Gets mailing with mediatypes
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return Mailing bean object or null
	 */
	Mailing getMailing(int mailingID, int companyID);

	/**
	 * Saves mailing, its mediatypes and trackable links
	 *
	 * @param mailing Mailing bean object; can be changed inside the method by loading id for new mailing
	 * @param preserveTrackableLinks if <code>true</code> unused trackable links are not removed from list
	 *
	 * @return id of saved mailing
	 */
	int saveMailing(Mailing mailing, boolean preserveTrackableLinks);

	/**
	 * Saves mailing, its mediatypes and trackable links
	 *
	 * @param mailing Mailing bean object; can be changed inside the method by loading id for new mailing
	 * @param preserveTrackableLinks if <code>true</code> unused trackable links are not removed from list
	 * @param errorTolerant whether ({@code true}) some errors can be ignored — otherwise ({@code false})
	 *                         an exception will be thrown on every saving error.
	 *
	 * @return id of saved mailing
	 */
	int saveMailing(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant, boolean removeUnusedContent);

	/**
	 * Marks mailing as deleted
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return true - success; false - if the mailing does not exist in database
	 */
	boolean markAsDeleted(int mailingID, int companyID);

	/**
	 * Loads non-deleted mailings from certain mailing list
	 *
	 * @param companyID
	 *              Id of the company for mailings
	 * @param mailinglistID
	 *              Id of mailing list
	 * @return List of Mailing
	 */
	List<Mailing> getMailingsForMLID( int companyID, int mailinglistID);

	/**
	 *  Loads mailing action names with their full urls
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return LinkedList
	 */
	List<Map<String, String>> loadAction(int mailingID, int companyID);

	/**
	 * Gets id of the mailing from certain mailing list that have been last sent for the given customer by the given company
	 *
	 * @param customerID
	 *              Id of the customer
	 * @param companyID
	 *              Id of the company
	 * @param mailinglist
	 *              Id of the mailing list
	 * @return positive number or zero
	 */
	int findLastNewsletter(int customerID, int companyID, int mailinglist);

	/**
	 *  Could not be used in fact because the column rdir_domain does not exist in mailinglist_tbl
	 *
	 */
	String getMailingRdirDomain(int mailingID, int companyID);

	/**
	 * Returns all (non-deleted) mailings as a list of lightweight objects.
	 *
	 * @param companyID company ID
	 *
	 * @return list of lightweight mailing objects
	 */
	List<LightweightMailing> getLightweightMailings( int companyID);

	/**
	 * Returns all (non-deleted) mailings and templates by given ids as a list of lightweight objects.
	 *
	 * @param companyId company ID.
	 * @param mailingIds a collection of mailing ids.
	 *
	 * @return list of lightweight mailing objects
	 */
	List<LightweightMailing> getLightweightMailings(int companyId, Collection<Integer> mailingIds);

	/**
	 * Selects all non-deleted mailings of certain company and creates paginated list according to given criteria of sorting and pagination
	 *
	 * @param admin current user.
	 * @param props filtering, sorting and pagination parameters for mailing list selection.
	 *
	 * @return PaginatedList of MailingBase
	 */
	PaginatedListImpl<Map<String, Object>> getMailingList(Admin admin, MailingsListProperties props) throws FulltextSearchInvalidQueryException;

	List<LightweightMailing> getLightweightMailings(Admin admin, MailingsListProperties props) throws FulltextSearchInvalidQueryException;

	/**
	 * Checks whether full-text indices for name and description of a mailing are available
	 */
	boolean isBasicFullTextSearchSupported();

	/**
	 * Checks whether full-text indices for mailing content are available
	 */
	boolean isContentFullTextSearchSupported();

	/**
	 *  Gets date format by given type number
	 * @param type
	 *          The type number
	 * @return  non-empty string
	 */
	String getFormat(int type);

	/**
	 * if a mailing has been as a world mailing a statusid has been generated
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return 0 if no worldmailing has been generated
	 */
	int getStatusidForWorldMailing(int mailingID, int companyID);

	/**
	 * Retrieve a genstatus value from the latest maildrop entry or -1 if there's no one.
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param statuses
	 *              Filter for work_status column
	 */
	int getLastGenstatus(int mailingID, char ...statuses);

	/**
	 * Checks, if a mailing has at least one recipient required for preview.
	 *
	 * @param mailingId
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return true, if at least one recipient is present, otherwise false
	 */
	boolean hasPreviewRecipients(int mailingId, int companyID);

	/**
	 * Is there any transmission for that mailing running ? - There is no entry
	 * in maildrop_status_tbl for that mailing_id -> ready - There are matching
	 * entries in both maildrop_status_tbl and mailing_account_tbl that means ->
	 * ready - There are only entries in maildrop_status_tbl -> not ready
	 *
	 * @param mailingID
	Id of the mailing in database
	 * @return true
	 */
	boolean isTransmissionRunning(int mailingID);

	/**
	 *  Checks if any action related to given mailing exists
	 *
	 * @param mailingId
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return true - has at list one action, otherwise - false
	 */
	boolean hasActions(int mailingId, int companyID);

	/**
	 * Returns the mailing IDs referencing the given template.
	 *
	 * @param mailTemplate
	 *            referenced template
	 *
	 * @return list of mailing IDs referencing given template
	 */
	List<Integer> getTemplateReferencingMailingIds(Mailing mailTemplate);

	/**
	 * Check if it's mailing or template
	 *
	 * @param templateID
	 *              Id of the mailing/template in database
	 * @param companyID
	 *              Id of the company that created a mailing/template
	 * @return  true - it's template, false - it's mailing
	 */
	boolean checkMailingReferencesTemplate(int templateID, int companyID);

	/**
	 * Checks the existence of mailing in the database
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param companyID
	 *              Id of the company that created a mailing
	 * @return true - if the mailing exists, false - if does not
	 */
	boolean exist(int mailingID, int companyID);

	/**
	 * Checks the existence of template in the database
	 */
	boolean exist(int mailingID, int companyID, boolean isTemplate);

	/**
	 * Loads list of non-deleted mailing have been sent by certain company
	 *
	 * @param admin current user
	 * @return  List of MailingBase bean objects
	 */
	PaginatedListImpl<MailingBase> getMailingsForComparison(MailingComparisonFilter filter, Admin admin);

	/**
	 * Loads list of non-deleted templates of certain company
	 *
	 * @param companyID
	 *               Id of the company
	 * @return List of MailingBase bean objects
	 */
	List<MailingBase> getTemplateMailingsByCompanyID( int companyID);

	/**
	 * Loads list of action-based mailings have been sent by certain company
	 *
	 * @param companyID
	 *              Id of the company that sent the mailings
	 * @return  List of MailingBase bean objects
	 */
	List<MailingBase> getMailingsByStatusE( int companyID);

	/**
	 * Loads list of non-deleted mailings/templates of certain company
	 * @param companyId
	 *             Id of the company
	 * @param isTemplate
	 *              true - load templates, false - load mailings
	 * @return List of Mailing objects
	 */
	List<Mailing> getMailings( int companyId, boolean isTemplate);

	/**
	 * Gets id of open action for the mailing
	 *
	 * @param mailingID
	 *              Id of the mailing
	 * @param companyID
	 *              Id  of the company
	 * @return positive integer or zero
	 */
	int getMailingOpenAction(int mailingID, int companyID);

	/**
	 * Gets id of click action for the mailing
	 *
	 * @param mailingID
	 *              Id of the mailing
	 * @param companyID
	 *              Id  of the company
	 * @return positive integer or zero
	 */
	int getMailingClickAction(int mailingID, int companyID);

	/**
	 * Gets parameter string for mailing of email type
	 * @param mailingID
	 *              Id of the mailing
	 * @return String object or null
	 */
	String getEmailParameter(int mailingID);

	String getSQLExpression(String targetExpression);

	MailingType getMailingType(int mailingID);

	List<MailingBase> getMailingTemplatesWithPreview(MailingTemplateSelectionFilter filter, Admin admin);

	boolean clearPlanDate(int mailingId, int companyId);

	boolean isTemplate(int mailingId, int companyId);

	boolean isMarkedAsDeleted(int mailingId, int companyID);
}
