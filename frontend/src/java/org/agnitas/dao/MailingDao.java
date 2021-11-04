/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;

public interface MailingDao {
	/**
	 * Checks whether a mailing was sent at least once and whether one was sent more than 'expire-mailtrack' (taken from company_info_tbl) days ago
	 *
	 * @param mailingID Id of the mailing in database
	 * @param companyID Id of the company that created a mailing
	 * @return an instance of {@link org.agnitas.beans.impl.MailingSendStatusImpl}
	 */
	MailingSendStatus getMailingSendStatus(int mailingID, @VelocityCheck int companyID);

	/**
	 * Gets mailing with mediatypes
     *
     * @param mailingID
     *              Id of the mailing in database
     * @param companyID
     *              Id of the company that created a mailing
     * @return Mailing bean object or null
	 */
	Mailing getMailing(int mailingID, @VelocityCheck int companyID);

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
	int saveMailing(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant) throws Exception;

	/**
	 * Marks mailing as deleted
     *
     * @param mailingID
     *              Id of the mailing in database
     * @param companyID
     *              Id of the company that created a mailing
     * @return true - success; false - if the mailing does not exist in database
	 */
	boolean deleteMailing(int mailingID, @VelocityCheck int companyID);

	/**
	 * Loads non-deleted mailings from certain mailing list
     *
     * @param companyID
     *              Id of the company for mailings
	 * @param mailinglistID
     *              Id of mailing list
	 * @return List of Mailing
	 */
	List<Mailing> getMailingsForMLID( @VelocityCheck int companyID, int mailinglistID);

    /**
     *  Loads mailing action names with their full urls
     *
     * @param mailingID
     *              Id of the mailing in database
     * @param companyID
     *              Id of the company that created a mailing
     * @return LinkedList
     */
	List<Map<String, String>> loadAction(int mailingID, @VelocityCheck int companyID);

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
	int findLastNewsletter(int customerID, @VelocityCheck int companyID, int mailinglist);

    /**
     * Gets values for agn tag with certain name for given company (including default value)
     *
     * @param name
     *          The name of agn tag
     * @param companyID
     *          The id of the company
     * @return string array or null
     */
	//public String[] getTag(String name, @VelocityCheck int companyID);

    /**
     *  Could not be used in fact because the column auto_url does not exist in mailing_tbl
     *
     */
	String getAutoURL(int mailingID);

    /**
     *  Could not be used in fact because the column rdir_domain does not exist in mailinglist_tbl
     *
     */
	String getMailingRdirDomain(int mailingID, @VelocityCheck int companyID);

	/**
	 * Returns all (non-deleted) mailings as a list of lightweight objects.
	 *
	 * @param companyID company ID
	 *
	 * @return list of lightweight mailing objects
	 */
	List<LightweightMailing> getLightweightMailings( @VelocityCheck int companyID);

	/**
	 * Returns all (non-deleted) mailings and templates by given ids as a list of lightweight objects.
	 *
	 * @param companyId company ID.
	 * @param mailingIds a collection of mailing ids.
	 *
	 * @return list of lightweight mailing objects
	 */
	List<LightweightMailing> getLightweightMailings(@VelocityCheck int companyId, Collection<Integer> mailingIds);

	/**
	 * Selects all non-deleted mailings of certain company and creates paginated list according to given criteria of sorting and pagination
	 *
	 * @param companyID an identifier of a current user's company.
	 * @param props filtering, sorting and pagination parameters for mailing list selection.
	 *
	 * @return PaginatedList of MailingBase
	 * @throws Exception
	 */
	PaginatedListImpl<Map<String, Object>> getMailingList(@VelocityCheck int companyID, int adminId, MailingsListProperties props);

	/**
     * Creates empty paginated list

     * @return PaginatedList of MailingBase
     */
    PaginatedListImpl<Map<String, Object>> getEmptyMailingList();

	/**
	 * Checks whether full-text indices for name and description of a mailing are available
	 *
	 * @return
	 */
	boolean isBasicFullTextSearchSupported();

	/**
	 * Checks whether full-text indices for mailing content are available
	 *
	 * @return
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
	int getStatusidForWorldMailing(int mailingID, @VelocityCheck int companyID);

	int getGenstatusForWorldMailing(int mailingID);

	/**
	 * Retrieve a genstatus value from the latest maildrop entry or -1 if there's no one.
	 *
	 * @param mailingID
	 *              Id of the mailing in database
	 * @param statuses
	 *              Filter for work_status column
	 * @return
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
	boolean hasPreviewRecipients(int mailingId, @VelocityCheck int companyID);

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
	boolean hasActions(int mailingId, @VelocityCheck int companyID);

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
     * @throws Exception
     */
	boolean checkMailingReferencesTemplate(int templateID, @VelocityCheck int companyID) throws Exception;

    /**
     * Checks the existence of mailing in the database
     *
     * @param mailingID
     *              Id of the mailing in database
     * @param companyID
     *              Id of the company that created a mailing
     * @return true - if the mailing exists, false - if does not
     */
	boolean exist(int mailingID, @VelocityCheck int companyID);

    /**
     * Checks the existence of template in the database
     *
     * @param mailingID
     * @param companyID
     * @param isTemplate
     * @return
     */
	boolean exist(int mailingID, @VelocityCheck int companyID, boolean isTemplate);

    /**
     * Gets names and descriptions of mailings listed by ids
     *
     * @param mailingIDList
     *                  String contains mailing ids separated with comma
     * @param allNames
     *                  HashTable for mailing names, is changing inside the method
     * @param allDesc
     *                  HashTable for mailing descriptions, is changing inside the method
     * @param companyID
     *                  Id of the company that created the mailings
     * @return  String object
     */
	String compareMailingsNameAndDesc(String mailingIDList, Map<Integer, String> allNames, Map<Integer, String> allDesc, @VelocityCheck int companyID);

