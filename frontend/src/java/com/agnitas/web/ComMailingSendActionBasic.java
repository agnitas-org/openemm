/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.SORT_NAME;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CONVERSION_RATE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.dao.FollowUpType;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.ModeType;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.Tuple;
import org.agnitas.web.MailingSendAction;
import org.agnitas.web.MailingSendForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComDkimDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.PredefinedType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.commons.TranslatableMessageException;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.bean.MailingDependentType;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.mailing.web.NotYourMailingException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingSendRecipientStatWorker;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ClassicTemplateGenerator;
import com.agnitas.util.NumericUtil;

import net.sf.json.JSONObject;

public class ComMailingSendActionBasic extends MailingSendAction {
	public enum MailGenerationOptimizationMode {
		NONE(0, null),
		DAY(1, "day"),
		NEXT_24h(2, "24h");

		private final int code;
		private final String maildropCode;

		MailGenerationOptimizationMode(final int code, final String maildropCode) {
			this.code = code;
			this.maildropCode = maildropCode;
		}

		public static MailGenerationOptimizationMode fromCode(final int code) {
			for(MailGenerationOptimizationMode mode : values()) {
				if(mode.code == code) {
					return mode;
				}
			}

			return null;
		}

		public static MailGenerationOptimizationMode fromMaildropCode(final String code) {
			if(code == null) {
				for(MailGenerationOptimizationMode mode : values()) {
					if(mode.maildropCode == null) {
						return mode;
					}
				}

			} else {
				for(MailGenerationOptimizationMode mode : values()) {
					if(code.equals(mode.maildropCode)) {
						return mode;
					}
				}
			}
			return null;
		}
	}

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingSendActionBasic.class);

	public static final int ACTION_UNLOCK_SEND = ACTION_SEND_LAST + 1;
	public static final int ACTION_SAVE_STATUSMAIL_RECIPIENTS = ACTION_SEND_LAST + 2;
	public static final int ACTION_VIEW_SEND2_NO_RECIPIENT_COUNT = ACTION_SEND_LAST + 3;
	public static final int ACTION_RECIPIENT_CALCULATE = ACTION_SEND_LAST + 4;
	public static final int ACTION_ACTIVATE_INTERVALMAILING = ACTION_SEND_LAST + 5;
	public static final int ACTION_DEACTIVATE_INTERVALMAILING = ACTION_SEND_LAST + 6;
    public static final int ACTION_PDF_PREVIEW = ACTION_SEND_LAST + 7;
    public static final int ACTION_SAVE_STATUSMAIL_ONERRORONLY = ACTION_SEND_LAST + 8;

    public static final int SAVE_RECIPIENTS_STATUS_WRONG = 1;
    public static final int SAVE_RECIPIENTS_STATUS_DUPLICATED = 2;
    public static final int SAVE_RECIPIENTS_STATUS_OK = 3;
    public static final int SAVE_RECIPIENTS_STATUS_BLACKLISTED = 4;

    public static final int ADMIN_TARGET_SINGLE_RECIPIENT = -1;

    private static final String FUTURE_TASK = "GET_MAILING_STAS";

    protected ComTrackableLinkDao trackableLinkDao;
    protected OnepixelDao onepixelDao;
    protected Map<String, Future<?>> futureHolder;
    protected ExecutorService workerExecutorService;
    protected ComWorkflowService workflowService;
    protected ComDkimDao dkimDao;
    protected MediatypesDao mediatypesDao;
    private GenerationPDFService generationPDFService;
    private ComBirtReportService birtReportService;
    private AutoExportService autoExportService;
    private EmmActionService actionService;
    private JavaMailService javaMailService;
    private GridServiceWrapper gridService;
    private WebStorage webStorage;
    private BirtReportFactory birtReportFactory;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
            case ACTION_VIEW_SEND:
                return "view_send";
            case ACTION_UNLOCK_SEND:
                return "unlock_send";
            case ACTION_DEACTIVATE_MAILING:
                return "deactivate_mailing";
            case ACTION_DEACTIVATE_INTERVALMAILING:
                return "deactivate_intervalmailing";
            case ACTION_ACTIVATE_RULEBASED:
                return "activate_rulebased";
            
            case ACTION_ACTIVATE_CAMPAIGN:
                return "activate_campaign";
            case ACTION_ACTIVATE_INTERVALMAILING:
                return "activate_intervalmailing";
            case ACTION_SAVE_STATUSMAIL_RECIPIENTS:
                return "save_statusmail_recipients";
			case ACTION_SAVE_STATUSMAIL_ONERRORONLY:
				return "save_statusmail_recipients";
            case ACTION_CONFIRM_SEND_WORLD:
                return "confirm_send_world";
            case ACTION_PREVIEW_SELECT:
                return "preview_select";
            
            case ACTION_VIEW_SEND2:
                return "view_send2";
            case ACTION_VIEW_SEND2_NO_RECIPIENT_COUNT:
                return "view_send2_no_recipient_count";
            case ACTION_CANCEL_MAILING:
                return "cancel_mailing";
            case ACTION_PDF_PREVIEW:
                return "pdf_preview";
                
            case ACTION_VIEW_DELSTATBOX:
                return "view_delstatbox";
            case ACTION_CANCEL_MAILING_REQUEST:
                return "cancel_mailing_request";
            case ACTION_SEND_ADMIN:
                return "send_admin";
            case ACTION_SEND_TEST:
                return "send_test";
                
            case ACTION_RESUME_MAILING_REQUEST:
            	return "resume_mailing_request";
            case ACTION_RESUME_MAILING:
            	return "resume_mailing";
            case ACTION_RESUME_MAILING_BY_COPY_REQUEST:
            	return "resume_mailing_by_copy_request";
            case ACTION_RESUME_MAILING_BY_COPY:
            	return "resume_mailing_by_copy";
           	
            case ACTION_SEND_WORLD:
                return "send_world";
            case ACTION_PREVIEW_HEADER:
                return "preview_header";
            case ACTION_PREVIEW:
                return "preview";
            case ACTION_CHECK_LINKS:
                return "check_links";
			case ACTION_PRIORITIZATION_SWITCHING:
				return "prioritization_switching";
			
            default:
                return super.subActionMethodName(subAction);
        }
    }
    
	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
	 *
	 * @param form
	 * @param request
	 * @param response
     * @param mapping The ActionMapping used to select this instance
	 * @return destination
	 */
    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// Validate the request parameters specified by the user
		ComMailingSendForm aForm = (ComMailingSendForm) form;
		PreviewForm previewForm = aForm.getPreviewForm();
		ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (logger.isInfoEnabled()) {
			logger.info("Action: " + aForm.getAction());
		}
		
        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        
        ComAdmin admin = AgnUtils.getAdmin(request);
		assert admin != null;
	
		int companyId = AgnUtils.getCompanyID(request);

		request.setAttribute("adminTargetGroupList", targetDao.getTestAndAdminTargetLights(companyId));
        request.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, aForm.getMailingID()));
        
        AgnUtils.setAdminDateTimeFormatPatterns(request);

		try {
			switch (aForm.getAction()) {
				case ACTION_VIEW_SEND:
					loadMailing(aForm, request);
					// TODO Remove this quick-hack and replace it with some more sophisticated code
					
					validateNeedTarget(aForm, errors);
					loadDeliveryStats(aForm, request);
					
					aForm.setAdminTargetGroupID(0);
					destination = mapping.findForward("send");
					break;
				
				case ACTION_UNLOCK_SEND:
					unlockMailing(aForm, request);
					loadMailing(aForm, request);
					loadDeliveryStats(aForm, request);
					loadFollowUpStat(aForm, request, errors, messages);
					aForm.setFollowUpType(getFollowUpType(aForm.getMailingID()));
					destination = mapping.findForward("send");
					break;
				
				case MailingSendAction.ACTION_SEND_ADMIN:
				case MailingSendAction.ACTION_SEND_TEST:
					loadMailing(aForm, request);
					sendTestMailing(aForm, request, messages, errors);
					loadDeliveryStats(aForm, request);
					
					extendedChecks(form, request, messages);
					if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
						errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
						destination = mapping.findForward("send");
					} else {
						aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
						destination = mapping.findForward("send");
					}
					break;

				case MailingSendAction.ACTION_DEACTIVATE_MAILING:
					deactivateMailing(aForm, request);
					loadMailing(aForm, request);
					aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					destination = mapping.findForward("send");
					break;
				
				case ACTION_DEACTIVATE_INTERVALMAILING:
					updateStatus(aForm, request, "disable");
					loadMailing(aForm, request);
					aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					destination = mapping.findForward("send");
					break;
				
				case MailingSendAction.ACTION_ACTIVATE_RULEBASED:
				case MailingSendAction.ACTION_ACTIVATE_CAMPAIGN:
					Mailing mailingToSend = loadMailing(aForm, request);
					
					if (validateActivation(request, aForm, errors, messages)) {
						if(!containsInvalidTargetGroups(companyId, aForm)) {
							try {
								if (isPostMailing(mailingToSend)) {
									TimeZone aZone = AgnUtils.getTimeZone(request);
									GregorianCalendar sendDate = new GregorianCalendar(aZone);
									sendDate.set(Integer.parseInt(aForm.getSendDate().substring(0, 4)), Integer.parseInt(aForm.getSendDate().substring(4, 6)) - 1, Integer.parseInt(aForm.getSendDate().substring(6, 8)), aForm.getSendHour(), aForm.getSendMinute());
									createPostTrigger(mailingToSend, companyId, sendDate.getTime());
								} else {
									sendMailing(aForm, request, messages);
								}
							} finally {
								loadMailing(aForm, request);
							}
							updateStatus(aForm, request, "active");
						} else {
							errors.add("global", new ActionMessage("error.mailing.containsInvaidTargetGroups"));
						}
					} else {
						updateStatus(aForm, request, "disable");
					}
					loadDeliveryStats(aForm, request);
					aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					destination = mapping.findForward("send");
					break;
				
				case ACTION_ACTIVATE_INTERVALMAILING:
					loadMailing(aForm, request);
					if (aForm.getTargetGroups() == null) {
						errors.add("global", new ActionMessage("error.mailing.rulebased_without_target"));
					} else if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
						errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
					} else if (containsInvalidTargetGroups(companyId, aForm)) {
						errors.add("global", new ActionMessage("error.mailing.containsInvaidTargetGroups"));
					} else {
						updateStatus(aForm, request, "active");
						aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					}
					destination = mapping.findForward("send");
					break;
				
				case ACTION_SAVE_STATUSMAIL_RECIPIENTS:
					int status = saveStatmailRecipients(aForm);
					loadMailing(aForm, request);
					loadDeliveryStats(aForm, request);
					switch (status) {
						case SAVE_RECIPIENTS_STATUS_OK:
							messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
							break;
						case SAVE_RECIPIENTS_STATUS_DUPLICATED:
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.email.duplicated"));
							break;
						case SAVE_RECIPIENTS_STATUS_BLACKLISTED:
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.email.blacklisted"));
							break;
						case SAVE_RECIPIENTS_STATUS_WRONG:
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.email.wrong"));
							break;
						default: break;
					}
					destination = mapping.findForward("send");
					break;
					
				case ACTION_SAVE_STATUSMAIL_ONERRORONLY:
					HttpUtils.responseJson(response, saveStatusmailOnErrorOnly(request));
                    return null;
				
				case ACTION_CONFIRM_SEND_WORLD:
					if (!NumericUtil.matchedUnsignedIntegerPattern(aForm.getMaxRecipients())) {
						errors.add("global", new ActionMessage("error.maxRecipients.notNumeric"));
						destination = mapping.findForward("send2");
						request.setAttribute("adminDateFormat", admin.getDateFormat().toPattern());
						aForm.setAction(MailingSendAction.ACTION_VIEW_SEND2);
					} else if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
						errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
						destination = mapping.findForward("send");
					} else {
						
						// Validation of send date
						TimeZone aZone = AgnUtils.getTimeZone(request);
						GregorianCalendar currentDate = new GregorianCalendar(aZone);
						GregorianCalendar sendDate = new GregorianCalendar(aZone);
						sendDate.set(Integer.parseInt(aForm.getSendDate().substring(0, 4)), Integer.parseInt(aForm.getSendDate().substring(4, 6)) - 1, Integer.parseInt(aForm.getSendDate().substring(6, 8)), aForm.getSendHour(), aForm.getSendMinute());
						
						if (currentDate.getTime().getTime() / 60000 - configService.getIntegerValue(ConfigValue.SendFairnessMinutes) <= sendDate.getTime().getTime() / 60000) {
							loadMailing(aForm, request);
							validateNeedTarget(aForm, errors);
							aForm.setAction(MailingSendAction.ACTION_SEND_WORLD);
							destination = mapping.findForward("send_confirm");
							
							// 	reportSendParameters have to be passed through
							// ActionRedirect didn't worked , so what ?
							request.setAttribute("reportSendAfter24h", aForm.isReportSendAfter24h());
							request.setAttribute("reportSendAfter48h", aForm.isReportSendAfter48h());
							request.setAttribute("reportSendAfter1Week", aForm.isReportSendAfter1Week());
							request.setAttribute("reportSendEmail", aForm.getReportSendEmail());
							request.setAttribute("potentialSendDate", admin.getDateFormat().format(sendDate.getTime()));
							request.setAttribute("potentialSendTime", admin.getTimeFormat().format(sendDate.getTime()));
							request.setAttribute("adminDateFormat", admin.getDateFormat().toPattern());
						} else {
							errors.add("global", new ActionMessage("error.you_choose_a_time_before_the_current_time"));
							destination = mapping.findForward("send2");
						}
						
						if (mailingDao.getMailingType(aForm.getMailingID()) == MailingTypes.FOLLOW_UP.getCode()) {
							// Check basemailing data for followup mailing
							String followUpFor = mailingDao.getFollowUpFor(aForm.getMailingID());
							if (StringUtils.isNotEmpty(followUpFor)) {
								int basemailingId = Integer.parseInt(followUpFor);
								Timestamp baseMailingSendDate = mailingDao.getLastSendDate(basemailingId);
								if (baseMailingSendDate == null || sendDate.getTime().before(baseMailingSendDate)) {
									errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.followup.senddate_before_basemail"));
								}
							}
						}
						
						if (containsInvalidTargetGroups(companyId, aForm)) {
							errors.add("global", new ActionMessage("error.mailing.containsInvaidTargetGroups"));
						}
							
						int num_recipients;
						
						int recipientsAmount = aForm.getSendStatText() + aForm.getSendStatHtml() + aForm.getSendStatOffline();
						// Sum up other mediatypes
						loadSendStats(aForm, request);
						for (Entry<Integer, Integer> entry : aForm.getSendStats().entrySet()) {
							if (entry.getKey() > 0 && entry.getValue() > 0) {
								recipientsAmount += entry.getValue();
							}
						}
						
						if (Integer.parseInt(aForm.getMaxRecipients()) <= 0) {
							num_recipients = recipientsAmount;
						} else {
							num_recipients = Math.min(Integer.parseInt(aForm.getMaxRecipients()), recipientsAmount);
						}
						final NumberFormat formatter = NumberFormat.getNumberInstance(admin.getLocale());
						request.setAttribute("num_recipients", formatter.format(num_recipients));
					}
					
					break;
				
				case MailingSendAction.ACTION_PREVIEW_SELECT:
					int mailingId = aForm.getMailingID();
					aForm.setFollowUpType(getFollowUpType(mailingId));
					loadMailing(aForm, request);
					
					MailingPreviewHelper.updateActiveMailingPreviewFormat(previewForm, request, mailingId, companyId, mailingDao);
					
					loadCustomerPreviewParams(request, mailingId, previewForm, errors);
					loadTargetGroupsPreviewParams(request, mailingId);

					destination = previewForm.isPure() ?
							mapping.findForward("preview_select_pure") :
							mapping.findForward("preview_select");
					break;
				
				case ACTION_VIEW_SEND2:
					final Mailing mailing = loadMailing(aForm, request);

					if (!checkIfSendConfigurationPossible(mailing, errors, messages)) {
						saveErrors(request, errors);
						return mapping.findForward("messages");
					}

					if (!validateNeedDkimKey(companyId, admin, aForm, messages, errors)) {
						destination = mapping.findForward("send");
					} else {
						validateNeedTarget(aForm, errors);

						//if (comForm.getFollowupFor() == null || comForm.getFollowupFor().equals("")) {
						destination = mapping.findForward("progress");
						boolean recipientStatIsDone = loadSendStats(aForm, request);
						//                        } else {
						//                            int companyID = aForm.getCompanyID(req);
						//                            int followUpID = aForm.getMailingID();
						//                            int baseMailID = Integer.parseInt(aForm.getFollowupFor());
						//                            String sessionID = req.getSession().getId();
						//                            getFollowUpStatsService().startCalculation(followUpID, baseMailID, companyID, sessionID, true);
						//                            recipientStatIsDone = true;
						//                        }
						
						if (recipientStatIsDone) {
							setAutomaticSendStates(aForm, (ComCompany) AgnUtils.getCompany(request));
							aForm.setAction(MailingSendAction.ACTION_CONFIRM_SEND_WORLD);
							String reportEmail = admin.getStatEmail();
							if (reportEmail == null || reportEmail.isEmpty()) {
								reportEmail = admin.getEmail();
							}
							aForm.setReportSendEmail(reportEmail);
							Set<Integer> targetGroups = (Set<Integer>) aForm.getTargetGroups();
							if (targetGroups == null) {
								targetGroups = new HashSet<>();
							}
							List<String> targetGroupNames = getTargetDao().getTargetNamesByIds(companyId, targetGroups);
							request.setAttribute("targetGroupNames", targetGroupNames);
							destination = mapping.findForward("send2");
						}
						
						if (!"true".equals(request.getParameter("ignore_ajax")) && HttpUtils.isAjax(request)) {
							if ("send2".equals(destination.getName())) {
								destination = mapping.findForward("ajax_send2");
							} else if ("progress".equals(destination.getName())) {
								destination = mapping.findForward("ajax_progress");
							}
						}
						
						if (aForm.getWorkflowId() != 0) {
							messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.workflow.delivery.stop"));
						}
						
						aForm.setAutoExports(autoExportService == null ? new ArrayList<>() : autoExportService.getMailingAutoExports(companyId, false));
						setInputControlAttributes(request, companyId);
					}
					break;
				
				case ACTION_VIEW_SEND2_NO_RECIPIENT_COUNT:
					//Works like view_send2 without statistic
					loadMailing(aForm, request);
					setAutomaticSendStates(aForm, (ComCompany) AgnUtils.getCompany(request));
					aForm.setAction(MailingSendAction.ACTION_CONFIRM_SEND_WORLD);
					Set<Integer> targetGroups = (Set<Integer>) aForm.getTargetGroups();
					if (targetGroups == null) {
						targetGroups = new HashSet<>();
					}
					List<String> targetGroupNames = getTargetDao().getTargetNamesByIds(companyId, targetGroups);
					request.setAttribute("targetGroupNames", targetGroupNames);
					destination = mapping.findForward("send2");
					break;
				
				case MailingSendAction.ACTION_CANCEL_MAILING:
					loadMailing(aForm, request);
					if (request.getParameter("kill") != null) {
						if (cancelMailingDelivery(aForm, request)) {
							loadDeliveryStats(aForm, request);
							mailingDao.updateStatus(aForm.getMailingID(), "canceled");
						}
						destination = mapping.findForward("send");
					} else {
						destination = mapping.findForward("send");
					}
					break;
				
				case ACTION_PDF_PREVIEW:
		            String baseUrl = configService.getValue(ConfigValue.PreviewUrl);

		            if (StringUtils.isBlank(baseUrl)) {
		                baseUrl = configService.getValue(ConfigValue.SystemUrl);
		            }
					String url = baseUrl + "/mailingsend.do;jsessionid=" + request.getSession().getId() +
							"?action=" + MailingSendAction.ACTION_PREVIEW + "&mailingID=" + aForm.getMailingID() +
							"&previewForm.format=" + MailingPreviewHelper.INPUT_TYPE_HTML +
							"&previewForm.size=" + previewForm.getSize() +
							"&previewForm.modeTypeId=" + previewForm.getModeTypeId() +
							"&previewForm.targetGroupId=" + previewForm.getTargetGroupId() +
							"&previewForm.customerID=" + previewForm.getCustomerID() +
							"&previewForm.noImages=" + previewForm.isNoImages();
					
					LightweightMailing lightweightMailing = mailingDao.getLightweightMailing(companyId, aForm.getMailingID());
					aForm.setShortname(lightweightMailing.getShortname());
					boolean errorOccured = false;
					File pdfFile = generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, aForm.getShortname(),
							admin, "", "Portrait", "Mailing");
					if (pdfFile != null) {
						try (
								ServletOutputStream responseOutput = response.getOutputStream();
								FileInputStream instream = new FileInputStream(pdfFile)
						) {
							response.setContentType("application/pdf");
				            HttpUtils.setDownloadFilenameHeader(response, aForm.getShortname() + ".pdf");
							response.setContentLength((int) pdfFile.length());
							byte bytes[] = new byte[16384];
							int len = 0;
							while ((len = instream.read(bytes)) != -1) {
								responseOutput.write(bytes, 0, len);
							}
						} catch (Exception e) {
							errorOccured = true;
							throw e;
						} finally {
							if (!errorOccured && pdfFile.exists()) {
								try {
									pdfFile.delete();
								} catch (Exception e) {
									logger.error("Cannot delete temporary pdf file: " + pdfFile.getAbsolutePath(), e);
								}
							}
						}
					}
					destination = null;
					break;
				
				default:
					aForm.setFollowUpType(getFollowUpType(aForm.getMailingID()));
					return super.execute(mapping, form, request, response);
			}
		} catch (TranslatableMessageException e) {
			logger.error("MAILINGSENDACTION exception ", e);

			if ("error.mailing.send.admin.maxMails".equals(e.getErrorMsgKey())) {
				int maxAdminMails = configService.getIntegerValue(ConfigValue.MaxAdminMails, companyId);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMsgKey(), maxAdminMails));
			} else {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMsgKey()));
			}
			destination = mapping.findForward("send");
		} catch (Exception e) {
			logger.error("MAILINGSENDACTION exception ", e);
			
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));

			destination = mapping.findForward("send");
		}

		if (destination != null && ("send2".equals(destination.getName()) || "ajax_send2".equals(destination.getName()))) {
            // Check if checkbox for duplicate email check on send should be set checked by default or not
            if (configService.getBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, companyId)) {
                aForm.setDoublechecking(true);
            }
        }

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			this.saveErrors(request, errors);
			if (aForm.getAction() == MailingSendAction.ACTION_SEND_ADMIN
					|| aForm.getAction() == MailingSendAction.ACTION_SEND_TEST
					|| aForm.getAction() == MailingSendAction.ACTION_SEND_WORLD) {

				return (new ActionForward(mapping.getInput()));
			}
		}

        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		return destination;
	}
	
	private void loadTargetGroupsPreviewParams(HttpServletRequest request, int mailingId) {
		List<TargetLight> targets = mailingService.listTargetGroupsOfMailing(AgnUtils.getCompanyID(request), mailingId);
		request.setAttribute("availableTargetGroups", targets);
	}
	
	private void loadCustomerPreviewParams(HttpServletRequest request, int mailingId, PreviewForm previewForm, ActionMessages errors) {
    	int companyId = AgnUtils.getCompanyID(request);
    	Map<Integer, String> recipientList = recipientDao.getAdminAndTestRecipientsDescription(companyId, mailingId);
        request.setAttribute("previewRecipients", recipientList);
		if (mailingDao.hasPreviewRecipients(mailingId, companyId)) {
			previewForm.setHasPreviewRecipient(true);
			if (previewForm.getModeType() == ModeType.RECIPIENT) {
				choosePreviewCustomerId(companyId, mailingId, previewForm, previewForm.isUseCustomerEmail(), errors, recipientList);
			}
		} else {
			previewForm.setHasPreviewRecipient(false);
		}
	}
	
	@Override
	protected boolean createPostTrigger(Mailing mailing, int companyId, Date sendDate) throws Exception {
		return false;
	}
	
	private JSONObject saveStatusmailOnErrorOnly(HttpServletRequest request) {
        String isStatusmailOnErrorOnlyParam = request.getParameter("statusmailOnErrorOnly");
        String mailingIdParam = request.getParameter("mailingId");
        boolean isUpdated = false;

        if (StringUtils.isNotBlank(isStatusmailOnErrorOnlyParam) && StringUtils.isNotBlank(mailingIdParam)) {
            boolean isStatusmailOnErrorOnly = BooleanUtils.toBoolean(isStatusmailOnErrorOnlyParam);
            int mailingId = NumberUtils.toInt(mailingIdParam);
            ComAdmin admin = AgnUtils.getAdmin(request);

            try {
                // creation of UAL entry
                Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
                String action = "switched mailing statusmailonerroronly";
                String description = String.format("statusmailonerroronly: %s, mailing type: %s. %s(%d)",
                		isStatusmailOnErrorOnly ? "true" : "false",
                        mailingTypeToString(mailing.getMailingType()),
                        mailing.getShortname(),
                        mailing.getId());

                if (mailingService.switchStatusmailOnErrorOnly(admin.getCompanyID(), mailingId, isStatusmailOnErrorOnly)) {
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

    private boolean validateNeedDkimKey(int companyID, ComAdmin admin, ComMailingSendForm mailingSendForm, ActionMessages messages, ActionMessages errors) {
	    MediatypeEmail mediatypeEmail = mailingDao.getMailing(mailingSendForm.getMailingID(), companyID).getEmailParam();
	    
	    // No further check, if media type "Email" is not active
	    if (!Mediatype.isActive(mediatypeEmail)) {
	    	return true;
	    }

	    String fromAddress = mediatypeEmail != null ? mediatypeEmail.getFromEmail() : "";
    	String senderDomain = AgnUtils.getDomainFromEmail(fromAddress);
        if (configService.getBooleanValue(ConfigValue.DkimGlobalActivation, companyID) && !dkimDao.existsDkimKeyForDomain(companyID, senderDomain)) {
        	if ("warning".equalsIgnoreCase(configService.getValue(ConfigValue.SendMailingWithoutDkimCheck, companyID))) {
        		messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.mandatoryDkimKeyMissing", senderDomain));
        		String toAddressList = configService.getValue(ConfigValue.Mailaddress_Support, companyID);
        		ComCompany company = companyDao.getCompany(companyID);
        		String companyAdminMail = company.getContactTech();
        		if (StringUtils.isNotBlank(companyAdminMail)) {
        			toAddressList += ", " + companyAdminMail;
        		}
        		javaMailService.sendEmail(null, null, null, null, null, toAddressList, null,
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.subject", admin.getLocale()),
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), company.getShortname() + " (CID: " + companyID + ")", senderDomain),
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), company.getShortname() + " (CID: " + companyID + ")", senderDomain), "UTF-8");
        		return true;
        	} else if ("error".equalsIgnoreCase(configService.getValue(ConfigValue.SendMailingWithoutDkimCheck, companyID))) {
	            errors.add("global", new ActionMessage("error.mailing.mandatoryDkimKeyMissing", senderDomain));
	            String toAddressList = configService.getValue(ConfigValue.Mailaddress_Support, companyID);
        		ComCompany company = companyDao.getCompany(companyID);
        		String companyAdminMail = company.getContactTech();
        		if (StringUtils.isNotBlank(companyAdminMail)) {
        			toAddressList += ", " + companyAdminMail;
        		}
        		javaMailService.sendEmail(null, null, null, null, null, toAddressList, null,
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.subject", admin.getLocale()),
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), company.getShortname() + " (CID: " + companyID + ")", senderDomain),
        			I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), company.getShortname() + " (CID: " + companyID + ")", senderDomain), "UTF-8");
	            return false;
        	} else {
        		return true;
        	}
        } else {
        	return true;
        }
    }

	private boolean choosePreviewCustomerId(int companyId, int mailingId, PreviewForm previewForm, boolean useCustomerEmail, ActionMessages errors, Map<Integer, String> recipientList) {
        String previewCustomerEmail = previewForm.getCustomerEmail();
        if (useCustomerEmail && StringUtils.isNotBlank(previewCustomerEmail)) {
            int customerId = getCustomerIdWithEmailInMailingList(companyId, mailingId, previewCustomerEmail);
            if(customerId > 0){
                previewForm.setCustomerID(customerId);
                previewForm.setCustomerATID(0);
                return true;
            } else {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.error.previewCustomerEmail"));
                previewForm.setCustomerEmail("");
            }
        } else if (previewForm.getCustomerATID() != 0) {
            previewForm.setCustomerID(previewForm.getCustomerATID());
            return true;
        }
        return setMinCustomerId(previewForm, recipientList);
    }

    private boolean setMinCustomerId(PreviewForm previewForm, Map<Integer, String> recipientList) {
        if (recipientList != null && !recipientList.isEmpty()) {
            int minId = Collections.min(recipientList.keySet());
            previewForm.setCustomerID(minId);
            previewForm.setCustomerATID(minId);
            return true;
        }
        return false;
    }

    @Override
    protected void checkForDependencies(HttpServletRequest req, MailingSendForm aForm, ActionMessages messages) {
        if(mailingDao.getFollowupMailings(aForm.getMailingID(), AgnUtils.getCompanyID(req), false).size() > 0){
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.followup.unschedule"));
        }
    }

    private int saveStatmailRecipients(ComMailingSendForm form) {
		String statmailRecipients = form.getStatusmailRecipients();
        int status = SAVE_RECIPIENTS_STATUS_OK;
        ArrayList<String> testRecipients = new ArrayList<>();

		if(statmailRecipients != null && !"".equals(statmailRecipients.trim())) {
			StringBuffer validatedRecipients = new StringBuffer();
		String regex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
			String whiteSpaceregex = "\\s+";
			String[] recipients = statmailRecipients.split(whiteSpaceregex);
			for(String recipient:recipients) {
                if (recipient.length() > 0) {
                    if(!recipient.matches(regex)) {
                        status = SAVE_RECIPIENTS_STATUS_WRONG;
                    } else if (testRecipients.contains(recipient)) {
                        status = SAVE_RECIPIENTS_STATUS_DUPLICATED;
                    } else {
                        validatedRecipients.append(" ");
                        validatedRecipients.append(recipient);
                        testRecipients.add(recipient);
                    }
                }
			}
			mailingDao.saveStatusmailRecipients(form.getMailingID(), validatedRecipients.toString());
		} else {
			mailingDao.saveStatusmailRecipients(form.getMailingID(), (form).getStatusmailRecipients().trim());
		}
        return status;
	}

    /**
	 * load EMM specific properties into the form e.g. 'Followup-Mailing'
     * @throws Exception
	 */
	@Override
    protected Mailing loadMailing(MailingSendForm aForm, HttpServletRequest request) throws Exception {
		Mailing aMailing = super.loadMailing(aForm, request);

        ComMailingSendForm comForm = (ComMailingSendForm) aForm;
        final int companyId = AgnUtils.getCompanyID(request);

		int mailingId = aMailing.getId();
		loadDependents(request, comForm, aMailing);

		String followUpFor = comForm.getFollowupFor();
		if (StringUtils.isEmpty(followUpFor) || StringUtils.equals("0", followUpFor.trim())) {
			MediatypeEmail mediatype = aMailing.getEmailParam();
			if(mediatype != null) {
				comForm.setFollowupFor(mediatype.getFollowupFor());
				comForm.setFollowUpType(mediatype.getFollowUpMethod());
			}
		}

		Tuple<Long, Long> maxSize = mailingBaseService.calculateMaxSize(aMailing);

		comForm.setApproximateMaxSizeWithoutExternalImages(maxSize.getFirst());
		comForm.setApproximateMaxSize(maxSize.getSecond());
		comForm.setSizeWarningThreshold(configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, companyId));
		comForm.setSizeErrorThreshold(configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, companyId));
		comForm.setStatusmailRecipients(aMailing.getStatusmailRecipients());
		comForm.setStatusmailOnErrorOnly(aMailing.isStatusmailOnErrorOnly());
		
		loadAvailablePreviewFormats(request, aMailing);

		int workflowId = mailingBaseService.getWorkflowId(mailingId, companyId);
        comForm.setWorkflowId(workflowId);

		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		if (workflow != null) {
			request.setAttribute("workflowSendDate", workflow.getGeneralStartDate());
			request.setAttribute("adminTimeZone", AgnUtils.getTimeZone(request));
        }

		if (aForm.getMailingtype() == MailingTypes.INTERVAL.getCode()) {
			String workStatus = mailingDao.getWorkStatus(companyId, aForm.getMailingID());
			if (workStatus == null || !workStatus.equals(MailingStatus.ACTIVE.getDbKey())) {
				// only active or disable is allowed for interval mailings
				workStatus = MailingStatus.DISABLE.getDbKey();
			}
			
			comForm.setWorkStatus(workStatus);
		}

        comForm.setIsTemplate(aMailing.isIsTemplate());

        int gridTemplateId = gridService.getGridTemplateIdByMailingId(aForm.getMailingID());
        comForm.setTemplateId(gridTemplateId);
        comForm.setMailingGrid(gridTemplateId > 0);

        // TODO: Restore mail generation optimization

        // For backward compatibility
        request.setAttribute("templateId", gridTemplateId);
        request.setAttribute("isMailingGrid", gridTemplateId > 0);
        
        request.setAttribute("enableAdminTestDelivery", true);
        
        comForm.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(aForm.getMailingID()));

        setSendButtonsControlAttributes(request, aMailing);
        
        return aMailing;
    }
	
	private void loadDependents(HttpServletRequest req, ComMailingSendForm form, Mailing mailing) {
		int mailingId = mailing.getId();
		if(mailing.getMailingType() == MailingType.ACTION_BASED.getCode()) {
			webStorage.access(WebStorage.MAILING_SEND_DEPENDENTS_OVERVIEW, entry -> {
				if (form.getNumberOfRows() <= 0) {
					form.setNumberOfRows(entry.getRowsCount());
					form.setFilterTypes(entry.getFilterTypes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
				} else {
					entry.setRowsCount(form.getNumberOfRows());
					if (form.getFilterTypes() == null) {
						entry.setFilterTypes(null);
					} else {
						entry.setFilterTypes(Arrays.asList(form.getFilterTypes()));
					}
				}
			});
			
			ComAdmin admin = AgnUtils.getAdmin(req);
			assert admin != null;

			form.setDependents(new ArrayList<>());
			addDependencies(admin.getCompanyID(), mailingId, form, MailingDependentType.ACTION);
			addDependencies(admin.getCompanyID(), mailingId, form, MailingDependentType.WORKFLOW);
			addDependencies(admin.getCompanyID(), mailingId, form, MailingDependentType.BOUNCE_FILTER);
		}
	}

	private void addDependencies(int companyId, int mailingId, ComMailingSendForm form, MailingDependentType type) {
		String[] filterTypes = form.getFilterTypes();
		List<Dependent<MailingDependentType>> dependents = form.getDependents();
		switch (type) {
			case ACTION:
				if (isFilterActive(filterTypes, type)) {
					dependents.addAll(getDependentType(actionService.getActionListBySendMailingId(companyId, mailingId),
                            (item) -> type.forId(item.getId(), item.getShortname())));
				}
				break;
			case WORKFLOW:
				if (isFilterActive(filterTypes, type)) {
					dependents.addAll(getDependentType(workflowService.getDependentWorkflowOnMailing(companyId, mailingId),
                            (item) -> type.forId(item.getWorkflowId(), item.getShortname())));
				}
				break;
			case BOUNCE_FILTER:
				List<BounceFilterDto> bounceFilters = bounceFilterService.getDependentBounceFiltersWithActiveAutoResponderByMailing(companyId, mailingId);
                if (isFilterActive(filterTypes, type)) {
					dependents.addAll(getDependentType(bounceFilters,
                            (item) -> type.forId(item.getId(), item.getShortName())));
				}
				form.setBounceFilterNames(StringUtils.join(bounceFilters.stream().map(BounceFilterDto::getShortName).collect(Collectors.toList()), ", "));
				break;
			default:
				//Do nothing
			
		}
	}
	
	private <T> List<Dependent<MailingDependentType>> getDependentType(List<T> list, Function<T, Dependent<MailingDependentType>> callback) {
		if (callback != null) {
			return list.stream().map(callback).collect(Collectors.toList());
		}

		return new ArrayList<>();
	}
	
	private boolean isFilterActive(String[] filterTypes, MailingDependentType type) {
		return ArrayUtils.isEmpty(filterTypes) || ArrayUtils.contains(filterTypes, type.name());
	}
	
	protected void loadAvailablePreviewFormats(HttpServletRequest request, Mailing mailing) {
		request.setAttribute("availablePreviewFormats", getAvailablePreviewFormats(mailing));
	}

	protected List<Integer> getAvailablePreviewFormats(Mailing mailing) {
		return mailing.getMediatypes().values().stream()
				.filter(type -> type.getStatus() == MediaTypeStatus.Active.getCode())
				.map(Mediatype::getMediaType)
				.map(MediaTypes::getMediaCode)
				.sorted()
				.collect(Collectors.toList());
    }

	private void sendTestMailing(ComMailingSendForm form, HttpServletRequest request, ActionMessages messages, ActionMessages errors) throws Exception {
		if (form.getAdminTargetGroupID() == ADMIN_TARGET_SINGLE_RECIPIENT) {
			List<String> testSingleRecipients = getTestSingleRecipients(form);

			if (validateTestRecipients(AgnUtils.getCompanyID(request), testSingleRecipients, errors)) {
				sendMailing(form, request, messages);
			}
		} else {
			sendMailing(form, request, messages);
		}
	}

	/**
	 * Sends mailing.
	 */
	@Override
	protected void sendMailing(MailingSendForm aForm, HttpServletRequest req, ActionMessages messages) throws Exception {
		final int companyId = AgnUtils.getCompanyID(req);

		int stepping, blocksize;
		boolean admin = false;
		boolean test = false;
		boolean world = false;
		Date sendDate = new Date();
		Date genDate = new Date();
		int startGen = MaildropGenerationStatus.NOW.getCode();
		ComMailingSendForm comForm = (ComMailingSendForm) aForm;
		MaildropEntry drop = new MaildropEntryImpl();

        switch (aForm.getAction()) {
            case MailingSendAction.ACTION_SEND_ADMIN:
                if (aForm.getMailingtype() == MailingTypes.INTERVAL.getCode()) {
                    drop.setId(0);
                    drop.setCompanyID(companyId);
                    drop.setMailingID(aForm.getMailingID());
                    drop.setStatus(MaildropStatus.ADMIN.getCode());
                    drop.setSendDate(new Date());
                    drop.setStepping(0);
                    drop.setBlocksize(0);
                    drop.setGenDate(new Date());
                    drop.setGenChangeDate(new Date());
                    drop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
                    drop.setAdminTestTargetID(0);

                    int maildropStatusId = maildropStatusDao.saveMaildropEntry(drop);
					int mailingListId = mailingDao.getMailinglistId(aForm.getMailingID(), companyId);

                    selectTestRecipients(companyId, mailingListId, maildropStatusId, getTestSingleRecipients(comForm));
                    triggerMaildrop(maildropStatusId);
                    return;
                } else {
                    drop.setStatus(MaildropStatus.ADMIN.getCode());
                    drop.setAdminTestTargetID(Math.max(0, comForm.getAdminTargetGroupID()));
                    admin = true;
                }
                break;

            case MailingSendAction.ACTION_SEND_TEST:
                if (aForm.getMailingtype() == MailingTypes.INTERVAL.getCode()) {
                    drop.setId(0);
                    drop.setCompanyID(companyId);
                    drop.setMailingID(aForm.getMailingID());
                    drop.setStatus(MaildropStatus.TEST.getCode());
                    drop.setSendDate(new Date());
                    drop.setStepping(0);
                    drop.setBlocksize(0);
                    drop.setGenDate(new Date());
                    drop.setGenChangeDate(new Date());
                    drop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
                    drop.setAdminTestTargetID(0);

					int maildropStatusId = maildropStatusDao.saveMaildropEntry(drop);
					int mailingListId = mailingDao.getMailinglistId(aForm.getMailingID(), companyId);

					selectTestRecipients(companyId, maildropStatusId, mailingListId, getTestSingleRecipients(comForm));
					triggerMaildrop(maildropStatusId);
                    return;
                } else {
                    drop.setStatus(MaildropStatus.TEST.getCode());
                    drop.setAdminTestTargetID(Math.max(0, comForm.getAdminTargetGroupID()));
                }
                admin = true;
                test = true;
                break;

            case MailingSendAction.ACTION_SEND_WORLD:
                drop.setStatus(MaildropStatus.WORLD.getCode());
                admin = true;
                test = true;
                world = true;
                break;

            case MailingSendAction.ACTION_ACTIVATE_RULEBASED:
                drop.setStatus(MaildropStatus.DATE_BASED.getCode());
                world = true;
                break;

            case MailingSendAction.ACTION_ACTIVATE_CAMPAIGN:
                drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
                world = true;
                break;
                
            default: break;
        }

        if (aForm.getAction() == MailingSendAction.ACTION_SEND_WORLD || aForm.getAction() == MailingSendAction.ACTION_ACTIVATE_CAMPAIGN || aForm.getAction() == MailingSendAction.ACTION_ACTIVATE_RULEBASED) {
			checkActivateDeeptracking(aForm.getMailingID(), companyId);
		}

		if (aForm.getSendDate() != null) {
			GregorianCalendar aCal = new GregorianCalendar(AgnUtils.getTimeZone(req));
			// 0-3: year, 4-5: month (jan=0), 6-7: day
			aCal.set(Integer.parseInt(aForm.getSendDate().substring(0, 4)),
					Integer.parseInt(aForm.getSendDate().substring(4, 6)) - 1,
					Integer.parseInt(aForm.getSendDate().substring(6, 8)),
					aForm.getSendHour(), aForm.getSendMinute());
			sendDate = aCal.getTime();
		}

		drop.setMaxRecipients(Integer.parseInt(comForm.getMaxRecipients()));

		Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), companyId);

		if (aMailing == null) {
			return;
		}

		if (aForm.getAction() == MailingSendAction.ACTION_SEND_WORLD) {
			scheduleReport(comForm, AgnUtils.getAdmin(req), aMailing, sendDate);

            scheduleExportOfRecipientData(comForm, req, sendDate);
        }

		if (comForm.getGenerationOptimization() == MailGenerationOptimizationMode.NONE.code &&
				(aMailing.getMailingType() == MailingTypes.NORMAL.getCode() || aMailing.getMailingType() == MailingTypes.FOLLOW_UP.getCode())) {
			drop.setMailGenerationOptimization(MailGenerationOptimizationMode.NONE.maildropCode);
            try {
            	int defaultBlocksize = isSteppingForced(companyId);
            	if (defaultBlocksize > 0) {
            		Tuple<Integer, Integer> defaultBlocksizeStepping = AgnUtils.makeBlocksizeAndSteppingFromBlocksize(defaultBlocksize, 60);
                    blocksize = defaultBlocksizeStepping.getFirst();
                    stepping = defaultBlocksizeStepping.getSecond();
            	} else {
	                if (aForm.getStepping() == 60){
	                    Tuple<Integer, Integer> blocksizeAndStepping = AgnUtils.makeBlocksizeAndSteppingFromBlocksize(aForm.getBlocksize(), aForm.getStepping());
	                    blocksize = blocksizeAndStepping.getFirst();
	                    stepping = blocksizeAndStepping.getSecond();
	                } else {
	                    stepping = aForm.getStepping();
	                    blocksize = aForm.getBlocksize();
	                }
            	}
			} catch (Exception e) {
				stepping = 0;
				blocksize = 0;
			}
		} else {
			stepping = 0;
			blocksize = 0;

			MailGenerationOptimizationMode mode = MailGenerationOptimizationMode.fromCode(comForm.getGenerationOptimization());
			if (mode != null) {
				drop.setMailGenerationOptimization(mode.maildropCode);
			}
		}

		Mailinglist aList = mailinglistDao.getMailinglist(aMailing.getMailinglistID(), companyId);
		if (aList == null) {
			throw new TranslatableMessageException("noMailinglistAssigned");
		}
		int maxAdminMails = configService.getIntegerValue(ConfigValue.MaxAdminMails, companyId);

		int numberOfRecipients = mailinglistDao.getNumberOfActiveSubscribers(admin, test, world, aMailing.getTargetID(), companyId, aList.getId());

		if (aForm.getAction() == MailingSendAction.ACTION_SEND_ADMIN || aForm.getAction() == MailingSendAction.ACTION_SEND_TEST) {
			int testRecipientsCount = getTestSingleRecipients(comForm).size();
			if (testRecipientsCount > 0) {
				numberOfRecipients = testRecipientsCount;
			}
		}

		if (numberOfRecipients == 0) {
			throw new TranslatableMessageException("error.mailing.no_subscribers");
		} else if ((admin || test) && !world && numberOfRecipients > maxAdminMails) {
			throw new TranslatableMessageException("error.mailing.send.admin.maxMails");
		}

		if (mailingDao.hasEmail(aMailing.getCompanyID(), aMailing.getId())) {
			MediatypeEmail param = aMailing.getEmailParam();

			// Check the text version of mailing.
			if (isContentBlank(aMailing, aMailing.getTextTemplate())) {
				if (mailingService.isTextVersionRequired(companyId, aForm.getMailingID())) {
					throw new TranslatableMessageException("error.mailing.no_text_version");
				} else {
					messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.mailing.no_text_version"));
				}
			}

			// Check the HTML version unless mail format is "only text".
			if (param.getMailFormat() != MailingModel.Format.TEXT.getCode()) {
				if (isContentBlank(aMailing, aMailing.getHtmlTemplate())) {
					throw new TranslatableMessageException("error.mailing.no_html_version");
				}
			}

			if (StringUtils.isBlank(param.getSubject())) {
				throw new TranslatableMessageException("error.mailing.subject.too_short");
			}

			String senderAddress = null;
			try {
				senderAddress = param.getFromAdr();
			} catch (Exception e) {
				logger.error("Error occurred: " + e.getMessage(), e);
			}

			if (StringUtils.isBlank(senderAddress)) {
				throw new TranslatableMessageException("error.mailing.sender_adress");
			}
		}
		
		if (drop.getStatus() == MaildropStatus.DATE_BASED.getCode()) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(sendDate);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			sendDate = calendar.getTime();
		}

		drop.setSendDate(sendDate);

		if (!DateUtil.isSendDateForImmediateDelivery(sendDate)) {
			// sent gendate if senddate is in future
			GregorianCalendar tmpGen = new GregorianCalendar();
			GregorianCalendar now = new GregorianCalendar();

			tmpGen.setTime(sendDate);
			tmpGen.add(Calendar.MINUTE, -mailingService.getMailGenerationMinutes(companyId));
			if (tmpGen.before(now)) {
				tmpGen = now;
			}
			genDate = tmpGen.getTime();
		}

		MediatypeEmail emailParam=aMailing.getEmailParam();

		if(emailParam != null) {
			emailParam.setFollowupFor(comForm.getFollowupFor());
			emailParam.setDoublechecking(comForm.isDoublechecking());
			emailParam.setSkipempty(comForm.isSkipempty());
			mailingDao.saveMailing(aMailing, false);
		}
		
		MailingTypes mailingType = MailingTypes.getByCode(aMailing.getMailingType());
		switch (mailingType) {
            case NORMAL:
                if (world && DateUtil.isDateForImmediateGeneration(genDate) && isPrioritized(aMailing)) {
                    startGen = MaildropGenerationStatus.SCHEDULED.getCode();
                }
                break;

            /*case DATE_BASED:
				if (test) {
					startGen = MaildropGenerationStatus.SCHEDULED.getCode();
                }
                break;*/
            default: break;
        }

        if (!DateUtil.isDateForImmediateGeneration(genDate)) {
            switch (mailingType) {
                case NORMAL:
                case FOLLOW_UP:
                    startGen = MaildropGenerationStatus.SCHEDULED.getCode();
                    updateStatusByMaildrop(comForm, req, drop);
                    break;
                default: break;
            }
        }

		if (mailingType != MailingTypes.FOLLOW_UP && world &&
				maildropService.isActiveMailing(aMailing.getId(), companyId)) {
			return;
		}

		drop.setGenStatus(startGen);
		drop.setGenDate(genDate);
		drop.setGenChangeDate(new Date());
		drop.setMailingID(aMailing.getId());
		drop.setCompanyID(companyId);
		drop.setStepping(stepping);
		drop.setBlocksize(blocksize);

		aMailing.getMaildropStatus().add(drop);

		mailingDao.saveMailing(aMailing, false);

		if (aForm.getAction() == MailingSendAction.ACTION_SEND_ADMIN || aForm.getAction() == MailingSendAction.ACTION_SEND_TEST) {
			selectTestRecipients(companyId, aMailing.getMailinglistID(), drop.getId(), getTestSingleRecipients(comForm));
		}

		if (startGen == MaildropGenerationStatus.NOW.getCode()
				&& drop.getStatus() != MaildropStatus.ACTION_BASED.getCode()
				&& drop.getStatus() != MaildropStatus.DATE_BASED.getCode()) {
			ClassicTemplateGenerator.generateClassicTemplate(aForm.getMailingID(), req, applicationContext);
			aMailing.triggerMailing(drop.getId(), new Hashtable<>(), applicationContext);
            updateStatusByMaildrop(comForm, req, drop);
		}

        // when the world mailing is sent we need to remove
        // - admin/test clicks
        // - admin/test openings
        // - admin/test data from success_xxx_tbl
        // as we don't want to show that in statistics
        if (drop.getStatus() != MaildropStatus.TEST.getCode() && drop.getStatus() != MaildropStatus.ADMIN.getCode()) {
            onepixelDao.deleteAdminAndTestOpenings(aMailing.getId(), companyId);
            trackableLinkDao.deleteAdminAndTestClicks(aMailing.getId(), companyId);
            mailingDao.cleanTestDataInSuccessTbl(aMailing.getId(), companyId);
            mailingDao.cleanTestDataInMailtrackTbl(aMailing.getId(), companyId);
        }

		if (logger.isInfoEnabled()) {
			logger.info("send mailing id: " + aMailing.getId() + " type: " + drop.getStatus());
		}
        logSendAction(AgnUtils.getAdmin(req), sendDate, aMailing, aForm.getAction());
	}

	private boolean checkIfSendConfigurationPossible(final Mailing mailing, final ActionMessages errors, final ActionMessages messages) {
		SimpleServiceResult result = mailingBaseService.checkContentNotBlank(mailing);

		for (Message msg : result.getErrorMessages()) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, msg.toStrutsMessage());
		}

		for (Message msg : result.getWarningMessages()) {
			messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, msg.toStrutsMessage());
		}

		return result.isSuccess();
	}
	
	private boolean isContentBlank(Mailing mailing, MailingComponent template) {
		if (template == null) {
			return true;
		}

		return mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
	}

	private void triggerMaildrop(int maildropStatusId) throws Exception {
		new MailoutClient().invoke("fire", Integer.toString(maildropStatusId));
	}

	private void selectTestRecipients(@VelocityCheck int companyId, int mailingListId, int maildropStatusId, List<String> addresses) {
		if (CollectionUtils.isNotEmpty(addresses)) {
			List<Integer> customerIds = recipientDao.insertTestRecipients(companyId, mailingListId, UserStatus.Suspend.getStatusCode(), "Test recipient for delivery test", addresses);
			maildropService.selectTestRecipients(companyId, maildropStatusId, customerIds);
		}
	}

	private void scheduleExportOfRecipientData(ComMailingSendForm aForm, HttpServletRequest req, Date sendDate) {
		if (autoExportService != null && isEnableMailingRecipientsReport(aForm)) {
			int autoExportId = aForm.getAutoExportId();
			int mailingId = aForm.getMailingID();
			ComAdmin admin = AgnUtils.getAdmin(req);

			if (aForm.isRecipientReportSendSendingTime()) {
				autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, sendDate);
			}

			if (aForm.isRecipientReportSendAfter24h()) {
				autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, DateUtils.addDays(sendDate, 1));
			}

			if (aForm.isRecipientReportSendAfter48h()) {
				autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, DateUtils.addDays(sendDate, 2));
			}

			if (aForm.isRecipientReportSendAfter1Week()) {
				autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, DateUtils.addDays(sendDate, 7));
			}
		}
	}
    
    private boolean isEnableMailingRecipientsReport(ComMailingSendForm aForm) {
        return (aForm.isRecipientReportSendAfter24h()
                || aForm.isRecipientReportSendAfter48h()
                || aForm.isRecipientReportSendAfter1Week()
                || aForm.isRecipientReportSendSendingTime()) && aForm.getAutoExportId() > 0;
    }

    private void updateStatusByMaildrop(ComMailingSendForm aForm, HttpServletRequest req, MaildropEntry drop) throws Exception {
        String status = "scheduled";
        if (drop.getStatus() == MaildropStatus.TEST.getCode()) {
            status = "test";
        } else if (drop.getStatus() == MaildropStatus.ADMIN.getCode()) {
            status = "admin";
        }
        updateStatus(aForm, req, status);
    }

    @Override
    protected String mailingTypeToString(int mailingType) {
		if (mailingType == MailingTypes.INTERVAL.getCode()) {
			return "interval";
		}
		if (mailingType == MailingTypes.FOLLOW_UP.getCode()) {
			return "followup";
		}
		return super.mailingTypeToString(mailingType);
    }

    
    private void scheduleReport(final ComMailingSendForm form, ComAdmin admin, final Mailing mailing, Date sendDate) throws Exception {
    	// Reports only, if sending world mailings
    	if (form.getAction() == MailingSendAction.ACTION_SEND_WORLD) {
    		if (form.isReportSendAfter24h()) {
    			scheduleNewReport(form, admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_24HOURS);
    		}
    		
    		if (form.isReportSendAfter48h()) {
    			scheduleNewReport(form, admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_48HOURS);
    		}

    		if (form.isReportSendAfter1Week()) {
    			scheduleNewReport(form, admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_WEEK);
    		}
    	}
    }
    
    
	/**
	 * Creates and setup report with mailing type @see {@link ReportSettingsType#MAILING}
	 *
	 * report settings don't consider because:
	 *  "send_date" and "end_date" are not used by {@link ComBirtReport#isTriggeredByMailing()} reports
	 *  "creation_date" is set by DB
	 *  "activation_date" is set when saving activated report
	 *
	 * Report settings should contains 'predefineMailing' @see {@link ComBirtReportSettings#PREDEFINED_ID_KEY} prop to be included in send report's list
	 * Keep in mind, 'selectedMailings' prop will be overwritten by @see {@link ComBirtReportService#getReportsToSend(int, List, List)}
	 *
	 */
    private void scheduleNewReport(final ComMailingSendForm form, ComAdmin admin, final Mailing mailing, final Date sendDate, final BirtReportType reportType) throws Exception {
		// Create the report itself
    	final ComBirtReport report = birtReportFactory.createReport();
		report.setReportType(reportType.getKey());
		
		Date mailingSendDate;
		if (sendDate != null) {
			mailingSendDate = sendDate;
		} else if (mailing.getPlanDate() != null) {
			mailingSendDate = mailing.getPlanDate();
		} else {
			mailingSendDate = new Date();
		}
        
        if (BirtReportType.TYPE_AFTER_MAILING_24HOURS == reportType) {
        	report.setEndDate(DateUtilities.addDaysToDate(mailingSendDate, 15));
        } else if (BirtReportType.TYPE_AFTER_MAILING_48HOURS == reportType) {
        	report.setEndDate(DateUtilities.addDaysToDate(mailingSendDate, 15));
        } else if ( BirtReportType.TYPE_AFTER_MAILING_WEEK == reportType) {
        	report.setEndDate(DateUtilities.addDaysToDate(mailingSendDate, 15));
        } else {
			logger.error("Invalid report type. Report type must be: " +
				BirtReportType.TYPE_AFTER_MAILING_24HOURS + ", " +
	            BirtReportType.TYPE_AFTER_MAILING_48HOURS + ", " +
	            BirtReportType.TYPE_AFTER_MAILING_WEEK + " but was " + reportType);
		}
		
		report.setActiveTab(ReportSettingsType.MAILING.getKey()); // Used by UI only?
    	report.setLanguage(admin.getAdminLang());
        report.setHidden(true);
        
		report.setCompanyID(admin.getCompanyID());
    	report.setShortname(buildReportName(reportType, mailing, admin.getLocale()));
    	report.setReportActive(1);
    	report.setFormat(ComBirtReport.FORMAT_PDF_INDEX);
    	report.setEmailRecipientList(AgnUtils.splitAndTrimList(form.getReportSendEmail()));
    	report.setEmailSubject(buildReportEmailSubject(admin.getLocale()));
    	report.setEmailDescription(buildReportEmailBody(mailing, admin.getLocale()));

    	// Create report parameters
    	final ComBirtReportMailingSettings reportMailingSettings = report.getReportMailingSettings();
    	
    	List<BirtReportSettingsUtils.Properties> figures = new ArrayList<>();
    	figures.addAll(BirtReportSettingsUtils.MAILING_FORMATS_GROUP);
    	figures.addAll(BirtReportSettingsUtils.MAILING_OPENER_GROUP);
    	figures.addAll(BirtReportSettingsUtils.MAILING_GENERAL_GROUP);
    	figures.addAll(BirtReportSettingsUtils.MAILING_DEVICES_GROUP);
    	
    	figures.forEach(figure -> {
    		boolean isActive = figure != CONVERSION_RATE;
			reportMailingSettings.setReportSetting(figure.getPropName(), isActive);
		});
  
    	reportMailingSettings.setReportSetting(ComBirtReportSettings.ENABLED_KEY, true);
    	reportMailingSettings.setReportSetting(ComBirtReportSettings.MAILING_FILTER_KEY, FilterType.FILTER_MAILING.getKey());
		reportMailingSettings.setReportSetting(ComBirtReportMailingSettings.MAILING_GENERAL_TYPES_KEY, ComBirtReportMailingSettings.MAILING_NORMAL);
		reportMailingSettings.setReportSetting(ComBirtReportSettings.MAILING_TYPE_KEY, BirtReportSettingsUtils.MAILINGS_CUSTOM);
		reportMailingSettings.setReportSetting(ComBirtReportMailingSettings.PERIOD_TYPE_KEY, PeriodType.DATE_RANGE_WEEK.getKey());
		reportMailingSettings.setReportSetting(ComBirtReportSettings.PREDEFINED_MAILINGS_KEY, PredefinedType.PREDEFINED_LAST_ONE.getValue());

		reportMailingSettings.setReportSetting(ComBirtReportSettings.PREDEFINED_ID_KEY, mailing.getId());
		reportMailingSettings.setReportSetting(ComBirtReportSettings.MAILINGS_KEY, Integer.toString(mailing.getId()));
		reportMailingSettings.setReportSetting(ComBirtReportSettings.TARGETS_KEY, "");
		reportMailingSettings.setReportSetting(ComBirtReportSettings.SORT_MAILINGS_KEY, SORT_NAME);

    	birtReportService.insert(report);
    }
    
    private static String buildReportEmailSubject(final Locale locale) {
    	return I18nString.getLocaleString("report.after.mailing.emailSubject", locale);
    }
    
    private static String buildReportEmailBody(final Mailing mailing, final Locale locale) {
    	/*
    	 * Parameters for message key "report.after.mailing.emailBody":
    	 *
    	 *  {0} mailing shortname
    	 */
    	
    	return I18nString.getLocaleString("report.after.mailing.emailBody", locale, mailing.getShortname());
    }
    
    private static String buildReportName(BirtReportType reportType, final Mailing mailing, final Locale locale) {
    	/*
    	 * Parameters for message key "report.mailing.afterSending.title"
    	 *
    	 * {0} 	internationalized type of report (report.after.mailing.24hours, report.after.mailing.48hours, report.after.mailing.week)
    	 * {1}	mailing ID
    	 * {2} 	mailing shortname
    	 *
    	 *
    	 * Example:
    	 * 'Report for mailing "{2}" {0} after sending mailing'
    	 * 'Report für Mailing "{2}" {0} nach Mailversand'
    	 */
    	
    	final String reportTypeMessageKey = reportTypeToMessageKey(reportType);
    	final String reportTypeString = (reportTypeMessageKey != null) ? I18nString.getLocaleString(reportTypeMessageKey, locale) : "";
    	
    	return I18nString.getLocaleString("report.mailing.afterSending.title", locale, reportTypeString, mailing.getId(), mailing.getShortname());
    }

    private static String reportTypeToMessageKey(BirtReportType reportType) {
    	switch(reportType) {
    	case TYPE_AFTER_MAILING_24HOURS:
    		return "report.after.mailing.24hours";
    	
    	case TYPE_AFTER_MAILING_48HOURS:
    		return "report.after.mailing.48hours";
    	
    	case TYPE_AFTER_MAILING_WEEK:
    		return "report.after.mailing.week";
    		
    	default:
    		return null;
    	}
    }

	/**
	 * Disables mailing.
	 */
	protected void unlockMailing(ComMailingSendForm aForm, HttpServletRequest req) throws Exception {
		Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

		if (aMailing == null) {
			return;
		}

		aMailing.setLocked(0);

		mailingDao.saveMailing(aMailing, false);
		updateStatus(aForm, req, "ready");
	}
	
	@Override
	protected int getBulkCompanyId(HttpServletRequest req) {
		final Object bulkGenerate = req.getSession().getAttribute("bulkGenerate");
		if (bulkGenerate != null){
			String companyIdString = req.getParameter("previewCompanyId");
			return NumberUtils.toInt(companyIdString, -1);
		}
		return -1;
	}

    /**
     * Loads sending statistics.
     */
    protected boolean loadSendStats(ComMailingSendForm comForm, HttpServletRequest req) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);

        Boolean recipientStatIsDone = false;
        String key = FUTURE_TASK + "@" + req.getSession(false).getId();
        if (!futureHolder.containsKey(key)) {
            futureHolder.put(key, getMailingStatFuture(mailingDao, comForm.getMailingID(), companyId));
        }

        //if we perform AJAX request (load next/previous page) we have to wait for preparing data
        if (HttpUtils.isAjax(req)) {
            while (!futureHolder.containsKey(key) || !futureHolder.get(key).isDone()) {
                if (comForm.getRefreshMillis() < 1000) { // raise the refresh time
                    comForm.setRefreshMillis(comForm.getRefreshMillis() + 50);
                }
                Thread.sleep(comForm.getRefreshMillis());
            }
        }

        if (futureHolder.containsKey(key) && futureHolder.get(key) != null && futureHolder.get(key).isDone()) {
            recipientStatIsDone = true;
            @SuppressWarnings("unchecked")
			Map<Integer, Integer> map = (Map<Integer, Integer>) futureHolder.get(key).get();
            futureHolder.remove(key);

            int numText = map.get(ComMailingDao.SEND_STATS_TEXT);
            int numHtml = map.get(ComMailingDao.SEND_STATS_HTML);
            int numOffline = map.get(ComMailingDao.SEND_STATS_OFFLINE);
            int numTotal = numText + numHtml + numOffline;

            map.remove(ComMailingDao.SEND_STATS_TEXT);
            map.remove(ComMailingDao.SEND_STATS_HTML);
            map.remove(ComMailingDao.SEND_STATS_OFFLINE);
            for (Entry<Integer, Integer> entry : map.entrySet()) {
                comForm.setSendStat(entry.getKey(), entry.getValue());
            }
            comForm.setSendStatText(numText);
            comForm.setSendStatHtml(numHtml);
            comForm.setSendStatOffline(numOffline);
            comForm.setSendStat(0, numTotal);
        }
        return recipientStatIsDone;
    }
    
	protected void updateStatus(ComMailingSendForm aForm, HttpServletRequest req, String status) throws Exception {
        updateStatus(aForm.getMailingID(), AgnUtils.getCompanyID(req), status);
	}


	/**
	 * this method gets the amount of mailings for the given followup and sets the values
	 * @param aForm
	 * @param req
	 */
	protected void loadFollowUpStat(ComMailingSendForm aForm, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
		// if mailing-Type is 3, we have a followup.
		if (mailingDao.getMailingType(aForm.getMailingID()) == MailingTypes.FOLLOW_UP.getCode()) {
			String followUpFor = mailingDao.getFollowUpFor(aForm.getMailingID());

        	if (StringUtils.isNotEmpty(followUpFor)) {
        		int basemailingId = Integer.parseInt(followUpFor);
            	
            	boolean wasBaseMailingSentByNow = mailingDao.getLastSendDate(basemailingId) != null;
            	if (!wasBaseMailingSentByNow) {
            		messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.followup.basemail_was_not_sent"));
            	} else {
            	    ComAdmin admin = AgnUtils.getAdmin(req);
            	    List<Mailing> availableBaseMailings = mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(), ComMailingLightService.TAKE_ALL_MAILINGS, "W", true);
                	boolean isBaseMailingTrackingDataAvailable = false;
                	for (Mailing mailing : availableBaseMailings) {
                		if (mailing.getId() == basemailingId) {
                			isBaseMailingTrackingDataAvailable = true;
                			break;
                		}
                	}
                	if (!isBaseMailingTrackingDataAvailable) {
                		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.followup.basemail_data_not_exists"));
                	}
            	}
        	}
		}
	}

	// set the fields reportSendAfter24h,reportSendAfter48h & reportSendAfter1Week to 'true' when in the company_tbl the field 'auto_mailing_report_active' is set to '1'
	private void setAutomaticSendStates(ComMailingSendForm form, ComCompany company) {
		if( company.isAutoMailingReportSendActivated()) {
			form.setReportSendAfter24h(true);
			form.setReportSendAfter48h(true);
			form.setReportSendAfter1Week(true);
		}
	}


	/**
	 * This method returns the type of this mailing. The returnvalues can be
	 * "followup_clicker", "followup_nonclicker", "followup_opener", "followup_nonopener"
	 * the reason for that is, that this string is used directly as translation given in messages.properties.
	 * therefore this value must be unique and can not be FollowUpType.TYPE_FOLLOWUP_CLICKER or something
	 * like that.
	 *
	 * @param mailingID
	 * @return
	 */
	protected String getFollowUpType(int mailingID) {
		int mailingType = mailingDao.getMailingType(mailingID);
		String followUpType = mailingDao.getFollowUpType(mailingID);

		if (mailingType == MailingTypes.FOLLOW_UP.getCode()) {
//			String tmpString = aMailing.getFollowUpType();

			if (FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey().equals(followUpType)) {
				return "followup_clicker";
			} else if (FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey().equals(followUpType)) {
				return "followup_nonclicker";
			} else if (FollowUpType.TYPE_FOLLOWUP_OPENER.getKey().equals(followUpType)) {
				return "followup_opener";
			} else if (FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey().equals(followUpType)) {
				return "followup_nonopener";
			}
		}
		return "";
	}

	@Override
	protected void loadDeliveryStats(MailingSendForm form,
			HttpServletRequest req) throws Exception {

		int companyID = mailingDao.getCompanyIdForMailingId(form.getMailingID());
		int adminsCompanyID = AgnUtils.getCompanyID(req);

		if(companyID != adminsCompanyID ) {
			throw new NotYourMailingException("This is not your mailing !");
		}
		super.loadDeliveryStats(form, req);
        req.setAttribute("adminTimeZone", AgnUtils.getTimeZone(req));
        req.setAttribute("adminDateFormat", AgnUtils.getAdmin(req).getDateFormat().toPattern());
	}

    private Future<Map<Integer, Integer>> getMailingStatFuture(ComMailingDao mailingDaoAsParam, int mailingId, @VelocityCheck int companyId) {
        return workerExecutorService.submit(new MailingSendRecipientStatWorker(mailingDaoAsParam, mailingId, companyId));
    }
	
	protected void checkActivateDeeptracking(int mailingID, int companyID) {
		if (companyDao.checkDeeptrackingAutoActivate(companyID)) {
			trackableLinkDao.activateDeeptracking(companyID, mailingID);
		}
	}

    protected int getCustomerIdWithEmailInMailingList(@VelocityCheck int companyID, int mailinID, String email){
        return recipientDao.getCustomerIdWithEmailInMailingList(companyID, mailinID, email);
    }
    
    @Override
    protected void extendedChecks(ActionForm form, HttpServletRequest request, ActionMessages messages) throws Exception {
		// nothing to do
	}

	@Override
    protected void setSendButtonsControlAttributes(final HttpServletRequest request, final Mailing mailing) throws Exception {
    	request.setAttribute("CAN_SEND_WORLDMAILING", checkCanSendWorldMailing(request, mailing));
    	request.setAttribute("CAN_ENABLE_SEND_WORLDMAILING", checkCanEnableSendWorldMailing(request, mailing));
    }
    
	@Override
    protected boolean checkCanSendWorldMailing(final HttpServletRequest request, final Mailing mailing) throws Exception {
		if (super.checkCanSendWorldMailing(request, mailing)) {
			if (AgnUtils.allowed(request, Permission.MAILING_CAN_SEND_ALWAYS)) {
                return true;
            } else {
                return !checkIfMailingIsLocked(mailing);
            }
		}
		return false;
    }

	protected boolean checkCanEnableSendWorldMailing(final HttpServletRequest request, final Mailing mailing) {
		if (AgnUtils.allowed(request, Permission.MAILING_CAN_ALLOW)) {
            return checkIfMailingIsLocked(mailing);
		}
        return false;
    }

	protected boolean checkIfMailingIsLocked(final Mailing mailing) {
		return mailing == null || mailing.getLocked() != 0;
	}

	private List<String> getTestSingleRecipients(ComMailingSendForm form) {
		String[] addresses = form.getMailingTestRecipients();

		if (addresses != null) {
			// Exclude blank addresses — just ignore them; exclude repeating addresses.
			addresses = Arrays.stream(addresses).map(StringUtils::trimToNull).filter(Objects::nonNull)
				.distinct().toArray(String[]::new);

			form.setMailingTestRecipients(addresses);

			return Arrays.asList(addresses);
		}

		return Collections.emptyList();
	}

	private boolean validateTestRecipients(@VelocityCheck int companyId, List<String> addresses, ActionMessages errors) {
		boolean present = false;
		boolean valid = true;
		boolean allowed = true;

		for (String address : addresses) {
			if (AgnUtils.isEmailValid(address)) {
				if (blacklistService.blacklistCheckCompanyOnly(address, companyId)) {
					allowed = false;
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.email.blacklisted", address));
				}
			} else if (valid) {
				valid = false;
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.email"));
			}
			present = true;
		}

		if (!present) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("enterEmailAddresses"));
		}

		return present && valid && allowed;
	}
	
	private int isSteppingForced(@VelocityCheck int companyID) {
		if (configService.getBooleanValue(ConfigValue.ForceSteppingBlocksize, companyID)) {
			return configService.getIntegerValue(ConfigValue.DefaultBlocksizeValue, companyID);
		} else {
			return 0;
		}
	}
	
	private final boolean containsInvalidTargetGroups(final int companyID, final ComMailingSendForm form) {
		try {
			final List<TargetLight> targetGroups = this.mailingService.listTargetGroupsOfMailing(companyID, form.getMailingID());
			
			for(final TargetLight tl : targetGroups) {
				if(!tl.isValid()) {
					return true;
				}
			}
			
			return false;
		} catch(final MailingNotExistException e) {
			return false;
		}
	}

	private void setInputControlAttributes(final HttpServletRequest request, final int companyId) {
        request.setAttribute("isMailtrackExtended", 
                configService.getBooleanValue(ConfigValue.MailtrackExtended, companyId));
	}

    public void setAutoExportService(AutoExportService autoExportService) {
        this.autoExportService = autoExportService;
    }

    @Required
    public void setDkimDao(ComDkimDao dkimDao) {
        this.dkimDao = dkimDao;
    }

    @Required
    public void setMediatypesDao(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }

    @Required
    public void setTrackableLinkDao(ComTrackableLinkDao trackableLinkDao) {
        this.trackableLinkDao = trackableLinkDao;
    }

    @Required
    public void setOnepixelDao(OnepixelDao onepixelDao) {
        this.onepixelDao = onepixelDao;
    }

    @Required
    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
	public void setActionService(EmmActionService actionService) {
		this.actionService = actionService;
	}

	@Required
    public void setFutureHolder(Map<String, Future<?>> futureHolder) {
        this.futureHolder = futureHolder;
    }

    @Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}

	@Required
    public void setBirtReportService(final ComBirtReportService service) {
        this.birtReportService = service;
    }

    @Required
    public void setGenerationPDFService(GenerationPDFService generationPDFService) {
        this.generationPDFService = generationPDFService;
    }

    @Required
    public void setJavaMailService(JavaMailService javaMailService) {
        this.javaMailService = javaMailService;
    }

    @Required
	public void setGridService(GridServiceWrapper gridServiceWrapper) {
		this.gridService = gridServiceWrapper;
	}
	
	@Required
	public void setBirtReportFactory(BirtReportFactory birtReportFactory) {
		this.birtReportFactory = birtReportFactory;
	}
}
