/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.Constants;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;
import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.TAGCheck;
import org.agnitas.service.ImportResult;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.MailingImporter;
import org.agnitas.service.MailingsQueryWorker;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DynTagException;
import org.agnitas.util.FileUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MissingEndTagException;
import org.agnitas.util.SafeString;
import org.agnitas.util.UnclosedTagException;
import org.agnitas.web.MailingBaseAction;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.ComMailingImpl;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.IntervalType;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.core.workflow.web.ComWorkflowAction;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.emm.grid.grid.service.MailingCreationOptions;
import com.agnitas.service.ComMailingLightVO;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.forms.ComMailingBaseForm;

import net.sf.json.JSONObject;

/**
 * Implementation of <strong>Action</strong> that handles Mailings
 */
public class ComMailingBaseAction extends MailingBaseAction {
    /** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingBaseAction.class);
	
    public static final int ACTION_VIEW_FAX_BG = ACTION_MAILING_BASE_LAST + 1;

	public static final int ACTION_SAVE_FAX_BG = ACTION_MAILING_BASE_LAST + 2;

	public static final int ACTION_VIEW_PRINT_BG = ACTION_MAILING_BASE_LAST + 3;

	public static final int ACTION_SAVE_PRINT_BG = ACTION_MAILING_BASE_LAST + 4;

	public static final int ACTION_CREATE_FOLLOW_UP = ACTION_MAILING_BASE_LAST + 5;

	public static final int ACTION_CONFIRM_UNDO = ACTION_MAILING_BASE_LAST + 6;

	public static final int ACTION_UNDO = ACTION_MAILING_BASE_LAST + 7;

	public static final int ACTION_BULK_CONFIRM_DELETE = ACTION_LAST + 8;

	public static final int ACTION_BULK_DELETE = ACTION_LAST + 9;

	public static final int ACTION_MOVE_MEDIA_UP = ACTION_MAILING_BASE_LAST + 8;

	public static final int ACTION_MOVE_MEDIA_DOWN = ACTION_MAILING_BASE_LAST + 9;

	public static final int ACTION_STATUS_CHECK = ACTION_MAILING_BASE_LAST + 10;

	public static final int ACTION_RECIPIENTS_CALCULATE = ACTION_MAILING_BASE_LAST + 11;

    public static final int ACTION_NEW_MAILING_GRID = ACTION_MAILING_BASE_LAST + 12;

    public static final int ACTION_GENERATE_MAILING = ACTION_MAILING_BASE_LAST + 13;

    public static final int ACTION_SAVE_MAILING_GRID = ACTION_MAILING_BASE_LAST + 14;

    public static final int ACTION_MAILING_TEMPLATES = ACTION_MAILING_BASE_LAST + 15;

    public static final int ACTION_MAILING_EXPORT = ACTION_MAILING_BASE_LAST + 16;

    public static final int ACTION_MAILING_IMPORT = ACTION_MAILING_BASE_LAST + 17;
    
    public static final int ACTION_IS_ADVERTISING_CONTENT_TYPE = ACTION_MAILING_BASE_LAST + 18;

	public static final int MAILING_PREVIEW_WIDTH = 300;

	public static final int MAILING_PREVIEW_HEIGHT = 300;

    public static final String MAILING_ID = "mailingID";
    public static final String CAMPAIGN_ID = "campaignID";

    private static final String[] RESERVED_PARAMETER_NAMES = new String[]{
            ComMailingParameterDao.PARAMETERNAME_INTERVAL,
            ComMailingParameterDao.PARAMETERNAME_ERROR,
            ComMailingParameterDao.PARAMETERNAME_NEXT_START
    };
    
    private static final String ACTIVE_MAILING_STATUS = "mailing.status.active";

	private ComMailingLightVO mailingLightService;

	private ComMailingBaseService mailingBaseService;

	private DynamicTagDao dynamicTagDao;

	protected PreviewImageService previewImageService;

	private ComMailingParameterService mailingParameterService;

    private ComWorkflowService workflowService;

    private ComMailingDeliveryStatService deliveryStatService;

    private LinkService linkService;

    private MaildropService maildropService;

    private ConversionService conversionService;
    
    private CopyMailingService copyMailingService;

    protected DynamicTagContentFactory dynamicTagContentFactory;

	protected MailingExporter mailingExporter;
    
	protected MailingImporter mailingImporter;

	protected GridServiceWrapper gridService;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_NEW_MAILING_GRID:
            return "new_mailing_grid";
        case ACTION_GENERATE_MAILING:
            return "generate_mailing";
        case ACTION_SAVE_MAILING_GRID:
            return "save_mailing_grid";
        case ACTION_CREATE_FOLLOW_UP:
            return "create_follow_up";
        case ACTION_VIEW_WITHOUT_LOAD:
            return "view_without_load";

        case ACTION_CONFIRM_UNDO:
            return "confirm_undo";
        case ACTION_UNDO:
            return "undo";
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
        case ACTION_MOVE_MEDIA_UP:
            return "move_media_up";

        case ACTION_MOVE_MEDIA_DOWN:
            return "move_media_down";
        case ACTION_RECIPIENTS_CALCULATE:
            return "recipients_calculate";
        case ACTION_CLONE_AS_MAILING:
            return "clone_as_mailing";
        case ACTION_REMOVE_TARGET:
            return "remove_target";
        case ACTION_SELECT_TEMPLATE:
            return "select_template";

        case ACTION_USED_ACTIONS:
            return "used_actions";

        case ACTION_MAILING_TEMPLATES:
            return "mailing_templates";

        case ACTION_MAILING_EXPORT:
            return "mailing_export";
        case ACTION_MAILING_IMPORT:
            return "mailing_import";
            
        case ACTION_IS_ADVERTISING_CONTENT_TYPE:
        	return "is_advertising_content_type";

        default:
            return super.subActionMethodName(subAction);
        }
    }

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ComAdmin admin = AgnUtils.getAdmin(request);

		// Validate the request parameters specified by the user
		ComMailingBaseForm mailingBaseForm = (ComMailingBaseForm) form;
		ActionMessages errors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (logger.isInfoEnabled()) {
			logger.info("execute: action " + mailingBaseForm.getAction());
		}

        HttpSession session = request.getSession();

		// check if mailtracking is active
		int companyId = AgnUtils.getCompanyID(request);
		boolean mailtracking = mailingLightService.isMailtrackingActive(companyId);
        session.setAttribute("mailtracking", Boolean.toString(mailtracking));

        ComGridTemplate gridTemplate;
        
		try {
			Integer forwardTargetItemId;
			String cloneForwardParams;

			switch (mailingBaseForm.getAction()) {
                case ACTION_MAILING_TEMPLATES:
                    request.setAttribute("localeDatePattern", AgnUtils.getDateFormatPattern(DateFormat.MEDIUM, admin));
                    String sort = getSort(request, mailingBaseForm, true);
                 	String direction = mailingBaseForm.getDir();
                    mailingBaseForm.setTemplateMailingBases(mailingDao.getMailingTemplatesWithPreview(companyId, sort, direction));

                    destination = mapping.findForward("mailing_templates");

                    ComWorkflowAction.updateForwardParameters(request);
                    setMailingWorkflowParameters(request, mailingBaseForm);
				    break;

                case ACTION_NEW:
                    List<Mailinglist> mlists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request));

                    if (mlists.size() > 0) {
                        mailingBaseForm.setAction(ACTION_SAVE);
                        mailingBaseForm.clearData();

                        //populate mailing data with info from workflow
                        ComWorkflowAction.updateForwardParameters(request);

                        mailingBaseForm.setMailingID(0);
                        mailingBaseForm.setGridTemplateId(0);
                        Map<String, String> map = AgnUtils.getReqParameters(request);
                        String templateIDString = map.get("templateID");
                        if (templateIDString != null && !templateIDString.isEmpty()) {
                            mailingBaseForm.setTemplateID(Integer.parseInt(templateIDString));
                            loadTemplateSettings(mailingBaseForm, request, true);
                        }
                        setMailingWorkflowParameters(request, mailingBaseForm);
                        String campaignIDString = map.get("campaignID");
                        if (campaignIDString != null && !campaignIDString.isEmpty()) {
                            mailingBaseForm.setCampaignID(Integer.parseInt(campaignIDString));
                        }
                        mailingBaseForm.getMediaEmail().setCompanyID(companyId);

                        destination = mapping.findForward("view");
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.noMailinglist"));
                        mailingBaseForm.setAction(ACTION_LIST);
                    }
					break;

                case ACTION_NEW_MAILING_GRID:
                    destination = createNewMailingGrid(mapping, mailingBaseForm, request, response, errors);
                    break;

                case ACTION_GENERATE_MAILING:
                    mlists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request));
                    gridTemplate = gridService.getGridTemplate(companyId, mailingBaseForm.getGridTemplateId());
                    if (gridTemplate != null && CollectionUtils.isNotEmpty(mlists)) {
                        MailingCreationOptions options = MailingCreationOptions.builder()
                                .setMailingListId( mlists.get(0).getId())
                                .setCreatedFromReleased(true)
                                .setAlwaysCreateNew(false)
                                .build();
                    	Mailing mailing = gridService.createGridMailing(AgnUtils.getAdmin(request), gridTemplate.getId(), options);
                        mailingBaseForm.setMailingID(mailing.getId());
                        previewImageService.generateMailingPreview(admin, request.getSession(false).getId(), mailing.getId(), true);
                        saveMailingData(mailingBaseForm, request, errors, messages);
                        mailingBaseForm.setAction(ACTION_SAVE);
                        resetShowTemplate(request, mailingBaseForm);
                        loadMailing(mailingBaseForm, request);
                        mailingBaseForm.setMailingGrid(true);
                        destination = mapping.findForward("view");
                    }
                    break;

                case ACTION_SAVE_MAILING_GRID:
                    ComWorkflowAction.updateForwardParameters(request);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    if (saveGridMailing(mailingBaseForm, request, errors, messages)) {
                        mailingBaseForm.setAction(ACTION_SAVE);
                        
                        ActionRedirect redirect = new ActionRedirect(mapping.findForward("mailing_grid_view"));
                        redirect.addParameter("isMailingGrid", mailingBaseForm.isIsMailingGrid());
                        redirect.addParameter("keepForward", BooleanUtils.toBoolean(request.getParameter(WorkflowParametersHelper.WORKFLOW_KEEP_FORWARD)));
                        redirect.addParameter("mailingID", mailingBaseForm.getMailingID());
                        redirect.addParameter("templateId", mailingBaseForm.getGridTemplateId());

                        destination = redirect;
                    } else {
                        mailingBaseForm.setAction(ACTION_SAVE_MAILING_GRID);

                        destination = mapping.findForward("view");
                    }
                    mailingBaseForm.setWorkflowForwardParams(cloneForwardParams);
                    break;

				case ACTION_CREATE_FOLLOW_UP:
                    ComWorkflowAction.updateForwardParameters(request);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    forwardTargetItemId = (Integer) session.getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
                    if (forwardTargetItemId != null && forwardTargetItemId != 0) {
                    	mailingBaseForm.setMailingID(forwardTargetItemId);
                    }

                    loadMailingDataForFollowUpMailing(mailingBaseForm, request);

                    mailingBaseForm.setWorkflowForwardParams(cloneForwardParams);

                    if (mailingBaseForm.isIsMailingGrid()) {
                        mailingBaseForm.setAction(ACTION_SAVE_MAILING_GRID);
                        mailingBaseForm.setMailingEditable(true);
                    } else {
                        mailingBaseForm.setAction(ACTION_SAVE);
                    }

                    destination = mapping.findForward("view");
                    break;

				case ACTION_VIEW:
				    boolean showTagWarnings = false;
                    if (request.getParameter("checkMailingTags") != null) {
                        showTagWarnings = BooleanUtils.toBoolean(request.getParameter("checkMailingTags"));
                    }
        
					prepareMailingView(request, mailingBaseForm, session, showTagWarnings, errors);
					destination = mapping.findForward("view");
                    writeUserActivityLog(AgnUtils.getAdmin(request), "view " + (mailingBaseForm.isIsTemplate() ? "template" : "mailing"),
                            mailingBaseForm.getShortname() + " (" + mailingBaseForm.getMailingID() + ")");
                    writeUserActivityLog(AgnUtils.getAdmin(request), "view mailing", mailingBaseForm.getShortname() + " (" + mailingBaseForm.getMailingID() + ")" + " active tab - settings");
                    break;

				case ACTION_VIEW_WITHOUT_LOAD:
                    if (mailingBaseForm.isIsMailingGrid() && mailingBaseForm.getMailingID() == 0) {
                        mailingBaseForm.setAction(ACTION_SAVE_MAILING_GRID);
                        destination = mapping.findForward("grid_base");
                    } else {
                        mailingBaseForm.setAction(ACTION_SAVE);
                        destination = mapping.findForward("view");
                    }
                    break;

				case ACTION_SAVE:
                    if (isMediaTypesPresent(mailingBaseForm, errors)) {
                        if (isMailingEditable(mailingBaseForm, request)) {
                            if (validatePlanDate(mailingBaseForm, request, errors)) {
                                validateNeedTarget(mailingBaseForm, request, errors, messages);

                                if (mailingBaseForm.getMailingType() == Mailing.TYPE_FOLLOWUP) {
                                    validateFollowUpBaseMailing(mailingBaseForm, request, errors, messages);
                                }

                                try {
                                    validateMailingMod(mailingBaseForm, request);
                                } catch (CharacterEncodingValidationExceptionMod e) {
                                    if (e.getSubjectErrors().size() > 0) {
                                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.subject"));
                                    }
                                    for (EncodingError mailingComponent : e.getFailedMailingComponents()) {
                                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.component", mailingComponent.getStrWithError(), mailingComponent.getLine()));
                                    }
                                    for (EncodingError dynTag : e.getFailedDynamicTags()) {
                                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.content", dynTag.getStrWithError(), dynTag.getLine()));
                                    }
                                }

                                saveMailing(mailingBaseForm, request, errors, messages);
                            }
                        } else {
                            if (checkFormContainsAlwaysAllowedChangesOnly(mailingBaseForm, request)) {
                                String shortname = mailingBaseForm.getShortname();
                                String description = mailingBaseForm.getDescription();
                                int campaignId = mailingBaseForm.getCampaignID();
                                boolean isArchived = mailingBaseForm.isArchived();

                                loadMailing(mailingBaseForm, request);

                                mailingBaseForm.setShortname(shortname);
                                mailingBaseForm.setDescription(description);
                                mailingBaseForm.setCampaignID(campaignId);
                                mailingBaseForm.setArchived(isArchived);

                                saveMailing(mailingBaseForm, request, errors, messages);
                            } else {
                                loadMailing(mailingBaseForm, request);
                                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
                            }
                        }
            		}

                    destination = mapping.findForward("view");
					break;

				case ACTION_CONFIRM_DELETE:
					mailingBaseForm.setAction(ACTION_DELETE);
                    boolean usedInActiveWorkflow = mailingDao.usedInRunningWorkflow(mailingBaseForm.getMailingID(), companyId);
                    mailingBaseForm.setUsedInActiveWorkflow(usedInActiveWorkflow);
                    if (mailingDao.getFollowupMailings(mailingBaseForm.getMailingID(), companyId, true).size() > 0){
                        messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.delete.followup.target"));
                    }
                    if (usedInActiveWorkflow) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.workflow.mailingUsedInActiveWorkflow"));
                    }
                    loadMailing(mailingBaseForm, request);
					destination = mapping.findForward("delete");
					break;

				case ACTION_CONFIRM_UNDO:
					if (mailingBaseForm.getMailingID() > 0 && mailingBaseService.checkUndoAvailable(mailingBaseForm.getMailingID())) {
						mailingBaseForm.setAction(ACTION_UNDO);
						loadMailing(mailingBaseForm, request);
					} else {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.undo_error"));
						// Respond with an error messages only
						request.setAttribute("excludeDialog", true);
					}
					destination = mapping.findForward("undo_confirm");
					break;

				case ACTION_UNDO:
					if (mailingBaseForm.getMailingID() > 0 && mailingBaseService.checkUndoAvailable(mailingBaseForm.getMailingID())) {
						try {
							restoreUndo(mailingBaseForm, request);
							messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
						} catch (Exception e) {
							logger.error("Unable to undo: " + e.getMessage(), e);
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.undo_error"));
						}
					} else {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.undo_error"));
					}

					// Respond with an error/success messages only
					request.setAttribute("excludeDialog", true);

					destination = mapping.findForward("undo_confirm");
					break;

				case ACTION_BULK_CONFIRM_DELETE:
					if (mailingBaseForm.getBulkIds().size() == 0) {
						if (mailingBaseForm.isIsTemplate()) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.template"));
						} else {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.mailing"));
						}
						mailingBaseForm.setErrors(errors);
						destination = super.execute(mapping, form, request, response);
					} else {
						mailingBaseForm.setAction(ACTION_BULK_DELETE);
						destination = mapping.findForward("bulk_delete_confirm");
					}
					break;

				case ACTION_BULK_DELETE:
                    if (deleteMailingsBulk(mailingBaseForm, request)) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                        mailingBaseForm.setMessages(messages);
                    }
					destination = super.execute(mapping, form, request, response);
					break;

				case ACTION_MOVE_MEDIA_UP:
					mailingBaseForm.setMoveMedia(mailingBaseForm.getActiveMedia(), true);
					mailingBaseForm.setAction(ACTION_SAVE);
					destination = mapping.findForward("view");
					break;

				case ACTION_MOVE_MEDIA_DOWN:
					mailingBaseForm.setMoveMedia(mailingBaseForm.getActiveMedia(), false);
					mailingBaseForm.setAction(ACTION_SAVE);
					destination = mapping.findForward("view");
					break;

				case ACTION_RECIPIENTS_CALCULATE:
                    HttpUtils.responseJson(response, calculateRecipients(admin, mailingBaseForm));
                    return null;

				case ACTION_CLONE_AS_MAILING:
                    ComWorkflowAction.updateForwardParameters(request);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    forwardTargetItemId = (Integer) session.getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
                    if (forwardTargetItemId != null && forwardTargetItemId != 0) {
                        mailingBaseForm.setMailingID(forwardTargetItemId);
                    }

                    loadMailingDataForNewMailing(mailingBaseForm, request);
                    mailingBaseForm.setWorkflowForwardParams(cloneForwardParams);

                    if (mailingBaseForm.isIsMailingGrid()) {
                        mailingBaseForm.setAction(ACTION_SAVE_MAILING_GRID);
                    } else {
                        mailingBaseForm.setAction(ACTION_SAVE);
                    }

                    destination = mapping.findForward("view");
                    break;

                case ACTION_LIST:
                    ComWorkflowAction.updateForwardParameters(request);
                    destination = super.execute(mapping, form, request, response);
                    writeUserActivityLog(AgnUtils.getAdmin(request), "mailings list", "active tab - overview");
                    break;

                case ACTION_SELECT_TEMPLATE:
                    loadTemplateSettings(mailingBaseForm, request, true);
                    mailingBaseForm.setAction(ACTION_SAVE);
                    destination=mapping.findForward("view");
                    break;

                case ACTION_REMOVE_TARGET:
                    // Action is not in use anymore - to be completely eliminated.
                    throw new UnsupportedOperationException();

                case ACTION_MAILING_EXPORT:
                	String fileFriendlyMailingName = mailingBaseForm.getShortname().replace("/", "_");
					String filename = "Mailing_" + fileFriendlyMailingName + "_" + mailingBaseForm.getCompanyID() + "_" + mailingBaseForm.getMailingID() + FileUtils.JSON_EXTENSION;
					File tmpFile = File.createTempFile("Mailing_" + mailingBaseForm.getCompanyID() + "_" + mailingBaseForm.getMailingID(), FileUtils.JSON_EXTENSION);
                    try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
						mailingExporter.exportMailingToJson(mailingBaseForm.getCompanyID(), mailingBaseForm.getMailingID(), outputStream, true);
					} catch (Exception e) {
						destination = mapping.findForward("view");
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.export"));
					}
                    if (errors.isEmpty()) {
						try (FileInputStream inputStream = new FileInputStream(tmpFile)) {
							response.setContentType("application/json");
			                HttpUtils.setDownloadFilenameHeader(response, filename);
							IOUtils.copy(inputStream, response.getOutputStream());
		                    writeUserActivityLog(AgnUtils.getAdmin(request), "export mailing", mailingBaseForm.getMailingID());
		                    destination = null;
		                    tmpFile.delete();
						} catch (Exception e) {
							destination = mapping.findForward("view");
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.export"));
						}
                    }
                	break;
                    
                case ACTION_MAILING_IMPORT:
                    if (mailingBaseForm.getUploadFile() == null) {
                        destination = viewInitialImport(request, mapping);
                        break;
                    }
                    
                    if (mailingBaseForm.getUploadFile() != null && mailingBaseForm.getUploadFile().getFileSize() == 0) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.file.missingOrEmpty"));
                        destination = viewInitialImport(request, mapping);
                        break;
                    }
                    
                    ImportResult result = null;
                    try (InputStream input = mailingBaseForm.getUploadFile().getInputStream()) {
                        
                        boolean importGridTemplateAllowed = admin.permissionAllowed(Permission.GRID_CHANGE) || configService.getBooleanValue(ConfigValue.GridTemplateImportAllowed, companyId);
                        // Import mailing data from upload file
                        result = mailingImporter.importMailingFromJson(admin.getCompanyID(), input, mailingBaseForm.isIsTemplate(), importGridTemplateAllowed, mailingBaseForm.isIsGrid());
                        
                    } catch (Exception e) {
                        logger.error("Mailing import failed", e);
                        result = ImportResult.builder().setSuccess(false).addError("error.mailing.import").build();
                    }
                    
                    if (result.isSuccess()) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.imported"));
                        for (Entry<String, Object[]> warningEntry : result.getWarnings().entrySet()) {
                            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage(warningEntry.getKey(), warningEntry.getValue()));
                        }
                        writeUserActivityLog(AgnUtils.getAdmin(request), "import mailing", result.getMailingID());
                        
                        destination = viewImportedMailing(result, mailingBaseForm, request, mapping, session, errors);
                    } else {
                        for (Entry<String, Object[]> errorEntry : result.getErrors().entrySet()) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(errorEntry.getKey(), errorEntry.getValue()));
                        }
                        
                        destination = viewInitialImport(request, mapping);
                    }
                    
                	break;
                	
				case ACTION_IS_ADVERTISING_CONTENT_TYPE:
					int mailingId = NumberUtils.toInt(request.getParameter("mailingId"), -1);
					boolean mailingSendAsAdvertising = mailingBaseService.isAdvertisingContentType(admin.getCompanyID(), mailingId);
					HttpUtils.responseJson(response, "{\"isAdvertisingContentType\" : " + mailingSendAsAdvertising + "}");
					return null;

				default:
					destination = super.execute(mapping, form, request, response);
			}
		} catch (Exception e) {
			logger.error("execute: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}

		checkShowDynamicTemplateCheckbox(mailingBaseForm, request);
		
		String destinationName = destination != null ? destination.getName() : "";
		if ("view".equals(destinationName) || "mailing_grid_view".equals(destinationName)) {
			if (mailingBaseForm.getMediaEmail() != null) {
				mailingBaseForm.setOldMailFormat(mailingBaseForm.getMediaEmail().getMailFormat());
			}

			if (mailingBaseForm.getTemplateID() > 0) {
                String name = mailingDao.getMailingName(mailingBaseForm.getTemplateID(), companyId);
				if (StringUtils.isNotEmpty(name)) {
					mailingBaseForm.setTemplateShortname(name);
				} else {
					mailingBaseForm.setTemplateShortname(SafeString.getLocaleString("mailing.No_Template", AgnUtils.getLocale(request)));
				}
			} else {
				mailingBaseForm.setTemplateShortname(SafeString.getLocaleString("mailing.No_Template", AgnUtils.getLocale(request)));
			}

            request.setAttribute("localeDatePattern", AgnUtils.getDateFormatPattern(DateFormat.MEDIUM, admin));

            mailingBaseForm.setTemplateMailingBases(mailingDao.getTemplateMailingsByCompanyID(companyId));
            prepareMailinglists(mailingBaseForm, AgnUtils.getAdmin(request));
            mailingBaseForm.setCampaigns(campaignDao.getCampaignList(companyId, "lower(shortname)", 1));
            mailingBaseForm.setTargetGroupsList(targetService.getTargetLights(companyId, mailingBaseForm.getTargetGroups(), true));
            mailingBaseForm.setTargets(targetService.getTargetLights(companyId));
            mailingBaseForm.setSplitTargets(targetService.getSplitTargetLights(companyId, ""));
            mailingBaseForm.setSplitTargetsForSplitBase(targetService.getSplitTargetLights(companyId, mailingBaseForm.getSplitBase()));
        }

		if (destination != null) {
			String forward = destination.getName();
			if (!mailingBaseForm.isIsTemplate()) {
				if ("list".equals(forward) || "ajax_list".equals(forward)) {
					mailingBaseForm.setMailingLists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request)));
				}
			}

			if ("list".equals(forward)) {
				if (mailingBaseForm.getFromCalendarPage() == 1) {
					mailingBaseForm.setFromCalendarPage(0);
					return new ActionForward("/calendar.action");
				} else {
					request.setAttribute("localeTablePattern", AgnUtils.getDateTimeFormatPattern(DateFormat.MEDIUM, DateFormat.SHORT, admin));
				}
			} else if ("view".equals(forward) || "grid_base".equals(forward)) {
				request.setAttribute("localDatePattern", AgnUtils.getDatePickerFormatPattern(admin));
                setCanUserChangeGeneralSettings(mailingBaseForm, request);
			}
		}

		// Report any errors we have discovered back to the original
		// form
		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			if (destination == null) {
				destination = mapping.findForward("list");
			}
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}

		mailingBaseForm.setUndoAvailable(mailingBaseService.checkUndoAvailable(mailingBaseForm.getMailingID()));

        if (session.getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS) == null) {
            session.setAttribute(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS, mailingBaseForm.getWorkflowForwardParams());
        }
        
        if (destination != null && ("view".equals(destination.getName()) || "grid_base".equals(destination.getName()))) {
            request.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(request)));
        }

		return destination;
	}
    
    private ActionForward viewInitialImport(HttpServletRequest request, ActionMapping mapping) {
        if (AgnUtils.interpretAsBoolean(request.getParameter("isTemplate"))) {
            return mapping.findForward("importTemplate");
        } else {
            return mapping.findForward("importMailing");
        }
    }
    
    protected ActionForward createNewMailingGrid(ActionMapping mapping, ComMailingBaseForm form, HttpServletRequest request, HttpServletResponse response, ActionMessages errors) throws Exception {
        form.setAction(ACTION_LIST);
        return execute(mapping, form, request, response);
    }
    
    protected ActionForward viewImportedMailing(final ImportResult result, final ComMailingBaseForm mailingBaseForm, final HttpServletRequest request, final ActionMapping mapping, final HttpSession session, ActionMessages errors) throws Exception {
        mailingBaseForm.setMailingID(result.getMailingID());
        mailingBaseForm.setTemplateID(0);
    
        writeUserActivityLog(AgnUtils.getAdmin(request), result.isTemplate() ? "import template" : "import mailing", result.getMailingID());
        
		prepareMailingView(request, mailingBaseForm, session);
		return mapping.findForward("view");
	}

	protected void prepareMailingView(HttpServletRequest req, ComMailingBaseForm mailingBaseForm, HttpSession session) throws Exception {
        prepareMailingView(req, mailingBaseForm, session, false, null);
    }
    
	protected void prepareMailingView(HttpServletRequest req, ComMailingBaseForm mailingBaseForm, HttpSession session, boolean showTagWarnings, ActionMessages errors) throws Exception {
		Integer forwardTargetItemId;
		ComWorkflowAction.updateForwardParameters(req);
		forwardTargetItemId = (Integer) session.getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
		if (forwardTargetItemId != null && forwardTargetItemId != 0) {
		    mailingBaseForm.setMailingID(forwardTargetItemId);
		}
		mailingBaseForm.setAction(ACTION_SAVE);
		resetShowTemplate(req, mailingBaseForm);
		String workflowForwardParams = mailingBaseForm.getWorkflowForwardParams();
		if (StringUtils.isBlank(workflowForwardParams)) {
            Object sessionParams = session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS);
            if (sessionParams != null) {
                workflowForwardParams = StringUtils.trimToEmpty((String) sessionParams);
            }
        }
        
        loadMailing(mailingBaseForm, req, true, showTagWarnings, errors);

		if (mailingBaseForm.getWorkflowId() == 0) {
		    Integer workflowId = (Integer) session.getAttribute(ComWorkflowAction.WORKFLOW_ID);
		    if (workflowId != null) {
		        mailingBaseForm.setWorkflowId(workflowId);
		    }
		}
		mailingBaseForm.setWorkflowForwardParams(workflowForwardParams);
		Map<String, String> forwardParams = AgnUtils.getParamsMap((String) session.getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS));
		processAdditionalForwardParams(mailingBaseForm, forwardParams);
		
		req.setAttribute("limitedRecipientOverview",
				mailingBaseForm.isWorldMailingSend() &&
						!mailinglistApprovalService.isAdminHaveAccess(AgnUtils.getAdmin(req), mailingBaseForm.getMailinglistID()));
    }

    protected void setMailingWorkflowParameters(HttpServletRequest request, ComMailingBaseForm aForm) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        WorkflowParameters workflowParameters = WorkflowParametersHelper.find(request);
        if (workflowParameters != null && workflowParameters.getWorkflowId() > 0) {
            int workflowId = workflowParameters.getWorkflowId();
            int mailingIconId = workflowParameters.getNodeId();
            
            Map<String, String> forwardParams = workflowParameters.getParamsAsMap();

            ComMailing mailing = new ComMailingImpl();
            workflowService.assignWorkflowDrivenSettings(admin, mailing, workflowId, mailingIconId);
            aForm.setSplitId(mailing.getSplitID());
            aForm.setMailinglistID(mailing.getMailinglistID());
            aForm.setTargetGroups(mailing.getTargetGroups());
            aForm.setTargetMode(mailing.getTargetMode());
            aForm.setTargetExpression(mailing.getTargetExpression());
            aForm.setComplexTargetExpression(mailing.hasComplexTargetExpression());
            if (mailing.getPlanDate() != null) {
                SimpleDateFormat dateFormat = AgnUtils.getDatePickerFormat(admin, true);
                aForm.setPlanDate(dateFormat.format(mailing.getPlanDate()));
            }
            aForm.setMailingType(NumberUtils.toInt(forwardParams.get("mailingType"), Mailing.TYPE_NORMAL));
            aForm.setCampaignID(mailing.getCampaignID());
            aForm.setArchived(mailing.getArchived() == 1);
            aForm.setWorkflowId(workflowId);
            aForm.setWorkflowForwardParams(workflowParameters.getWorkflowForwardParams());
            processAdditionalForwardParams(aForm, forwardParams);
        }
    }

    private boolean checkFormContainsAlwaysAllowedChangesOnly(ComMailingBaseForm form, HttpServletRequest request) {
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(form.getMailingID());

        String textTemplateText = request.getParameter("textTemplate");
        String htmlTemplateText = request.getParameter("htmlTemplate");

        if (gridTemplateId > 0) {
            return form.getCompanyID() == mailing.getCompanyID() &&
                    form.getMailingID() == mailing.getId() &&
                    StringUtils.equals(textTemplateText, mailing.getTextTemplate().getEmmBlock());
        } else {
            return form.getCompanyID() == mailing.getCompanyID() &&
                    form.getMailingID() == mailing.getId() &&
                    StringUtils.equals(textTemplateText, mailing.getTextTemplate().getEmmBlock()) &&
                    StringUtils.equals(htmlTemplateText, mailing.getHtmlTemplate().getEmmBlock());
        }
    }

	private void processAdditionalForwardParams(ComMailingBaseForm aForm, Map<String, String> forwardParams) {
        if (forwardParams.containsKey("workflowFollowUpParentMailing")) {
            aForm.setFollowMailing(forwardParams.get("workflowFollowUpParentMailing"));
            aForm.setParentMailing(NumberUtils.toInt(aForm.getFollowMailing()));
        }
        if (forwardParams.containsKey("workflowFollowUpDecisionCriterion")) {
            WorkflowReactionType decisionCriterion = WorkflowReactionType.valueOf(forwardParams.get("workflowFollowUpDecisionCriterion"));
            String followUpMethod = WorkflowUtils.getFollowUpMethod(decisionCriterion);

            if (followUpMethod != null) {
                aForm.setFollowUpMailingType(followUpMethod);
            }
        }
    }

    @Override
	protected void processDependantMailings(int mailingID, HttpServletRequest req) {
        final ComAdmin admin = AgnUtils.getAdmin(req);
        final int companyId = admin.getCompanyID();

        List<Integer> followupMailings = mailingDao.getFollowupMailings(mailingID, companyId, true);
        for (Integer followupMailingID : followupMailings) {
            ComMailing followupMailing = (ComMailing) mailingDao.getMailing(followupMailingID, companyId);
            boolean deliveryCancelled = cancelMailingDelivery(Mailing.TYPE_FOLLOWUP, followupMailingID, followupMailing.getShortname(), req);
            followupMailing.setParameters(mailingParameterService.getMailingParameters(companyId, followupMailingID));
            if (deliveryCancelled) {
                mailingDao.updateStatus(followupMailingID, "canceled");
            }
            MediatypeEmail paramEmail = followupMailing.getEmailParam();
            paramEmail.deleteFollowupParameters();
            setMailingParameters(admin, followupMailingID, followupMailing.getParameters());
            cancelDependentMailings(followupMailingID, req);
        }
    }

    private void setMailingParameters(ComAdmin admin, int mailingId, List<ComMailingParameter> parameters) {
        List<UserAction> userActions = new ArrayList<>();
        mailingParameterService.updateParameters(admin.getCompanyID(), mailingId, parameters, admin.getAdminID(), userActions);

        for (UserAction action : userActions) {
            writeUserActivityLog(admin, action);
        }
    }

    /**
     * Cancel folloup mailings
     * @param mailingID - target mailing id.
     * @param req
     */
    private void cancelDependentMailings(int mailingID, HttpServletRequest req) {
        final int companyID = AgnUtils.getCompanyID(req);

        List<Integer> followupMailings = mailingDao.getFollowupMailings(mailingID, companyID, false);
        for (Integer followupMailingID : followupMailings) {
            LightweightMailing followupMailing = mailingDao.getLightweightMailing(followupMailingID);
            boolean deliveryCancelled = cancelMailingDelivery(Mailing.TYPE_FOLLOWUP, followupMailingID, followupMailing.getShortname(), req);
            if (deliveryCancelled) {
                mailingDao.updateStatus(followupMailingID, "canceled");
                cancelDependentMailings(followupMailingID, req);
            }
        }
    }

