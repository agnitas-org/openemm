/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.agnitas.web.mvc.Popups;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.service.SimpleServiceResult;


public interface ComMailingBaseService {
    boolean isMailingExists(int mailingId, int companyId);
    boolean isMailingExists(int mailingId, int companyId, boolean isTemplate);
    boolean checkUndoAvailable(int mailingId);
    boolean isTextTemplateExists(Admin admin, int mailingId);

    void bulkDelete(Set<Integer> mailingsIds, int companyId);

    int getWorkflowId(int mailingId, int companyId);

    int saveMailingWithUndo(Mailing mailing, int adminId, boolean preserveTrackableLinks);
    void restoreMailingUndo(ApplicationContext ctx, int mailingId, int companyId) throws Exception;

    /**
     * Set target groups (see {@link Mailing#setTargetGroups(Collection)}) and their combining mode (see {@link Mailing#setTargetMode(int)}) if it's permitted.
     * Here are the reasons why target groups couldn't be updated:
     * - mailing doesn't exist or marked as deleted;
     * - mailing is managed by campaign manager (see {@link com.agnitas.dao.ComMailingDao#usedInCampaignManager(int)});
     *
     * @param mailingId an identifier of a mailing to be updated.
     * @param companyId an identifier of current user's company.
     * @param targetGroupIds target group identifiers to be set to a mailing.
     * @param conjunction use conjunction ({@code true}) or disjunction ({@code false}) to combine target groups.
     * @return {@code true} if succeeded or {@code false} if mailing doesn't exist or updating is not permitted.
     * @throws TooManyTargetGroupsInMailingException if a composed target expression is too long.
     */
    boolean setTargetGroups(int mailingId, int companyId, Collection<Integer> targetGroupIds, boolean conjunction) throws TooManyTargetGroupsInMailingException;

    String getMailingName(int mailingId, int companyId);