    /**
     *  For each mailing from the given list loads number of openers and chose the biggest number
     *
     * @param mailingIDList
     *                  List of mailing ids
     * @param companyID
     *                  Id of the company that sent the mailings
     * @param allOpen
     *                  Mailing id with number of openers, is updating inside the method
     * @param biggestOpened
     *                  Max number of openers, is updating inside the method
     * @param aTarget
     *                  Target bean object with recipients target group data
     * @return max number of openers
     */
	int compareMailingsOpened(String mailingIDList, @VelocityCheck int companyID, Map<Integer, Integer> allOpen, int biggestOpened, ComTarget aTarget);

    /**
     * For each mailing from the given list loads number of clickers and chose the biggest number
     *
     * @param mailingIDList
     *                  List of mailing ids
     * @param allClicks
     *                  Mailing id with number of clickers, is updating inside the method
     * @param biggestClicks
     *                  Max number of clickers, is updating inside the method
     * @param companyID
     *                  Id of the company that sent the mailings
     * @param aTarget
     *                  Target bean object with recipients target group data
     * @return max number of clickers
     */
	int compareMailingsTotalClicks(String mailingIDList, Map<Integer, Integer> allClicks, int biggestClicks, @VelocityCheck int companyID, ComTarget aTarget);

    /**
     * For each mailing from the given list loads numbers of customer which did not open (opt-out) or skipped (bounce) the mailing, and chose the biggest numbers
     *
     * @param mailingIDList
     *                  List of mailing ids
     * @param allOptout
     *                  Mailing id with number of non-openers, is updating inside the method
     * @param allBounce
     *                  Mailing id with number of bounces, is updating inside the method
     * @param biggestOptout
     *                  Max number of non-openers, is updating inside the method
     * @param biggestBounce
     *                   Max number of bounces, is updating inside the method
     * @param companyID
     *                  Id of the company that sent the mailings
     * @param aTarget
     *                  Target bean object with recipients target group data
     * @return Map with biggest bounce and biggest optout values
     */
	Map<String, Integer> compareMailingsOptoutAndBounce(String mailingIDList, Map<Integer, Integer> allOptout, Map<Integer, Integer> allBounce, int biggestOptout, int biggestBounce, @VelocityCheck int companyID, ComTarget aTarget);

    /**
     * Loads list of non-deleted mailing have been sent by certain company
     *
     * @param companyID
     *                  Id of the company that sent the mailings
     * @param adminId
	 * @return  List of MailingBase bean objects
     */
	List<MailingBase> getMailingsForComparation(@VelocityCheck int companyID, int adminId);

	List<MailingBase> getMailingsForComparation(@VelocityCheck int companyID, int adminId, int targetId);

    /**
     * Loads list of templates of certain company
     *
     * @param companyID
     *               Id of the company
     * @return List of Mailing bean objects
     */
	List<Mailing> getTemplates( @VelocityCheck int companyID, int targetId);

    /**
     * Loads list of non-deleted templates of certain company
     *
     * @param companyID
     *               Id of the company
     * @return List of MailingBase bean objects
     */
	List<MailingBase> getTemplateMailingsByCompanyID( @VelocityCheck int companyID);

    /**
     * Gets mailing by given id
     *
     * @param templateID
     *              Id of the mailing in database
     * @param companyID
     *              Id of the company that created a mailing
     * @return  MailingBase bean object or null
     */
	MailingBase getMailingForTemplateID(int templateID, @VelocityCheck int companyID);

    /**
     * Loads list of action-based mailings have been sent by certain company
     *
     * @param companyID
     *              Id of the company that sent the mailings
     * @return  List of MailingBase bean objects
     */
	List<MailingBase> getMailingsByStatusE( @VelocityCheck int companyID);

    /**
     * Loads list of non-deleted mailings/templates of certain company
     * @param companyId
     *             Id of the company
     * @param isTemplate
     *              true - load templates, false - load mailings
     * @return List of Mailing objects
     */
	List<Mailing> getMailings( @VelocityCheck int companyId, boolean isTemplate);

    /**
     * Gets id of open action for the mailing
     *
     * @param mailingID
     *              Id of the mailing
     * @param companyID
     *              Id  of the company
     * @return positive integer or zero
     */
	int getMailingOpenAction(int mailingID, @VelocityCheck int companyID);

    /**
     * Gets id of click action for the mailing
     *
     * @param mailingID
     *              Id of the mailing
     * @param companyID
     *              Id  of the company
     * @return positive integer or zero
     */
	int getMailingClickAction(int mailingID, @VelocityCheck int companyID);

    /**
     * Gets parameter string for mailing of email type
     * @param mailingID
     *              Id of the mailing
     * @return String object or null
     */
	String getEmailParameter(int mailingID);

	String getSQLExpression(String targetExpression);

	List<LightweightMailing> getLightweightIntervalMailings(int companyId);

	List<LightweightMailing> getLightweightIntervalMailings(int companyID, int targetId);

    /**
     * returns the mailing-Type for the given mailing.
     * eg. 3 means a Follow-Up mailing.
     * @param mailingID
     * @return
     */
    int getMailingType(int mailingID);

	Date getMailingPlanDate(int mailingId, @VelocityCheck int companyId);

    boolean isOracleDB();

	List<MailingBase> getMailingTemplatesWithPreview(@VelocityCheck int companyId, int targetId, String sort, String direction);
}