    /**
     * Cancel specific mailing. Port from MailingSendAction.
     *
     * @param cachedMailtype
     * @param mailingID
     * @param shortname
     * @param req
     * @return
     */
    private boolean cancelMailingDelivery(int cachedMailtype, int mailingID, String shortname, HttpServletRequest req) {
        boolean isDeliveryCanceled = deliveryStatService.cancelDelivery(AgnUtils.getCompanyID(req), mailingID);
        if (isDeliveryCanceled) {
            final String description = String.format("%s (%d) %s", shortname, mailingID, mailingTypeToString(cachedMailtype));
            writeUserActivityLog(AgnUtils.getAdmin(req), "do cancel mailing", description);
        }
        return isDeliveryCanceled;
    }

    private boolean validatePlanDate(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors) {
        String planDateAsString = form.getPlanDate();

        if (StringUtils.isNotEmpty(planDateAsString)) {
            ComAdmin admin = AgnUtils.getAdmin(req);
            TimeZone timezone = AgnUtils.getTimeZone(admin);
            DateFormat dateFormat = AgnUtils.getDatePickerFormat(admin, true);

            assert (admin != null);

            try {
                Date planDate = dateFormat.parse(planDateAsString);
                Date today = DateUtilities.midnight(timezone);

                if (planDate.before(today)) {
                    Date originPlanDate = mailingDao.getMailingPlanDate(form.getMailingID(), admin.getCompanyID());

                    originPlanDate = DateUtilities.midnight(originPlanDate, timezone);

                    if (planDate.equals(originPlanDate)) {
                        // Untouched plan date should not be validated (whether or not it is in the past)
                        return true;
                    } else {
                        form.setPlanDate(dateFormat.format(today));

                        if (isUsedByWorkflowManager(form, req)) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.plan.date.pastSetWithCampaignEditor"));
                        } else {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.plan.date.past"));
                        }
                        return false;
                    }
                } else {
                    return true;
                }
            } catch (ParseException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.wrong.plan.date.format"));
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean isUsedByWorkflowManager(ComMailingBaseForm form, HttpServletRequest req) {
        // Check if a mailing already used in some workflow
        Integer workflowId = mailingBaseService.getWorkflowId(form.getMailingID(), AgnUtils.getCompanyID(req));
        if (workflowId > 0) {
            return true;
        }

        // Check if a user came from a workflow manger in order to use this mailing there
        workflowId = (Integer) req.getSession().getAttribute(ComWorkflowAction.WORKFLOW_ID);

        return workflowId != null && workflowId > 0;
    }

    /**
     * If Mailing created from template with rule " Mailing should only be sent with Target group-selection",
     * validate if at least one Target Group is set for Mailing.
     *
     * @param form
     * @param messages
     * @param errors
     */
    private void validateNeedTarget(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
        boolean noTargets = CollectionUtils.isEmpty(form.getTargetGroups());

        if (!form.isIsTemplate() && form.isNeedsTarget() && noTargets) {
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.rulebased_without_target"));
        }

		if (form.getMailingType() == Mailing.TYPE_DATEBASED || form.getMailingType() == Mailing.TYPE_INTERVAL) {
            int workflowId = form.getWorkflowId();

            if (workflowId > 0) {
                noTargets = !workflowService.isAdditionalRuleDefined(AgnUtils.getCompanyID(req), form.getMailingID(), workflowId);
            }

            if (noTargets) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.rulebased_without_target"));
            }
		}
    }

    private boolean isMediaTypesPresent(ComMailingBaseForm form, ActionMessages errors) {
        for (boolean isUsed : form.getUseMediaType()) {
            if (isUsed) {
                return true;
            }
        }
        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.mediatype.none"));
        return false;
    }

    private void validateFollowUpBaseMailing(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
        String followUpFor = mailingDao.getFollowUpFor(form.getMailingID());
        if (StringUtils.isNotEmpty(followUpFor)) {
            int baseMailingId = Integer.parseInt(followUpFor);

            if (mailingDao.getLastSendDate(baseMailingId) == null) {
                messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.followup.basemail_was_not_sent"));
            } else if (!isBaseMailingTrackingDataAvailable(baseMailingId, AgnUtils.getCompanyID(req))) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.followup.basemail_data_not_exists"));
            }
        }
    }

