/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;

public interface ComMailingDao extends MailingDao {
	int SEND_STATS_TEXT = 100;
	int SEND_STATS_HTML = 101;
	int SEND_STATS_OFFLINE = 102;

    List<Map<String, Object>> getMailingsForMLIDs(Set<Integer> mailinglistIds, @VelocityCheck int companyId);

	Mailing getMailingWithDeletedDynTags(int mailingID, @VelocityCheck int companyID);
	
	boolean updateStatus(int mailingID, String status);
	
	// returns all Mailings in a linked List.
	List<ComMailing> getAllMailings(@VelocityCheck int companyID);
	// returns the given amount of mailings
	List<ComMailing> getMailings(@VelocityCheck int companyID, int adminId, int count, String mailingStatus, boolean takeMailsForPeriod);

    List<Map<String, Object>> getMailings(@VelocityCheck int companyId, String commaSeparatedMailingIds);
	
	boolean hasEmail(int mailingID);

    Map<Integer, Integer> getSendStats(ComMailing mailing, @VelocityCheck int companyId) throws Exception;

	boolean saveStatusmailRecipients(int mailingID, String statusmailRecipients);

	boolean saveStatusmailOnErrorOnly(int companyID, int mailingID, boolean statusmailOnErrorOnly);

    /**
     * Get mailings which is set as the followup to given one,
     *
     * @param mailingID - target mailing id
     * @param companyID
     * @param includeUnscheduled - whether the unscheduled followup mailings will be included.
     * @return - list of followup mailings id. Is not recursive.
     */
    List<Integer> getFollowupMailings(int mailingID, @VelocityCheck int companyID, boolean includeUnscheduled);

	/**
	 * The last send-date of a mailing. The state 'W' will be preferred even if there a later send-dates with state 'A' or 'T' !
	 * @param mailingID
	 * @return - the send-date , null if the mailing has not been sent yet
	 */
	Timestamp getLastSendDate(int mailingID) ;

	/**
	 * Get a list of mailing data (mailing_id, shortname, description and mailinglist's shortname).
	 *
	 * Use {@code searchName} and {@code searchDescription} to apply filter (fulltext search) to column(s) shortname and/or description. To enable the fulltext search engine
	 * a {@code searchQuery} argument should be a non-blank vendor-specific (see {@link com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator#generateSpecificQuery(String)}) query text
	 * and either {@code searchName} or {@code searchDescription} (or both) should be {@code true}.
	 *
	 * The {@code sortCriteria} argument (when valid non-blank criteria name passed) specifies the primary sorting column. If {@code sortCriteria} is blank or invalid
	 * then the relevance criteria (if fulltext search enabled) or mailing_id (if fulltext search disabled) used as a primary sorting criteria.
	 * The mailing_id column is a secondary sorting criteria (if primary criteria isn't mailing_id; always follows a descending order).
	 *
	 * @param companyID an identifier of current company.
	 * @param searchQuery a vendor-specific query for fulltext search.
	 * @param searchName whether ({@code true}) or not ({@code false}) use fulltext search for mailing shortname column.
	 * @param searchDescription whether ({@code true}) or not ({@code false}) use fulltext search for mailing description column.
	 * @param sortCriteria a primary sorting criteria (a column name).
	 * @param sortAscending a primary sorting direction (ascending ({@code true}) or descending ({@code false})).
	 * @param pageNumber a pagination parameter (1-based sequential index of a page to be shown).
     * @param pageSize a pagination parameter (rows count per page).
     * @return an instance of {@link org.agnitas.beans.impl.PaginatedListImpl} that holds resulting list and sorting/pagination parameters.
     */
	PaginatedListImpl<Map<String, Object>> getMailingShortList(@VelocityCheck int companyID, String searchQuery, boolean searchName, boolean searchDescription, String sortCriteria, boolean sortAscending, int pageNumber, int pageSize);

    int saveUndoMailing(int mailingId, int adminId);

    void deleteUndoDataOverLimit(int mailingId);
	
	int getFollowUpStat(ComFollowUpStats comFollowUpStats, boolean useTargetGroups) throws Exception;

    boolean isMailingMarkedDeleted(int mailingID, @VelocityCheck int companyID);
	
	int getFollowUpStat(int mailingID, int followUpFor, String followUpType, @VelocityCheck int companyID, boolean useTargetGroups) throws Exception;

	int getFollowUpStat(int followUpFor, String followUpType, @VelocityCheck int companyID, String sqlTargetExpression) throws Exception;

	/**
	 * returns the type of a Followup Mailing as String.
	 * The String can be fount in the mailing-class (eg. Mailing.TYPE_FOLLOWUP_CLICKER)
	 * if no followup is found, null is the returnvalue!
	 * @param mailingID
	 * @return
	 */
	String getFollowUpType(int mailingID);

    String getTargetExpressionForId(int expressionID);

    int getCompanyIdForMailingId(int mailingId);
	
	ComRdirMailingData getRdirMailingData(int mailingId);
	
