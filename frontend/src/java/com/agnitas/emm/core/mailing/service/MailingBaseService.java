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
import java.util.Set;
import javax.sql.DataSource;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingSendStatus;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonFilter;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Popups;
import org.springframework.context.ApplicationContext;

public interface MailingBaseService {

    boolean isMailingExists(int mailingId, int companyId);

    boolean isMailingExists(int mailingId, int companyId, boolean isTemplate);

    boolean checkUndoAvailable(int mailingId);

    boolean isTextTemplateExists(Admin admin, int mailingId);

    void bulkDelete(Set<Integer> mailingsIds, int companyId);

    boolean deleteMailing(int mailingId, int companyId);

    int getWorkflowId(int mailingId, int companyId);

    int saveMailingWithUndo(Mailing mailing, int adminId, boolean preserveTrackableLinks);

    boolean saveUndoData(int mailingId, int adminId);

    void restoreMailingUndo(ApplicationContext ctx, int mailingId, int companyId);

    String getMailingName(int mailingId, int companyId);

    PaginatedList<Map<String, Object>> getPaginatedMailingsData(Admin admin, MailingsListProperties props);
    
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
     */
    int calculateRecipients(int companyId, int mailingId, int mailingListId, int splitId, Collection<Integer> altgIds, Collection<Integer> targetGroupIds, boolean conjunction);

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
     */
    int calculateRecipients(int companyId, int mailingId);

	DataSource getDataSource();

    int calculateRecipients(CalculationRecipientsConfig config);
	
	/**
	 * Check if mailing content type for referenced mailing is null or has 'advertising' value
	 *
	 * @param companyId the company id of current user
	 * @param mailingId an identifier of a mailing
	 * @return true if mailing has advertising content type
	 */
	boolean isAdvertisingContentType(int companyId, int mailingId);

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

    void doTextTemplateFilling(Mailing mailing, Admin admin, Popups popups);
    
    Mailing getMailing(int companyId, int mailingId);
    
    /**
     * Loads list of non-deleted mailing have been sent by certain company
     *
     * @param admin
     *                  Id of the company that sent the mailings
     * @return  List of MailingBase bean objects
     */
    PaginatedList<MailingBase> getMailingsForComparison(MailingComparisonFilter filter, Admin admin);
    
    Map<Integer, String> getMailingNames(List<Integer> mailingIds, int companyId);

	List<LightweightMailing> getMailingsByType(MailingType type, int companyId);

	List<LightweightMailing> getMailingsByType(MailingType type, int companyId, boolean includeInactive);
    
    MailingSendStatus getMailingSendStatus(int mailingId, int companyId);
    
    MailingType getMailingType(int mailingId);

    Date getMailingLastSendDate(int mailingId);

    /**
     * Check if required content (depends on selected media types) is not blank.
     *
     * @param mailing a mailing entity to check.
     */
    SimpleServiceResult checkContentNotBlank(Mailing mailing);

    void activateTrackingLinksOnEveryPosition(Mailing mailing, ApplicationContext context) throws Exception;

    void deactivateTrackingLinksOnEveryPosition(Mailing mailing, ApplicationContext context);

	List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded);

    int getMailinglistId(int mailingId, int companyId);

    boolean isTemplate(int companyId, int mailingId);
}