    private boolean isBaseMailingTrackingDataAvailable(int baseMailingId, int companyId) {
        List<ComMailing> availableBaseMailings = mailingDao.getMailings(companyId, ComMailingLightVO.TAKE_ALL_SNOWFLAKE_MAILINGS, "W", true);
        // Check if base mailing tracking data is available
        for (ComMailing mailing : availableBaseMailings) {
            if (mailing.getId() == baseMailingId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether or not a mailing is editable.
     * Basically a world sent mailing is not editable but there's a permission {@link com.agnitas.emm.core.Permission#MAILING_CONTENT_CHANGE_ALWAYS}
     * that unlocks sent mailing so it could be edited anyway.
     *
     * @param form a struts form bean object to get current mailing to check.
     * @param request a request object to retrieve an authority from.
     * @return whether ({@code true}) or not ({@code false}) mailing editing is permitted.
     */
    private boolean isMailingEditable(ComMailingBaseForm form, HttpServletRequest request) {
    	if(maildropService.isActiveMailing(form.getMailingID(), AgnUtils.getCompanyID(request))) {
            return AgnUtils.allowed(request, Permission.MAILING_CONTENT_CHANGE_ALWAYS);
		} else {
            return true;
        }
	}

	private boolean saveGridMailing(ComMailingBaseForm form, HttpServletRequest request, ActionMessages errors, ActionMessages messages) throws Exception {
        final int companyId = AgnUtils.getCompanyID(request);

        if (!mailingBaseService.isMailingExists(form.getMailingID(), companyId, false)) {
            int mailingListId = form.getMailinglistID();
            if (mailingListId <= 0 || !mailinglistService.exist(mailingListId, companyId)) {
                List<Mailinglist> mailingLists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request));
                if (CollectionUtils.isNotEmpty(mailingLists)) {
                    mailingListId = mailingLists.get(0).getId();
                } else {
                    mailingListId = 0;
                }
                if (mailingListId > 0) {
                    form.setMailinglistID(mailingListId);
                } else {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.noMailinglist"));
                    return false;
                }
            }

            if (!createGridMailing(form, request, errors)) {
                logger.error("Unable to generate grid mailing out of grid template #" + form.getGridTemplateId());
                return false;
            }
        }

        form.setMailingGrid(true);
        saveMailingData(form, request, errors, messages);
        previewImageService.generateMailingPreview(AgnUtils.getAdmin(request), request.getSession(false).getId(), form.getMailingID(), true);
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

        resetShowTemplate(request, form);
        loadMailing(form, request);

        return true;
    }

    private boolean createGridMailing(ComMailingBaseForm form, HttpServletRequest request, ActionMessages errors) {
        final int companyId = AgnUtils.getCompanyID(request);
        int templateId = form.getGridTemplateId();

        ComGridTemplate template;

        if (templateId > 0) {
            template = gridService.getGridTemplate(companyId, templateId);
            if (template == null) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
                // Template is not available
                return false;
            } else {
                // Ensure that we use an identifier of the origin
                templateId = template.getId();
            }
        } else {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
            // Template identifier is invalid
            return false;
        }

        boolean isCloning = template.getMailingId() > 0;

        Mailing mailing;
        try {
            String targetExpression = generateTargetExpression(form);
            MailingCreationOptions options = MailingCreationOptions.builder()
                    .setMailingListId(form.getMailinglistID())
                    .setCreatedFromReleased(true)
                    .setAlwaysCreateNew(isCloning)
                    .setTextTemplateStub(form.getTextTemplate())
                    .setTargetGroupExtension(targetExpression)
                    .build();
            mailing = gridService.createGridMailing(AgnUtils.getAdmin(request), templateId, options);
        } catch (LinkService.ParseLinkException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link", e.getErrorLink()));
            logger.error("Error occurred: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
            logger.error("Error occurred: " + e.getMessage(), e);
            return false;
        }

        form.setMailingID(mailing.getId());
        form.setGridTemplateId(templateId);
        form.setTargetGroups(mailing.getTargetGroups());

        // Keep an HTML provided by grid template
        MailingComponent component = mailing.getHtmlTemplate();
        if (component != null) {
            form.setHtmlTemplate(component.getEmmBlock());
        }

        return true;
    }

    private String generateTargetExpression(ComMailingBaseForm form) {
        String targetExpression = form.getTargetExpression();

        if (StringUtils.isBlank(targetExpression)) {
            boolean conjunction = form.getTargetMode() == Mailing.TARGET_MODE_AND;
            targetExpression = TargetExpressionUtils.makeTargetExpression(form.getTargetGroups(), conjunction);
        }

        return targetExpression;
    }

    protected void saveMailing(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
        try {
            saveMailingData(form, req, errors, messages);

            String workflowForwardParams = form.getWorkflowForwardParams();
            int workflowId = form.getWorkflowId();

            form.clearData(true);
            loadMailing(form, req);

            if (form.getWorkflowId() == 0) {
                form.setWorkflowId(workflowId);
            }
            form.setWorkflowForwardParams(workflowForwardParams);

            previewImageService.generateMailingPreview(AgnUtils.getAdmin(req), req.getSession(false).getId(), form.getMailingID(), true);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

            validateMailingModules(form, req);
        } catch (AgnTagException e) {
            req.setAttribute("errorReport", e.getReport());
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags"));
        } catch (MissingEndTagException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Missing end tag", e);
            }
            errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags.missing_end_tag", e.getLineNumber(), e.getTag()));
        } catch (UnclosedTagException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Unclosed tag", e);
            }
            errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags.unclosed_tag", e.getTag()));
        } catch (DynTagException e) {
            if (logger.isInfoEnabled()) {
                logger.info("General error in tag", e);
            }
            errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags.general_tag_error", e.getLineNumber(), e.getTag()));
        } catch (TooManyTargetGroupsInMailingException e) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Too many target groups for mailing %d", form.getMailingID()), e);
            }
            errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.tooManyTargetGroups"));
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
        }
    }

	/**
	 * Saves current mailing in DB (including mailing components, content blocks, dynamic tags, dynamic tags contents
	 * and trackable links)
	 *
	 * @param form struts form bean object
	 * @param request  request
	 * @param messages  not in use
	 * @throws Exception if anything went wrong
	 */
	protected void saveMailingData(ComMailingBaseForm form, HttpServletRequest request, ActionMessages errors, ActionMessages messages) throws Exception {
        final int companyId = AgnUtils.getCompanyID(request);
        final ComAdmin admin = AgnUtils.getAdmin(request);
        final ApplicationContext applicationContext = getApplicationContext(request);
        
		ComMailing aMailing;
        int gridTemplateId = 0;
		boolean mailingIsNew;
		if (mailingBaseService.isMailingExists(form.getMailingID(), companyId)) {
			// Use existing mailing and fill in changed data from form afterwards
			aMailing = (ComMailing) mailingDao.getMailing(form.getMailingID(), companyId);
			mailingIsNew = false;
		} else if (mailingBaseService.isMailingExists(form.getTemplateID(), companyId)) {
			// Make a copy of the template mailing and fill in changed data from form afterwards
			int copiedMailingID = copyMailingService.copyMailing(companyId, form.getTemplateID(), companyId, form.getShortname(), form.getDescription());
			aMailing = (ComMailing) mailingDao.getMailing(copiedMailingID, companyId);
			aMailing.setCompanyID(companyId);
			aMailing.setMailTemplateID(form.getTemplateID());
			mailingIsNew = false;
		} else {
			// Create a new empty mailing and fill in data from form afterwards
			form.setMailingID(0);
            aMailing = mailingFactory.newMailing();
            aMailing.init(companyId, applicationContext);
            aMailing.setId(0);
            aMailing.setCompanyID(companyId);
            mailingIsNew = true;
		}

        List<String> userActions = new LinkedList<>();
        if (!mailingIsNew) {
            gridTemplateId = gridService.getGridTemplateIdByMailingId(aMailing.getId());
            userActions.addAll(getEditActionStrings(form, aMailing));
        }

        // repairs mailing html template
        // problem occurs while saving different mailing via two different tabs
        // see GWUA-4079
        if (!mailingIsNew && gridTemplateId > 0) {
            MailingComponent htmlTemplate = aMailing.getHtmlTemplate();
            if (Objects.nonNull(htmlTemplate)) {
                form.setHtmlTemplate(htmlTemplate.getEmmBlock());
            }
        }

        // Grid-based mailings have generated HTML template (not user-defined).
        if (gridTemplateId == 0) {
            MediatypeEmail mediatypeEmail = form.getMediaEmail();

            // Check if user selected "text only" e-mail format.
            if (mediatypeEmail.getMailFormat() == 0) {
                mediatypeEmail.setHtmlTemplate("");
            } else if (form.getOldMailFormat() == 0) {
                if (StringUtils.isEmpty(mediatypeEmail.getHtmlTemplate())) {
                    mediatypeEmail.setHtmlTemplate("[agnDYN name=\"HTML-Version\"/]");
                }
            }

            if (StringUtils.isNotEmpty(mediatypeEmail.getHtmlTemplate())) {
            	validateLinks(companyId, mediatypeEmail.getHtmlTemplate(), mediatypeEmail.getMailingID(),  form.getMailinglistID(), errors);
            }
        }

        form.clearTargetsData();

		int splitId = targetService.getTargetListSplitId(form.getSplitBase(), form.getSplitPart(), form.isWmSplit());
        if (splitId != aMailing.getSplitID()) {
            if (splitId == ComMailing.NONE_SPLIT_ID) {
                userActions.add("edit list split changed to none");
            } else {
                StringBuilder listSplitAction = new StringBuilder(form.getSplitBase());
                formatSplitString(listSplitAction);
                listSplitAction.insert(0,"edit list split changed to ");
                listSplitAction.append(" part #").append(form.getSplitPart());
                userActions.add(listSplitAction.toString());
            }
        }
        aMailing.setSplitID(splitId);
        loadSplitTarget(form, splitId,false);
        aMailing.setIsTemplate(form.isIsTemplate());
        aMailing.setCampaignID(form.getCampaignID());
        aMailing.setDescription(form.getDescription());
        aMailing.setShortname(form.getShortname());
        aMailing.setMailinglistID(form.getMailinglistID());
        aMailing.setMailingType(form.getMailingType());
        aMailing.setMailingContentType(form.getMailingContentType());
        aMailing.setArchived(form.isArchived() ? 1 : 0);

		if (form.getAssignTargetGroups() && !aMailing.hasComplexTargetExpression() || StringUtils.isBlank(aMailing.getTargetExpression())) {
		    aMailing.setTargetExpression(generateTargetExpression(form));
		}

        form.setLocked(true);
        aMailing.setLocked(form.getLocked() ? 1 : 0);
		aMailing.setNeedsTarget(form.isNeedsTarget());
		aMailing.setMediatypes(form.getMediatypes());
		aMailing.setUseDynamicTemplate(form.getUseDynamicTemplate());

        Date mailingPlanDate = null;
        if (StringUtils.isNotEmpty(form.getPlanDate())) {
            try {
                mailingPlanDate = AgnUtils.getDatePickerFormat(admin, true).parse(form.getPlanDate());
            } catch (ParseException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.wrong.plan.date.format"));
            }
        }
        aMailing.setPlanDate(mailingPlanDate);

		try {
            MediatypeEmail media = aMailing.getEmailParam();

            media.setLinefeed(form.getEmailLinefeed());
            media.setCharset(form.getEmailCharset());
            media.setOnepixel(form.getEmailOnepixel());
            
            int mailingType = aMailing.getMailingType();
			if (mailingType == Mailing.TYPE_FOLLOWUP) {
                String followUpMethod = form.getFollowUpMailingType();

                // Assign follow-up parameters if available.
                if (StringUtils.isNotEmpty(followUpMethod)) {
                    int baseMailingId = form.getParentMailing();
                    if (baseMailingId == aMailing.getId()) {
                        throw new Exception("Cannot create cyclic follow-up mailing");
                    }

                    media.setFollowupFor(Integer.toString(baseMailingId));
                    media.setFollowUpMethod(followUpMethod);
                }
            } else {
                // Remove follow-up parameters for non-follow-up mailing.
                media.deleteFollowupParameters();
            }
            
            if (mailingType != Mailing.TYPE_DATEBASED) {
			    media.deleteDateBasedParameters();
            }

			for (MediaTypes type : MediaTypes.values()) {
                int code = type.getMediaCode();
                if (form.getUseMediaType(code)) {
                    form.getMedia(code).syncTemplate(aMailing, applicationContext);
                }
            }

			List<String> dynNamesForDeletion = new Vector<>();
			aMailing.buildDependencies(true, dynNamesForDeletion, applicationContext, messages, errors);

			// Mark names in dynNamesForDeletion as "deleted"
            dynamicTagDao.markNamesAsDeleted(aMailing.getId(), dynNamesForDeletion);
        } catch (Exception e) {
			logger.error("Error in save mailing id: " + form.getMailingID(), e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.content"));
		}

		validateMailingTagsAndComponents(aMailing, request, errors);

		aMailing.getComponents().forEach((name, component) -> {
			if (component.getEmmBlock() != null) {
	            String text = AgnTagUtils.unescapeAgnTags(component.getEmmBlock());
	            component.setEmmBlock(text, component.getMimeType());

	            Integer rdirLinkLine = linkService.getLineNumberOfFirstRdirLink(text);
	            if (rdirLinkLine != null) {
	                messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.encoded", name, rdirLinkLine));
	            }
			}
        });

		ComMailingContentChecker.checkHtmlWarningConditions(aMailing, messages);

        mailingBaseService.doTextTemplateFilling(aMailing, admin, messages);
        mailingBaseService.saveMailingWithUndo(aMailing, admin.getAdminID(), AgnUtils.allowed(request, Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
        form.setMailingID(aMailing.getId());
        form.setGridTemplateId(gridTemplateId);

        if (gridTemplateId > 0) {
            saveMailingGridInfo(form, request);
        }

	    if (AgnUtils.allowed(request, Permission.MAILING_CHANGE)) {
            List<ComMailingParameter> parameters = collectMailingParameters(form, request, mailingIsNew ? null : userActions, messages);
            aMailing.setParameters(parameters);
	    	setMailingParameters(admin, aMailing.getId(), parameters);
	    }

		if (aMailing.isIsTemplate()) {
			updateMailingsWithDynamicTemplate(aMailing, request);
		}

        final String type = (form.isIsTemplate() ? "template" : "mailing");
        final String description = getMailingDescription(form);
        if (mailingIsNew) {
            writeUserActivityLog(admin, "create " + type, description);
        } else {
            if (userActions.size() > 0) {
                writeUserActivityLog(admin, "edit " + type + " settings", StringUtils.join(userActions, "; ") + "; " + description);
            }
        }
	}

    private void validateLinks(int companyId,  String htmlTemplate, int mailingId, int mailingListId, ActionMessages errors) {
	    try {
            LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(htmlTemplate, mailingId, mailingListId, companyId);
            List<LinkService.ErrorneousLink> linksWithErrors = linkScanResult.getErrorneousLinks();
            if (CollectionUtils.isNotEmpty(linksWithErrors)) {
                linksWithErrors.forEach(link -> errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(link.getErrorMessageKey())));
            }
            
            final List<LinkService.ErrorneousLink> localLinks = linkScanResult.getLocalLinks();
            if (CollectionUtils.isNotEmpty(localLinks)) {
            	localLinks.forEach(link -> errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT, new ActionMessage(link.getErrorMessageKey())));
            }
        } catch (Exception e) {
            logger.warn("something went wrong while validating links in html template");
        }
    }

    private boolean deleteMailingsBulk(ComMailingBaseForm form, HttpServletRequest req) {
        Set<Integer> ids = form.getBulkIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            final ComAdmin admin = AgnUtils.getAdmin(req);
            final int companyId = AgnUtils.getCompanyID(req);

            // It only changes the 'deleted' column value
            mailingBaseService.bulkDelete(ids, companyId);

            for (int mailingId : ids) {
                String description = mailingBaseService.getMailingName(mailingId, companyId) + " (" + mailingId + ")";
                writeUserActivityLog(admin, "delete " + (form.isIsTemplate() ? "template" : "mailing"), description);
            }
            return true;
        }
        return false;
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

    @Override
    protected MailingsQueryWorker createMailingsQueryWorker(ActionMessages errors, MailingBaseForm mailingBaseForm, HttpServletRequest req, int companyId, String types, boolean isTemplate, String sort, String direction, int page, int rownums, final boolean includeTargetGroups) throws Exception {
        ComMailingBaseForm comMailingBaseForm = (ComMailingBaseForm) mailingBaseForm;

        MailingsListProperties mailingsListProps = new MailingsListProperties();
        mailingsListProps.setTypes(types);
        mailingsListProps.setTemplate(isTemplate);

        String searchQuery = comMailingBaseForm.getSearchQueryText();
        boolean searchName = comMailingBaseForm.isSearchNameChecked();
        boolean searchDescription = comMailingBaseForm.isSearchDescriptionChecked();
        boolean searchContent = comMailingBaseForm.isSearchContentChecked();

        if (searchName || searchDescription || searchContent) {
            if (StringUtils.isBlank(searchQuery)) {
                searchQuery = null;
            }
        }

        mailingsListProps.setSearchQuery(searchQuery);
        mailingsListProps.setSearchName(searchName);
        mailingsListProps.setSearchDescription(searchDescription);
        mailingsListProps.setSearchContent(searchContent);

		if (!comMailingBaseForm.isIsTemplate()) {

            String[] filterBadge = comMailingBaseForm.getBadgeFilters();
            if (filterBadge != null && filterBadge.length != 0) {
                List<String> badges = new ArrayList<>();

                for (String badge : filterBadge) {
                    if (StringUtils.isNotBlank(badge)) {
                        badges.add(badge);
                    }
                }
                mailingsListProps.setBadge(badges);
            }

            String[] filterStatuses = comMailingBaseForm.getFilterStatus();
			if (filterStatuses != null && filterStatuses.length != 0) {
                List<String> statuses = new ArrayList<>();

                for (String status : filterStatuses) {
					if (StringUtils.isNotBlank(status)) {
						statuses.add(status);
					}
				}
                mailingsListProps.setStatuses(statuses);
			}

            Integer[] filterMailingLists = comMailingBaseForm.getFilterMailingList();
			if (filterMailingLists != null && filterMailingLists.length != 0) {
                List<Integer> mailingLists = new ArrayList<>();

				for (Integer mailingListId : filterMailingLists) {
					if (mailingListId != null && mailingListId > 0) {
						mailingLists.add(mailingListId);
					}
				}
                mailingsListProps.setMailingLists(mailingLists);
			}

            String filterSendDateBeginAsString = comMailingBaseForm.getFilterSendDateBegin();
            String filterSendDateEndAsString = comMailingBaseForm.getFilterSendDateEnd();
            mailingsListProps.setSendDateBegin(getDate(filterSendDateBeginAsString));
            mailingsListProps.setSendDateEnd(getDate(filterSendDateEndAsString));
		}

        mailingsListProps.setSort(sort);
        mailingsListProps.setDirection(direction);
        mailingsListProps.setPage(page);
        mailingsListProps.setRownums(rownums);
        mailingsListProps.setIncludeTargetGroups(includeTargetGroups);
        mailingsListProps.setAdditionalColumns(getAdditionalColumns(mailingBaseForm));
        mailingsListProps.setCreationDateBegin(getDate(comMailingBaseForm.getFilterCreationDateBegin()));
        mailingsListProps.setCreationDateEnd(getDate(comMailingBaseForm.getFilterCreationDateEnd()));

        return new MailingsQueryWorker(mailingDao, companyId, mailingsListProps);
    }

    private Date getDate(String dateStr) {
        try {
            return new SimpleDateFormat(Constants.DATE_PATTERN).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
         * Format list split string from its name to human readable format.
         * For example converting "050505050575" to "5% / 5% / 5% / 5% / 5% / 75%"
         * @param builder string builder containing split name
         */
    private static void formatSplitString(StringBuilder builder) {
        final int firstSeparatorPlace = 2;
        final int splitWithDelimiterLength = 6;
        for (int i = firstSeparatorPlace, j = 0;
             i < builder.length();
             i+= splitWithDelimiterLength, j+= splitWithDelimiterLength) {
                builder.insert(i,"% / ");
                if (builder.indexOf("0",j) == j){
                    builder.delete(j,j+1);
                    i--;
                    j--;
                }
        }
        builder.append("%");
    }

    private List<ComMailingParameter> collectMailingParameters(ComMailingBaseForm form, HttpServletRequest request, List<String> userActions, ActionMessages messages) {
        final ComAdmin admin = AgnUtils.getAdmin(request);
        final int mailingId = form.getMailingID();

        assert (admin != null);

        // Let's retrieve all the parameters currently stored.
        List<ComMailingParameter> parameters = mailingParameterService.getMailingParameters(admin.getCompanyID(), mailingId);

        // Separate reserved parameters (interval mailing parameters).
        List<ComMailingParameter> previousReservedParameters = removeReservedParameters(parameters);

        // Overwrite all the parameters with the user-defined ones if user is permitted to change parameters.
        if (AgnUtils.allowed(request, Permission.MAILING_PARAMETER_CHANGE)) {
            boolean isEmptyValueWarning = false;

            // Overwrite parameters.
            parameters = new ArrayList<>();

            // Prevent new parameter adding unless explicitly requested.
            if (!form.isAddParameter()) {
                form.getParameterMap().remove(0);
            }

            for (ComMailingParameter parameter : form.getParameterMap().values()) {
                if (StringUtils.isNotEmpty(parameter.getName())) {
                    if (ArrayUtils.contains(RESERVED_PARAMETER_NAMES, parameter.getName())) {
                        logger.error("User tried to use reserved mailing parameter name: " + parameter.getName());
                        continue;
                    }

                    // Show warning when a parameter has a name but has no value.
                    if (StringUtils.isEmpty(parameter.getValue()) && !isEmptyValueWarning) {
                        isEmptyValueWarning = true;
                        messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.parameter.emptyValue"));
                    }
                    parameters.add(parameter);
                }
            }
        }

        if (form.getMailingType() == Mailing.TYPE_INTERVAL) {
            String value = getIntervalParameterValue(form);
            parameters.addAll(generateIntervalParameters(admin, mailingId, value, previousReservedParameters, userActions));
        }

        return parameters;
    }

    private List<ComMailingParameter> removeReservedParameters(List<ComMailingParameter> parameters) {
        List<ComMailingParameter> removed = new ArrayList<>();

        parameters.removeIf(parameter -> {
            if (ArrayUtils.contains(RESERVED_PARAMETER_NAMES, parameter.getName())) {
                removed.add(parameter);
                return true;
            }
            return false;
        });

        return removed;
    }

    private List<ComMailingParameter> generateIntervalParameters(ComAdmin admin, int mailingId, String intervalValue, List<ComMailingParameter> previousReservedParameters, List<String> userActions) {
        final int companyId = admin.getCompanyID();
        final int adminId = admin.getAdminID();
        final Date now = new Date();

        List<ComMailingParameter> parameters = new ArrayList<>();

        Map<String, ComMailingParameter> previousReservedParametersMap = toParametersMap(previousReservedParameters);
        ComMailingParameter intervalParameter = previousReservedParametersMap.get(ComMailingParameterDao.PARAMETERNAME_INTERVAL);

        if (userActions != null) {
            if (!StringUtils.equals(intervalParameter == null ? "" : intervalParameter.getValue(), intervalValue == null ? "" : intervalValue)) {
                userActions.add("edit interval");
            }
        }

        if (intervalValue != null) {
            if (intervalParameter == null) {
                intervalParameter = new ComMailingParameter(0, mailingId, companyId, ComMailingParameterDao.PARAMETERNAME_INTERVAL, "", "", now, now, adminId, adminId);
            }

            intervalParameter.setValue(intervalValue);
            intervalParameter.setChangeDate(now);
            intervalParameter.setChangeAdminID(adminId);
            parameters.add(intervalParameter);

            ComMailingParameter errorParameter = previousReservedParametersMap.get(ComMailingParameterDao.PARAMETERNAME_ERROR);
            if (errorParameter != null) {
                parameters.add(errorParameter);
            }

            // Evaluate and assign the next start date for interval mailing.
            ComMailingParameter nextStartParameter = previousReservedParametersMap.get(ComMailingParameterDao.PARAMETERNAME_NEXT_START);
            if (nextStartParameter == null) {
                nextStartParameter = new ComMailingParameter();
                nextStartParameter.setCreationAdminID(adminId);
            }
            nextStartParameter.setChangeAdminID(adminId);
            nextStartParameter.setName(ComMailingParameterDao.PARAMETERNAME_NEXT_START);
            nextStartParameter.setValue(new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM).format(DateUtilities.calculateNextJobStart(intervalValue)));
            nextStartParameter.setCompanyID(companyId);
            nextStartParameter.setMailingID(mailingId);
            parameters.add(nextStartParameter);
        }

        return parameters;
    }

    private String getIntervalParameterValue(ComMailingBaseForm form) {
        switch (form.getIntervalType()) {
            case Weekly:
                return getIntervalWeekdaysPattern(form.getIntervalDays()) + ":" + getIntervalTimePattern(form.getIntervalTime());

            case TwoWeekly:
                return getIntervalWeekdaysPattern(form.getIntervalDays()) + (isWeekEven() ? "Ev" : "Od") + ":" + getIntervalTimePattern(form.getIntervalTime());

            case Monthly:
                return getIntervalMonthPattern(form.getNumberOfMonth(), form.getIntervalDayOfMonth()) + ":" + getIntervalTimePattern(form.getIntervalTime());
                
            case Weekdaily:
                return getIntervalWeekdailyPattern(form.getWeekdayOrdinal(), form.getIntervalDays(), getIntervalTimePattern(form.getIntervalTime()));

            default:
                return null;
        }
    }

    private String getIntervalWeekdailyPattern(int weekdayOrdinal, boolean[] days, String timePattern) {
        StringBuilder intervalPatternString = new StringBuilder();
        if (days != null) {
            int count = Math.min(days.length, 7);

            for (int i = 0; i < count; i++) {
                if (days[i]) {
                	if (intervalPatternString.length() > 0) {
                		intervalPatternString.append(";");
                	}
                	intervalPatternString.append(weekdayOrdinal);
                	intervalPatternString.append(DateUtilities.getWeekdayShortnameByID(i + 1));
                	intervalPatternString.append(":" + timePattern);
                }
            }
        }
        return intervalPatternString.toString();
	}

	private String getIntervalMonthPattern(int month, int date) {
        DecimalFormat format = new DecimalFormat("00");
        return (month > 1 ? month : "") + "M" + format.format(date == -1 ? 99 : date);
    }

    private String getIntervalWeekdaysPattern(boolean[] days) {
        StringBuilder weekdays = new StringBuilder();

        if (days != null) {
            int count = Math.min(days.length, 7);

            for (int i = 0; i < count; i++) {
                if (days[i]) {
                    weekdays.append(DateUtilities.getWeekdayShortnameByID(i + 1));
                }
            }
        }

        return weekdays.toString();
    }

    private String getIntervalTimePattern(String time) {
        if (time == null) {
            return "";
        } else {
            return time.replace(":", "");
        }
    }

    /**
     * Check whether a current week of year is even or odd.
     * @return {@code true} if even, {@code false} if odd.
     */
    private boolean isWeekEven() {
        Calendar calendar = Calendar.getInstance();
        return DateUtilities.makeWeekOfYearISO8601Compliant(calendar).get(Calendar.WEEK_OF_YEAR) % 2 == 0;
    }

    /**
     * Create a name-keyed map for all the given parameters.
     * Warning: keep in mind that parameter names are not guaranteed to be unique!
     *
     * @param parameters given parameters.
     * @return name-keyed map of parameters.
     */
	private Map<String, ComMailingParameter> toParametersMap(List<ComMailingParameter> parameters) {
        if (CollectionUtils.isNotEmpty(parameters)) {
            Map<String, ComMailingParameter> map = new HashMap<>(parameters.size());
            for (ComMailingParameter parameter : parameters) {
                String name = parameter.getName();
                if (name != null) {
                    map.put(name, parameter);
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    private Map<Integer, ComMailingParameter> toFormParametersMap(List<ComMailingParameter> parameters) {
        Map<Integer, ComMailingParameter> map = new HashMap<>();

	    if (CollectionUtils.isNotEmpty(parameters)) {
	        for (int i = 0; i < parameters.size(); i++) {
	            map.put(i + 1, parameters.get(i));
            }
        }

        return map;
    }

	private void loadIntervalData(ComMailingBaseForm form, ComMailingParameter intervalParameter) {
		Pattern monthRulePattern = Pattern.compile("\\d{0,2}M\\d{2}:\\d{4}");
		Pattern weekdailyRulePattern = Pattern.compile("\\d\\D\\D:\\d{4}(;\\\\d\\\\D\\\\D:\\\\d{4})*");

		if (intervalParameter != null && StringUtils.isNotBlank(intervalParameter.getValue())) {
			String interval = intervalParameter.getValue();
			if (monthRulePattern.matcher(interval).matches()) {
				String xMonth = interval.substring(0, interval.indexOf("M"));
				if (xMonth.length() == 0) {
					xMonth = "1";
				}
				String day = interval.substring(interval.indexOf("M") + 1, interval.indexOf(":"));
				String time = interval.substring(interval.indexOf(":") + 1);

				form.setIntervalType(IntervalType.Monthly);
				form.setIntervalDays(new boolean[7]);
				form.setIntervalTime(time.substring(0, 2) + ":" + time.substring(2, 4));
				form.setIntervalDayOfMonth(Integer.parseInt(day));
				form.setIntervalNumberOfMonth(Integer.parseInt(xMonth));
			} else if (weekdailyRulePattern.matcher(interval).matches()) {
				String[] patternParts = interval.split(";");
				int weekdayOrdinal = 0;
				boolean[] weekdays = new boolean[7];
				String time = "";
				for (String patternPart : patternParts) {
					weekdayOrdinal = Integer.parseInt(patternPart.substring(0, 1));
					weekdays[DateUtilities.getWeekdayIndex(patternPart.substring(1, 3)) - 1] = true;
					time = patternPart.substring(patternPart.indexOf(":") + 1);
				}

				form.setIntervalType(IntervalType.Weekdaily);
				form.setIntervalDays(weekdays);
				form.setIntervalTime(time.substring(0, 2) + ":" + time.substring(2, 4));
				form.setWeekdayOrdinal(weekdayOrdinal);
				form.setIntervalDays(weekdays);
			} else {
				IntervalType type = IntervalType.Weekly;
				String wochenTage = interval.substring(0, interval.indexOf(":"));
				String zeit = interval.substring(interval.indexOf(":") + 1);
				boolean[] weekdays = new boolean[7];
				for (String weekDay : AgnUtils.chopToChunks(wochenTage, 2)) {
					if (weekDay.equalsIgnoreCase("ev") || weekDay.equalsIgnoreCase("od")) {
						type = IntervalType.TwoWeekly;
					} else {
						weekdays[DateUtilities.getWeekdayIndex(weekDay) - 1] = true;
					}
				}
				form.setIntervalType(type);
				form.setIntervalDays(weekdays);
				form.setIntervalTime(zeit.substring(0, 2) + ":" + zeit.substring(2, 4));
				form.setIntervalDayOfMonth(0);
			}
		} else {
			form.setIntervalType(IntervalType.None);
			form.setIntervalDays(new boolean[7]);
			form.setIntervalTime(null);
			form.setIntervalDayOfMonth(0);
		}
	}

	private void updateMailingsWithDynamicTemplate(ComMailing template, HttpServletRequest request) {
        final ApplicationContext context = getApplicationContext(request);
        final int companyId = AgnUtils.getCompanyID(request);

		List<Integer> referencingMailings = mailingDao.getTemplateReferencingMailingIds(template);
		if (referencingMailings == null) {
			return;
		}

		MailingComponent srcText = template.getTextTemplate();
        MailingComponent srcHtml = template.getHtmlTemplate();

		Mailing mailing;
		MailingComponent mailingComponent;

		for (int mailingId : referencingMailings) {
			mailing = mailingDao.getMailing(mailingId, companyId);

			// First, handle text template
			mailingComponent = mailing.getTextTemplate();

			// Modify text template only if mailing and template have both a text template
			if (srcText != null && mailingComponent != null) {
				mailingComponent.setEmmBlock(srcText.getEmmBlock(), "text/plain");
			}

			// Next, handle HTML template
			mailingComponent = mailing.getHtmlTemplate();

			// Modify HTML template only if mailing and template have both a HTML template
			if (srcHtml != null && mailingComponent != null) {
				mailingComponent.setEmmBlock(srcHtml.getEmmBlock(), "text/html");
			}

			try {
				mailing.buildDependencies(true, context);
				mailingDao.saveMailing(mailing, false);
			} catch (Exception e) {
				logger.error("unable to update mailing ID " + mailingId, e);

				if (logger.isDebugEnabled()) {
					logger.debug("unable to update mailing ID " + mailingId, e);
				}
			}
		}
	}

    private String getParentNameOrNameOfCopy(ComGridTemplate template) {
        String templateName = template.getName();
        if (template.getParentTemplateId() != 0) {
            template = gridService.getGridTemplate(template.getCompanyId(), template.getParentTemplateId());
            if (template != null) {
                templateName = template.getName();
            }
        }
        return templateName;
    }

	/**
	 * Loads mailing.
	 */
	@Override
	protected void loadMailing(MailingBaseForm form, HttpServletRequest req) throws Exception {
		loadMailing((ComMailingBaseForm) form, req, true, false, null);
	}

	protected void loadMailing(ComMailingBaseForm form, HttpServletRequest request, boolean preserveCmListSplit, boolean showTagWarnings, ActionMessages errors) throws Exception {
	    final ComAdmin admin = AgnUtils.getAdmin(request);
	    final int companyId = admin.getCompanyID();

		ComMailing aMailing;
		if (form.getMailingID() > 0) {
			aMailing = (ComMailing) mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
		} else {
			aMailing = mailingFactory.newMailing();
			aMailing.init(companyId, getApplicationContext(request));
			aMailing.setId(0);
			aMailing.setCompanyID(companyId);
            aMailing.setIsTemplate(form.isIsTemplate());
            form.setMailingID(0);
		}

        form.clearData(true);
        form.setShortname(aMailing.getShortname());
        form.setMailingContentType(aMailing.getMailingContentType());
        form.setDescription(aMailing.getDescription());
        form.setMailingType(aMailing.getMailingType());
        form.setMailinglistID(aMailing.getMailinglistID());
        form.setCampaignID(aMailing.getCampaignID());
        form.setTemplateID(aMailing.getMailTemplateID());
        form.setArchived(aMailing.getArchived() != 0);
        form.setTargetMode(aMailing.getTargetMode());
        form.setTargetGroups(aMailing.getTargetGroups());
        form.setNeedsTarget(aMailing.getNeedsTarget());
        form.setSplitId(aMailing.getSplitID());
        form.setUseDynamicTemplate(aMailing.getUseDynamicTemplate());
        form.setIsTemplate(aMailing.isIsTemplate());

		if (aMailing.getMailingType() == Mailing.TYPE_INTERVAL) {
			String workStatus = mailingDao.getWorkStatus(companyId, aMailing.getId());
            if (StringUtils.equals(workStatus, ACTIVE_MAILING_STATUS)) {
                form.setWorldMailingSend(true);
            } else {
                // `active` and `disable` statuses are only allowed for interval mailings
                form.setWorldMailingSend(false);
            }
		} else {
            form.setWorldMailingSend(this.maildropService.isActiveMailing(aMailing.getId(), aMailing.getCompanyID()));
		}

        loadMailingGridTemplate(form, request);

        if (aMailing.getPlanDate() != null) {
            SimpleDateFormat dateFormat = AgnUtils.getDatePickerFormat(admin, true);
            form.setPlanDate(dateFormat.format(aMailing.getPlanDate()));
        } else {
            form.setPlanDate("");
        }

        loadSplitTarget(form, aMailing.getSplitID(), preserveCmListSplit);

        form.setComplexTargetExpression(aMailing.hasComplexTargetExpression());
        form.setMediatypes(aMailing.getMediatypes());

        MediatypeEmail type = aMailing.getEmailParam();
		if (type != null) {
            form.setEmailOnepixel(type.getOnepixel());

			// Set Followup-Parameter (if any)
			String followUpFor = type.getFollowupFor();
            form.setFollowMailing(followUpFor);

            int parentMailingId = 0;
			// convert String with followup ID to Int.
			if (StringUtils.isNotBlank(followUpFor)) {
				try {
                    parentMailingId = Integer.parseInt(followUpFor);
				} catch (NumberFormatException e) {
					logger.error("Invalid base mailing id: " + followUpFor, e);
				}
			}

			form.setParentMailing(parentMailingId);
            form.setFollowUpMailingType(type.getFollowUpMethod());

			try {
                form.setEmailReplytoEmail(new InternetAddress(type.getReplyAdr()).getAddress());
			} catch (Exception e) {
				// do nothing
			}
			try {
                form.setEmailReplytoFullname(new InternetAddress(type.getReplyAdr()).getPersonal());
			} catch (Exception e) {
				// do nothing
			}
			try {
                form.setEnvelopeEmail(new InternetAddress(type.getEnvelopeEmail()).getAddress());
			} catch (Exception e) {
				// do nothing
				if (logger.isInfoEnabled()) {
					logger.info("info:" + e, e);
				}
			}
            form.setEmailLinefeed(type.getLinefeed());
            form.setEmailCharset(type.getCharset());
		}

		final String[] labels = { "Text", "FAX", "Print", "MMS", "SMS", "WHATSAPP" };
        MailingComponent comp;

		for (int c = 0; c < labels.length; c++) {
			comp = aMailing.getTemplate(labels[c]);
			if (comp != null) {
                form.getMedia(c).setTemplate(comp.getEmmBlock());
			}
		}

		comp = aMailing.getHtmlTemplate();
		if (comp != null) {
            form.setHtmlTemplate(comp.getEmmBlock());
		}

		loadMailingParameters(form, request);

        if (aMailing.getId() > 0) {
            form.setWorkflowId(mailingDao.getWorkflowId(aMailing.getId()));
        }

        if(form.getMailingID() > 0) {
            form.setMailingEditable(isMailingEditable(form, request));
        } else {
            form.setMailingEditable(true);
        }
        
        if (showTagWarnings) {
            try {
                validateMailingTagsAndComponents(aMailing, request, errors);
            } catch (AgnTagException e) {
                request.setAttribute("errorReport", e.getReport());
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags"));
            }
        }

		if (logger.isInfoEnabled()) {
			logger.info("loadMailing: mailing loaded");
		}
	}

	private void loadMailingGridTemplate(ComMailingBaseForm form, HttpServletRequest request) {
        form.setNotes(StringUtils.EMPTY);
        form.setMailingGrid(false);
        form.setGridTemplateId(0);
        form.setGridTemplateName(null);

        int gridTemplateId = gridService.getGridTemplateIdByMailingId(form.getMailingID());
        if (gridTemplateId > 0) {
            ComGridTemplate template = gridService.getGridTemplate(AgnUtils.getCompanyID(request), gridTemplateId);
            if (template != null) {
                form.setMailingGrid(true);
                form.setGridTemplateId(gridTemplateId);
                form.setGridTemplateName(getParentNameOrNameOfCopy(template));

                Map<String, Object> meta = getMailingGridInfo(form, request);

                Object notes = meta.get("NOTES");
                form.setNotes(notes == null ? StringUtils.EMPTY : String.valueOf(notes));
                form.setOwnerName(String.valueOf(meta.get("OWNER_NAME")));
            }
        }
        request.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(request)));
    }

    private Map<String, Object> getMailingGridInfo(ComMailingBaseForm form, HttpServletRequest request) {
        Map<String, Object> meta = gridService.getMailingGridInfo(AgnUtils.getCompanyID(request), form.getMailingID());

        if (meta == null) {
            meta = saveMailingGridInfo(form, request);
        }

        return meta;
    }

    private Map<String, Object> saveMailingGridInfo(ComMailingBaseForm form, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();

        ComAdmin admin = AgnUtils.getAdmin(request);

        data.put("TEMPLATE_ID", form.getGridTemplateId());
        data.put("NOTES", form.getNotes());
        data.put("OWNER", admin.getAdminID());
        data.put("OWNER_NAME", admin.getUsername());

        gridService.saveMailingGridInfo(form.getMailingID(), admin.getCompanyID(), data);

        return data;
    }

    private void loadMailingParameters(ComMailingBaseForm form, HttpServletRequest request) {
        List<ComMailingParameter> parameters = mailingParameterService.getMailingParameters(AgnUtils.getCompanyID(request), form.getMailingID());

        // Separate reserved parameters from user-defined ones.
        Map<String, ComMailingParameter> reservedParametersMap = toParametersMap(removeReservedParameters(parameters));

        // Load interval data from the corresponding parameter.
        loadIntervalData(form, reservedParametersMap.get(ComMailingParameterDao.PARAMETERNAME_INTERVAL));

        form.setParameterMap(toFormParametersMap(parameters));
    }

	protected void restoreUndo(ComMailingBaseForm form, HttpServletRequest req) throws Exception {
		ApplicationContext aContext = getApplicationContext(req);
        mailingBaseService.restoreMailingUndo(aContext, form.getMailingID(), form.getCompanyID());
        ComMailing mailing = (ComMailing) mailingDao.getMailing(form.getMailingID(), form.getCompanyID());
        String description = String.format("%s %s (%d)", mailing.isIsTemplate() ? "template" : "mailing", mailing.getShortname(), mailing.getId());
        writeUserActivityLog(AgnUtils.getAdmin(req), "edit undo", description);
	}

	/**
	 * Loads chosen mailing template data into form.
	 *
	 * @param template  Mailing bean object, contains mailing template data
	 * @param mailingBaseForm  MailingBaseForm object
	 * @param regularTemplate whether the regular template is used or a mailing (clone & edit)
	 */
	protected void copyTemplateSettingsToMailingForm(ComMailing template, MailingBaseForm mailingBaseForm, HttpServletRequest req, boolean regularTemplate) {
	    ComMailingBaseForm form = (ComMailingBaseForm) mailingBaseForm;
        MailingComponent tmpComp;

        Integer workflowId = (Integer) req.getSession().getAttribute(ComWorkflowAction.WORKFLOW_ID);
		// If we already have a campaign we don't have to override settings inherited from it
		boolean overrideInherited = (workflowId == null || workflowId == 0 || !regularTemplate);

		if (overrideInherited) {
            form.setMailingType(template.getMailingType());
            form.setMailinglistID(template.getMailinglistID());
            form.setCampaignID(template.getCampaignID());
		}

		if (overrideInherited || template.getMailingType() == Mailing.TYPE_DATEBASED) {
            form.setTargetGroups(template.getTargetGroups());
		}

		form.setTargetMode(template.getTargetMode());
		form.setMediatypes(template.getMediatypes());
		form.setArchived(template.getArchived() != 0);
		form.setNeedsTarget(template.getNeedsTarget());
		form.setUseDynamicTemplate(template.getUseDynamicTemplate());
		form.setMailingContentType(template.getMailingContentType());

        // load template for this mailing
        if ((tmpComp=template.getHtmlTemplate())!=null) {
            form.setHtmlTemplate(tmpComp.getEmmBlock());
        }

        if ((tmpComp=template.getTextTemplate())!=null) {
            form.setTextTemplate(tmpComp.getEmmBlock());
        }

        MediatypeEmail type=template.getEmailParam();
        if (type != null) {
            form.setEmailOnepixel(type.getOnepixel());
            try {
                form.setEmailReplytoEmail(new InternetAddress(type.getReplyAdr()).getAddress());
            } catch (Exception e) {
                // do nothing
            }
            try {
                form.setEmailReplytoFullname(new InternetAddress(type.getReplyAdr()).getPersonal());
            } catch (Exception e) {
                // do nothing
            }
            form.setEmailLinefeed(type.getLinefeed());
            form.setEmailCharset(type.getCharset());
        }

		// Create a clone copy of all mailing parameters
		List<ComMailingParameter> templateMailingParameters = mailingParameterService.getMailingParameters(template.getCompanyID(), template.getId());
		List<ComMailingParameter> newParameters = new ArrayList<>();

		if (templateMailingParameters != null) {
			for (ComMailingParameter parameter : templateMailingParameters) {
				ComMailingParameter newParameter = new ComMailingParameter();

				newParameter.setName(parameter.getName());
				newParameter.setValue(parameter.getValue());
				newParameter.setDescription(parameter.getDescription());
				newParameter.setCreationDate(parameter.getCreationDate());

				newParameters.add(newParameter);
			}
		}

		form.setParameterMap(toFormParametersMap(newParameters));
	}

	private JSONObject calculateRecipients(ComAdmin admin, ComMailingBaseForm form) {
        final JSONObject data = new JSONObject();

        CalculationRecipientsConfig config = conversionService.convert(form, CalculationRecipientsConfig.class);
        config.setSplitId(targetService.getTargetListSplitId(form.getSplitBase(), form.getSplitPart(), form.isWmSplit()));
        config.setCompanyId(admin.getCompanyID());
        config.setConjunction(form.getTargetMode() != Mailing.TARGET_MODE_OR);

        try {
            data.element("count", mailingBaseService.calculateRecipients(config));
            data.element("success", true);
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            data.element("success", false);
        }

        return data;
	}

    private void validateMailingMod(ComMailingBaseForm form, HttpServletRequest request) throws Exception {
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
        characterEncodingValidator.validateMod(form, mailing);
    }

    private void validateMailingModules(ComMailingBaseForm form, HttpServletRequest request) throws Exception {
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
        validateMailingModules(mailing, request);
    }

    /**
     * Validate agn-tags and dyn-tags
     *
     * @param mailing
     * @throws Exception
     */
    private void validateMailingModules(Mailing mailing, HttpServletRequest req) throws Exception {
        if (mailing.getTextTemplate() != null) {
            String textEmmBlock = mailing.getTextTemplate().getEmmBlock();
            mailing.findDynTagsInTemplates(textEmmBlock, getApplicationContext(req));
        }
        if (mailing.getHtmlTemplate() != null) {
            String htmlEmmBlock = mailing.getHtmlTemplate().getEmmBlock();
            mailing.findDynTagsInTemplates(htmlEmmBlock, getApplicationContext(req));
        }
    }

    private void validateMailingTagsAndComponents(ComMailing mailing, HttpServletRequest request, ActionMessages errors) throws Exception {
        List<String[]> errorReports = new ArrayList<>();

        Map<String, List<AgnTagError>> agnTagsValidationErrors = mailing.checkAgnTagSyntax(getApplicationContext(request));
        if (MapUtils.isNotEmpty(agnTagsValidationErrors)) {
            agnTagsValidationErrors.forEach((componentName, validationErrors) -> {
                // noinspection ThrowableResultOfMethodCallIgnored
                AgnTagError firstError = validationErrors.get(0);

                String displayComponentName = componentName;
                if (displayComponentName.startsWith("agn")) {
                    displayComponentName = displayComponentName.substring(3);
                }

                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.agntag.mailing.component", displayComponentName, firstError.getFullAgnTagText()));
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(firstError.getErrorKey().getMessageKey(), firstError.getAdditionalErrorDataWithLineInfo()));

                for (AgnTagError error : validationErrors) {
                    errorReports.add(new String[]{ componentName, error.getFullAgnTagText(), error.getLocalizedMessage(request.getLocale()) });
                }
            });
        } else if (mailing.getId() > 0) {
        	// Only use backend/preview agn syntax check if mailing was already stored in database before (= not new mailing)
            Vector<String> outFailures = new Vector<>();
            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailing.getId());

            try {
                mailing.getComponents().forEach((name, component) -> {
                    StringBuffer reportContents = new StringBuffer();
                    if (component.getEmmBlock() != null && !tagCheck.checkContent(component.getEmmBlock(), reportContents, outFailures)) {
                        appendErrorsToList(name, errorReports, reportContents);
                    }
                });
            } finally {
                tagCheck.done();
            }
        }

        if (!errorReports.isEmpty()) {
            throw new AgnTagException("error.template.dyntags", errorReports);
        }
    }

    protected void loadMailingDataForNewMailing(MailingBaseForm form, HttpServletRequest req) {
        final ComMailingBaseForm aForm = (ComMailingBaseForm) form;
        final int mailingOriginId = aForm.getMailingID();

        try {
            ComMailing origin = null;
            if (mailingOriginId > 0) {
                origin = (ComMailing) mailingDao.getMailing(mailingOriginId, AgnUtils.getCompanyID(req));
            }

            aForm.clearData();

            aForm.setTemplateID(mailingOriginId);
            aForm.setIsTemplate(false);
            aForm.setWmSplit(false);
            aForm.setSplitBaseMessage("");
            aForm.setSplitPartMessage("");
            req.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(req)));

            if (origin != null) {
                copyTemplateSettingsToMailingForm(origin, aForm, req, false);
                aForm.setShortname(SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(req)) + " " + origin.getShortname());
                aForm.setMailingContentType(origin.getMailingContentType());
                aForm.setDescription(origin.getDescription());
                aForm.setIsTemplate(origin.isIsTemplate());
                aForm.setSplitId(origin.getSplitID());
                loadSplitTarget(aForm, origin.getSplitID(), false);

                int templateId = gridService.getGridTemplateIdByMailingId(mailingOriginId);
                if (templateId > 0) {
                    ComGridTemplate template = gridService.getGridTemplate(AgnUtils.getCompanyID(req), templateId);
                    aForm.setMailingGrid(true);
                    aForm.setGridTemplateId(templateId);
                    aForm.setGridTemplateName(getParentNameOrNameOfCopy(template));
                } else {
                    aForm.setMailingGrid(false);
                }
            }

            aForm.setMailingID(0);
            aForm.setCopiedMailing(true);
            form.setMailingEditable(true);
        } catch (Exception e) {
            logger.error("execute: " + e, e);
        }
    }

    protected void loadMailingDataForFollowUpMailing(MailingBaseForm form, HttpServletRequest req) {
        final ComMailingBaseForm aForm = (ComMailingBaseForm) form;
        final int mailingOriginId = aForm.getMailingID();

        try {
            ComMailing origin = null;
            if (mailingOriginId > 0) {
                origin = (ComMailing) mailingDao.getMailing(mailingOriginId, AgnUtils.getCompanyID(req));
            }

            aForm.clearData();

            aForm.setTemplateID(mailingOriginId);
            aForm.setIsTemplate(false);
            aForm.setWmSplit(false);
            aForm.setSplitBaseMessage("");
            aForm.setSplitPartMessage("");
            req.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(req)));

            if (origin != null) {
                copyTemplateSettingsToMailingForm(origin, aForm, req, false);
                aForm.setShortname(SafeString.getLocaleString("mailing.Followup_Mailing", AgnUtils.getLocale(req)) + " " + origin.getShortname());
                aForm.setMailingContentType(origin.getMailingContentType());
                aForm.setDescription(SafeString.getLocaleString("default.description", AgnUtils.getLocale(req)));
                aForm.setIsTemplate(origin.isIsTemplate());
                aForm.setSplitId(origin.getSplitID());
                loadSplitTarget(aForm, origin.getSplitID(), false);

                int templateId = gridService.getGridTemplateIdByMailingId(mailingOriginId);
                if (templateId > 0) {
                    ComGridTemplate template = gridService.getGridTemplate(AgnUtils.getCompanyID(req), templateId);
                    aForm.setMailingGrid(true);
                    aForm.setGridTemplateId(templateId);
                    aForm.setGridTemplateName(getParentNameOrNameOfCopy(template));
                } else {
                    aForm.setMailingGrid(false);
                }

                aForm.setParentMailing(origin.getId());
            }

            aForm.setMailingType(ComMailing.TYPE_FOLLOWUP);
            MediatypeEmail mediatype = origin.getEmailParam();
            mediatype.setFollowupFor(Integer.toString(origin.getId()));
            mediatype.setFollowUpMethod(ComMailing.TYPE_FOLLOWUP_NON_OPENER);
            Map<Integer, Mediatype> mediatypes = new HashMap<>();
            mediatypes.put(0, mediatype);
            aForm.setMediatypes(mediatypes);

            aForm.setMailingID(0);
            aForm.setCreatedAsFollowUp(true);
        } catch (Exception e) {
            logger.error("execute: " + e, e);
        }
    }

    protected void loadSplitTarget(ComMailingBaseForm aForm, int splitId, boolean preserveCmListSplit) {
        if (splitId > 0) {
            String name = targetService.getTargetSplitName(splitId);

            if (StringUtils.isNotEmpty(name)) {
                if (name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
                    if (preserveCmListSplit) {
                        aForm.setSplitBase(name.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length(), name.lastIndexOf('_')));
                        aForm.setSplitPart(name.substring(name.lastIndexOf("_") + 1));
                        String[] parts = aForm.getSplitBase().split(";");
                        String splitBaseMessage = "";
                        for (int i = 1; i <= parts.length; i++) {
                            String part = parts[i - 1];
                            splitBaseMessage += part + "% / ";
                            if (i == Integer.parseInt(aForm.getSplitPart())) {
                                aForm.setSplitPartMessage(i + ". " + part + "%");
                            }
                        }
                        splitBaseMessage = splitBaseMessage.substring(0, splitBaseMessage.length() - 2);
                        aForm.setSplitBaseMessage(splitBaseMessage);
                        aForm.setWmSplit(true);
                        return;
                    }
                } else {
                    aForm.setSplitBase(name.substring(12, name.indexOf('_', 13)));
                    aForm.setSplitPart(name.substring(name.indexOf('_', 13) + 1));
                    aForm.setWmSplit(false);
                    return;
                }
            }
        }

        aForm.setWmSplit(false);
        aForm.setSplitBase(splitId == -1 ? "yes" : "none");
        aForm.setSplitPart("1");
    }

	/**
	 * Gets mailing template data from db and calls method for loading the data into form.
	 *
	 * @param aForm  MailingBaseForm object
	 * @param req  request
	 * @param regularTemplate whether the regular template is used or a mailing (clone & edit)
	 */
	protected void loadTemplateSettings(MailingBaseForm aForm, HttpServletRequest req, boolean regularTemplate) {
		if (aForm.getTemplateID() > 0) {
			ComMailing template = (ComMailing) mailingDao.getMailing(aForm.getTemplateID(), AgnUtils.getCompanyID(req));
			if (template != null) {
				copyTemplateSettingsToMailingForm(template, aForm, req, regularTemplate);
			}
		} else {
            // Reset mailinglist in case previous template was disabled for user.
            aForm.setMailinglistID(0);
        }
	}

	@Override
    protected void prepareListParameters(MailingBaseForm mailingBaseForm) {
	    ComMailingBaseForm form = (ComMailingBaseForm) mailingBaseForm;

        webStorage.access(ComWebStorage.MAILING_OVERVIEW, storage -> {
            if (form.getNumberOfRows() > 0) {
                storage.setRowsCount(form.getNumberOfRows());

                if (!form.isIsTemplate()) {
                    storage.setSelectedFields(Arrays.asList(form.getSelectedFields()));
                    storage.setMailingTypeNormal(form.getMailingTypeNormal());
                    storage.setMailingTypeDate(form.getMailingTypeDate());
                    storage.setMailingTypeEvent(form.getMailingTypeEvent());
                    storage.setMailingTypeFollowup(form.isMailingTypeFollowup());
                    storage.setMailingTypeInterval(form.isMailingTypeInterval());
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());

                if (!form.isIsTemplate()) {
                    form.setSelectedFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    form.setMailingTypeNormal(storage.isMailingTypeNormal());
                    form.setMailingTypeDate(storage.isMailingTypeDate());
                    form.setMailingTypeEvent(storage.isMailingTypeEvent());
                    form.setMailingTypeFollowup(storage.isMailingTypeFollowup());
                    form.setMailingTypeInterval(storage.isMailingTypeInterval());
                }
            }
        });
    }
    
    @Required
    public void setGridService(GridServiceWrapper gridServiceWrapper) {
	    this.gridService = gridServiceWrapper;
    }

	@Required
	public void setMailingLightService(ComMailingLightVO mailingLightService) {
		this.mailingLightService = mailingLightService;
	}

	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}

	@Required
	public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
		this.dynamicTagDao = dynamicTagDao;
	}

	/*@Required
	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}*/

	@Required
	public void setMailingParameterService(ComMailingParameterService mailingParameterService) {
		this.mailingParameterService = mailingParameterService;
	}

	@Required
	public void setPreviewImageService(PreviewImageService previewImageService) {
		this.previewImageService = previewImageService;
	}

	@Required
    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
    public void setDeliveryStatService(ComMailingDeliveryStatService deliveryStatService) {
        this.deliveryStatService = deliveryStatService;
    }

    @Required
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

	@Required
	public final void setMaildropService(final MaildropService service) {
		this.maildropService = service;
	}

	@Required
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

	@Required
    public void setCopyMailingService(CopyMailingService copyMailingService) {
        this.copyMailingService = copyMailingService;
    }

    @Required
    public void setDynamicTagContentFactory(DynamicTagContentFactory dynamicTagContentFactory) {
        this.dynamicTagContentFactory = dynamicTagContentFactory;
    }

    @Required
	public void setMailingExporter(MailingExporter mailingExporter) {
		this.mailingExporter = mailingExporter;
	}

    @Required
	public void setMailingImporter(MailingImporter mailingImporter) {
		this.mailingImporter = mailingImporter;
	}

}