	/**
	 * returns the base mailing for the given one.
	 * @param mailingID
	 * @return
	 */
	String getFollowUpFor(int mailingID);
	
	/**
	 * returns the last change timestamp of the given mailing.
	 * @param mailingID
	 * @return
	 */
	Date getChangeDate(int mailingID);
	
    PaginatedListImpl<Map<String, Object>> getDashboardMailingList(@VelocityCheck int companyId, int adminId, String sort, String direction, int rownums);

	PaginatedListImpl<Map<String, Object>> getDashboardThumbnailsMailingList(@VelocityCheck int companyId, int adminId, String sort, String direction, int rownums);
    
    /**
     * Get the last n sent world mailings in descend order
     * @param companyId
     * @param adminId
	 * @param number number of mailings you want to achieve
	 * @return a list of hashmaps. Currently there are the keys mailingid (int) and shortname (string) available
     */
    List<Map<String, Object>> getLastSentWorldMailings(@VelocityCheck int companyId, int adminId, int number);

    int getLastSentMailingId(@VelocityCheck int companyID);
	    
    Date getSendDate(@VelocityCheck int companyId, int mailingId);
    
    
    /**
     * this method returns the mailing-ID for the last sent world mailing.
     * @param companyID
     * @param customerID
     * @return
     */
    int getLastSentMailing(@VelocityCheck int companyID, int customerID);
    
    /**
	 * This method returns the mailing ID for a already sent world-mailing with the given companyID and
	 * (if given) the mailingListID. If no mailingListID is given (null or "0") it will be ignored.
	 * @param companyID
	 * @param mailingListID
	 * @return
	 */
    int getLastSentWorldMailingByCompanyAndMailinglist(@VelocityCheck int companyID, int mailingListID);

    List<MailingBase> getSentWorldMailingsForReports(@VelocityCheck int companyID, int number);

    List<MailingBase> getPredefinedMailingsForReports(@VelocityCheck int companyId, int number, int filterType, int filterValue, int mailingType, String orderKey);

	List<MailingBase> getPredefinedNormalMailingsForReports(@VelocityCheck int companyId, Date from, Date to, int filterType, int filterValue, String orderKey);

	//List<LightweightMailing> getTemplateOverview(@VelocityCheck int companyID);
	
	boolean deleteContentFromMailing(Mailing mailing, int contentID);

	boolean deleteContentFromMailing(@VelocityCheck int companyId, int mailingId, int contentId);

    void deleteAllDynTags(int mailingId);

    List<Map<String, Object>> getSentAndScheduled(@VelocityCheck int companyId, int adminId, Date startDate, Date endDate);

    List<Map<String, Object>> getPlannedMailings(@VelocityCheck int companyId, int adminId, Date startDate, Date endDate);

    Map<Integer, Integer> getOpeners(@VelocityCheck int companyId, List<Integer> mailingsId);

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
	Map<Integer, Integer> getOpeners(@VelocityCheck int companyId, Collection<Integer> mailingsId, boolean currentRecipientsOnly);

    Map<Integer, Integer> getClickers(@VelocityCheck int companyId, List<Integer> mailingsId);

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
	Map<Integer, Integer> getClickers(@VelocityCheck int companyId, Collection<Integer> mailingsId, boolean currentRecipientsOnly);

	Map<Integer, Integer> getSentNumber(@VelocityCheck int companyId, Collection<Integer> mailingsId);

    PaginatedListImpl<Map<String, Object>> getUnsentMailings(@VelocityCheck int companyId, int adminId, int rownums);

    PaginatedListImpl<Map<String, Object>> getPlannedMailings(@VelocityCheck int companyId, int adminId, int rownums);
    
    List<LightweightMailing> getMailingNames(@VelocityCheck int companyID, int adminId);

    List<LightweightMailing> getAllMailingsSorted(@VelocityCheck int companyId, int adminId, String sortFiled, String sortDirection);

	List<LightweightMailing> getMailingsDateSorted(@VelocityCheck int companyID, int adminId);

    List<Map<String, Object>> getMailingsNamesByStatus(@VelocityCheck int companyID, int adminId, List<Integer> mailingTypes,
													   String workStatus, String mailingStatus,
													   boolean takeMailsForPeriod, String sort, String order);

    List<LightweightMailing> getMailingsDependentOnTargetGroup(@VelocityCheck int companyID, int targetGroupID);

    boolean existMailingsDependsOnTargetGroup(int companyID, int targetGroupID);

	boolean existsNotSentMailingsDependsOnTargetGroup(int companyID, int targetGroupID);

    boolean existMailingsWhichComponentDependsOnTargetGroup(int companyID, int targetGroupID);

	boolean existsNotSentMailingsWhichComponentDependsOnTargetGroup(int companyID, int targetGroupID);

    boolean existMailingsWhichContentDependsOnTargetGroup(int companyID, int targetGroupID);

	boolean existsNotSentMailingsWhichContentDependsOnTargetGroup(int companyID, int targetGroupID);

	List<LightweightMailing> getActionDbMailingNames(@VelocityCheck int companyID, String types, String sort, String direction);
        
