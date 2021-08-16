/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.linkcheck.beans.LinkReachability;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.ModeType;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.service.LinkcheckService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SafeString;
import org.agnitas.util.Tuple;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.commons.TranslatableMessageException;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingPriorityService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.ClassicTemplateGenerator;
import com.agnitas.web.PreviewForm;

import net.sf.json.JSONObject;

/**
 * Implementation of <strong>Action</strong> that validates a user logon.
 */
public class MailingSendAction extends StrutsActionBase implements ApplicationContextAware {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingSendAction.class);

	public static final int ACTION_VIEW_SEND = ACTION_LAST + 1;
	public static final int ACTION_SEND_ADMIN = ACTION_LAST + 2;
	public static final int ACTION_SEND_TEST = ACTION_LAST + 3;
	public static final int ACTION_SEND_WORLD = ACTION_LAST + 4;
	public static final int ACTION_VIEW_SEND2 = ACTION_LAST + 5;
	public static final int ACTION_VIEW_DELSTATBOX = ACTION_LAST + 6;
	public static final int ACTION_ACTIVATE_CAMPAIGN = ACTION_LAST + 7;
	public static final int ACTION_ACTIVATE_RULEBASED = ACTION_LAST + 8;
	public static final int ACTION_PREVIEW_SELECT = ACTION_LAST + 9;
	public static final int ACTION_PREVIEW = ACTION_LAST + 10;
	public static final int ACTION_PREVIEW_HEADER = ACTION_LAST + 13;
	public static final int ACTION_DEACTIVATE_MAILING = ACTION_LAST + 14;
	public static final int ACTION_CHANGE_SENDDATE = ACTION_LAST + 15;
	public static final int ACTION_CANCEL_MAILING_REQUEST = ACTION_LAST + 16;
	public static final int ACTION_CANCEL_MAILING = ACTION_LAST + 17;
	public static final int ACTION_CONFIRM_SEND_WORLD = ACTION_LAST + 18;
	public static final int ACTION_CHECK_LINKS = ACTION_LAST + 19;
	public static final int ACTION_PRIORITIZATION_SWITCHING = ACTION_LAST + 20;
	public static final int ACTION_RESUME_MAILING_REQUEST = ACTION_LAST + 21;
	public static final int ACTION_RESUME_MAILING = ACTION_LAST + 22;
	public static final int ACTION_RESUME_MAILING_BY_COPY_REQUEST = ACTION_LAST + 23;
	public static final int ACTION_RESUME_MAILING_BY_COPY = ACTION_LAST + 24;
	public static final int ACTION_SEND_LAST = ACTION_RESUME_MAILING_BY_COPY;
	public static final int ACTION_PREVIEW_IFRAME = ACTION_RESUME_MAILING_BY_COPY;

    public static final int PREVIEW_MODE_HEADER = 1;
    public static final int PREVIEW_MODE_TEXT = 2;
    public static final int PREVIEW_MODE_HTML = 3;
    public static final int PREVIEW_MODE_OFFLINE = 4;

    // tag errors
    public static final String TEMPLATE = "__TEMPLATE__";
	public static final String SUBJECT = "__SUBJECT__";
	public static final String FROM = "__FROM__";

	/** DAO accessing target groups. */
    protected ComTargetDao targetDao;
    protected ComMailingDao mailingDao;
    protected ComRecipientDao recipientDao;
    protected BlacklistService blacklistService;
    private MailingComponentDao mailingComponentDao;
    private LinkcheckService linkcheckService;
    protected MailingFactory mailingFactory;
    protected MailinglistDao mailinglistDao;
    private TAGCheckFactory tagCheckFactory;
    private DataSource dataSource;
    protected PreviewFactory previewFactory;
    protected ComTargetService targetService;
    protected MaildropService maildropService;
    protected ComCompanyDao companyDao;

    protected MaildropStatusDao maildropStatusDao;
    protected BounceFilterService bounceFilterService;
    
    /** Mailing service. */
    protected MailingService mailingService;
    private ComMailingDeliveryStatService deliveryStatService;
    protected MailingPriorityService mailingPriorityService;
    protected ComMailingBaseService mailingBaseService;
    protected ConfigService configService;
    protected MediatypeFactory mediatypeFactory;
    
    protected MailingStopService mailingStopService;
    private MailingPreviewService mailingPreviewService;
    
    protected ApplicationContext applicationContext;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
     * ACTION_VIEW_SEND: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     loads delivery statistic data from db into the form;<br>
     *     loads names of mailing target groups into a form;<br>
     *     calculates statistics-frame size according to number of target groups selected for mailing;<br>
     *     sets the flag for displaying of send test- or admin-mails buttons;<br>
     *     sets destination="send".
     * <br><br>
     * ACTION_VIEW_DELSTATBOX: loads delivery statistic data from db into the form;<br>
     *     loads names of mailing target groups into a form;<br>
     *     calculates statistics-frame size according to number of target groups selected for mailing;<br>
     *     sets the flag for displaying of send test- or admin-mails buttons;<br>
     *     sets destination="view_delstatbox".
     * <br><br>
     * ACTION_CANCEL_MAILING_REQUEST: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     forwards to jsp with question to cancel mailing generation.
     * <br><br>
     * ACTION_CANCEL_MAILING: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     if the request parameter "kill" is set - cancels mailing generation; if the mailing generation was
     *     canceled successfully - loads delivery statistic to form, loads mailing targets to request, calculates
     *     statistics-frame size according to number of target groups selected for mailing, sets the flag for
     *     displaying of send test- or admin-mails buttons;<br>
     *     forwards to mailing send page ("send");
     * <br><br>
     * ACTION_VIEW_SEND2: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     loads send statistic data from db into the form (number of sent html-mails, text-mails, offline-mails,
     *     total number of sent mails);<br>
     *     loads target group names into the form<br>
     *     sets destination="send2".
     * <br><br>
     * ACTION_SEND_ADMIN: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     creates maildrop-entry with type <code>MaildropStatus.ADMIN.getCode()</code> ('A');<br>
     *     performs mailing validation before sending: checks that there's at least 1 recipient, checks that subject and
     *     sender address are not empty, checks that html and text versions are not empty;<br>
     *     triggers mailing sending with a mailgun and a newly created maildrop-entry; mailiing will be sent only to
     *     admin-recipients.<br>
     *     loads delivery statistic data from db into the form;<br>
     *     loads names of mailing target groups into a form;<br>
     *     Sets destination="send".
     * <br><br>
     * ACTION_SEND_TEST: do exactly the same things as ACTION_SEND_ADMIN with the only difference that the type
     *     of created maidrop-entry is <code>MaildropStatus.TEST.getCode()</code> ('T'). Mailing will be sent to admin- and
     *     test-recipients
     * <br><br>
     * ACTION_DEACTIVATE_MAILING: removes maildrop status entries of mailing with status
     *     <code>MaildropStatus.ACTION_BASED.getCode()</code> and <code>MaildropStatus.DATE_BASED.getCode()</code> ('E' and 'R');
     *     In fact that means that the sending of actionbased/datebased mailing will be stopped<br>
     *     loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     sets destination="send".
     * <br><br>
     * ACTION_CONFIRM_SEND_WORLD: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     forwards to jsp with question to send the mailing.
     * <br><br>
     * ACTION_ACTIVATE_RULEBASED:
     * ACTION_ACTIVATE_CAMPAIGN:
     * ACTION_SEND_WORLD: creates maildrop-entry with appropriate type depending on action:
     *     <code>MaildropStatus.WORLD.getCode()</code>, <code>MaildropStatus.ACTION_BASED.getCode()</code> or
     *     <code>MaildropStatus.DATE_BASED.getCode()</code> ('W', 'E' or 'R');<br>
     *     performs mailing validation before sending: checks that there's at least 1 recipient, checks that subject and
     *     sender address are not empty, checks that html and text versions are not empty;<br>
     *     If the send should be performed now - regenerates mailing content from data, triggers
     *     mailing sending with a mailgun and a newly created maildrop-entry; mailiing will be sent to all active
     *     recipients (admin, test and world).<br>
     *     loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     loads delivery statistic data from db into the form;<br>
     *     loads names of mailing target groups into a form;<br>
     *     Sets destination="send".
     * <br><br>
     * ACTION_PREVIEW_SELECT: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     loads list of mailing admin and test recipients into the request;<br>
     *     checks if the mailing has at least one admin or test recipient and stores the check result in the form;
     *     if the check result is "true" and there wasn't preview-recipient selected by user - stores the
     *     smallest value of recipient id in the form as a preview recipient. (Preview recipient is a recipient
     *     the mailing preview will be generated for)<br>
     *     Sets destination="preview_select".
     * <br><br>
     * ACTION_PREVIEW_HEADER: performs tag-check for mailing subject and from-address, if the check is ok - generates
     *     backend preview. If the preview header is not null - takes subject and from-address from backend preview and
     *     sets that to form, in other case sets subject and from-address taken from emailParam of mailing into form.
     *     Sets mail format and mailinglist ID to form.<br>
     *     If preview header generation fails, loads dynamic tag error report into request and forwards to error
     *     report page;<br>
     *     in case of successful preview header generation, loads mailing attachments and personalized attachments into
     *     request and sets destination="preview_header".
     * <br><br>
     * ACTION_PREVIEW: Checks if preview customer ID is set and sets the result of check to form property<br>
     *     If the preview customer ID is set in form - generates mailing preview according to format selected by user
     *     and sets it to form; sets mail format and mailinglist ID to form. Forwards to preview page. If preview
     *     generation failed - stores error report to request and forwards to error report page<br>
     *     If the preview customer id is not set in form forwards to error report page
     * <br><br>
     * ACTION_CHECK_LINKS: loads mailing into the form;<br>
     *     loads list of target groups into request;<br>
     *     loads delivery statistic data from db into the form;<br>
     *     checks if the mailing links are available (the url-request ), if there's at least one link invalid - creates
     *     error message with the list of invalid links; if all links are ok - adds success message<br>
     *     Forwards to "send".
     * <br><br>
     * @param form ActionForm object
     * @param req request
     * @param res response
     * @param mapping the ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination specified in struts-config.xml to forward to next jsp
     */
	@Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest req,
            HttpServletResponse res)
            throws IOException, ServletException {

        // Validate the request parameters specified by the user
        MailingSendForm aForm=null;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;

        if (!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }

        aForm = (MailingSendForm) form;
        if (logger.isInfoEnabled()) {
            logger.info("Action: " + aForm.getAction());
        }

        try {
            switch(aForm.getAction()) {
                case ACTION_VIEW_SEND:
                    loadMailing(aForm, req);
                    // TODO Remove this quick-hack and replace it with some more sophisticated code

                    loadDeliveryStats(aForm, req);
                    
                    destination=mapping.findForward("send");
                    break;

                case ACTION_VIEW_DELSTATBOX:
                	loadMailing(aForm, req);
                    loadDeliveryStats(aForm, req);
                    AgnUtils.setAdminDateTimeFormatPatterns(req);
                    destination=mapping.findForward("view_delstatbox");
                    writeUserActivityLog(AgnUtils.getAdmin(req), "view delstatbox", "active tab - send mailing");
                    break;

                case ACTION_CANCEL_MAILING_REQUEST:
                    loadMailing(aForm, req);
                    checkForDependencies(req, aForm, messages);
                    aForm.setAction(MailingSendAction.ACTION_CANCEL_MAILING);
                    destination=mapping.findForward("cancel_generation_question");
                    break;

                case ACTION_CANCEL_MAILING:
                    loadMailing(aForm, req);
                    if(req.getParameter("kill")!=null) {
                        if(cancelMailingDelivery(aForm, req)) {
                            loadDeliveryStats(aForm, req);
                        }
                        destination=mapping.findForward("send");
                    }
                    break;
                    
                case ACTION_RESUME_MAILING_REQUEST:
                    loadMailing(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_RESUME_MAILING);
                    destination=mapping.findForward("resume_generation_question");
                	break;
                	
                case ACTION_RESUME_MAILING:
                    if(resumeMailingDelivery(aForm, req)) {
                        loadDeliveryStats(aForm, req);
                    }
                    destination=mapping.findForward("send");
                	break;
                	
                case ACTION_RESUME_MAILING_BY_COPY_REQUEST:
                    loadMailing(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_RESUME_MAILING_BY_COPY);
                    destination=mapping.findForward("resume_generation_by_copy_question");
                	break;
                	
               case ACTION_RESUME_MAILING_BY_COPY: {
            	   loadMailing(aForm, req);
            	   final int newMailingID =  copyMailingToResume(AgnUtils.getCompanyID(req), aForm);
            	   aForm.setMailingID(newMailingID);
            	   loadMailing(aForm, req);
            	   
                   if (aForm.getColumnwidthsList() == null) {
                       aForm.setColumnwidthsList(getInitializedColumnWidthList(5));
                   }
                   AgnUtils.setAdminDateTimeFormatPatterns(req);
            	   
                   destination=mapping.findForward("list");
            	   break;
               }

                case MailingSendAction.ACTION_VIEW_SEND2:
                    loadMailing(aForm, req);
                    loadSendStats(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_CONFIRM_SEND_WORLD);
                    Set<Integer> targetGroups = (Set<Integer>) aForm.getTargetGroups();
                    if(targetGroups == null){
                        targetGroups = new HashSet<>();
                    }
                    List<String> targetGroupNames = getTargetDao().getTargetNamesByIds(AgnUtils.getCompanyID(req), targetGroups);
                    req.setAttribute("targetGroupNames", targetGroupNames);
                    destination=mapping.findForward("send2");
                    break;

                case MailingSendAction.ACTION_SEND_ADMIN:
                case MailingSendAction.ACTION_SEND_TEST:
                    loadMailing(aForm, req);
                    sendMailing(aForm, req, messages);
                    loadDeliveryStats(aForm, req);

					extendedChecks(form, req, messages);
                    aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
                    destination=mapping.findForward("send");
                    break;

                case MailingSendAction.ACTION_DEACTIVATE_MAILING:
                    deactivateMailing(aForm, req);

                    loadMailing(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
                    destination=mapping.findForward("send");
                    break;

                case ACTION_CONFIRM_SEND_WORLD:
                    loadMailing(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_SEND_WORLD);
                    destination=mapping.findForward("send_confirm");
                    break;

                case MailingSendAction.ACTION_ACTIVATE_RULEBASED:
                case MailingSendAction.ACTION_ACTIVATE_CAMPAIGN:
                case MailingSendAction.ACTION_SEND_WORLD:

                	Mailing mailingToSend = loadMailing(aForm, req);

                    if(validateActivation(req, aForm, errors, messages)) {
                        try {
                        	if (isPostMailing(mailingToSend)) {
								TimeZone aZone = AgnUtils.getTimeZone(req);
								GregorianCalendar sendDate = new GregorianCalendar(aZone);
								sendDate.set(Integer.parseInt(aForm.getSendDate().substring(0, 4)), Integer.parseInt(aForm.getSendDate().substring(4, 6)) - 1, Integer.parseInt(aForm.getSendDate().substring(6, 8)), aForm.getSendHour(), aForm.getSendMinute());
								createPostTrigger(mailingToSend, mailingToSend.getCompanyID(), sendDate.getTime());
							} else {
								sendMailing(aForm, req, messages);
							}
                        } finally {
                            loadMailing(aForm, req);
                        }
                    }

                    loadDeliveryStats(aForm, req);
                    aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
                    destination=mapping.findForward("send");
                    break;
    
                case MailingSendAction.ACTION_PREVIEW_HEADER:
                    loadPreviewHeaderData(aForm, req);
                    req.setAttribute("components", mailingComponentDao.getPreviewHeaderComponents(aForm.getMailingID(), AgnUtils.getCompanyID(req)));
                    
					destination = mapping.findForward("preview_header");

                    break;

                case MailingSendAction.ACTION_PREVIEW:
                    if (aForm.getPreviewForm().getModeType() == ModeType.TARGET_GROUP) {
                        destination = getTargetBasedPreview(req, mapping, aForm, errors);
                    } else {
                        destination = getCustomerBasedPreview(req, mapping, aForm, errors);
                    }
                    break;

                case MailingSendAction.ACTION_CHECK_LINKS:
                    loadMailing(aForm, req);
                    loadDeliveryStats(aForm, req);

                    checkUrlsInMailing(aForm, req, messages, errors);
                    
                    if(errors.isEmpty()) {
                    	messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("link.check.success"));
                    }
                    aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
                    destination = mapping.findForward("send");
                    break;

                case MailingSendAction.ACTION_PRIORITIZATION_SWITCHING:
                    if (mailingPriorityService != null) {
                        HttpUtils.responseJson(res, savePrioritizationState(req));
                    }
                    return null;
                    
				default:
					break;
            }
        } catch (TranslatableMessageException e) {
			logger.error("execute: " + e, e);

			if ("error.mailing.send.admin.maxMails".equals(e.getErrorMsgKey())) {
				int maxAdminMails = configService.getIntegerValue(ConfigValue.MaxAdminMails, AgnUtils.getCompanyID(req));
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMsgKey(), maxAdminMails));
			} else {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMsgKey()));
			}
			destination = mapping.findForward("send");
		} catch (Exception e) {
			logger.error("execute: " + e, e);
			
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));

			destination = mapping.findForward("send");
		}

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
            if(aForm.getAction()==MailingSendAction.ACTION_SEND_ADMIN ||
                    aForm.getAction()==MailingSendAction.ACTION_SEND_TEST ||
                    aForm.getAction()==MailingSendAction.ACTION_SEND_WORLD) {
                return (new ActionForward(mapping.getInput()));
            }
        }

        if (!messages.isEmpty()){
            saveMessages(req, messages);
        }

        return destination;
    }

    private ActionForward getCustomerBasedPreview(HttpServletRequest req, ActionMapping mapping, MailingSendForm form, ActionMessages errors) {
	    PreviewForm previewForm = form.getPreviewForm();
	    if (previewForm.getCustomerID() > 0) {
            previewForm.setHasPreviewRecipient(true);
            return getPreview(form.getMailingID(), previewForm, mapping, req, errors);
        } else {
            previewForm.setHasPreviewRecipient(false);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.preview.no_recipient"));
        }
	    return mapping.findForward("preview_errors");
    }
    
    private ActionForward getTargetBasedPreview(HttpServletRequest req, ActionMapping mapping, MailingSendForm form, ActionMessages errors) {
	    PreviewForm previewForm = form.getPreviewForm();
        return getPreview(form.getMailingID(), previewForm, mapping, req, errors);
    }
    
    private JSONObject savePrioritizationState(HttpServletRequest request) {
        String isPrioritizationDisallowedParam = request.getParameter("isPrioritizationDisallowed");
        String mailingIdParam = request.getParameter("mailingId");
        boolean isUpdated = false;

        if (StringUtils.isNotBlank(isPrioritizationDisallowedParam) && StringUtils.isNotBlank(mailingIdParam)) {
            boolean isPrioritizationDisallowed = BooleanUtils.toBoolean(isPrioritizationDisallowedParam);
            int mailingId = NumberUtils.toInt(mailingIdParam);
            ComAdmin admin = AgnUtils.getAdmin(request);

            try {
                if (mailingPriorityService.setPrioritizationAllowed(admin, mailingId, !isPrioritizationDisallowed)) {
                    // creation of UAL entry
                    Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
                    String action = "switched mailing prioritization";
                    String description = String.format("prioritization: %s, mailing type: %s. %s(%d)",
                            isPrioritizationDisallowed ? "disallowed" : "allowed",
                            mailingTypeToString(mailing.getMailingType()),
                            mailing.getShortname(),
                            mailing.getId());

                    isUpdated = true;
                    writeUserActivityLog(admin, action, description);
                }
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
        }

        JSONObject response = new JSONObject();
        response.element("success", isUpdated);
        return response;
    }

    protected boolean isPrioritized(Mailing mailing) {
        if (mailingPriorityService != null) {
            return mailingPriorityService.getMailingPriority(mailing.getCompanyID(), mailing.getId()) > 0;
        }

        return false;
    }

    protected void checkForDependencies(HttpServletRequest req, MailingSendForm aForm, ActionMessages messages) { /* nothing to do here */ }

        /**
         * Returns a list of unreachable links
         * @param form MailingSendForm object
         * @param request HTTP request
         * @param errors {@link ActionMessages} to add errors found during link checking
         * @return number of erroneous links
         */
    
    protected void checkUrlsInMailing(final MailingSendForm form, final HttpServletRequest request, final ActionMessages messages, final ActionMessages errors) {
    	assert errors != null : "No ActionMessages for errors";
		
    	final Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
        final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());

        try {
	    	mailing.buildDependencies(false, null, webApplicationContext, messages, errors, AgnUtils.getAdmin(request));
	
	    	checkForUnreachableLinks(mailing, request, errors);
        } catch(final Exception e) {
        	logger.error("Error checking links", e);
        }
    }

    private int checkForUnreachableLinks(final Mailing mailing, HttpServletRequest req, ActionMessages errors) {
    	assert errors != null : "No ActionMessages for errors";
    	assert mailing != null: "No mailing";
    	
    	// retrieve the list of links
		try {
            Collection<ComTrackableLink> links = mailing.getTrackableLinks().values();
			
			List<LinkReachability> resultList = linkcheckService.checkLinkReachability(links);
			for(LinkReachability availability : resultList) {
				switch(availability.getReachability()) {
				 case TIMED_OUT:
			    	 errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link.timeout", availability.getUrl() + " <br>"));
			    	 break;
			    	 
				 case NOT_FOUND:
			    	 errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link.notReachable", availability.getUrl() + " <br>"));
					 break;
					 
			     default:
			    	 errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link", availability.getUrl() + " <br>"));
				}
			}

			return resultList.size();
		} catch (Exception e) {
			logger.error("checkForInvalidLinks: "+e, e);
			return 0; // TODO: This does not make much sense, but preserves old logic
		}
    }

    /**
     * Check if there is at list one admin or test recipient binding for the mailing list of the mailing.
     * @param mailingId MailingSendForm object
     * @param admin
     * @return  true==success
     *          false==the mailing list has no admin or test recipient bindings
     */
    protected boolean hasPreviewRecipient(int mailingId, ComAdmin admin) {
    	return mailingDao.hasPreviewRecipients(mailingId, admin.getCompanyID());
    }

    /**
     * Loads mailing data from db into form; also gets list of target groups from db and stores it in the request.
     * @param aForm MailingSendForm object
     * @param request HTTP request
     * @throws GeneralSecurityException
     * @throws UnsupportedEncodingException
     */
    protected Mailing loadMailing(MailingSendForm aForm, HttpServletRequest request) throws Exception {
    	Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(request));

        if (aMailing == null) {
            aMailing = mailingFactory.newMailing();
            aMailing.init(AgnUtils.getCompanyID(request), applicationContext);
            aMailing.setId(0);
            aMailing.setCompanyID(AgnUtils.getCompanyID(request));
            aForm.setMailingID(0);
        }

        aForm.setShortname(aMailing.getShortname());
        aForm.setDescription(aMailing.getDescription());
        aForm.setIsTemplate(aMailing.isIsTemplate());
        aForm.setMailingtype(aMailing.getMailingType());
        aForm.setWorldMailingSend(this.maildropService.isActiveMailing(aMailing.getId(), aMailing.getCompanyID()));
        aForm.setTargetGroups(aMailing.getTargetGroups());
        aForm.setNeedsTarget(aMailing.getNeedsTarget());
        aForm.setPrioritizationDisallowed(!aMailing.isPrioritizationAllowed());
        if (aMailing.getEmailParam() == null || aMailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()){
	        aForm.setEmailFormat(MediaTypes.EMAIL.getMediaCode());
	        aForm.getPreviewForm().setFormat(MailingPreviewHelper.INPUT_TYPE_TEXT);
        } else {
        	aForm.setEmailFormat(aMailing.getEmailParam().getMailFormat());
        }
        
        aForm.setMailinglistID(aMailing.getMailinglistID());
        aForm.setMailing(aMailing);
        aForm.setHasDeletedTargetGroups(this.targetService.hasMailingDeletedTargetGroups(aMailing));
        
        loadSteppingBlocksize(aForm, aMailing);
        request.setAttribute("targetGroups", targetDao.getTargetLights(AgnUtils.getCompanyID(request)));
        setSendButtonsControlAttributes(request, aMailing);
        
        return aMailing;
    }

	private void loadSteppingBlocksize(MailingSendForm aForm, Mailing aMailing) {
		for (MaildropEntry drop : aMailing.getMaildropStatus()) {
        	if (drop.getStatus() == MaildropStatus.WORLD.getCode()) {
        		aForm.setStepping(drop.getStepping());
        		aForm.setBlocksize(drop.getBlocksize());
        	}
        }
		performSteppingBlocksizeCalculation(aForm);
	}
	
	private void performSteppingBlocksizeCalculation(MailingSendForm aForm) {
		int blocksizeSwitch = aForm.getBlocksize();
		switch (blocksizeSwitch) {
		case 250:
			if (aForm.getStepping() == 15) {
				aForm.setBlocksize(1000);
				break;
			} else {
				break;
			}
		case 1250:
			if (aForm.getStepping() == 15) {
				aForm.setBlocksize(5000);
				break;
			} else {
				break;
			}
		case 2500:
			if (aForm.getStepping() == 15) {
				aForm.setBlocksize(10000);
				break;
			} else {
				break;
			}
		case 2083:
			if (aForm.getStepping() == 5) {
				aForm.setBlocksize(25000);
				break;
			} else {
				break;
			}
		case 4166:
			if (aForm.getStepping() == 5) {
				aForm.setBlocksize(50000);
				break;
				
			} else if (aForm.getStepping() == 1) {
				aForm.setBlocksize(250000);
				break;
			} else {
				break;
			}
		case 1666:
			if (aForm.getStepping() == 1) {
				aForm.setBlocksize(100000);
				break;
			} else {
				break;
			}
		case 8333:
			if (aForm.getStepping() == 1) {
				aForm.setBlocksize(500000);
				break;
			} else {
				break;
			}
		default:
			//do nothing
			break;
		}
	}

    /**
     * Loads delivery statistic data from db into the form;
     * adjusts delivery stats frame height and stores the height value in the form;
     * sets flag for displaying admin and test send buttons.
     * @param aForm MailingSendForm object
     * @param request HTTP request
     * @throws Exception
     */
    protected void loadDeliveryStats(MailingSendForm aForm, HttpServletRequest request) throws Exception {
        DeliveryStat deliveryStat = deliveryStatService.getDeliveryStats(AgnUtils.getCompanyID(request), aForm.getMailingID(), aForm.getMailingtype());
        aForm.setDeliveryStat(deliveryStat);
        
        AgnUtils.setAdminDateTimeFormatPatterns(request);

        Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(request));
		List<String> targetNames = targetDao.getTargetNamesByIds(AgnUtils.getCompanyID(request), this.targetService.getTargetIdsFromExpression(aMailing));
		aForm.setTargetGroupsNames(targetNames);

		int frameHeight = 201;
		if (targetNames.size() > 1) {
			frameHeight += (targetNames.size() - 1) * 13;
		}
		aForm.setFrameHeight(frameHeight);
		
		// set flag for displaying admin- and test-send-buttons
		aForm.setTransmissionRunning(mailingDao.isTransmissionRunning(aForm.getMailingID()));

	}

	protected boolean validateActivation(HttpServletRequest request, MailingSendForm form, ActionMessages errors, ActionMessages messages) {
        if (WorkflowParametersHelper.isWorkflowDriven(request) &&
                (form.getMailingtype() == MailingTypes.DATE_BASED.getCode() ||
                        form.getMailingtype() == MailingTypes.ACTION_BASED.getCode())) {
            return false;
        } else {
        	Tuple<Long, Long> calculatedMaxMailingSizes = mailingBaseService.calculateMaxSize(form.getMailing());
            Long approximateMaxSizeWithoutImages = calculatedMaxMailingSizes.getFirst();
        	long maximumMailingSizeAllowed = configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, AgnUtils.getCompanyID(request));
        	if (approximateMaxSizeWithoutImages > maximumMailingSizeAllowed) {
        		errors.add("global", new ActionMessage("error.mailing.size.large", maximumMailingSizeAllowed));
	        	return false;
	        } else {
                Long approximateMaxSize = calculatedMaxMailingSizes.getSecond();
	        	long warningMailingSize = configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, AgnUtils.getCompanyID(request));
	        	if (approximateMaxSize > warningMailingSize) {
	        		messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.size.large", warningMailingSize));
	        	}
		        return validateNeedTarget(request, form, errors);
	        }
		}
    }
    
    /**
     * If Mailing created from template with rule " Mailing should only be sent with Target group-selection",
     * validate if at least one Target Group is set for Mailing.
     *
     * @param form MailingSendForm object
     * @param errors ActionMessages object
     * @return  true = mailing can be sent
     *          false = it is need to set Target Group
     */
    protected boolean validateNeedTarget(MailingSendForm form, ActionMessages errors) {
        if (!form.isIsTemplate() && form.isNeedsTarget() &&
                (form.getTargetGroups() == null || form.getTargetGroups().isEmpty())) {
            errors.add("global", new ActionMessage("error.mailing.rulebased_without_target"));
            return false;
        }
        return true;
    }
    
    protected boolean validateNeedTarget(HttpServletRequest req, MailingSendForm form, ActionMessages errors) {
        if (form.getTargetGroups() == null && form.getMailingtype() == MailingTypes.DATE_BASED.getCode()) {
			errors.add("global", new ActionMessage("error.mailing.rulebased_without_target"));
			return false;
		}
		
		if (AgnUtils.getCompanyID(req) == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
			errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
			return false;
		}
		
		return true;
    }

    /**
     * Tries to cancel mailing delivery and returns true if delivery is canceled, or false - if the delivery could not be
     * canceled or some error occurred on execution of delivery canceling.
     * @param aForm MailingSendForm object
     * @param req HTTP request
     * @return  true=success
     *          false=delivery could not be canceled
     */
    protected boolean cancelMailingDelivery(MailingSendForm aForm, HttpServletRequest req) {
        int cachedMailtype = aForm.getMailingtype();
        int mailingID = aForm.getMailingID();
        String shortname = aForm.getShortname();

        return  cancelMailingDelivery(cachedMailtype, mailingID, shortname, req);
    }
    
    private final boolean resumeMailingDelivery(final MailingSendForm form, final HttpServletRequest request) {
    	try {
    		final boolean isDeliveryResumed = this.mailingStopService.resumeMailing(AgnUtils.getCompanyID(request), form.getMailingID());
    		
	        if (isDeliveryResumed) {
	            writeUserActivityLog(AgnUtils.getAdmin(request), "do resume mailing", "Mailing: " + form.getShortname() + " (" + form.getMailingID() + ")");
	        }

	        return isDeliveryResumed;
    	} catch(final MailingStopServiceException e) {
    		logger.error(String.format("Error resuming mailing %d", form.getMailingID()), e);
    		
    		return false;
    	}
    }

    /**
     * Cancel mailing delivery routine.
     * @param cachedMailtype
     * @param mailingID
     * @param shortname
     * @param req HTTP request
     * @return  true=success
     *          false=delivery could not be canceled
     */
    protected boolean cancelMailingDelivery(int cachedMailtype, int mailingID, String shortname, HttpServletRequest req) {
    	try {
	        final boolean isDeliveryCancelled = this.mailingStopService.stopMailing(AgnUtils.getCompanyID(req), mailingID, false);
	        
	        if (isDeliveryCancelled) {
	            writeUserActivityLog(AgnUtils.getAdmin(req), "do cancel mailing", "Mailing type: " + mailingTypeToString(cachedMailtype) + ". " + shortname + " (" + mailingID + ")");
	        }
	        return isDeliveryCancelled;
    	} catch(final MailingStopServiceException e) {
    		logger.error(String.format("Error stopping mailing %d", mailingID), e);
    		
    		return false;
    	}
    }

    /**
     * Creates maildrop entry object for storing mailing send data (status of send mailing, mailing send date and time,
     * mailing content); checks syntax of mailing content by generating dummy preview; loads maildrop entry into mailing,
     * saves mailing in database; checks send date and time, and sends mailing if it should be sent immediately.
     * @param aForm MailingSendForm object
     * @param req HTTP request
     * @throws Exception
     */
    protected void sendMailing(MailingSendForm aForm, HttpServletRequest req, ActionMessages messages) throws Exception {
    	int stepping, blocksize;
        boolean admin=false;
        boolean test=false;
        boolean world=false;
        boolean isPreserveTrackableLinks = false;
        java.util.Date sendDate=new java.util.Date();
        java.util.Date genDate=new java.util.Date();
        int startGen = MaildropGenerationStatus.NOW.getCode();
        MaildropEntry maildropEntry = new MaildropEntryImpl();
        final PreviewForm previewForm = aForm.getPreviewForm();
        final boolean useBackendPreview = configService.getBooleanValue(ConfigValue.Development.UseBackendMailingPreview, AgnUtils.getCompanyID(req));

        switch(aForm.getAction()) {
            case MailingSendAction.ACTION_SEND_ADMIN:
                maildropEntry.setStatus(MaildropStatus.ADMIN.getCode());
                admin=true;
                break;

            case MailingSendAction.ACTION_SEND_TEST:
                maildropEntry.setStatus(MaildropStatus.TEST.getCode());
                admin=true;
                test=true;
                break;

            case MailingSendAction.ACTION_SEND_WORLD:
                maildropEntry.setStatus(MaildropStatus.WORLD.getCode());
                admin=true;
                test=true;
                world=true;
                isPreserveTrackableLinks = true;
                break;

            case MailingSendAction.ACTION_ACTIVATE_RULEBASED:
                maildropEntry.setStatus(MaildropStatus.DATE_BASED.getCode());
                world=true;
                break;

            case MailingSendAction.ACTION_ACTIVATE_CAMPAIGN:
                maildropEntry.setStatus(MaildropStatus.ACTION_BASED.getCode());
                world=true;
                break;
                
			default:
				break;
        }

        if(aForm.getSendDate()!=null) {
            GregorianCalendar aCal=new GregorianCalendar(TimeZone.getTimeZone(AgnUtils.getAdmin(req).getAdminTimezone()));

            aCal.set(Integer.parseInt(aForm.getSendDate().substring(0, 4)), Integer.parseInt(aForm.getSendDate().substring(4, 6))-1, Integer.parseInt(aForm.getSendDate().substring(6, 8)), aForm.getSendHour(), aForm.getSendMinute());
            sendDate=aCal.getTime();
        }

        Mailing aMailing=mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

        if(aMailing==null) {
            return;
        }

        MailingTypes mailingType = MailingTypes.getByCode(aMailing.getMailingType());
        stepping = 0;
        blocksize = 0;
        try {
			stepping = aForm.getStepping();
			blocksize = aForm.getBlocksize();
		} catch (Exception e) {
			stepping = 0;
			blocksize = 0;
		}

        Mailinglist aList=mailinglistDao.getMailinglist(aMailing.getMailinglistID(), AgnUtils.getCompanyID(req));
        String preview=null;

        if(mailinglistDao.getNumberOfActiveSubscribers(admin, test, world, aMailing.getTargetID(), aList.getCompanyID(), aList.getId())==0) {
            throw new TranslatableMessageException("error.mailing.no_subscribers");
        }

        // check syntax of mailing by generating dummy preview
        
        preview = useBackendPreview
        		? this.mailingPreviewService.renderTextPreview(aMailing.getId(), previewForm.getCustomerID())
        		: aMailing.getPreview(aMailing.getTextTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, previewForm.getCustomerID(), applicationContext);
        if (StringUtils.isBlank(preview)) {
            if (mailingService.isTextVersionRequired(AgnUtils.getCompanyID(req), aForm.getMailingID())) {
                throw new TranslatableMessageException("error.mailing.no_text_version");
            } else {
                messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.mailing.no_text_version"));
            }
        }
        
        preview = useBackendPreview
        		? this.mailingPreviewService.renderHtmlPreview(aMailing.getId(), previewForm.getCustomerID())
        		: aMailing.getPreview(aMailing.getHtmlTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, previewForm.getCustomerID(), applicationContext);
        if(aForm.getEmailFormat()>0 && preview.trim().length()==0) {
            throw new TranslatableMessageException("error.mailing.no_html_version");
        }
        
        preview = useBackendPreview
        		? this.mailingPreviewService.renderPreviewFor(aMailing.getId(), previewForm.getCustomerID(), aMailing.getEmailParam().getSubject())
        		: aMailing.getPreview(aMailing.getEmailParam().getSubject(), MailingPreviewHelper.INPUT_TYPE_HTML, previewForm.getCustomerID(), applicationContext);
        if (StringUtils.isBlank(aMailing.getEmailParam().getSubject())) {
            throw new TranslatableMessageException("error.mailing.subject.too_short");
        }
        
        preview = useBackendPreview
        		? this.mailingPreviewService.renderPreviewFor(aMailing.getId(), previewForm.getCustomerID(), aMailing.getEmailParam().getFromAdr())
        		: aMailing.getPreview(aMailing.getEmailParam().getFromAdr(), MailingPreviewHelper.INPUT_TYPE_HTML, previewForm.getCustomerID(), applicationContext);
        if(preview.trim().length()==0) {
            throw new TranslatableMessageException("error.mailing.sender_adress");
        }

        maildropEntry.setSendDate(sendDate);

        if(!DateUtil.isSendDateForImmediateDelivery(sendDate)) {
            // sent gendate if senddate is in future
            GregorianCalendar tmpGen=new GregorianCalendar();
            GregorianCalendar now = new GregorianCalendar();

            tmpGen.setTime(sendDate);
            tmpGen.add(Calendar.MINUTE, -mailingService.getMailGenerationMinutes(aMailing.getCompanyID()));
            if(tmpGen.before(now)) {
                tmpGen = now;
            }
            genDate=tmpGen.getTime();
        }

		switch (mailingType) {
            case NORMAL:
                if (world && DateUtil.isDateForImmediateGeneration(genDate) && isPrioritized(aMailing)) {
                    startGen = MaildropGenerationStatus.SCHEDULED.getCode();
                }
                break;
            default: break;
        }

        if (!DateUtil.isDateForImmediateGeneration(genDate)) {
            switch (mailingType) {
                case NORMAL:
                case FOLLOW_UP:
                    startGen = MaildropGenerationStatus.SCHEDULED.getCode();
                    break;
                default: break;
            }
        }
        
        if (world && maildropService.isActiveMailing(aMailing.getId(), aMailing.getCompanyID())) {
            return;
        } else if (isPostMailing(aMailing)) {
        	// POST/Triggerdialog mailings have their own deliveryprocess via TriggerdialogDeliveryJobWorker
        	return;
        } else {
	        maildropEntry.setGenStatus(startGen);
	        maildropEntry.setGenDate(genDate);
	        maildropEntry.setGenChangeDate(new java.util.Date());
	        maildropEntry.setMailingID(aMailing.getId());
	        maildropEntry.setCompanyID(aMailing.getCompanyID());
	        maildropEntry.setStepping(stepping);
			maildropEntry.setBlocksize(blocksize);
	
	        aMailing.getMaildropStatus().add(maildropEntry);
	
	        mailingDao.saveMailing(aMailing, isPreserveTrackableLinks);
	        if (startGen == MaildropGenerationStatus.NOW.getCode() &&
	                maildropEntry.getStatus() != MaildropStatus.ACTION_BASED.getCode() &&
	                maildropEntry.getStatus() != MaildropStatus.DATE_BASED.getCode()) {
	        	ClassicTemplateGenerator.generateClassicTemplate(aForm.getMailingID(), req, applicationContext);
	            aMailing.triggerMailing(maildropEntry.getId(), new Hashtable<>(), this.applicationContext);
	        }
	        if (logger.isInfoEnabled()) {
	        	logger.info("send mailing id: " + aMailing.getId()+" type: " + maildropEntry.getStatus());
	        }
	
	        logSendAction(AgnUtils.getAdmin(req), sendDate, aMailing, aForm.getAction());
        }
    }

    /**
     * Write mailing send to user log
     * @param admin — admin
     * @param sendDate — scheduled send date
     * @param aMailing — mailing to send
     */
    protected void logSendAction(ComAdmin admin, Date sendDate, Mailing aMailing, int sendActionType) {
        final Locale backendLocale = Locale.UK;
        final String mailingType = mailingTypeToString(aMailing.getMailingType());
        MailingTypes type = MailingTypes.getByCode(aMailing.getMailingType());
        assert(type != null);
    
        switch (type) {
            case ACTION_BASED:
                writeUserActivityLog(admin, "do activate mailing", "Mailing type: " + mailingType + ". " + getTriggerMailingDescription(aMailing));
                break;

            case DATE_BASED:
                writeUserActivityLog(admin, "do activate mailing", "Mailing type: " + mailingType + ", at: " + DateUtilities.getDateFormat(DateFormat.SHORT, backendLocale).format(sendDate) + ". " + getTriggerMailingDescription(aMailing));
                break;

            default:
                writeUserActivityLog(admin, "do schedule mailing", "Mailing type: " + mailingType + ", at: " + DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, backendLocale).format(sendDate) + ". " + getRegularMailingDescription(aMailing, sendActionType));
        }
    }

    protected String getSendActionTypeDescription(int sendActionType) {
        switch(sendActionType) {
            case ACTION_SEND_ADMIN:
                return "send to admin recipients";

            case ACTION_SEND_TEST:
                return "send to test recipients";

            case ACTION_SEND_WORLD:
                return "send to world recipients";

            default:
                return "unknown - " + sendActionType;
        }
    }

    protected String getTriggerMailingDescription(Mailing mailing) {
        return String.format(
                "%s (%d)",
                mailing.getShortname(),
                mailing.getId()
        );
    }

    protected String getRegularMailingDescription(Mailing mailing, int sendActionType) {
        return String.format(
                "%s (%d) (delivery type: %s)",
                mailing.getShortname(),
                mailing.getId(),
                getSendActionTypeDescription(sendActionType)
        );
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
     * Gets mailing from database, removes maildrop entry of the mailing and save the mailing in database.
     * @param aForm  MailingSendForm object
     * @param req  HTTP request
     * @throws Exception
     */
    protected void deactivateMailing(MailingSendForm aForm, HttpServletRequest req) throws Exception {
        int companyID = AgnUtils.getCompanyID(req);
        Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), companyID);
        
        if (mailing != null && WorkflowParametersHelper.isWorkflowDriven(req)) {
            if (mailing.getMailingType() == MailingTypes.ACTION_BASED.getCode() ||
                    mailing.getMailingType() == MailingTypes.DATE_BASED.getCode()) {
                mailing = null;
            }
        }
    
        if (mailing != null && mailing.getMailingType() == MailingTypes.ACTION_BASED.getCode() &&
                bounceFilterService.isMailingUsedInBounceFilterWithActiveAutoResponder(companyID, mailing.getId())) {
            mailing = null;
        }
    
       
        if (mailing != null) {
            maildropStatusDao.cleanup(mailing.getMaildropStatus());

            mailingDao.saveMailing(mailing, false);

            String description = String.format("%s (%s)", mailing.getShortname(), mailing.getId());
            writeUserActivityLog(AgnUtils.getAdmin(req), "do cancel mailing", "Mailing type: " + mailingTypeToString(mailing.getMailingType()) + ". " + description);
            
            updateStatus(mailing.getId(), companyID, "disable");
        }
    }
    
    
	protected void updateStatus(int mailingId, int companyId, String status) throws Exception {
		Mailing aMailing = mailingDao.getMailing(mailingId, companyId);

		if (aMailing == null) {
			return;
		}
		mailingDao.updateStatus(aMailing.getId(), status);
	}
	
    protected ActionForward getPreview(int mailingId, PreviewForm previewForm, ActionMapping mapping, HttpServletRequest req, ActionMessages errors) {
        try {
          return getPreview(mailingId, previewForm, mapping, req);
        } catch (AgnTagException agnTagException) {
          req.setAttribute("errorReport", agnTagException.getReport());
          errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags"));
        } catch (Exception e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
        }
        return mapping.findForward("preview_errors");
    }

    /**
     * Generates mailing preview by given mailing format. Calls generate preview method; for mailings of HTML or text
     * format, if the generated preview string is empty, analyzes preview content blocks and throws AgnTagException contains
     * report with data about invalid content.
     * Forwards to preview page according to mailing format.
     * @param mailingId
     * @param previewForm
     * @param mapping the ActionMapping used to select this instance
     * @param request HTTP request
     * @return action forward for displaying page with preview
     * @throws Exception
     */
    protected ActionForward getPreview (int mailingId, PreviewForm previewForm, ActionMapping mapping, HttpServletRequest request) throws Exception {
        int companyId = getBulkCompanyId(request);
        if (companyId <= 0) {
            companyId = AgnUtils.getCompanyID(request);
        }
        int previewFormat = previewForm.getFormat();
    
        Mailing aMailing = mailingDao.getMailing(mailingId, companyId);
        if (aMailing == null) {
            return mapping.findForward("preview." + previewFormat);
        }
        
        ComAdmin admin = AgnUtils.getAdmin(request);
	
        MediaTypes mediaType = MailingPreviewHelper.castPreviewFormatToMediaType(previewFormat, mediatypeFactory);
        if (mediaType == null) {
            mediaType = MediaTypes.EMAIL;
            previewFormat = MailingPreviewHelper.INPUT_TYPE_TEXT;
            previewForm.setFormat(previewFormat);
        }
        
        if (MediaTypes.EMAIL == mediaType) {
            Page previewPage = generateBackEndPreview(aMailing.getId(), previewForm);
            
            if (MailingPreviewHelper.INPUT_TYPE_HTML == previewFormat) {
                String previewContent = previewForm.isNoImages() ? previewPage.getStrippedHTML() : previewPage.getHTML();
                if (previewContent != null) {
                    //mobile image urls are resolved in makePreview by isMobile parameter
                    previewForm.setPreviewContent(previewContent);
                } else {
                    analyzeErrors(admin, aMailing.getHtmlTemplate().getEmmBlock(), mailingId, aMailing);
                }
            }

            if (MailingPreviewHelper.INPUT_TYPE_TEXT == previewFormat) {
            	final String previewContent = previewPage.getText();
                if (previewContent != null) {
                    previewForm.setPreviewContent(StringUtils.defaultIfBlank(
                            StringUtils.substringBetween(previewContent, "<pre>", "</pre>"),
                            previewContent));
                } else {
                    analyzeErrors(admin, aMailing.getTextTemplate().getEmmBlock(), mailingId, aMailing);
                }
            }
        } else if (isPostPreview(mediaType, aMailing)) {
        	Locale locale = AgnUtils.getAdmin(request).getLocale();
            previewForm.setPreviewContent(I18nString.getLocaleString("noPostalPreview.html", locale));
        } else {
            MailingComponent component = aMailing.getTemplate(mediaType);
            if (component != null) {
            	final String previewContent = this.configService.getBooleanValue(ConfigValue.Development.UseBackendMailingPreview, companyId)
            			? (
            				previewFormat == MailType.TEXT.getIntValue()
            					? this.mailingPreviewService.renderTextPreview(mailingId, previewForm.getCustomerID())
            					: this.mailingPreviewService.renderHtmlPreview(mailingId, previewForm.getCustomerID())
            			  )
            			: aMailing.getPreview(component.getEmmBlock(), previewFormat, previewForm.getCustomerID(), true, applicationContext);

                if (previewContent != null) {
                    previewForm.setPreviewContent(previewContent);
                }
            }
		}
		
//        aForm.setEmailFormat(aMailing.getEmailParam().getMailFormat());
//        aForm.setMailinglistID(aMailing.getMailinglistID());
		return mapping.findForward("preview." + previewFormat);
	}
    
    private void analyzeErrors(ComAdmin admin, String template, int mailingId, Mailing aMailing) throws Exception {
        Map<String, DynamicTag> dynTagsMap = aMailing.getDynTags();
        analyzePreviewError(admin, template, dynTagsMap, mailingId);
    }
	
	protected boolean isPostPreview(MediaTypes mediaType, Mailing mailing) {
        return false;
	}
	
	protected boolean isPostMailing(Mailing aMailing) {
		return false;
	}
	
	protected int getBulkCompanyId(HttpServletRequest req) {
		return -1;
	}

    /**
     * Gets mailing from database, validates mailing subject and from address, if the  subject and the from address is ok,
     * generates preview, parses preview header to get values for sender and subject and loads parsed values into the form.
     * If the validation fails, the method throws AgnTagException contains the report about invalid characters.
     * @param aForm MailingSendForm object
     * @param req HTTP request
     * @throws Exception
     */
    protected boolean loadPreviewHeaderData(MailingSendForm aForm, HttpServletRequest req) {
        Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

        if (aMailing == null) {
            return false;
        }
    
        String subjectParameter = aMailing.getEmailParam().getSubject();
        String fromParameter = "";
        
        try {
            fromParameter = aMailing.getEmailParam().getFromAdr();
            
            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(aForm.getMailingID(), AgnUtils.getLocale(req));
            
            Vector<String> failures = new Vector<>();
            StringBuffer fromReportBuffer = new StringBuffer();
            StringBuffer subjectReportBuffer = new StringBuffer();
    
            if (!tagCheck.checkContent(fromParameter, fromReportBuffer, failures)) {
                req.setAttribute("isTagFailureInFromAddress", true);
            }
    
            if (!tagCheck.checkContent(subjectParameter, subjectReportBuffer, failures)) {
                req.setAttribute("isTagFailureInSubject", true);
            }
    
            tagCheck.done();
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }
    
        PreviewForm previewForm = aForm.getPreviewForm();
        Page output = generateBackEndPreview(aMailing.getId(), previewForm);
        String header = output.getHeader();
        String senderEmail = fromParameter;
        String subject = subjectParameter;

        if (header != null) {
            senderEmail = PreviewHelper.getFrom(header);
            subject = PreviewHelper.getSubject(header);
        }

        previewForm.setSenderEmail(senderEmail);
        previewForm.setSubject(subject);

        if (aMailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            previewForm.setFormat(MailingPreviewHelper.INPUT_TYPE_TEXT);
        }
        
        return true;
    }

    /**
     * Creates report about errors in dynamic tags.
     * @param blockName name of content block with invalid content
     * @param errorReports  list of messages about parsing errors (is changing inside the method)
     * @param templateReport content with errors
     */
    protected void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
		Map<String,String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
		for (Entry<String,String> entry:tagsWithErrors.entrySet()) {
			String[] errorRow = new String[3];
			errorRow[0] = blockName; // block
			errorRow[1] =  entry.getKey(); // tag
			errorRow[2] =  entry.getValue(); // value
			
			errorReports.add(errorRow);
		}
		List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);
		for (String error:errorsWithoutATag){
			String[] errorRow = new String[3];
			errorRow[0] = blockName;
			errorRow[1] = "";
			errorRow[2] = error;
			errorReports.add(errorRow);
		}
	}


    /**
     * Loads sent mailing statistics from database.
     * @param aForm MailingSendForm object
     * @param req HTTP request
     * @throws Exception
     */
	protected void loadSendStats(MailingSendForm aForm, HttpServletRequest req) throws Exception {
        int numText = 0;
        int numHtml = 0;
        int numOffline = 0;
        int numTotal = 0;
        StringBuffer sqlSelection = new StringBuffer(" ");
        ComTarget aTarget = null;
        boolean isFirst = true;
        int numTargets = 0;
        String tmpOp = "AND ";

        Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

        if (aMailing.getTargetMode() == Mailing.TARGET_MODE_OR) {
            tmpOp = "OR ";
        }

        if (aForm.getTargetGroups() != null && aForm.getTargetGroups().size() > 0) {
            Iterator<Integer> aIt = aForm.getTargetGroups().iterator();

            while (aIt.hasNext()) {
                aTarget = targetDao.getTarget((aIt.next()).intValue(), AgnUtils.getCompanyID(req));
                if (aTarget!=null) {
                    if (isFirst) {
                        isFirst=false;
                    } else {
                    	sqlSelection.append(tmpOp);
                    }
                    sqlSelection.append("("  + aTarget.getTargetSQL() + ") ");
                    numTargets++;
                }
            }
            if (numTargets>1) {
                sqlSelection.insert(0, " AND (");
            } else {
                sqlSelection.insert(0, " AND ");
            }
            if (!isFirst && numTargets>1) {
                sqlSelection.append(") ");
            }
        }

        String sqlStatement="SELECT count(*), bind.mediatype, cust.mailtype FROM customer_" + AgnUtils.getCompanyID(req) + "_tbl cust, customer_" +
                AgnUtils.getCompanyID(req) + "_binding_tbl bind WHERE bind.mailinglist_id=" + aMailing.getMailinglistID() +
                " AND cust.customer_id=bind.customer_id" + sqlSelection.toString() + " AND bind.user_status=1 GROUP BY bind.mediatype, cust.mailtype";

        
        // TODO: Move code to DAO layer
        try (Connection con = dataSource.getConnection()) {
        	try (Statement stmt = con.createStatement()) {
        		try (ResultSet rset = stmt.executeQuery(sqlStatement)) {
                    while (rset.next()){
                        switch (rset.getInt(2)) {
                            case 0:
                                switch (MailingModel.Format.getByCode(rset.getInt(3))) {
                                    case TEXT: // only Text
                                        numText+=rset.getInt(1);
                                        break;

                                    case ONLINE_HTML: // Online-HTML
                                        if (aMailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) { // only Text-Mailing
                                            numText += rset.getInt(1);
                                        } else {
                                            numHtml += rset.getInt(1);
                                        }
                                        break;

                                    case OFFLINE_HTML: // Offline-HTML
                                        if (aMailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) { // only Text-Mailing
                                            numText += rset.getInt(1);
                                        }
                                        if (aMailing.getEmailParam().getMailFormat() == MailingModel.Format.ONLINE_HTML.getCode()) { // only Text/Online-HTML-Mailing
                                            numHtml += rset.getInt(1);
                                        }
                                        if (aMailing.getEmailParam().getMailFormat() == MailingModel.Format.OFFLINE_HTML.getCode()) { // every format
                                            numOffline += rset.getInt(1);
                                        }
                                        break;
                                        
									default:
										break;
                                }
                                break;
                            default:
                                aForm.setSendStat(rset.getInt(2), rset.getInt(1));
                        }
                    }
        		}
        	}
        } catch (Exception e) {
            logger.error("loadSendStats: " + e);
            logger.error("SQL: " + sqlStatement);
            throw new Exception("SQL-Error: " + e);
        }

        numTotal += numText;
        numTotal += numHtml;
        numTotal += numOffline;

        aForm.setSendStatText(numText);
        aForm.setSendStatHtml(numHtml);
        aForm.setSendStatOffline(numOffline);
        aForm.setSendStat(0, numTotal);
    }

    /**
     * Generates mailing preview.
     * @param mailingID id of mailing
     * @param previewForm object contains preview settings
     * @return  Page object contains mailing preview
     */
	public Page generateBackEndPreview(int mailingID, PreviewForm previewForm) {
        Preview.Size size = Preview.Size.getSizeById(previewForm.getSize());
        boolean isMobileView = size == Preview.Size.MOBILE_PORTRAIT || size == Preview.Size.MOBILE_LANDSCAPE;

        Preview preview = previewFactory.createPreview();
		Page output;
		switch (previewForm.getModeType()) {
            case TARGET_GROUP:
                output = preview.makePreview(mailingID, 0, previewForm.getTargetGroupId(), isMobileView);
                break;
            case RECIPIENT:
                //proceed default case
            default:
                output = preview.makePreview(mailingID, previewForm.getCustomerID(), false, isMobileView);
                break;
        }
		
		preview.done();
		return output;
	}

    /**
     * Checks content of dynamic tags; prepares error report and raises AgnTagException.
     * @param template mailing template with dynamic tags
     * @param dynTagsMap map of dynamic tags
     * @param mailingID id of mailing
     * @throws Exception
     */
	protected void analyzePreviewError(ComAdmin admin, String template, Map<String, DynamicTag> dynTagsMap, int mailingID) throws Exception {
		List<String[]> errorReports = new ArrayList<>();
		Vector<String> outFailures = new Vector<>();
		TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailingID, admin.getLocale());

		StringBuffer templateReport = new StringBuffer();
		if (!tagCheck.checkContent(template, templateReport, outFailures)) {
			appendErrorsToList(TEMPLATE, errorReports, templateReport);
		}

		for (DynamicTag tag : dynTagsMap.values()) {
			Map<Integer, DynamicTagContent> tagContentMap = tag.getDynContent();
			for (DynamicTagContent tagContentValue : tagContentMap.values()) {
				StringBuffer contentOutReport = new StringBuffer();
				if (!tagCheck.checkContent(tagContentValue.getDynContent(), contentOutReport, outFailures)) {
					appendErrorsToList(tag.getDynName(), errorReports, contentOutReport);
				}
			}
		}
		
		if (errorReports.size() == 0) {
			Locale locale = admin.getLocale();
			errorReports.add(new String[]{SafeString.getLocaleString("preview.error.empty", locale)});
		}
		
		throw new AgnTagException("error.template.dyntags", errorReports);
	}

	/**
	 * Returns DAO accessing target groups.
	 * 
	 * @return DAO accessing target groups.
	 */
    public ComTargetDao getTargetDao() {
        return targetDao;
    }

    /**
     * Sets DAO accessing target groups.
     * 
     * @param targetDao DAO accessing target groups
     */
    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public MailingComponentDao getMailingComponentDao() {
        return mailingComponentDao;
    }

    public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
        this.mailingComponentDao = mailingComponentDao;
    }

    public LinkcheckService getLinkcheckService() {
        return linkcheckService;
    }

    public void setLinkcheckService(LinkcheckService linkcheckService) {
        this.linkcheckService = linkcheckService;
    }

    public void setMailingFactory(MailingFactory mailingFactory) {
        this.mailingFactory = mailingFactory;
    }

    @Required
    public void setDeliveryStatService(ComMailingDeliveryStatService deliveryStatService) {
        this.deliveryStatService = deliveryStatService;
    }

    public void setMailingPriorityService(MailingPriorityService mailingPriorityService) {
        this.mailingPriorityService = mailingPriorityService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

    public void setMailinglistDao(MailinglistDao mailinglistDao) {
        this.mailinglistDao = mailinglistDao;
    }

    public TAGCheckFactory getTagCheckFactory() {
        return tagCheckFactory;
    }

    public void setTagCheckFactory(TAGCheckFactory tagCheckFactory) {
        this.tagCheckFactory = tagCheckFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PreviewFactory getPreviewFactory() {
        return previewFactory;
    }

    public void setPreviewFactory(PreviewFactory previewFactory) {
        this.previewFactory = previewFactory;
    }

    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }
    
    /**
     * Set service dealing with target groups.
     * 
     * @param targetService service dealing with target groups
     */
    @Required
    public void setTargetService(ComTargetService targetService) {
    	this.targetService = targetService;
    }
    
    /**
     * Set mailing service.
     * 
     * @param service mailing service
     */
    @Required
    public void setMailingService(MailingService service) {
    	this.mailingService = service;
    }

    @Required
    public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
        this.maildropStatusDao = maildropStatusDao;
    }

    @Required
    public void setBounceFilterService(BounceFilterService bounceFilterService) {
        this.bounceFilterService = bounceFilterService;
    }

    @Required
    public final void setMaildropService(final MaildropService service) {
    	this.maildropService = service;
    }
    
    protected void extendedChecks(ActionForm form, HttpServletRequest request, ActionMessages messages) throws Exception {
		// nothing to do
	}
    
    
    protected void setSendButtonsControlAttributes(final HttpServletRequest request, final Mailing mailing) throws Exception {
    	request.setAttribute("CAN_SEND_WORLDMAILING", checkCanSendWorldMailing(request, mailing));
    }
    
    protected boolean checkCanSendWorldMailing(final HttpServletRequest request, final Mailing mailing) throws Exception {
        if (mailing.getMailingType() == MailingTypes.NORMAL.getCode() ||
            mailing.getMailingType() == MailingTypes.FOLLOW_UP.getCode()) {
            return !maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
        }

        return false;
    }
    
    private final int copyMailingToResume(final int companyID, final MailingSendForm form) throws Exception {
    	return this.mailingStopService.copyMailingForResume(
    			companyID,
    			form.getMailingID(),
    			form.getShortname(),
    			"Copy of " + form.getShortname());		// TODO i18n required here
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
 
	@Required
    public void setBlacklistService(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Required
    public void setConfigService(final ConfigService service) {
        this.configService = service;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }
    
    @Required
    public final void setMailingStopService(final MailingStopService service) {
    	this.mailingStopService = Objects.requireNonNull(service, "Mailing stop service is empty");
    }
    
    @Required
    public void setMediatypeFactory(MediatypeFactory mediatypeFactory) {
        this.mediatypeFactory = mediatypeFactory;
    }
	
	protected boolean createPostTrigger(Mailing mailing, int companyId, Date sendDate) throws Exception {
		return false;
	}
	
	@Required
	public final void setMailingPreviewService(final MailingPreviewService service) {
		this.mailingPreviewService = Objects.requireNonNull(service, "MailingPreviewService is null");
	}
}
