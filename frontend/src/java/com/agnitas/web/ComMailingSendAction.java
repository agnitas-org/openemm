/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

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
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComDkimDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportImpl;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.PredefinedType;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.commons.TranslatableMessageException;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.ComFollowUpStatsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.mailing.web.NotYourMailingException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComMailingLightVO;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingSendRecipientStatWorker;
import com.agnitas.util.ClassicTemplateGenerator;
import com.agnitas.util.NumericUtil;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.Tuple;
import org.agnitas.web.MailingSendAction;
import org.agnitas.web.MailingSendForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

public class ComMailingSendAction extends MailingSendAction {
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
	private static final transient Logger logger = Logger.getLogger(ComMailingSendAction.class);

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

    protected ComFollowUpStatsService followUpStatsService;
    protected ComCompanyDao companyDao;
    protected ComTrackableLinkDao trackableLinkDao;
    protected OnepixelDao onepixelDao;
    protected Map<String, Future<?>> futureHolder;
    protected ExecutorService workerExecutorService;
    protected ComWorkflowService workflowService;
    protected ComDkimDao dkimDao;
    protected MediatypesDao mediatypesDao;
    protected ComMailingBaseService mailingBaseService;
    private GenerationPDFService generationPDFService;
    private ConfigService configService;
    private ComBirtReportService birtReportService;
    private AutoExportService autoExportService;
    private JavaMailService javaMailService;
    private GridServiceWrapper gridService;

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
    	final int companyId = AgnUtils.getCompanyID(request);
		// Validate the request parameters specified by the user
		ComMailingSendForm aForm = (ComMailingSendForm) form;
		ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (logger.isInfoEnabled()) {
			logger.info("Action: " + aForm.getAction());
		}
		
        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

