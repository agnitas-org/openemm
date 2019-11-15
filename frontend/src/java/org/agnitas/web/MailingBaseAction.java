/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.service.MailingsQueryWorker;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CharacterEncodingValidator;
import org.agnitas.util.HtmlUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.forms.MailingBaseForm;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.forms.ComMailingBaseForm;

/**
 * Implementation of <strong>Action</strong> that handles Mailings
 */
public class MailingBaseAction extends StrutsActionBase {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingBaseAction.class);

	public static final String FUTURE_TASK = "GET_MAILING_LIST";

    public static final int ACTION_SELECT_TEMPLATE = ACTION_LAST+1;
    public static final int ACTION_REMOVE_TARGET = ACTION_LAST+2;
    public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST+3;
    public static final int ACTION_CLONE_AS_MAILING = ACTION_LAST+4;
    public static final int ACTION_USED_ACTIONS = ACTION_LAST + 5;
    public static final int ACTION_VIEW_TABLE_ONLY = ACTION_LAST +6;
    public static final int ACTION_MAILING_BASE_LAST = ACTION_LAST+6;

    protected ComMailinglistService mailinglistService;
    protected ComMailingDao mailingDao;
    protected Map<String, Future<PaginatedListImpl<Map<String, Object>>>> futureHolder;

    /** DAO accessing target groups. */
    protected ComTargetService targetService;
    protected TAGCheckFactory tagCheckFactory;
    protected ExecutorService workerExecutorService;
    protected ComCampaignDao campaignDao;
    protected CharacterEncodingValidator characterEncodingValidator;
    protected MailingFactory mailingFactory;
	protected ConfigService configService;
    protected WebStorage webStorage;
    private JavaMailService javaMailService;
	
    protected MailinglistApprovalService mailinglistApprovalService;

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
	 * ACTION_LIST: Initializes columns width list for the mailings-list-table, forwards to "list"
	 * <br><br>
	 * ACTION_SAVE: validates character encoding in mailing subject, mailing components and content blocks;
     *     saves mailing in database;
     *     if the current mailing is template - updates all mailings using this template with dynamic-template
     *     property set and saves these mailings to database;
     *     loads saved mailing into form;
     *     if the mailing was cloned and the original mailing has data, clones data for the saved mailing;
     *     forwards to "view".
	 * <br><br>
     * ACTION_VIEW: loads mailing data from database into a form, forwards to "view". Also resets showTemplate property
     *     of form which indicates if new need to show the template-section of a mailing.
     * <br><br>
     * ACTION_NEW: checks if there is at least one mailinglist in database: shows error message if no mailing list was found,
     *     if the mailinglist exists - clears the form and forwards to "view".
     * <br><br>
     * ACTION_REMOVE_TARGET: removes given target group from the list of chosen target groups; forwards to "view".
     * <br><br>
     * ACTION_SELECT_TEMPLATE: loads the settings of chosen template into current form (mailing type, mailinglist,
     *     target-groups etc.); doesn't save mailing to database; forwards to "view".
     * <br><br>
     * ACTION_CLONE_AS_MAILING: clears the form; sets the properties of original mailing to form (original mailing
     *     is mailing used as source for cloning); names the new mailing as "Copy of " + name of original; stores
     *     the id of original mailing in form as templateID; forwards to mailing view page.
     * <br><br>
     * ACTION_USED_ACTIONS: loads map of emm actions used by current mailing into form; forwards to mailing actions
     *     page (forward is "action").
     * <br><br>
     * ACTION_VIEW_WITHOUT_LOAD: just forwards to "view" without reloading form data (is used after failing form
     *     validation).
     * <br><br>
	 * ACTION_CONFIRM_DELETE: loads mailing into form; forwards to jsp with question to confirm deletion (forward
     *     is "delete").
	 * <br><br>
	 * ACITON_DELETE: marks the mailing as deleted in database; forwards to "list".
	 * <br><br>
	 * Any other ACTION_* would cause a forward to "list"
     * <br><br>
     * If destination is "list" - calls a FutureHolder to get the list of mailings/templates according to selected
     * mailing types and isTemplate form-property (indicates if we we work with templates or mailings). <br>
     * If the Future object is not ready, increases the page refresh time by 50ms until it reaches 1 second.
     * (The page refresh time - is the wait-time before calling the action again while the FutureHolder is
     * running; the initial value is 250ms). While the FutureHolder is running - destination is "loading".<br>
     * When the FutureHolder is finished - the list of mailings/templates is set to request, the destination is "list".
     * <br><br>
     * If destination is "view": updates oldMailFormat property with the current value;<br>
     * loads list of templates and template name to form;<br>
     * loads mailinglists, campaigns, target groups and selected target groups into form.
     * <br><br>
     * If destination is null and there are errors found - forwards to "list"
     * @param form  ActionForm object
     * @param req   request
     * @param res   response
     * @param mapping The ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest req,
            HttpServletResponse res)
            throws IOException, ServletException {

        // Validate the request parameters specified by the user
        MailingBaseForm aForm = null;
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();
    	ActionForward destination = null;

        aForm = (MailingBaseForm)form;

        if (logger.isInfoEnabled()) {
        	logger.info("execute: action "+aForm.getAction());
        }

        boolean hasAnyPermission = true;

        if (aForm.isIsTemplate()) {
            if (!AgnUtils.allowed(req, Permission.TEMPLATE_SHOW)) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
                saveErrors(req, errors);
                hasAnyPermission = false;
                //return null;
            }
        } else {
            if (!AgnUtils.allowed(req, Permission.MAILING_SHOW)) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
                saveErrors(req, errors);
                hasAnyPermission = false;
                //return null;
            }
        }

        req.setAttribute("hasPermission", hasAnyPermission);

        if (hasAnyPermission) {
            try {
                switch(aForm.getAction()) {
                    case MailingBaseAction.ACTION_LIST:
                        if (aForm.getColumnwidthsList() == null) {
                            aForm.setColumnwidthsList(getInitializedColumnWidthList(5));
                        }
                        destination=mapping.findForward("list");
                        break;

                    case MailingBaseAction.ACTION_REMOVE_TARGET:
                        removeTarget(aForm, req);
                        aForm.setAction(MailingBaseAction.ACTION_SAVE);
                        destination=mapping.findForward("view");
                        break;

                    case MailingBaseAction.ACTION_DELETE:
                        aForm.setAction(MailingBaseAction.ACTION_LIST);
                        processDependantMailings(aForm.getMailingID(), req);
                        deleteMailing(aForm, req);
                        destination=mapping.findForward("list");
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                        aForm.setMessages(messages);
                        break;

                    case MailingBaseAction.ACTION_USED_ACTIONS:
                        loadActions(aForm, req);
                        destination = mapping.findForward("action");
                        break;

                    default:
                        aForm.setAction(MailingBaseAction.ACTION_LIST);
                        destination=mapping.findForward("list");
                }

            } catch (Exception e) {
                logger.error("execute: "+e, e);
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            }

            if (destination != null && "list".equals(destination.getName())) {
                try {
                    req.setAttribute("fieldsMap",  MailingAdditionalColumn.values());

                    destination = mapping.findForward("loading");
                    String key =  FUTURE_TASK+"@"+ req.getSession(false).getId();

                    if( !futureHolder.containsKey(key) ) {
                        Future<PaginatedListImpl<Map<String, Object>>> mailingListFuture = getMailingListFuture(errors, req, aForm.isIsTemplate(), aForm);
                        futureHolder.put(key, mailingListFuture);
                    }

                    //if we perform AJAX request (load next/previous page) we have to wait for preparing data
                    if (HttpUtils.isAjax(req)) {
                        while (!futureHolder.containsKey(key) || !futureHolder.get(key).isDone()) {
                            if (aForm.getRefreshMillis() < 1000) { // raise the refresh time
                                aForm.setRefreshMillis( aForm.getRefreshMillis() + 50 );
                            }
                            Thread.sleep(aForm.getRefreshMillis());
                        }
                    }

                    if (futureHolder.containsKey(key) && futureHolder.get(key).isDone()) {
                        // Method Future.get() could throw an exception so at first we have to remove one from a holder
                        req.setAttribute("mailinglist", futureHolder.remove(key).get());
                        destination = mapping.findForward("list");
                        aForm.setRefreshMillis(RecipientForm.DEFAULT_REFRESH_MILLIS);
                        saveMessages(req, aForm.getMessages());
                        saveErrors(req, aForm.getErrors());
                        aForm.setMessages(null);
                        aForm.setErrors(null);
                    }
                    else {
                        if( aForm.getRefreshMillis() < 1000 ) { // raise the refresh time
                            aForm.setRefreshMillis( aForm.getRefreshMillis() + 50 );
                        }
                        aForm.setError(false);
                    }
                } catch (Exception e) {
                    logger.error("getMailingList: "+e, e);
                    String exceptionMessage = configService.getValue(ConfigValue.SupportEmergencyUrl);
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.exception", exceptionMessage));
                    aForm.setError(true); // do not refresh when an error has been occurred
                }
            }

            checkShowDynamicTemplateCheckbox(aForm, req);

            if (destination != null && "view".equals(destination.getName())) {
                if (aForm.getMediaEmail() != null) {
                    aForm.setOldMailFormat(aForm.getMediaEmail().getMailFormat());
                }
                aForm.setTemplateMailingBases(mailingDao.getTemplateMailingsByCompanyID(AgnUtils.getCompanyID(req)));
                if(aForm.getTemplateID() != 0) {
                   MailingBase mb = mailingDao.getMailingForTemplateID(aForm.getTemplateID(),AgnUtils.getCompanyID(req));
                   aForm.setTemplateShortname(mb.getShortname().compareTo("") != 0 ? mb.getShortname() : SafeString.getLocaleString("mailing.No_Template", (Locale) req.getSession().getAttribute(Globals.LOCALE_KEY)));
                }
                else {
                	aForm.setTemplateShortname(SafeString.getLocaleString("mailing.No_Template", (Locale) req.getSession().getAttribute(Globals.LOCALE_KEY)));
                }

                prepareMailinglists(aForm, AgnUtils.getAdmin(req));
                aForm.setCampaigns(campaignDao.getCampaignList(AgnUtils.getCompanyID(req),"lower(shortname)",1));
                aForm.setTargetGroupsList(targetService.getTargetLights(AgnUtils.getCompanyID(req), aForm.getTargetGroups(), true));
                aForm.setTargets(targetService.getTargetLights(AgnUtils.getCompanyID(req)));
            }
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
            if(destination == null) {
                destination=mapping.findForward("list");
            }
        }

        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
        	saveMessages(req, messages);
        }

        if (destination != null && "list".equals(destination.getName()) && !aForm.isIsTemplate()) {
            aForm.setSearchEnabled(mailingDao.isBasicFullTextSearchSupported());
            aForm.setContentSearchEnabled(mailingDao.isContentFullTextSearchSupported());
        } else {
            aForm.setSearchEnabled(false);
            aForm.setContentSearchEnabled(false);
        }

        return destination;
    }

    /**
     * Unschedule the followup mailings recursively. Delete the followup parameter for the first level followup dependency.
     *
     * This is a stub method which is implemented in ComMailingBaseAction where it makes sense.
     *
     * @param mailingID - target mailing id
     * @param req
     */
    protected void processDependantMailings(int mailingID, HttpServletRequest req) {
		// nothing to do
    }

	protected void resetShowTemplate(HttpServletRequest req, MailingBaseForm aForm) {
		String showTemplate = req.getParameter("showTemplate");
		if(showTemplate == null || !showTemplate.equals("true")) {
			aForm.setShowTemplate(false);
		}
	}

    /**
     * Loads mailing data from db.
     *
     * @param aForm  MailingBaseForm object
     * @param req request
     * @throws Exception
     */
    protected void loadMailing(MailingBaseForm aForm, HttpServletRequest req) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes target group from the list of chosen targets.
     *
     * @param aForm  MailingBaseForm object
     * @param req request
     * @throws Exception
     */
    protected void removeTarget(MailingBaseForm aForm, HttpServletRequest req) throws Exception {
		Collection<Integer> allTargets = aForm.getTargetGroups();
		Integer tmpInt = null;

		if (allTargets != null) {
			Iterator<Integer> aIt = allTargets.iterator();
			while (aIt.hasNext()) {
				tmpInt = aIt.next();
				if (aForm.getTargetID() == tmpInt.intValue()) {
					allTargets.remove(tmpInt);
					break;
				}
			}
		}

		if (allTargets == null || allTargets.isEmpty()) {
			aForm.setTargetGroups(null);
		}
    }

    /**
     * Create list of fields changed in mailing
     * @param aForm - MailingBaseForm object from user side
     * @param aMailing - not yet changed mailing loaded from database
     * @return - list of string description of changes scheduled
     */
    protected List<String> getEditActionStrings(MailingBaseForm aForm, Mailing aMailing) {
        final String editKeyword = "edit ";
        StringBuilder actionMessage = new StringBuilder(editKeyword);
        List<String> actions = new LinkedList<>();

        Collection<Integer> mailingTargetGroups = aMailing.getTargetGroups();
        if ((mailingTargetGroups == null && aForm.getTargetGroups() != null) ||
                (mailingTargetGroups != null && !mailingTargetGroups.equals(aForm.getTargetGroups()))) {
            Set<Integer> oldGroups = new HashSet<>();
            if (mailingTargetGroups != null) {
                oldGroups.addAll(mailingTargetGroups);
            }
            Set<Integer> newGroups = new HashSet<>();
            Set<Integer> groupsUpdate = new HashSet<>();
            if(aForm.getTargetGroups() != null){
                groupsUpdate.addAll(aForm.getTargetGroups());
                newGroups.addAll(groupsUpdate);
            }
            newGroups.removeAll(oldGroups);
            oldGroups.removeAll(groupsUpdate);
            if(oldGroups.size() != 0){
                actionMessage.append("removed ");
                for (Integer next : oldGroups) {
                    actionMessage.append(next).append(", ");
                }
            }
            if(newGroups.size() != 0){
                actionMessage.append("added ");
                for (Integer next : newGroups) {
                    actionMessage.append(next).append(", ");
                }
            }
            if(actionMessage.length() != editKeyword.length()){
                actionMessage.delete(actionMessage.length()-2,actionMessage.length()); //remove last two characters: comma and space
                actionMessage.insert(editKeyword.length(),"target groups ");
                actions.add(actionMessage.toString());
                actionMessage.delete(editKeyword.length(), actionMessage.length());
            }
        }

        if(aMailing.getMailingType() != aForm.getMailingType()){
            actionMessage.append("mailing type from ")
                    .append(mailingTypeToString(aMailing.getMailingType()))
                    .append(" to ")
                    .append(mailingTypeToString(aForm.getMailingType()));
            actions.add(actionMessage.toString());
            actionMessage.delete(editKeyword.length(), actionMessage.length());
        }

        if(aMailing.getMailinglistID() != aForm.getMailinglistID()){
            actionMessage.append("mailing list changed from ").append(aMailing.getMailinglistID()).append(" to ").append(aForm.getMailinglistID());
            if (aForm.getMailinglistID() == 0) {
            	//send mail
            	String message = "Mailinglist ID in mailing template (" + aForm.getMailingID() + ") was set to 0.  Please check if the content still exists!";
            	javaMailService.sendEmail(configService.getValue(ConfigValue.Mailaddress_Error), "Mailinglist set to 0", message, HtmlUtils.replaceLineFeedsForHTML(message));
            }

            actions.add(actionMessage.toString());
            actionMessage.delete(editKeyword.length(), actionMessage.length());
        }

        MediatypeEmail aMailingParam = aMailing.getEmailParam();
        collectParametersChanges("Short name", aMailing.getShortname(), aForm.getShortname(), actionMessage, actions, editKeyword);
        collectParametersChanges("Description", StringUtils.trimToEmpty(aMailing.getDescription()), StringUtils.trimToEmpty(aForm.getDescription()), actionMessage, actions, editKeyword);
        MailingComponent textComponent = aMailing.getTextTemplate();
        if (textComponent != null) {
        	String aMailingTextBlockValue = textComponent.getEmmBlock();
	        String aFormTextBlockValue = aForm.getTextTemplate();
	        if (!(StringUtils.equalsIgnoreCase(aMailingTextBlockValue, aFormTextBlockValue))) {
	            actionMessage.append("mailing Frame content - Text block was changed");
	            actions.add(actionMessage.toString());
	            actionMessage.delete(editKeyword.length(), actionMessage.length());
	        }
        }
        MailingComponent htmlComponent = aMailing.getHtmlTemplate();
        if (htmlComponent != null) {
	        String aMailingHtmlBlockValue = htmlComponent.getEmmBlock();
	        String aFormHtmlBlockValue = aForm.getHtmlTemplate();
	        if (!(StringUtils.equalsIgnoreCase(aMailingHtmlBlockValue, aFormHtmlBlockValue))) {
	            actionMessage.append("mailing Frame content - Html block was changed");
	            actions.add(actionMessage.toString());
	            actionMessage.delete(editKeyword.length(), actionMessage.length());
	        }
        }
        
        collectParametersChanges("Subject", aMailingParam.getSubject(), aForm.getEmailSubject(), actionMessage, actions, editKeyword);
        collectParametersChanges("Format", getMailFormatName(aMailingParam.getMailFormat()), getMailFormatName(aForm.getMediaEmail().getMailFormat()), actionMessage, actions, editKeyword);
        collectParametersChanges("Sender e-mail", aMailingParam.getFromEmail(), aForm.getMediaEmail().getFromEmail(), actionMessage, actions, editKeyword);
        collectParametersChanges("Sender full name", StringUtils.trimToEmpty(aMailingParam.getFromFullname()), aForm.getSenderFullname(), actionMessage, actions, editKeyword);
        collectParametersChanges("Reply-to e-mail", aMailingParam.getReplyEmail(), aForm.getMediaEmail().getReplyEmail(), actionMessage, actions, editKeyword);
        collectParametersChanges("Reply-to full name",  StringUtils.trimToEmpty(aMailingParam.getReplyFullname()), aForm.getMediaEmail().getReplyFullname(), actionMessage, actions, editKeyword);
        collectParametersChanges("Character set",aMailingParam.getCharset(), aForm.getEmailCharset(), actionMessage, actions, editKeyword);
        collectParametersChanges("Line break after", aMailingParam.getLinefeed(), aForm.getEmailLinefeed(), actionMessage, actions, editKeyword);
        collectParametersChanges("Measure opening rate", aMailingParam.getOnepixel(), aForm.getEmailOnepixel(), actionMessage, actions, editKeyword);
        collectParametersChanges("General campaign", aMailing.getCampaignID(), aForm.getCampaignID(), actionMessage, actions, editKeyword);
        collectParametersChanges("Archive", aMailing.getArchived() != 0, aForm.isArchived(), actionMessage, actions, editKeyword);
        return actions;
    }

    private void collectParametersChanges(String valueName, Object oldValue, Object newValue, StringBuilder actionMessage, List<String> actions, String editKeyword) {
        if (!ObjectUtils.equals(oldValue, newValue)) {
            actionMessage.append("mailing ")
                    .append(valueName)
                    .append(" from ")
                    .append(oldValue)
                    .append(" to ")
                    .append(newValue);
            actions.add(actionMessage.toString());
            actionMessage.delete(editKeyword.length(), actionMessage.length());
        }
    }

    /**
     * Converts mailing type integer constant to human readable string representation.
     * @param mailingType - mailing type integer constant
     * @return - mailing type string representation
     */
    protected String mailingTypeToString(int mailingType) {
        return MailingModel.getMailingType(mailingType).getName();
    }

    /**
     * Construct mailing description acceptable for user log
     * @param aForm MailingBaseForm data
     * @return mailing description including name and ID.
     */
    protected String getMailingDescription(MailingBaseForm aForm) {
        return aForm.getShortname() + " (" + aForm.getMailingID() + ")";
    }

    /**
     * Marks mailing as deleted and updated mailing data in database
     *
     * @param aForm MailingBaseForm object
     * @param req  request
     * @throws Exception
     */
    protected void deleteMailing(MailingBaseForm aForm, HttpServletRequest req) throws Exception {
        mailingDao.deleteMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));
        writeUserActivityLog(AgnUtils.getAdmin(req), "delete " + (aForm.isIsTemplate() ? "template" : "mailing"), getMailingDescription(aForm));
    }

    /**
     * Loads list of emm actions into form
     *
     * @param aForm MailingBaseForm object
     * @param req request
     * @throws Exception
     */
    protected void loadActions(MailingBaseForm aForm, HttpServletRequest req) throws Exception {
        List<Map<String, String>> map = mailingDao.loadAction(aForm.getMailingID(), AgnUtils.getCompanyID(req));
    	aForm.setActions(map);
    }

    /**
     * Creates paginated list by given sorting parameters and filter conditions (templates or mailings, mailings of certain types).
     *
     * @param req request
     * @param isTemplate true -> templates, false -> mailings
     * @param form MailingBaseForm object
     * @return Future object
     * @throws Exception
     */
    public Future<PaginatedListImpl<Map<String, Object>>> getMailingListFuture(ActionMessages errors, HttpServletRequest req, boolean isTemplate, MailingBaseForm form) throws Exception {
    	String sort = getSort(req, form);
     	String direction = form.getDir();

        prepareListParameters(form);

     	if (form.isNumberOfRowsChanged()) {
            form.setPageNumber(1);
            form.setNumberOfRowsChanged(false);
     	} else {
            String pageStr = req.getParameter("page");
            if (StringUtils.isNotBlank(pageStr)) {
                form.setPage(pageStr);
            } else if (StringUtils.isBlank(form.getPage())) {
                form.setPageNumber(1);
            }
        }

     	int page = form.getPageNumber();
     	int rownums = form.getNumberOfRows();

        MailingsQueryWorker mailingsQueryWorker = createMailingsQueryWorker(errors, form, req, AgnUtils.getCompanyID(req), form.getTypes(), isTemplate, sort, direction, page, rownums, true);
        return workerExecutorService.submit(mailingsQueryWorker);
    }

    protected void prepareListParameters(MailingBaseForm form) {
        webStorage.access(WebStorage.MAILING_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0) {
                storage.setRowsCount(form.getNumberOfRows());

                if (!form.isIsTemplate()) {
                    storage.setMailingTypeNormal(form.getMailingTypeNormal());
                    storage.setMailingTypeDate(form.getMailingTypeDate());
                    storage.setMailingTypeEvent(form.getMailingTypeEvent());
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());

                if (!form.isIsTemplate()) {
                    form.setMailingTypeNormal(storage.isMailingTypeNormal());
                    form.setMailingTypeDate(storage.isMailingTypeDate());
                    form.setMailingTypeEvent(storage.isMailingTypeEvent());
                }
            }
        });
    }

    protected MailingsQueryWorker createMailingsQueryWorker(ActionMessages errors, MailingBaseForm mailingBaseForm, HttpServletRequest req, int companyId, String types, boolean isTemplate, String sort, String direction, int page, int rownums, final boolean includeTargetGroups) throws Exception {
        MailingsListProperties props = new MailingsListProperties();
        props.setTypes(types);
        props.setTemplate(isTemplate);
        props.setSort(sort);
        props.setDirection(direction);
        props.setPage(page);
        props.setRownums(rownums);
        props.setIncludeTargetGroups(includeTargetGroups);
        props.setAdditionalColumns(getAdditionalColumns(mailingBaseForm));

        return new MailingsQueryWorker(mailingDao, companyId, props);
    }

    protected Set<String> getAdditionalColumns(MailingBaseForm form) {
        String[] columns = ((ComMailingBaseForm) form).getSelectedFields();
        if (columns != null) {
            return new HashSet<>(Arrays.asList(columns));
        }
        return Collections.emptySet();
    }

	protected final String getSort(final HttpServletRequest request, final MailingBaseForm aForm, final boolean sortForTemplates) {
		String sort = request.getParameter("sort");
		
		if(sortForTemplates) {
			 if( sort == null ) {
				 sort = aForm.getTemplateSort();
			 } else {
				 aForm.setTemplateSort(sort);
			 }
		} else {
			if( sort == null ) {
				sort = aForm.getSort();
			} else {
				aForm.setSort(sort);
			}
		}
		 
		return sort;
	}

    /**
     * Creates report about errors in dynamic tags.
     *
     * @param blockName name of content block with invalid content
     * @param errorReports  list of messages about parsing errors (is changing inside the method)
     * @param templateReport content with errors
     */
	protected void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
		Map<String,String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
		for(Entry<String,String> entry:tagsWithErrors.entrySet()) {
			String[] errorRow = new String[3];
			errorRow[0] = blockName; // block
			errorRow[1] =  entry.getKey(); // tag
			errorRow[2] =  entry.getValue(); // value

			errorReports.add(errorRow);
		}
		List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);
		for(String error:errorsWithoutATag){
			String[] errorRow = new String[3];
			errorRow[0] = blockName;
			errorRow[1] = "";
			errorRow[2] = error;
			errorReports.add(errorRow);
		}
	}

    /**
     * Sets available mailinglists only for the mailing view (not for list of mailings). <br>
     * If the admin has permission to see a mailinglist of the mailing
     * or mailing is copied
     * or mailing is created as followup
     * or there is no chosen mailinglist for the mailing
     * than sets all available mailinglists for the admin in the form. <br>
     * If the admin has no permission to see a mailinglist of mailing
     * than marks mailinglists as not editable.
     * So user will be able to only see the disabled mailinglist, but not to change it. <br>
     * @param admin current admin
     */
	protected void prepareMailinglists(MailingBaseForm form, ComAdmin admin){
        List<Mailinglist> enabledMailinglists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin);
        if(form.getMailinglistID() > 0
                && !form.isCopiedMailing()
                && !form.isCreatedAsFollowUp()
                && enabledMailinglists.stream()
                .noneMatch(mailinglist -> mailinglist.getId() == form.getMailinglistID())){
            form.setMailingLists(Collections.singletonList(mailinglistService.getMailinglist(form.getMailinglistID(), admin.getCompanyID())));
            form.setCanChangeMailinglist(false);
        } else {
            form.setMailingLists(enabledMailinglists);
            form.setCanChangeMailinglist(true);
        }
    }

    public MailingDao getMailingDao() {
        return mailingDao;
    }

    public ComCampaignDao getCampaignDao() {
        return campaignDao;
    }

    public CharacterEncodingValidator getCharacterEncodingValidator() {
        return characterEncodingValidator;
    }

    public TAGCheckFactory getTagCheckFactory() {
        return tagCheckFactory;
    }

    public MailingFactory getMailingFactory() {
        return mailingFactory;
    }

	protected void checkShowDynamicTemplateCheckbox( MailingBaseForm mailingBaseForm, HttpServletRequest request) {
		boolean showCheckbox = false;

		if (mailingBaseForm.isIsTemplate()) {
			// For templates checkbox is always show and enabled
			showCheckbox = true;
		} else if (mailingBaseForm.getTemplateID() != 0) {
			// For mailings, checkbox is always shows if and only if referenced mailing-record defines template
			// Checkbox is only enabled, if such a mailing has ID 0 (new mailing)

			showCheckbox = mailingDao.checkMailingReferencesTemplate( mailingBaseForm.getTemplateID(), AgnUtils.getCompanyID( request));
		} else {
			// in all other cases, the checkbox is hidden
			showCheckbox = false;
		}

		request.setAttribute("show_dynamic_template_checkbox", showCheckbox);
	}

    protected WebApplicationContext getApplicationContext(HttpServletRequest req){
        return WebApplicationContextUtils.getRequiredWebApplicationContext(req.getSession().getServletContext());
    }

    /**
     * User allowed to change general settings of mailing if mailing is template or user has no {@link Permission#MAILING_SETTINGS_HIDE}
     */
    protected void setCanUserChangeGeneralSettings(MailingBaseForm form, HttpServletRequest req){
        form.setCanChangeEmailSettings(form.isIsTemplate() || !AgnUtils.allowed(req, Permission.MAILING_SETTINGS_HIDE));
    }

    private String getMailFormatName(int id){

        switch (id){
            case 0:
                return "only Text";
            case 1:
                return "Text and HTML";
            case 2:
                return "Text, HTML and Offline-HTML";
            default:
                return "Unknown mail format";
        }
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setFutureHolder(Map<String, Future<PaginatedListImpl<Map<String, Object>>>> futureHolder) {
        this.futureHolder = futureHolder;
    }

    public void setCampaignDao(ComCampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    @Required
    public void setMailinglistService(ComMailinglistService mailinglistService) {
        this.mailinglistService = mailinglistService;
    }

    public void setCharacterEncodingValidator(CharacterEncodingValidator characterEncodingValidator) {
        this.characterEncodingValidator = characterEncodingValidator;
    }

    public void setTagCheckFactory(TAGCheckFactory tagCheckFactory) {
        this.tagCheckFactory = tagCheckFactory;
    }

    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @Required
    public void setMailingFactory(MailingFactory mailingFactory) {
        this.mailingFactory = mailingFactory;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }
    
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

    @Required
    public void setJavaMailService(JavaMailService javaMailService) {
        this.javaMailService = javaMailService;
    }
    
    @Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}
}
