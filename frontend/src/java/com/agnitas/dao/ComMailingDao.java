/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface ComMailingDao extends MailingDao {

	Mailing getMailingWithDeletedDynTags(int mailingID, int companyID);

	boolean updateStatus(Mailing mailing, MailingStatus mailingStatus);

	boolean updateStatus(int mailingID, MailingStatus mailingStatus);
	
	boolean updateStatus(int mailingID, MailingStatus mailingStatus, Date sendDate);
	
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

    boolean isMailingMarkedDeleted(int mailingID, int companyID);

	/**
	 * returns the type of a Followup Mailing as String.
	 * The String can be fount in the mailing-class (eg. FollowUpType.TYPE_FOLLOWUP_CLICKER)
	 * if no followup is found, null is the returnvalue!
	 */
	String getFollowUpType(int mailingID);

    int getCompanyIdForMailingId(int mailingId);
	
	ComRdirMailingData getRdirMailingData(int mailingId);
	
	/**
	 * returns the base mailing for the given one.
	 */
	String getFollowUpFor(int mailingID) throws Exception;
	
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

	List<Map<String, Object>> getSentAndScheduled(Admin admin, Date startDate, Date endDate);

	List<Map<String, Object>> getPlannedMailings(Admin admin, Date startDate, Date endDate);

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

    String getWorkStatus(int companyID, int mailingID);
    
	List<Integer> getMailingIdsForIntervalSend();

	List<LightweightMailing> getLightweightMailings(int companyID, int targetId);

	List<Integer> getSampleMailingIDs();
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final  int companyId);
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final  int companyId, boolean includeInactive);

    String getMailingName(int mailingId, int companyId);

    Map<Integer, String> getMailingNames(Collection<Integer> mailingIds, int companyId);

	List<Map<String, Object>> getMailingsForActionOperationGetArchiveList(int companyID, int campaignID);

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

	List<LightweightMailing> listAllActionBasedMailingsForMailinglist(int companyID, int mailinglistID);

	LightweightMailing getLightweightMailing(int companyId, int mailingId);

	boolean tryToLock(int mailingId, int adminId, int companyId, long duration, TimeUnit durationUnit);

	String getEmailSubject(int companyID, int mailingID) throws Exception;

	MailingContentType getMailingContentType(int companyId, int mailingId);
	
    int getMailingLockingAdminId(int mailingId, int companyId);

	List<LightweightMailing> getUnsetMailingsForRootTemplate(int companyId, int templateId);

	boolean isActiveIntervalMailing(int companyID, int mailingID);

	boolean resumeDateBasedSending(int mailingId);

	boolean isThresholdClearanceExceeded(int mailingId);

	void removeApproval(int mailingID, int companyID);

	boolean isApproved(int mailingId, int companyId);

	List<Map<String, Object>> getMailingsMarkedAsDeleted(int companyID, Date deletedMailingExpire);

	void deleteOutdatedMailingData(int mailingID);

	void markMailingAsDataDeleted(int mailingID);

	/**
	 * Only store changed shortname, description an archiveid.
	 * Those are the values that are allowed for change even after a mailing was delievered
	 */
	boolean saveMailingDescriptiveData(Mailing mailing);
}