		request.setAttribute("adminTargetGroupList", targetDao.getTestAndAdminTargetLights(companyId));
        request.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(AgnUtils.getAdmin(request), aForm.getMailingID()));

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
					aForm.setFollowUpType(getFollowUpType(aForm, request));
					destination = mapping.findForward("send");
					break;
				
				case MailingSendAction.ACTION_SEND_ADMIN:
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
					updateStatus(aForm, request, "disable");
					break;
				
				case ACTION_DEACTIVATE_INTERVALMAILING:
					updateStatus(aForm, request, "disable");
					loadMailing(aForm, request);
					aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					destination = mapping.findForward("send");
					break;
				
				case MailingSendAction.ACTION_ACTIVATE_RULEBASED:
				case MailingSendAction.ACTION_ACTIVATE_CAMPAIGN:
					loadMailing(aForm, request);
					
					if (validateActivation(request, aForm, errors)) {
						try {
							sendMailing(aForm, request, messages);
						} finally {
							loadMailing(aForm, request);
						}
					}
					loadDeliveryStats(aForm, request);
					aForm.setAction(MailingSendAction.ACTION_VIEW_SEND);
					destination = mapping.findForward("send");
					updateStatus(aForm, request, "active");
					break;
				
				case ACTION_ACTIVATE_INTERVALMAILING:
					loadMailing(aForm, request);
					if (aForm.getTargetGroups() == null) {
						errors.add("global", new ActionMessage("error.mailing.rulebased_without_target"));
					} else if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
						errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
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
				
				// Why the hell we don't have small methods we could overwrite ?
				// Why does anybody forces me to copy code ?
				case ACTION_CONFIRM_SEND_WORLD:
					if (!NumericUtil.matchedUnsignedIntegerPattern(aForm.getMaxRecipients())) {
						errors.add("global", new ActionMessage("error.maxRecipients.notNumeric"));
						destination = mapping.findForward("send2");
						request.setAttribute("adminDateFormat", AgnUtils.getAdmin(request).getDateFormat().toPattern());
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
							request.setAttribute("potentialSendDate", AgnUtils.getAdmin(request).getDateFormat().format(sendDate.getTime()));
							request.setAttribute("potentialSendTime", AgnUtils.getAdmin(request).getTimeFormat().format(sendDate.getTime()));
							request.setAttribute("adminDateFormat", AgnUtils.getAdmin(request).getDateFormat().toPattern());
						} else {
							errors.add("global", new ActionMessage("error.you_choose_a_time_before_the_current_time"));
							destination = mapping.findForward("send2");
						}
						
						if (mailingDao.getMailingType(aForm.getMailingID()) == Mailing.TYPE_FOLLOWUP) {
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
						int num_recipients;
						if (Integer.parseInt(aForm.getMaxRecipients()) <= 0) {
							num_recipients = aForm.getSendStatText() + aForm.getSendStatHtml() + aForm.getSendStatOffline();
						} else {
							num_recipients = Math.min(Integer.parseInt(aForm.getMaxRecipients()), aForm.getSendStatText() + aForm.getSendStatHtml() + aForm.getSendStatOffline());
						}
						final NumberFormat formatter = NumberFormat.getNumberInstance(AgnUtils.getAdmin(request).getLocale());
						request.setAttribute("num_recipients", formatter.format(num_recipients));
					}
					
					break;
				
				case MailingSendAction.ACTION_PREVIEW_SELECT:
					aForm.setFollowUpType(getFollowUpType(aForm, request));
					loadMailing(aForm, request);
					
					MailingPreviewHelper.updateActiveMailingPreviewFormat(aForm, request, companyId, mailingDao);
					
					Map<Integer, String> recipientList = putPreviewRecipientsInRequest(request, aForm.getMailingID(), companyId);
					if (hasPreviewRecipient(aForm, request)) {
						aForm.setHasPreviewRecipient(true);
						choosePreviewCustomerId(request, aForm, aForm.isUseCustomerEmail(), errors, recipientList);
					} else {
						aForm.setHasPreviewRecipient(false);
					}
					destination = aForm.isPreviewSelectPure() ? mapping.findForward("preview_select_pure") : mapping.findForward("preview_select");
					break;
				
				case ACTION_VIEW_SEND2:
					Boolean recipientStatIsDone = false;
					loadMailing(aForm, request);
					if (!validateNeedDkimKey(companyId, AgnUtils.getAdmin(request), aForm, messages, errors)) {
						destination = mapping.findForward("send");
					} else {
						validateNeedTarget(aForm, errors);
						
						//if (comForm.getFollowupFor() == null || comForm.getFollowupFor().equals("")) {
						destination = mapping.findForward("progress");
						recipientStatIsDone = loadSendStats(aForm, request);
						//                        } else {
						//                            int companyID = aForm.getCompanyID(req);
						//                            int followUpID = aForm.getMailingID();
						//                            int baseMailID = Integer.parseInt(aForm.getFollowupFor());
						//                            String sessionID = req.getSession().getId();
						//                            getFollowUpStatsService().startCalculation(followUpID, baseMailID, companyID, sessionID, true);
						//                            recipientStatIsDone = true;
						//                        }
						
						if (recipientStatIsDone) {
							loadMailing(aForm, request);
							setAutomaticSendStates(aForm, (ComCompany) AgnUtils.getCompany(request));
							aForm.setAction(MailingSendAction.ACTION_CONFIRM_SEND_WORLD);
							ComAdmin reportAdmin = AgnUtils.getAdmin(request);
							String reportEmail = reportAdmin.getStatEmail();
							if (reportEmail == null || reportEmail.isEmpty()) {
								reportEmail = reportAdmin.getEmail();
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
						
						if (HttpUtils.isAjax(request)) {
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
					String hostUrl = configService.getValue(ConfigValue.SystemUrl);
					String url = hostUrl + "/mailingsend.do;jsessionid=" + request.getSession().getId() +
							"?action=" + MailingSendAction.ACTION_PREVIEW + "&mailingID=" + aForm.getMailingID() +
							"&previewFormat=" + 1 /*html format*/ + "&previewSize=" + aForm.getPreviewSize() +
							"&previewCustomerID=" + aForm.getPreviewCustomerID() + "&noImages=" + aForm.isNoImages();
					LightweightMailing lightweightMailing = mailingDao.getLightweightMailing(aForm.getMailingID());
					aForm.setShortname(lightweightMailing.getShortname());
					boolean errorOccured = false;
					File pdfFile = generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, aForm.getShortname(), AgnUtils.getAdmin(request), "", "Portrait", "Mailing");
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
					aForm.setFollowUpType(getFollowUpType(aForm, request));
					return super.execute(mapping, form, request, response);
			}
		} catch (TranslatableMessageException e) {
			logger.error("MAILINGSENDACTION exception ", e);

			if ("error.mailing.send.admin.maxMails".equals(e.getErrorMsgKey())) {
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

    private boolean validateNeedDkimKey(int companyID, ComAdmin admin, ComMailingSendForm mailingSendForm, ActionMessages messages, ActionMessages errors) {
	    MediatypeEmail mediatypeEmail = mailingDao.getMailing(mailingSendForm.getMailingID(), companyID).getEmailParam();
	    String fromAddress = mediatypeEmail != null ? mediatypeEmail.getFromEmail() : "";
    	String senderDomain = AgnUtils.getDomainFromEmail(fromAddress);
        if (!dkimDao.existsDkimKeyForDomain(companyID, senderDomain)) {
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

	private boolean choosePreviewCustomerId(HttpServletRequest req, ComMailingSendForm aForm, boolean useCustomerEmail, ActionMessages errors, Map<Integer, String> recipientList) {
        String previewCustomerEmail = aForm.getPreviewCustomerEmail();
        if (useCustomerEmail && previewCustomerEmail != null && !previewCustomerEmail.isEmpty()) {
            int customerId = getCustomerIdWithEmailInMailingList(AgnUtils.getCompanyID(req), aForm.getMailingID(), previewCustomerEmail);
            if(customerId > 0){
                aForm.setPreviewCustomerID(customerId);
                aForm.setPreviewCustomerATID(0);
                return true;
            } else {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.error.previewCustomerEmail"));
                aForm.setPreviewCustomerEmail("");
            }
        } else if(aForm.getPreviewCustomerATID() != 0) {
            aForm.setPreviewCustomerID(aForm.getPreviewCustomerATID());
            return true;
        }
        return setMinCustomerId(aForm, recipientList);
    }

    private boolean setMinCustomerId(ComMailingSendForm aForm, Map<Integer, String> recipientList) {
        if (recipientList != null && !recipientList.isEmpty()) {
            int minId = Collections.min(recipientList.keySet());
            aForm.setPreviewCustomerID(minId);
            aForm.setPreviewCustomerATID(minId);
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

    @Override
    protected void cancelDependentMailings(int mailingID, HttpServletRequest req) {
        int companyID = AgnUtils.getCompanyID(req);
        List<Integer> followupMailings = mailingDao.getFollowupMailings(mailingID, companyID, false);
        for (Integer followupMailingID : followupMailings) {
            LightweightMailing followupMailing = mailingDao.getLightweightMailing(followupMailingID);
            boolean deliveryCancelled = cancelMailingDelivery(Mailing.TYPE_FOLLOWUP, followupMailingID, followupMailing.getShortname(), req);
            if(deliveryCancelled){
                mailingDao.updateStatus(followupMailingID, "canceled");
                cancelDependentMailings(followupMailingID, req);
            }
        }
    }

    /**
	 * load EMM specific properties into the form e.g. 'Followup-Mailing'
	 */
	@Override
    protected void loadMailing(MailingSendForm aForm, HttpServletRequest req) {
		super.loadMailing(aForm, req);

        final int companyId = AgnUtils.getCompanyID(req);
        ComMailingSendForm comForm = (ComMailingSendForm) aForm;
        ComMailing aMailing = (ComMailing) mailingDao.getMailing(aForm.getMailingID(), companyId);

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
		comForm.setSizeWarningThreshold(configService.getIntegerValue(ConfigValue.MailingSizeWarningThreshold) * 1024);
		comForm.setSizeErrorThreshold(configService.getIntegerValue(ConfigValue.MailingSizeErrorThreshold) * 1024);
		comForm.setStatusmailRecipients(aMailing.getStatusmailRecipients());

        loadAvailablePreviewFormats(comForm, aMailing);

		int workflowId = mailingBaseService.getWorkflowId(aMailing.getId(), companyId);
        comForm.setWorkflowId(workflowId);

		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		if (workflow != null) {
			req.setAttribute("workflowSendDate", workflow.getGeneralStartDate());
			req.setAttribute("adminTimeZone", AgnUtils.getTimeZone(req));
        }

		if (aForm.getMailingtype() == Mailing.TYPE_INTERVAL) {
			String workStatus = mailingDao.getWorkStatus(companyId, aForm.getMailingID());
			if (workStatus == null || !workStatus.equals("mailing.status.active")) {
				// only active or disable is allowed for interval mailings
				workStatus = "mailing.status.disable";
			}
			
			comForm.setWorkStatus(workStatus);
		}

        comForm.setIsTemplate(aMailing.isIsTemplate());

        int gridTemplateId = gridService.getGridTemplateIdByMailingId(aForm.getMailingID());
        comForm.setTemplateId(gridTemplateId);
        comForm.setMailingGrid(gridTemplateId > 0);

        // TODO: Restore mail generation optimization

        // For backward compatibility
        req.setAttribute("templateId", gridTemplateId);
        req.setAttribute("isMailingGrid", gridTemplateId > 0);

        comForm.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(aForm.getMailingID()));

        setSendButtonsControlAttributes(req, aMailing);
    }

    private void loadAvailablePreviewFormats(ComMailingSendForm form, Mailing mailing) {
        List<Integer> availablePreviewFormats = new ArrayList<>(mailing.getMediatypes().keySet());
        Collections.sort(availablePreviewFormats);
        form.setAvailablePreviewFormats(availablePreviewFormats);
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
		int startGen = MaildropEntry.GEN_NOW;
		ComMailingSendForm comForm = (ComMailingSendForm) aForm;
		MaildropEntry drop = new MaildropEntryImpl();

        switch (aForm.getAction()) {
            case MailingSendAction.ACTION_SEND_ADMIN:
                if (aForm.getMailingtype() == Mailing.TYPE_INTERVAL) {
                    drop.setId(0);
                    drop.setCompanyID(companyId);
                    drop.setMailingID(aForm.getMailingID());
                    drop.setStatus(MaildropStatus.ADMIN.getCode());
                    drop.setSendDate(new Date());
                    drop.setStepping(0);
                    drop.setBlocksize(0);
                    drop.setGenDate(new Date());
                    drop.setGenChangeDate(new Date());
                    drop.setGenStatus(1);
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
                if (aForm.getMailingtype() == Mailing.TYPE_INTERVAL) {
                    drop.setId(0);
                    drop.setCompanyID(companyId);
                    drop.setMailingID(aForm.getMailingID());
                    drop.setStatus(MaildropStatus.TEST.getCode());
                    drop.setSendDate(new Date());
                    drop.setStepping(0);
                    drop.setBlocksize(0);
                    drop.setGenDate(new Date());
                    drop.setGenChangeDate(new Date());
                    drop.setGenStatus(1);
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
			scheduleReport(comForm, req, aMailing);

            scheduleExportOfRecipientData(comForm, req, sendDate);
        }

		if (comForm.getGenerationOptimization() == MailGenerationOptimizationMode.NONE.code && (aMailing.getMailingType() == Mailing.TYPE_NORMAL || aMailing.getMailingType() == Mailing.TYPE_FOLLOWUP)) {
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

		MailinglistDao listDao = getMailinglistDao();
		Mailinglist aList = listDao.getMailinglist(aMailing.getMailinglistID(), companyId);
		maxAdminMails = companyDao.getMaxAdminMails(companyId);

		int numberOfRecipients = listDao.getNumberOfActiveSubscribers(admin, test, world, aMailing.getTargetID(), companyId, aList.getId());

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

		if (mailingDao.hasEmail(aMailing.getId())) {
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
			if (param.getMailFormat() >= ComMailing.INPUT_TYPE_HTML) {
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

        switch (aMailing.getMailingType()) {
            case Mailing.TYPE_NORMAL:
                if (world && DateUtil.isDateForImmediateGeneration(genDate) && isPrioritized(aMailing)) {
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;

            case Mailing.TYPE_DATEBASED:
                if (test) {
                    // Set genstatus equals 0 to trigger WM-specific test sending mode of backend for date-based mailings.
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;
            default: break;
        }

        if (!DateUtil.isDateForImmediateGeneration(genDate)) {
            switch (aMailing.getMailingType()) {
                case Mailing.TYPE_NORMAL:
                case Mailing.TYPE_FOLLOWUP:
                    startGen = MaildropEntry.GEN_SCHEDULED;
                    updateStatusByMaildrop(comForm, req, drop);
                    break;
                default: break;
            }
        }

		if (aMailing.getMailingType() != Mailing.TYPE_FOLLOWUP && world &&
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

		if (startGen == MaildropEntry.GEN_NOW
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

	private boolean isContentBlank(Mailing mailing, MailingComponent template) {
		return mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
	}

	private void triggerMaildrop(int maildropStatusId) {
		new MailoutClient().invoke("fire", Integer.toString(maildropStatusId));
	}

	private void selectTestRecipients(@VelocityCheck int companyId, int mailingListId, int maildropStatusId, List<String> addresses) {
		if (CollectionUtils.isNotEmpty(addresses)) {
			List<Integer> customerIds = recipientDao.insertTestRecipients(companyId, mailingListId, UserStatus.Suspend.getStatusCode(), addresses);
			maildropService.selectTestRecipients(companyId, maildropStatusId, customerIds);
		}
	}

    private void scheduleExportOfRecipientData(ComMailingSendForm aForm, HttpServletRequest req, Date sendDate) {
        if (autoExportService != null && isEnableMailingRecipientsReport(aForm)) {
            int autoExportId = aForm.getAutoExportId();
            int mailingId = aForm.getMailingID();
            ComAdmin admin = AgnUtils.getAdmin(req);
	
	        if(aForm.isRecipientReportSendSendingTime()){
                autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, null);
            }

            if(aForm.isRecipientReportSendAfter24h()){
                autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, DateUtils.addDays(sendDate, 1));
            }

            if(aForm.isRecipientReportSendAfter48h()){
                autoExportService.saveMailingAutoExport(admin, autoExportId, mailingId, DateUtils.addDays(sendDate, 2));
            }

            if(aForm.isRecipientReportSendAfter1Week()){
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
        switch(mailingType){
            case Mailing.TYPE_INTERVAL:
                return "interval";
            case Mailing.TYPE_FOLLOWUP:
                return "followup";
            default:
                return super.mailingTypeToString(mailingType);
        }
    }

    
    private void scheduleReport(final ComMailingSendForm form, final HttpServletRequest req, final Mailing mailing) {
    	// Reports only, if sending world mailings
    	if (form.getAction() == MailingSendAction.ACTION_SEND_WORLD) {
    		if (form.isReportSendAfter24h()) {
    			scheduleNewReport(form, req, mailing, BirtReportType.TYPE_AFTER_MAILING_24HOURS);
    		}
    		
    		if (form.isReportSendAfter48h()) {
    			scheduleNewReport(form, req, mailing, BirtReportType.TYPE_AFTER_MAILING_48HOURS);
    		}

    		if (form.isReportSendAfter1Week()) {
    			scheduleNewReport(form, req, mailing, BirtReportType.TYPE_AFTER_MAILING_WEEK);
    		}
    	}
    }
    
    private void scheduleNewReport(final ComMailingSendForm form, final HttpServletRequest req, final Mailing mailing, final BirtReportType reportType) {
    	final ComAdmin admin = AgnUtils.getAdmin(req);

    	// Create report parameters
    	final ComBirtReportMailingSettings reportMailingSettings = new ComBirtReportMailingSettings();
    	reportMailingSettings.setReportSetting("activateLinkStatistics", true);
    	reportMailingSettings.setReportSetting("clickersAfterDevice", true);
    	reportMailingSettings.setReportSetting("clickingRecipients", true);
    	reportMailingSettings.setReportSetting("clickingAnonymous", true);
    	reportMailingSettings.setReportSetting(ComBirtReportMailingSettings.ENABLED_KEY, true);
    	reportMailingSettings.setReportSetting("hardbounces", true);
    	reportMailingSettings.setReportSetting("html", true);
    	reportMailingSettings.setReportSetting("mailingFilter", BirtReportSettingsUtils.FILTER_MAILING_VALUE);
    	reportMailingSettings.setReportSetting("mailingGeneralType", ComBirtReportMailingSettings.MAILING_NORMAL);
    	reportMailingSettings.setReportSetting("mailingType", BirtReportSettingsUtils.MAILINGS_CUSTOM);			// TODO: Check parameter value
    	reportMailingSettings.setReportSetting("offlineHtml", true);
    	reportMailingSettings.setReportSetting("openersAfterDevice", true);
    	reportMailingSettings.setReportSetting("openersInvisible", true);
    	reportMailingSettings.setReportSetting("openersMeasured", true);
    	reportMailingSettings.setReportSetting("openersTotal", true);
		reportMailingSettings.setReportSetting("openingsAnonymous", true);
    	reportMailingSettings.setReportSetting("periodType", PeriodType.DATE_RANGE_WEEK.getKey());
    	reportMailingSettings.setReportSetting("predefinedMailings", PredefinedType.PREDEFINED_LAST_ONE.getValue());
    	reportMailingSettings.setReportSetting(ComBirtReportMailingSettings.PREDEFINED_ID_KEY, mailing.getId());
    	reportMailingSettings.setReportSetting("selectedMailings", Integer.toString(mailing.getId()));		// Overwritten by com.agnitas.emm.core.birtreport.service.impl.ComBirtReportServiceImpl.checkReportToSend(ComBirtReport)
    	reportMailingSettings.setReportSetting("selectedTargets", "");
    	reportMailingSettings.setReportSetting("signedOff", true);
    	reportMailingSettings.setReportSetting("softBounces", true);
    	reportMailingSettings.setReportSetting("sortMailings", "name");
    	reportMailingSettings.setReportSetting("text", true);
    	
    	
    	// Create the report itself
    	final ComBirtReportImpl report = new ComBirtReportImpl();
    	report.setCompanyID(admin.getCompanyID());
    	report.setShortname(buildReportName(reportType, mailing, admin.getLocale()));
    	report.setReportActive(1);
    	report.setReportType(reportType.getKey());
    	report.setSendDays(0);
    	report.setFormat(ComBirtReport.FORMAT_PDF_INDEX);
    	report.setSendEmail(form.getReportSendEmail());
    	report.setEmailSubject(buildReportEmailSubject(admin.getLocale()));
    	report.setEmailDescription(buildReportEmailBody(mailing, admin.getLocale()));
    	// TODO: "send_time" not used?
    	// "send_date" not used by report types 6, 7 and 8
    	// "creation_date" is set by DB
    	// "admin_id" is not longer used
    	// "activation_date" is set by DAO when saving activated report
    	// "end_date" not used by report types 6, 7 and 8
    	report.setActiveTab(2);									// Used by UI only?
    	report.setLanguage(admin.getAdminLang());
    	report.setReportMailingSettings(reportMailingSettings);
        report.setHidden(true);
    	
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
	protected ActionForward getPreview(ActionMapping mapping, MailingSendForm aForm, HttpServletRequest req) throws Exception {
        int companyId = AgnUtils.getCompanyID(req);
        try {
            final Object bulkGenerate = req.getSession().getAttribute("bulkGenerate");
            if (bulkGenerate != null){
                String companyIdString = req.getParameter("previewCompanyId");
                companyId = Integer.valueOf(companyIdString);
            }
        } catch (Exception e) {
            // do nothing, will take company id from action
        }
		Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), companyId);
		if (aMailing == null) {
			return mapping.findForward("preview." + aForm.getPreviewFormat());
		}
		
		String[] tmplNames = { "Text", "Html", "FAX", "PRINT", "MMS", "SMS"};
		if (aForm.getPreviewFormat() == Mailing.INPUT_TYPE_HTML || aForm.getPreviewFormat() == Mailing.INPUT_TYPE_TEXT) {
			Preview preview = previewFactory.createPreview();
    		Page output = preview.makePreview(aMailing.getId(), aForm.getPreviewCustomerID(), false);
    		preview.done();

			if (aForm.getPreviewFormat() == Mailing.INPUT_TYPE_HTML ) {
                String previewAsString = aForm.isNoImages() ? output.getStrippedHTML() : output.getHTML();

				if (previewAsString == null) {

					String htmlTemplate = aMailing.getHtmlTemplate().getEmmBlock();

					Map<String, DynamicTag> dynTagsMap = aMailing.getDynTags();
					int mailingID = aForm.getMailingID();

					analyzePreviewError(htmlTemplate, dynTagsMap, mailingID, req);
				}
				else {
                    previewAsString = replaceImagesWithMobileComponents(aMailing.getComponents(), aForm.getPreviewSize(), previewAsString);
                    aForm.setPreview( previewAsString );
				}
			} else if (aForm.getPreviewFormat() == Mailing.INPUT_TYPE_TEXT ) {
                String previewString = output.getText();
				if( previewString == null ) {
					String htmlTemplate = aMailing.getTextTemplate().getEmmBlock();

					Map<String, DynamicTag> dynTagsMap = aMailing.getDynTags();
					int mailingID = aForm.getMailingID();

					analyzePreviewError(htmlTemplate, dynTagsMap, mailingID, req);
				}
				else {

				if( previewString.indexOf("<pre>") > -1  ) {
					previewString = previewString.substring( previewString.indexOf("<pre>") + 5, previewString.length() );
				}
				if(previewString.lastIndexOf("</pre>") > -1) {
					previewString = previewString.substring(0,previewString.lastIndexOf("</pre>"));
				}
				aForm.setPreview( previewString );
				}
			}
			
			aForm.setEmailFormat(aMailing.getEmailParam().getMailFormat());
			aForm.setMailinglistID(aMailing.getMailinglistID());
		} else {
			aForm.setPreview(aMailing.getPreview(aMailing.getTemplate(
				tmplNames[aForm.getPreviewFormat()]).getEmmBlock(), aForm
				.getPreviewFormat(), aForm.getPreviewCustomerID(), true,
				applicationContext));
			aForm.setEmailFormat(aMailing.getEmailParam().getMailFormat());
			aForm.setMailinglistID(aMailing.getMailinglistID());
		}
        return mapping.findForward("preview." + aForm.getPreviewFormat());
	}

    /**
     * Replacing the standard images with mobile images if present
     * @param components - mailing dynamic components
     * @param previewSize - size of preview choosen by user
     *@param previewAsString - mailing preview  @return - resulting mailing preview.
     */
    private String replaceImagesWithMobileComponents(Map<String, MailingComponent> components, int previewSize, String previewAsString) {
        if (previewSize == Preview.Size.MOBILE_PORTRAIT.getValue() ||
                previewSize == Preview.Size.MOBILE_LANDSCAPE.getValue()) {
            final Set<Map.Entry<String, MailingComponent>> componentEntries = components.entrySet();
            for (Map.Entry<String, MailingComponent> component : componentEntries) {
                int componentType = component.getValue().getType();
                if (componentType == MailingComponent.TYPE_HOSTED_IMAGE ||
                        componentType == MailingComponent.TYPE_IMAGE) {
                    final String componentName = component.getKey();
                    final String replacementName = ShowImageServlet.MOBILE_IMAGE_PREFIX + componentName;
                    final MailingComponent replacementComponent = components.get(replacementName);
                    if (replacementComponent != null &&
                            (replacementComponent.getType() == MailingComponent.TYPE_HOSTED_IMAGE || replacementComponent.getType() == MailingComponent.TYPE_IMAGE)){
                        previewAsString = previewAsString.replaceAll(componentName, replacementName);
                    }
                }
            }
        }
        return previewAsString;
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
		Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

		if (aMailing == null) {
			return;
		}
		mailingDao.updateStatus(aMailing.getId(), status);
	}


	/**
	 * this method gets the amount of mailings for the given followup and sets the values
	 * @param aForm
	 * @param req
	 */
	protected void loadFollowUpStat(ComMailingSendForm aForm, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
		// if mailing-Type is 3, we have a followup.
		if (mailingDao.getMailingType(aForm.getMailingID()) == Mailing.TYPE_FOLLOWUP) {
			String followUpFor = mailingDao.getFollowUpFor(aForm.getMailingID());

        	if (StringUtils.isNotEmpty(followUpFor)) {
        		int basemailingId = Integer.parseInt(followUpFor);
            	
            	boolean wasBaseMailingSentByNow = mailingDao.getLastSendDate(basemailingId) != null;
            	if (!wasBaseMailingSentByNow) {
            		messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.followup.basemail_was_not_sent"));
            	} else {
            		List<ComMailing> availableBaseMailings = mailingDao.getMailings(AgnUtils.getCompanyID(req), ComMailingLightVO.TAKE_ALL_SNOWFLAKE_MAILINGS, "W", true);
                	boolean isBaseMailingTrackingDataAvailable = false;
                	for (ComMailing mailing : availableBaseMailings) {
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
	 * therefore this value must be unique and can not be Mailing.TYPE_FOLLOWUP_CLICKER or something
	 * like that.
	 * @param aForm
	 * @param req
	 * @return
	 */
	protected String getFollowUpType(ComMailingSendForm aForm, HttpServletRequest req) {
		int mailingID = aForm.getMailingID();
		int mailingType = mailingDao.getMailingType(aForm.getMailingID());
		String followUpType = mailingDao.getFollowUpType(mailingID);

		if (mailingType == Mailing.TYPE_FOLLOWUP ) {
//			String tmpString = aMailing.getFollowUpType();

			if (Mailing.TYPE_FOLLOWUP_CLICKER.equals(followUpType)) {
				return "followup_clicker";
			} else if (Mailing.TYPE_FOLLOWUP_NON_CLICKER.equals(followUpType)) {
				return "followup_nonclicker";
			} else if (Mailing.TYPE_FOLLOWUP_OPENER.equals(followUpType)) {
				return "followup_opener";
			} else if (Mailing.TYPE_FOLLOWUP_NON_OPENER.equals(followUpType)) {
				return "followup_nonopener";
			}
		}
		return "";
	}

	@Override
	protected void loadDeliveryStats(MailingSendForm form,
			HttpServletRequest req) throws Exception {

		int mailingID = form.getMailingID();

		int companyID = mailingDao.getCompanyIdForMailingId(mailingID);

		int adminsCompanyID = AgnUtils.getCompanyID(req);

		if(companyID != adminsCompanyID ) {
			throw new NotYourMailingException("This is not your mailing !");
		}
		super.loadDeliveryStats(form, req);
        req.setAttribute("adminTimeZone", TimeZone.getTimeZone(AgnUtils.getAdmin(req).getAdminTimezone()));
        req.setAttribute("adminDateFormat", AgnUtils.getAdmin(req).getDateFormat().toPattern());
	}

    private Future<Map<Integer, Integer>> getMailingStatFuture(ComMailingDao mailingDao, int mailingId, @VelocityCheck int companyId) {
        return workerExecutorService.submit(new MailingSendRecipientStatWorker(mailingDao, mailingId, companyId));
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
    protected void setSendButtonsControlAttributes(final HttpServletRequest request, final Mailing mailing) {
    	request.setAttribute("CAN_SEND_WORLDMAILING", checkCanSendWorldMailing(request, mailing));
    	request.setAttribute("CAN_ENABLE_SEND_WORLDMAILING", checkCanEnableSendWorldMailing(request, mailing));
    }

    protected boolean validateActivation(HttpServletRequest req, MailingSendForm form, ActionMessages errors) {
        if (WorkflowParametersHelper.isWorkflowDriven(req) &&
                (form.getMailingtype() == Mailing.TYPE_DATEBASED || form.getMailingtype() == Mailing.TYPE_ACTIONBASED)) {
            return false;
        }
        
        return validateNeedTarget(req, form, errors);
    }
    
    protected boolean validateNeedTarget(HttpServletRequest req, MailingSendForm form, ActionMessages errors) {
        if (form.getTargetGroups() == null && form.getMailingtype() == Mailing.TYPE_DATEBASED) {
			errors.add("global", new ActionMessage("error.mailing.rulebased_without_target"));
			return false;
		}
		
		if (AgnUtils.getCompanyID(req) == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
			errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
			return false;
		}
		
		return true;
    }
    
	@Override
    protected boolean checkCanSendWorldMailing(final HttpServletRequest request, final Mailing mailing) {
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
    public void setFollowUpStatsService(ComFollowUpStatsService followUpStatsService) {
        this.followUpStatsService = followUpStatsService;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
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
    public void setFutureHolder(Map<String, Future<?>> futureHolder) {
        this.futureHolder = futureHolder;
    }

    @Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

    @Required
    public void setConfigService(final ConfigService service) {
        this.configService = service;
    }

    @Required
    public void setBirtReportService(final ComBirtReportService service) {
        this.birtReportService = service;
    }

	@Override
	@Required
	public final void setMaildropStatusDao(final MaildropStatusDao dao) {
		this.maildropStatusDao = dao;
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
}