    /**
     * Retrieve a subset (see pagination parameters {@code pageNumber} and {@code rowsPerPage}) of a recipients that successfully
     * received the mailing referenced by {@code mailingId}. Use zero/negative value for {@code rowsPerPage} to retrieve all
     * the available rows without pagination.
     *
     * @param mailingId an identifier of the target mailing
     * @param companyId an identifier of current user's company
     * @param filterType recipients to retrieve. Allowed values:
     * {@link MailingRecipientExportWorker#MAILING_RECIPIENTS_ALL}, {@link MailingRecipientExportWorker#MAILING_RECIPIENTS_OPENED},
     * {@link MailingRecipientExportWorker#MAILING_RECIPIENTS_CLICKED}, {@link MailingRecipientExportWorker#MAILING_RECIPIENTS_BOUNCED},
     * {@link MailingRecipientExportWorker#MAILING_RECIPIENTS_UNSUBSCRIBED}.
     * @param pageNumber a sequential number of a page
     * @param rowsPerPage a maximal entries count shown at a page
     * @param sortCriterion column name to sort by
     * @param sortAscending sort direction
     * @return a filled {@link org.agnitas.beans.impl.PaginatedListImpl} instance
     * @throws Exception
     */
    PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns) throws Exception;

    PaginatedListImpl<Map<String, Object>> getPaginatedMailingsData(Admin admin, MailingsListProperties props);
    
    /**
     * A shortcut for {@link #getDynamicTags(int, int, boolean)} passing {@code false} as {@code resetIds} argument.
     */
    List<DynamicTag> getDynamicTags(int mailingId, int companyId);

    /**
     * Retrieve a dynamic contents of a mailing referenced by {@code mailingId}.
     *
     * @param mailingId an identifier of a mailing whose dynamic content is to be retrieved.
     * @param companyId an identifier of a company of the current user.
     * @param resetIds if {@code true} then assign zeros to all the identifiers (convenient for further copying this dynamic content to another mailing).
     * @return a list of an entities representing dynamic contents.
     */
    List<DynamicTag> getDynamicTags(int mailingId, int companyId, boolean resetIds);
    
    /**
     * Search for all dynamic tags name by {@code mailing}
     *
     * @param mailing
     * @return a list of dyn tag names
     */
    List<String> getDynamicTagNames(Mailing mailing);

    DynamicTag getDynamicTag(int companyId, int dynNameId);

    /**
     * Get distinct number of recipients for referenced mailinglist and other settings.
     * Included recipients must:
     * - belong to a mailinglist referenced by {@code mailingListId};
     * - be active (see {@link UserStatus#Active});
     * - be selected by target expression (see {@code targetGroupIds} and {@code conjunction});
     * - be selected by list split referenced by {@code splitId} (if > 0).
     *
     * @param companyId an identifier of a company of the current user.
     * @param mailingListId an identifier of a mailinglist that an included recipient should belong to.
     * @param splitId an identifier of a list split (target group) that an included recipient should be selected by.
     * @param targetGroupIds a set of target groups that an included recipient should be selected by (see also {@code conjunction}).
     * @param conjunction a target groups combining mode ({@code true} means AND, {@code false} means OR).
     * @return distinct number of recipients for referenced mailinglist and other settings.
     * @throws Exception if calculation is impossible or some data is corrupted.
     */
    int calculateRecipients(int companyId, int mailingListId, int splitId, Collection<Integer> altgIds, Collection<Integer> targetGroupIds, boolean conjunction) throws Exception;

    /**
     * Get distinct number of recipients for referenced mailinglist and other settings.
     * Included recipients must:
     * - belong to a mailinglist referenced by {@code mailingListId};
     * - be active (see {@link UserStatus#Active});
     * - be selected by target expression of the mailing referenced by {@code mailingId};
     * - be selected by list split referenced by {@code splitId} (if > 0).
     *
     * @param companyId an identifier of a company of the current user.
     * @param mailingId an identifier of a mailing that target expression should be taken from.
     * @param mailingListId an identifier of a mailinglist that an included recipient should belong to.
     * @param splitId an identifier of a list split (target group) that an included recipient should be selected by.
     * @return distinct number of recipients for referenced mailinglist and other settings.
     * @throws Exception if calculation is impossible or some data is corrupted.
     */
    int calculateRecipients(int companyId, int mailingId, int mailingListId, int splitId) throws Exception;

    /**
     * Get distinct number of recipients for referenced mailing.
     * Included recipients must:
     * - belong to a mailinglist of referenced mailing;
     * - be active (see {@link UserStatus#Active});
     * - be selected by target expression and split list (if any) of referenced mailing.
     *
     * @param companyId an identifier of a company of the current user.
     * @param mailingId an identifier of a mailing to calculate recipients for.
     * @return distinct number of recipients for referenced mailing.
     * @throws Exception if calculation is impossible or some data is corrupted.
     */
    int calculateRecipients(int companyId, int mailingId) throws Exception;
	DataSource getDataSource();

    int calculateRecipients(CalculationRecipientsConfig config) throws Exception;
	
	/**
	 * Check if mailing content type for referenced mailing is null or has 'advertising' value
	 *
	 * @param companyId the company id of current user
	 * @param mailingId an identifier of a mailing
	 * @return true if mailing has advertising content type
	 */
	boolean isAdvertisingContentType(int companyId, int mailingId);
	
	boolean isLimitedRecipientOverview(Admin admin, int mailingId);

    /**
     * Check if the given mailing content is blank (resolve all the dyn-tags (if any) and check if mail contains at least
     * one non-whitespace character).
     * Notice that an algorithm is recursive so if content refers some dynamic block that refers another one then
     * all the levels will be processed properly.
     *
     * @param content a given mailing content to evaluate.
     * @param contentMap a map of user-defined content block that the dyn-tags should be replaced with.
     * @return whether ({@code true}) or not ({@code false}) a content is considered blank.
     */
    boolean isContentBlank(String content, Map<String, DynamicTag> contentMap);

    void doTextTemplateFilling(Mailing mailing, Admin admin, ActionMessages messages);
    
    void doTextTemplateFilling(Mailing mailing, Admin admin, Popups popups);
    
    Mailing getMailing(int companyId, int mailingId);
    
    /**
     * Loads list of non-deleted mailing have been sent by certain company
     *
     * @param admin
     *                  Id of the company that sent the mailings
     * @return  List of MailingBase bean objects
     */
    List<MailingBase> getMailingsForComparison(Admin admin);
    
    Map<Integer, String> getMailingNames(List<Integer> mailingIds, int companyId);

	List<LightweightMailing> getMailingsByType(MailingType type, int companyId);
	List<LightweightMailing> getMailingsByType(MailingType type, int companyId, boolean includeInactive);
    
    MailingSendStatus getMailingSendStatus(int mailingId, int companyId);
    
    MailingType getMailingType(int mailingId) throws Exception;

    Date getMailingLastSendDate(int mailingId);

    /**
     * Check if required content (depends on selected media types) is not blank.
     *
     * @param mailing a mailing entity to check.
     */
    SimpleServiceResult checkContentNotBlank(Mailing mailing);

    void activateTrackingLinksOnEveryPosition(Admin admin, Mailing mailing, ApplicationContext context) throws Exception;
    
	List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded);
}