    boolean deleteMailingsByCompanyIDReally(@VelocityCheck int companyID);
    
    Mailing copyMailing(int newCompanyId, int mailinglistID, Mailing aTemplate, boolean isTemplate) throws Exception;
    
    ComMailing copyMailing(int newCompanyId, int mailinglistID, ComMailing aTemplate, boolean isTemplate) throws Exception;

    int copyMailing(int mailingId, int companyId, String newMailingNamePrefix) throws Exception;

    List<Integer> getBirtReportMailingsToSend(int companyID, int reportId, Date startDate, Date endDate, int filter, int filterValue);

    Map<String, Object> getMailingWithWorkStatus(int mailingId, int companyId);

    boolean usedInCampaignManager(int mailingId);

	/**
	 * Check if a mailing referenced by {@code mailingId} is being used in a running (status is either "active" or "testing") workflow.
	 *
	 * @param mailingId an identifier of a mailing to be checked.
	 * @param companyId an identifier of a company that owns a referenced mailing.
	 * @return {@code true} if there's running workflow using referenced mailing or {@code false} otherwise.
	 */
    boolean usedInRunningWorkflow(int mailingId, @VelocityCheck int companyId);

    int getWorkflowId(int mailingId);

	int getWorkflowId(int mailingId, @VelocityCheck int companyId);

    void cleanTestDataInSuccessTbl(int mailingId, @VelocityCheck int companyId);

    String getWorkStatus(int companyID, int mailingID);
    
	List<Integer> getMailingIdsForIntervalSend();
	
	List<Integer> getSampleMailingIDs();
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final @VelocityCheck  int companyId);
	
	List<LightweightMailing> getMailingsByType(final int mailingType, final @VelocityCheck  int companyId, boolean includeInactive);

    String getMailingName(int mailingId, @VelocityCheck int companyId);

    Map<Integer, String> getMailingNames(Collection<Integer> mailingIds, @VelocityCheck int companyId);

	List<Map<String, Object>> getMailingsForActionOperationGetArchiveList(int companyID, int campaignID);

	int getAnyMailingIdForCompany(int companyID);

	void cleanTestDataInMailtrackTbl(int mailingId, int companyId);

	/**
	 * Retrieve an identifier of mailinglist (see {@link ComMailing#getMailinglistID()}) assigned to referenced mailing.
	 *
	 * @param mailingId an identifier of the mailing whose mailinglistId should be retrieved.
	 * @param companyId an identifier of a company that owns referenced mailing.
	 * @return identifier of assigned mailinglist or 0.
	 */
	int getMailinglistId(int mailingId, @VelocityCheck int companyId);

	/**
	 * Retrieve target expression assigned to the referenced mailing.
	 *
	 * @param mailingId an identifier of the mailing whose target expression should be retrieved.
	 * @param companyId an identifier of a company that owns referenced mailing.
	 * @return target expression
	 * or {@code null} (if mailing doesn't exist)
	 * or empty string (if mailing is there but target expression is not assigned).
	 */
	String getTargetExpression(int mailingId, @VelocityCheck int companyId);

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
	String getTargetExpression(int mailingId, @VelocityCheck int companyId, boolean appendListSplit);

	/**
	 * Set a target expression to a mailing.
	 *
	 * @param mailingId an identifier of a mailing to be updated.
	 * @param companyId an identifier of current user's company.
	 * @param targetExpression a new target expression.
	 * @return {@code true} if succeeded or {@code false} if mailing doesn't exist (or marked as deleted).
	 * @throws TooManyTargetGroupsInMailingException if a target expression is too long.
	 */
	boolean setTargetExpression(int mailingId, @VelocityCheck int companyId, String targetExpression) throws TooManyTargetGroupsInMailingException;

	boolean isActiveIntervalMailing(final int mailingID);

	/**
	 * Retrieve a dynamic contents of a mailing referenced by {@code mailingId}.
	 *
	 * @param mailingId an identifier of a mailing whose dynamic content is to be retrieved.
	 * @param companyId an identifier of a company of the current user.
	 * @return a list of an entities representing dynamic contents.
	 */
	List<DynamicTag> getDynamicTags(int mailingId, @VelocityCheck int companyId);

	DynamicTag getDynamicTag(int dynNameId, @VelocityCheck int companyId);

	boolean deleteAccountSumEntriesByCompany(@VelocityCheck int companyID);
	
	boolean isAdvertisingContentType(@VelocityCheck int companyId, int mailingId);

	/**
	 * See GWUA-3991.
	 */
    boolean isTextVersionRequired(@VelocityCheck int companyId, int mailingId);

	Date getMailingSendDate(int companyID, int mailingID);

	List<LightweightMailing> listAllActionBasedMailingsForMailinglist(int companyID, int mailinglistID);

	LightweightMailing getLightweightMailing(int companyId, int mailingId);

	boolean tryToLock(int mailingId, int adminId, @VelocityCheck int companyId, long duration, TimeUnit durationUnit);

	MailingContentType getMailingContentType(int companyId, int mailingId);
}
