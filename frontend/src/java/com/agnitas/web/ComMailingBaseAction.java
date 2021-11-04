/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.updateForwardParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.dao.FollowUpType;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.mailing.service.MailingModel;
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
import org.agnitas.service.UserMessageException;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DynTagException;
import org.agnitas.util.FileUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HtmlUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.MissingEndTagException;
import org.agnitas.util.SafeString;
import org.agnitas.util.UnclosedTagException;
import org.agnitas.util.ZipUtilities;
import org.agnitas.web.MailingBaseAction;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.IntervalType;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.mailing.web.RequestAttributesHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToActionMessages;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.TargetUtils;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToActionMessages;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.emm.grid.grid.service.MailingCreationOptions;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.forms.ComMailingBaseForm;

import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.json.JSONObject;

/**
 * Implementation of <strong>Action</strong> that handles Mailings
 */
public class ComMailingBaseAction extends MailingBaseAction {
    /** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingBaseAction.class);
	
    public static final int ACTION_VIEW_FAX_BG = ACTION_MAILING_BASE_LAST + 1;

	public static final int ACTION_SAVE_FAX_BG = ACTION_MAILING_BASE_LAST + 2;

	public static final int ACTION_VIEW_POST_BG = ACTION_MAILING_BASE_LAST + 3;

	public static final int ACTION_SAVE_POST_BG = ACTION_MAILING_BASE_LAST + 4;

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
    
    public static final int ACTION_IMPORT_TEMPLATES = ACTION_MAILING_BASE_LAST + 19;
    
    public static final int MAILING_PREVIEW_WIDTH = 300;
    
    public static final int MAILING_PREVIEW_HEIGHT = 300;
    
    public static final String MAILING_ID = "mailingID";
    public static final String CAMPAIGN_ID = "campaignID";
    
    private static final String[] RESERVED_PARAMETER_NAMES = new String[]{
            ComMailingParameterDao.PARAMETERNAME_INTERVAL,
            ComMailingParameterDao.PARAMETERNAME_ERROR,
            ComMailingParameterDao.PARAMETERNAME_NEXT_START
    };

    private ComMailingLightService mailingLightService;

	private ComMailingBaseService mailingBaseService;

	private DynamicTagDao dynamicTagDao;

	protected PreviewImageService previewImageService;

	private ComMailingParameterService mailingParameterService;

    private ComWorkflowService workflowService;

    private LinkService linkService;

    private MaildropService maildropService;

    private ConversionService conversionService;
    
    private CopyMailingService copyMailingService;

    protected DynamicTagContentFactory dynamicTagContentFactory;

	protected MailingExporter mailingExporter;
    
	protected MailingImporter mailingImporter;

	protected GridServiceWrapper gridService;
	
	protected MailingStopService mailingStopService;

    private AgnTagService agnTagService;

    private AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;

	protected MailingService mailingService;
	
	protected MailingPropertiesRules mailingPropertiesRules;
	protected RequestAttributesHelper requestAttributesHelper;
    protected AdminService adminService;

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
        case ACTION_IMPORT_TEMPLATES:
            return "import_templates";
            
        case ACTION_IS_ADVERTISING_CONTENT_TYPE:
        	return "is_advertising_content_type";

        default:
            return super.subActionMethodName(subAction);
        }
    }

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);

		// Validate the request parameters specified by the user
		ComMailingBaseForm mailingBaseForm = (ComMailingBaseForm) form;
		ActionMessages errors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;
        
        // Fallback. Mailing is by default editable. Other methods may change this value afterwards
        request.setAttribute(RequestAttributesHelper.MAILNG_EDITABLE_REQUEST_ATTRIBUTE_NAME, true);

		if (logger.isInfoEnabled()) {
			logger.info("execute: action " + mailingBaseForm.getAction());
		}

        HttpSession session = request.getSession();

		// check if mailtracking is active
		int companyId = admin.getCompanyID();
		boolean mailtracking = mailingLightService.isMailtrackingActive(companyId);
        session.setAttribute("mailtracking", Boolean.toString(mailtracking));

        ComGridTemplate gridTemplate;
        
		try {
			Integer forwardTargetItemId;
			String cloneForwardParams;

			switch (mailingBaseForm.getAction()) {
                case ACTION_MAILING_TEMPLATES:
                    request.setAttribute("localeDatePattern", admin.getDateFormat().toPattern());
                    String sort = getSort(request, mailingBaseForm, true);
                 	String direction = mailingBaseForm.getDir();
                    mailingBaseForm.setTemplateMailingBases(mailingService.getTemplatesWithPreview(admin, sort, direction));

                    destination = mapping.findForward("mailing_templates");

                    WorkflowUtils.updateForwardParameters(request, true);
                    setMailingWorkflowParameters(request, mailingBaseForm);
				    break;

                case ACTION_NEW:
                    List<Mailinglist> mlists = mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request));
                    Map<String, String> map = AgnUtils.getReqParameters(request);
                    int templateId = NumberUtils.toInt(map.get("templateID"));

                    if (templateId == 0 && admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE)) {
                        //could not create mailing without template if mailing.settings.hide permission activated
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
                        saveErrors(request, errors);
                        return mapping.findForward("messages");
                    }

                    if (mlists.size() > 0) {
                        mailingBaseForm.setAction(ACTION_SAVE);
                        mailingBaseForm.clearData();

                        //populate mailing data with info from workflow
                        updateForwardParameters(request, true);

                        mailingBaseForm.setMailingID(0);
                        mailingBaseForm.setGridTemplateId(0);

                        if (templateId > 0) {
                            mailingBaseForm.setTemplateID(templateId);
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
                    updateForwardParameters(request, true);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    warnIfTargetsHaveDisjunction(admin, mailingBaseForm, messages);
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
				    if (admin.permissionAllowed(Permission.UPDATE_MAILINGLIST_ALLOW_DELETION)) {
                        int mailinglistID = mailingBaseForm.getMailinglistID();
                        if (mailingBaseForm.getMailinglistID() <= 0 || !mailinglistService.exist(mailinglistID, companyId)) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.noMailinglist"));
                            destination = mapping.findForward("view");
                            break;
                        }
                    }
                    updateForwardParameters(request, true);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    forwardTargetItemId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
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
        
					prepareMailingView(request, mailingBaseForm, session, showTagWarnings, errors, messages);
					destination = mapping.findForward("view");
                    writeUserActivityLog(AgnUtils.getAdmin(request), "view " + (mailingBaseForm.isIsTemplate() ? "template" : "mailing"),
                            mailingBaseForm.getShortname() + " (" + mailingBaseForm.getMailingID() + ")");
                    writeUserActivityLog(AgnUtils.getAdmin(request), "view mailing", mailingBaseForm.getShortname() + " (" + mailingBaseForm.getMailingID() + ")" + " active tab - settings");
                    
                    insertTargetModeCheckboxControlAttribute(request, mailingBaseForm, admin);
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
				    boolean saved = false;
                    if (isMediaTypesPresent(mailingBaseForm, errors)) {
                        if (isMailingEditable(mailingBaseForm, request)) {
                            if (validateIllegalScriptElement(mailingBaseForm)) {
                                if (validatePlanDate(mailingBaseForm, request, errors)) {
                                    validateNeedTarget(mailingBaseForm, request, errors, messages);
                                    warnIfTargetsHaveDisjunction(admin, mailingBaseForm, messages);

                                    if (mailingBaseForm.getMailingType() == MailingTypes.FOLLOW_UP.getCode()) {
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

                                    if (errors == null || errors.size() == 0) {
                                    	saved = saveMailing(mailingBaseForm, request, errors, messages);
                                    }
                                }
                            } else {
                                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.content.illegal.script"));
                            }
                        } else {
                            if (checkFormContainsAlwaysAllowedChangesOnly(mailingBaseForm, request)) {
                                if (validateIllegalScriptElement(mailingBaseForm)) {
                                    String shortname = mailingBaseForm.getShortname();
                                    String description = mailingBaseForm.getDescription();
                                    int campaignId = mailingBaseForm.getCampaignID();
                                    boolean isArchived = mailingBaseForm.isArchived();

                                    loadMailing(mailingBaseForm, request);

                                    mailingBaseForm.setShortname(shortname);
                                    mailingBaseForm.setDescription(description);
                                    mailingBaseForm.setCampaignID(campaignId);
                                    mailingBaseForm.setArchived(isArchived);

                                    saved = saveMailing(mailingBaseForm, request, errors, messages);
                                } else {
                                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.content.illegal.script"));
                                }
                            } else {
                                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
                            }
                        }
            		}

                    if (saved) {
                        prepareMailingView(request, mailingBaseForm, session, false, errors, messages);
                        destination = mapping.findForward("view");
                    } else {
                        destination = mapping.findForward("messages");
                    }
                    
                    insertTargetModeCheckboxControlAttribute(request, mailingBaseForm, admin);
					break;

				case ACTION_CONFIRM_DELETE:
					final ObjectUsages objectUsages = getObjectUsageService().listUsageOfMailing(companyId, mailingBaseForm.getMailingID());
					
					if(objectUsages.isEmpty()) {
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
					} else {
						final boolean fromListPage = Boolean.parseBoolean(request.getParameter("fromListPage"));
						
						ObjectUsagesToActionMessages.objectUsagesToActionMessages("error.mailing.used", "error.mailing.used.withMore", objectUsages, errors, AgnUtils.getLocale(request));
						saveErrors(request, errors);
						
						destination = fromListPage ? mapping.findForward("list") : mapping.findForward("view");
					}
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
                    updateForwardParameters(request, true);

                    cloneForwardParams = mailingBaseForm.getWorkflowForwardParams();
                    forwardTargetItemId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
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
                    updateForwardParameters(request, true);
                    destination = super.execute(mapping, form, request, response);
                    writeUserActivityLog(AgnUtils.getAdmin(request), "mailings list", "active tab - overview");
                    
                    AgnUtils.setAdminDateTimeFormatPatterns(request);
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
                case ACTION_IMPORT_TEMPLATES:
                    if (mailingBaseForm.getUploadFile() == null) {
                        destination = viewInitialImport(mailingBaseForm.getAction(), request, mapping);
                        break;
                    }

                    if (mailingBaseForm.getUploadFile() != null && mailingBaseForm.getUploadFile().getFileSize() == 0) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.file.missingOrEmpty"));
                        destination = viewInitialImport(mailingBaseForm.getAction(), request, mapping);
                        break;
                    }

                    ImportResult result;
                    if (mailingBaseForm.getUploadFile().getFileName().toLowerCase().endsWith(".json")) {
	                    try (InputStream input = mailingBaseForm.getUploadFile().getInputStream()) {
	                        // Import mailing data from upload file
	                        result = mailingImporter.importMailingFromJson(admin.getCompanyID(), input, mailingBaseForm.isIsTemplate(), mailingBaseForm.isImportTemplateOverwrite(), mailingBaseForm.isIsGrid());
	                    } catch (Exception e) {
	                        logger.error("Mailing import failed", e);
	                        result = ImportResult.builder().setSuccess(false).addError("error.mailing.import").build();
	                    }
                    } else if (mailingBaseForm.getUploadFile().getFileName().toLowerCase().endsWith(".zip")) {
                    	// Unzip file to temp directory and import each included json file
                		File tempZipFile = File.createTempFile("Mailing_Import", ".json.zip");
                		File tempUnzippedDirectory = new File(tempZipFile.getAbsolutePath() + ".unzipped");
                		try {
	                    	try (InputStream inputZipped = mailingBaseForm.getUploadFile().getInputStream();
	                    			FileOutputStream tempZipFileOutputStream = new FileOutputStream(tempZipFile)) {
	                         	IOUtils.copy(inputZipped, tempZipFileOutputStream);
	                    	}
	                    	
	                    	ZipUtilities.decompress(tempZipFile, tempUnzippedDirectory);
	                    	
	                    	for (File tempUnzippedImportFile : tempUnzippedDirectory.listFiles()) {
	                    		if (tempUnzippedImportFile.getName().toLowerCase().endsWith(".json")) {
	                    			try (InputStream dataInputStream = new FileInputStream(tempUnzippedImportFile)) {
	    		                        // Import mailing data from upload file
	    		                        result = mailingImporter.importMailingFromJson(admin.getCompanyID(), dataInputStream, mailingBaseForm.isIsTemplate(), mailingBaseForm.isImportTemplateOverwrite(), mailingBaseForm.isIsGrid());
	    		                    } catch (Exception e) {
	    		                        logger.error("Mailing import failed", e);
	    		                        result = ImportResult.builder().setSuccess(false).addError("error.mailing.import").build();
	    		                    }
		                    	}
	                    	}
                		} finally {
                			result = null;
                			if (tempZipFile.exists()) {
                				tempZipFile.delete();
                			}
                			if (tempUnzippedDirectory.exists()) {
                				FileUtils.removeRecursively(tempUnzippedDirectory);
                			}
                		}
                    } else {
                    	result = ImportResult.builder().setSuccess(false).addError("error.import.invalidDataType", ".json, .zip").build();
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.invalidDataType", ".json, .zip"));
                        mailingBaseForm.setAction(ACTION_LIST);
                    }

                    if (result == null) {
                    	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.data.missing"));
                    	destination = viewInitialImport(mailingBaseForm.getAction(), request, mapping);
                    } else if (!result.isSuccess()) {
                        for (Entry<String, Object[]> errorEntry : result.getErrors().entrySet()) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(errorEntry.getKey(), errorEntry.getValue()));
                        }
                        destination = viewInitialImport(mailingBaseForm.getAction(), request, mapping);
                    } else {
                        int altgId = adminService.getAccessLimitTargetId(admin);
                        if (altgId > 0) {
                    		String targetExpression = mailingDao.getTargetExpression(result.getMailingID(), companyId);
                    		if (StringUtils.isNotBlank(targetExpression)) {
                    			targetExpression = targetExpression.replace("|", "&");
                    		}
                    		if (!TargetUtils.getInvolvedTargetIds(targetExpression).contains(altgId)) {
                    			targetExpression = altgId + (StringUtils.isBlank(targetExpression) ? "" : "&" + targetExpression);
                    		}
                    		mailingDao.setTargetExpression(result.getMailingID(), companyId, targetExpression);
                    	}
                    	
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.imported"));
                        for (Entry<String, Object[]> warningEntry : result.getWarnings().entrySet()) {
                            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage(warningEntry.getKey(), warningEntry.getValue()));
                        }
                        writeUserActivityLog(AgnUtils.getAdmin(request), "import mailing", result.getMailingID());
    
                        destination = viewImportedMailing(result, mailingBaseForm, request, mapping, session, errors, messages);
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

            request.setAttribute("localeDatePattern", admin.getDateFormat().toPattern());
            request.setAttribute("isCampaignEnableTargetGroups",
                    configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, AgnUtils.getCompanyID(request)));

            mailingBaseForm.setTemplateMailingBases(mailingDao.getTemplateMailingsByCompanyID(companyId));
            prepareMailinglists(mailingBaseForm, AgnUtils.getAdmin(request));
            mailingBaseForm.setCampaigns(campaignDao.getCampaignList(companyId, "lower(shortname)", 1));
            mailingBaseForm.setTargetGroupsList(targetService.getTargetLights(companyId, mailingBaseForm.getTargetGroups(), true));
            mailingBaseForm.setTargets(targetService.getTargetLights(admin));
            mailingBaseForm.setTargetComplexities(targetService.getTargetComplexities(companyId));
            mailingBaseForm.setSplitTargets(targetService.getSplitTargetLights(companyId, ""));
            mailingBaseForm.setSplitTargetsForSplitBase(targetService.getSplitTargetLights(companyId, mailingBaseForm.getSplitBase()));
            loadReferenceTables(request, mailingBaseForm);
            addExtendedViewParams(request, mailingBaseForm);
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
					request.setAttribute("localeTablePattern", admin.getDateTimeFormat().toPattern());
				}
			} else if ("view".equals(forward) || "grid_base".equals(forward)) {
				request.setAttribute("localDatePattern", admin.getDateFormat().toPattern());
                setCanUserChangeGeneralSettings(mailingBaseForm, request);
                request.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, AgnUtils.getCompanyID(request)));
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

        if (session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS) == null) {
            session.setAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS, mailingBaseForm.getWorkflowForwardParams());
        }
        
    	addUiControlAttributes(admin, request, mailingBaseForm);

		return destination;
	}

    private void warnIfTargetsHaveDisjunction(ComAdmin admin, ComMailingBaseForm form, ActionMessages messages) {
        if (form.getMailingType() == MailingTypes.DATE_BASED.getCode()) {
            if (CollectionUtils.size(form.getTargetGroups()) > 1) {
                boolean conjunction = isAltgEnabled(admin) || form.getTargetMode() == Mailing.TARGET_MODE_AND;
                if (!conjunction) {
                    messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.target.disjunction"));
                }
            }
        }
    }

    private void insertTargetModeCheckboxControlAttribute(final HttpServletRequest request, final ComMailingBaseForm mailingBaseForm, final ComAdmin admin) {
    	request.setAttribute("SHOW_TARGET_MODE_CHECKBOX", this.isTargetModeCheckboxVisible(mailingBaseForm));
    	request.setAttribute("DISABLE_TARGET_MODE_CHECKBOX", this.isTargetModeCheckboxDisabled(admin, mailingBaseForm));
	}

	private boolean validateIllegalScriptElement(ComMailingBaseForm mailingBaseForm) {
        if (HtmlUtils.containsElementByTag(mailingBaseForm.getHtmlTemplate(), "script") ||
                HtmlUtils.containsElementByTag(mailingBaseForm.getTextTemplate(), "script")) {
            return false;
        }

        return true;
    }

	private ActionForward viewInitialImport(int action, HttpServletRequest request, ActionMapping mapping) {
        if (ACTION_IMPORT_TEMPLATES == action) {
            return mapping.findForward("importView");
        }
    
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
    
    protected ActionForward viewImportedMailing(final ImportResult result, final ComMailingBaseForm mailingBaseForm, final HttpServletRequest request, final ActionMapping mapping, final HttpSession session, ActionMessages errors, ActionMessages messages) throws Exception {
        mailingBaseForm.setMailingID(result.getMailingID());
        mailingBaseForm.setTemplateID(0);
    
        writeUserActivityLog(AgnUtils.getAdmin(request), result.isTemplate() ? "import template" : "import mailing", result.getMailingID());
        
		prepareMailingView(request, mailingBaseForm, session);
		return mapping.findForward("view");
	}

	protected void prepareMailingView(HttpServletRequest req, ComMailingBaseForm mailingBaseForm, HttpSession session) throws Exception {
        prepareMailingView(req, mailingBaseForm, session, false, null, null);
    }
    
	protected Mailing prepareMailingView(HttpServletRequest request, ComMailingBaseForm mailingBaseForm, HttpSession session, boolean showTagWarnings, ActionMessages errors, ActionMessages messages) throws Exception {
		Integer forwardTargetItemId;
		updateForwardParameters(request, true);
		forwardTargetItemId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
		if (forwardTargetItemId != null && forwardTargetItemId != 0) {
		    mailingBaseForm.setMailingID(forwardTargetItemId);
		}
		mailingBaseForm.setAction(ACTION_SAVE);
		resetShowTemplate(request, mailingBaseForm);
		String workflowForwardParams = mailingBaseForm.getWorkflowForwardParams();
		if (StringUtils.isBlank(workflowForwardParams)) {
            Object sessionParams = session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS);
            if (sessionParams != null) {
                workflowForwardParams = StringUtils.trimToEmpty((String) sessionParams);
            }
        }
        
        Mailing mailing = loadMailing(mailingBaseForm, request, true, showTagWarnings, errors, messages);

		if (mailingBaseForm.getWorkflowId() == 0) {
		    Integer workflowId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
		    if (workflowId != null && workflowId > 0) {
		        mailingBaseForm.setWorkflowId(workflowId);
		    }
		}
		mailingBaseForm.setWorkflowForwardParams(workflowForwardParams);
		Map<String, String> forwardParams = AgnUtils.getParamsMap((String) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS));
		processAdditionalForwardParams(mailingBaseForm, forwardParams);
		
		request.setAttribute("limitedRecipientOverview",
				mailingBaseForm.isWorldMailingSend() &&
						!mailinglistApprovalService.isAdminHaveAccess(AgnUtils.getAdmin(request), mailingBaseForm.getMailinglistID()));
		
		requestAttributesHelper.addMailingEditableAttribute(mailing, AgnUtils.getAdmin(request), request);
		
		return mailing;
    }

	protected void setMailingWorkflowParameters(HttpServletRequest request, ComMailingBaseForm aForm) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        WorkflowParameters workflowParameters = WorkflowParametersHelper.defaultIfEmpty(request, aForm.getWorkflowId());
        if (workflowParameters.getWorkflowId() > 0) {
            int workflowId = workflowParameters.getWorkflowId();
            int mailingIconId = workflowParameters.getNodeId();
            
            Map<String, String> forwardParams = workflowParameters.getParamsAsMap();

            Mailing mailing = new MailingImpl();
            workflowService.assignWorkflowDrivenSettings(admin, mailing, workflowId, mailingIconId);
            aForm.setSplitId(mailing.getSplitID());
            aForm.setMailinglistID(mailing.getMailinglistID());
            aForm.setTargetGroups(mailing.getTargetGroups());
            aForm.setTargetMode(mailing.getTargetMode());
            aForm.setTargetExpression(mailing.getTargetExpression());
            aForm.setComplexTargetExpression(mailing.hasComplexTargetExpression());
            if (mailing.getPlanDate() != null) {
                SimpleDateFormat dateFormat = admin.getDateFormat();
                aForm.setPlanDate(dateFormat.format(mailing.getPlanDate()));
            }
            aForm.setMailingType(NumberUtils.toInt(forwardParams.get("mailingType"), MailingTypes.NORMAL.getCode()));
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
            Mailing followupMailing = mailingDao.getMailing(followupMailingID, companyId);
            boolean deliveryCancelled = cancelMailingDelivery(MailingTypes.FOLLOW_UP.getCode(), followupMailingID, followupMailing.getShortname(), req);
            followupMailing.setParameters(mailingParameterService.getMailingParameters(companyId, followupMailingID));
            if (deliveryCancelled) {
                mailingDao.updateStatus(followupMailingID, MailingStatus.CANCELED);
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
            LightweightMailing followupMailing = mailingDao.getLightweightMailing(companyID, followupMailingID);
            boolean deliveryCancelled = cancelMailingDelivery(MailingTypes.FOLLOW_UP.getCode(), followupMailingID, followupMailing.getShortname(), req);
            if (deliveryCancelled) {
                mailingDao.updateStatus(followupMailingID, MailingStatus.CANCELED);
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
    	try {
	    	final boolean isDeliveryCancelled = this.mailingStopService.stopMailing(AgnUtils.getCompanyID(req), mailingID, false);
	    	
	        if (isDeliveryCancelled) {
	            final String description = String.format("%s (%d) %s", shortname, mailingID, mailingTypeToString(cachedMailtype));
	            writeUserActivityLog(AgnUtils.getAdmin(req), "do cancel mailing", description);
	        }
	        
	        return isDeliveryCancelled;
    	} catch(final MailingStopServiceException e) {
    		logger.error(String.format("Error stopping mailing %d", mailingID), e);
    		
    		return false;
    	}
    }

    private boolean validatePlanDate(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors) {
        String planDateAsString = form.getPlanDate();

        if (StringUtils.isNotEmpty(planDateAsString)) {
            ComAdmin admin = AgnUtils.getAdmin(req);

            assert (admin != null);

            TimeZone timezone = AgnUtils.getTimeZone(admin);
            DateFormat dateFormat = admin.getDateFormat();

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
        workflowId = (Integer) req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);

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

		if (form.getMailingType() == MailingTypes.DATE_BASED.getCode() ||
                form.getMailingType() == MailingTypes.INTERVAL.getCode()) {
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
            } else if (!isBaseMailingTrackingDataAvailable(baseMailingId, AgnUtils.getAdmin(req))) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.followup.basemail_data_not_exists"));
            }
        }
    }

    private boolean isBaseMailingTrackingDataAvailable(int baseMailingId, ComAdmin admin) {
        List<Mailing> availableBaseMailings;
        availableBaseMailings = mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(), ComMailingLightService.TAKE_ALL_MAILINGS, "W", true);
        // Check if base mailing tracking data is available
        for (Mailing mailing : availableBaseMailings) {
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
    	if (maildropService.isActiveMailing(form.getMailingID(), AgnUtils.getCompanyID(request))) {
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
            String targetExpression = generateTargetExpression(AgnUtils.getAdmin(request), form);
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
        } catch (UserMessageException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
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

    private String generateTargetExpression(ComAdmin admin, ComMailingBaseForm form) {
        String targetExpression = form.getTargetExpression();

        if (StringUtils.isBlank(targetExpression)) {
        	boolean conjunction = isAltgEnabled(admin) || form.getTargetMode() == Mailing.TARGET_MODE_AND;
            targetExpression = TargetExpressionUtils.makeTargetExpression(form.getTargetGroups(), conjunction);
        }

        return targetExpression;
    }

    protected boolean saveMailing(ComMailingBaseForm form, HttpServletRequest req, ActionMessages errors, ActionMessages messages) {
        boolean saved = false;
        try {
            saveMailingData(form, req, errors, messages);

            if (form.isMailingChanged() && form.getMailingID() != 0) {
                mailingService.updateStatus(form.getCompanyID(), form.getMailingID(), MailingStatus.EDIT);
            }

            saved = true;

            String workflowForwardParams = form.getWorkflowForwardParams();
            int workflowId = form.getWorkflowId();

            form.clearData(true);
            Mailing mailing = loadMailing(form, req);

            if (form.getWorkflowId() == 0) {
                form.setWorkflowId(workflowId);
            }
            form.setWorkflowForwardParams(workflowForwardParams);

            previewImageService.generateMailingPreview(AgnUtils.getAdmin(req), req.getSession(false).getId(), form.getMailingID(), true);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

            validateMailingModules(mailing);
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
        return saved;
    }

	/**
	 * Saves current mailing in DB (including mailing components, content blocks, dynamic tags, dynamic tags contents
	 * and trackable links)
	 *
	 * @param form struts form bean object
	 * @param request  request
	 * @param warnings  warnings
	 * @throws Exception if anything went wrong
	 */
	protected void saveMailingData(ComMailingBaseForm form, HttpServletRequest request, ActionMessages errors, ActionMessages warnings) throws Exception {
        final int companyId = AgnUtils.getCompanyID(request);
        final ComAdmin admin = AgnUtils.getAdmin(request);
        final ApplicationContext applicationContext = getApplicationContext(request);
        
		Mailing aMailing;
        int gridTemplateId = 0;
		boolean mailingIsNew;
		if (mailingBaseService.isMailingExists(form.getMailingID(), companyId)) {
			// Use existing mailing and fill in changed data from form afterwards
			aMailing = mailingDao.getMailing(form.getMailingID(), companyId);
			mailingIsNew = false;
		} else if (mailingBaseService.isMailingExists(form.getTemplateID(), companyId)) {
			// Make a copy of the template mailing and fill in changed data from form afterwards
			final String type = (form.isIsTemplate() ? "template" : "mailing");
			int copiedMailingID = copyMailingService.copyMailing(companyId, form.getTemplateID(), companyId, form.getShortname(), form.getDescription());
			writeUserActivityLog(admin, "create " + type, "Copy from ID: " + form.getTemplateID() + ", to ID: " + copiedMailingID);
			aMailing = mailingDao.getMailing(copiedMailingID, companyId);
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
            if (mediatypeEmail.getMailFormat() == MailingModel.Format.TEXT.getCode()) {
                mediatypeEmail.setHtmlTemplate("");
            } else if (form.getOldMailFormat() == MailingModel.Format.TEXT.getCode()) {
                if (StringUtils.isEmpty(mediatypeEmail.getHtmlTemplate())) {
                    mediatypeEmail.setHtmlTemplate("[agnDYN name=\"HTML-Version\"/]");
                }
            }

            if (StringUtils.isNotEmpty(mediatypeEmail.getHtmlTemplate())) {
            	validateLinks(companyId, mediatypeEmail.getHtmlTemplate(), mediatypeEmail.getMailingID(),  form.getMailinglistID(), errors, warnings);
            }
        }

        if (form.getMailingType() == MailingType.ACTION_BASED.getCode() &&
                !configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, companyId)) {
            form.clearTargetsData();
        }

		int splitId = targetService.getTargetListSplitId(form.getSplitBase(), form.getSplitPart(), form.isWmSplit());
        if(splitId == Mailing.YES_SPLIT_ID) {//-1 Should not be saved to DB
            splitId = Mailing.NONE_SPLIT_ID;
        }
        if (splitId != aMailing.getSplitID()) {
            if (splitId == Mailing.NONE_SPLIT_ID) {
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
        aMailing.setFrequencyCounterDisabled(form.isFrequencyCounterDisabled());

        if (!aMailing.hasComplexTargetExpression() && form.getAssignTargetGroups()) {
        	// Only change target expressions, that are NOT complex and not managed by workflowmanager
        	
	        boolean useOperatorAnd;
	        if (isTargetModeCheckboxVisible(form) && !isTargetModeCheckboxDisabled(admin, form)) {
				useOperatorAnd = isAltgEnabled(admin) || form.getTargetMode() == Mailing.TARGET_MODE_AND;
	        } else {
	        	// Use currently set targetgroup operator
	        	useOperatorAnd = StringUtils.isBlank(aMailing.getTargetExpression()) || !aMailing.getTargetExpression().contains(TargetExpressionUtils.OPERATOR_OR);
	        }
	        
	        String newTargetExpression = form.getTargetExpression();
	        if (StringUtils.isBlank(newTargetExpression)) {
	        	newTargetExpression = TargetExpressionUtils.makeTargetExpression(form.getTargetGroups(), useOperatorAnd);
	        }
		    aMailing.setTargetExpression(newTargetExpression);
        }
        
        form.setLocked(true);
        aMailing.setLocked(form.getLocked() ? 1 : 0);
		aMailing.setNeedsTarget(form.isNeedsTarget());
		aMailing.setMediatypes(form.getMediatypes());
		aMailing.setUseDynamicTemplate(form.getUseDynamicTemplate());

        Date mailingPlanDate = null;
        if (StringUtils.isNotEmpty(form.getPlanDate())) {
            try {
                mailingPlanDate = admin.getDateFormat().parse(form.getPlanDate());
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
			if (mailingType == MailingTypes.FOLLOW_UP.getCode()) {
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
            
            if (mailingType != MailingTypes.DATE_BASED.getCode()) {
			    media.deleteDateBasedParameters();
            }

			for (MediaTypes type : MediaTypes.values()) {
                int code = type.getMediaCode();
                if (form.getUseMediaType(code)) {
                    form.getMedia(code).syncTemplate(aMailing, applicationContext);
                }
            }

			List<String> dynNamesForDeletion = new Vector<>();
			aMailing.buildDependencies(true, dynNamesForDeletion, applicationContext, warnings, errors, admin);

			// Mark names in dynNamesForDeletion as "deleted"
            dynamicTagDao.markNamesAsDeleted(aMailing.getId(), dynNamesForDeletion);
        } catch (Exception e) {
			logger.error("Error in save mailing id: " + form.getMailingID(), e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.content"));
		}

		if (validateMailingListId(aMailing, request, errors)) {
            validateMailingTagsAndComponents(aMailing, request, errors, warnings);
        }

		aMailing.getComponents().forEach((name, component) -> {
			if (component.getEmmBlock() != null) {
	            String text = AgnTagUtils.unescapeAgnTags(component.getEmmBlock());
	            component.setEmmBlock(text, component.getMimeType());

	            Integer rdirLinkLine = linkService.getLineNumberOfFirstRdirLink(companyId, text);
	            if (rdirLinkLine != null) {
	                warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.encoded", name, rdirLinkLine));
	            }
			}
        });

		ComMailingContentChecker.checkHtmlWarningConditions(aMailing, warnings);

        if (form.getUseMediaEmail()) {
            mailingBaseService.doTextTemplateFilling(aMailing, admin, warnings);
        }
        mailingBaseService.saveMailingWithUndo(aMailing, admin.getAdminID(), AgnUtils.allowed(request, Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
        form.setMailingID(aMailing.getId());
        form.setGridTemplateId(gridTemplateId);

        if (gridTemplateId > 0) {
            saveMailingGridInfo(form, request);
        }

	    if (AgnUtils.allowed(request, Permission.MAILING_CHANGE)) {
            List<ComMailingParameter> parameters = collectMailingParameters(form, request, mailingIsNew ? null : userActions, warnings);
            aMailing.setParameters(parameters);
	    	setMailingParameters(admin, aMailing.getId(), parameters);
	    }

        saveReferenceContentSettings(form, request, errors, warnings);

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

    protected void saveReferenceContentSettings(ComMailingBaseForm mailingBaseForm, HttpServletRequest request, ActionMessages errors, ActionMessages messages) {
	    // Do nothing.
    }

    private void validateLinks(int companyId,  String htmlTemplate, int mailingId, int mailingListId, ActionMessages errors, final ActionMessages warnings) {
	    try {
            LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(htmlTemplate, mailingId, mailingListId, companyId);
            List<LinkService.ErrorneousLink> linksWithErrors = linkScanResult.getErrorneousLinks();
            if (CollectionUtils.isNotEmpty(linksWithErrors)) {
                linksWithErrors.forEach(link -> errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(link.getErrorMessageKey(), link.getLinkText())));
            }
            
            final List<LinkService.ErrorneousLink> localLinks = linkScanResult.getLocalLinks();
            if (CollectionUtils.isNotEmpty(localLinks)) {
            	localLinks.forEach(link -> errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT, new ActionMessage(link.getErrorMessageKey(), link.getLinkText())));
            }
            
            LinkScanResultToActionMessages.linkWarningsToActionMessages(linkScanResult, warnings);
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
    protected MailingsQueryWorker createMailingsQueryWorker(ActionMessages errors, MailingBaseForm mailingBaseForm, HttpServletRequest request, int companyId, int adminId, String types, boolean isTemplate, String sort, String direction, int page, int rownums, final boolean includeTargetGroups) throws Exception {
    	ComAdmin admin = AgnUtils.getAdmin(request);
    	
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
            mailingsListProps.setSendDateBegin(StringUtils.isBlank(filterSendDateBeginAsString) ? null : admin.getDateFormat().parse(filterSendDateBeginAsString));
            mailingsListProps.setSendDateEnd(StringUtils.isBlank(filterSendDateEndAsString) ? null : admin.getDateFormat().parse(filterSendDateEndAsString));
		}

        mailingsListProps.setSort(sort);
        mailingsListProps.setDirection(direction);
        mailingsListProps.setPage(page);
        mailingsListProps.setRownums(rownums);
        mailingsListProps.setIncludeTargetGroups(includeTargetGroups);
        mailingsListProps.setAdditionalColumns(getAdditionalColumns(mailingBaseForm));
        mailingsListProps.setCreationDateBegin(StringUtils.isBlank(comMailingBaseForm.getFilterCreationDateBegin()) ? null : admin.getDateFormat().parse(comMailingBaseForm.getFilterCreationDateBegin()));
        mailingsListProps.setCreationDateEnd(StringUtils.isBlank(comMailingBaseForm.getFilterCreationDateEnd()) ? null : admin.getDateFormat().parse(comMailingBaseForm.getFilterCreationDateEnd()));
		mapExtendedProperties(mailingsListProps, request);

        return new MailingsQueryWorker(mailingDao, companyId, adminId, mailingsListProps);
    }

	protected void mapExtendedProperties(final MailingsListProperties targetProperties, final HttpServletRequest request) {
		// nothing to do by now
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

        if (form.getMailingType() == MailingTypes.INTERVAL.getCode()) {
            String value = form.getIntervalParameterValue();
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
	            map.put(i, parameters.get(i));
            }
        }

        return map;
    }

	private void loadIntervalData(ComMailingBaseForm form, ComMailingParameter intervalParameter) {
		Pattern monthRulePattern = Pattern.compile("\\d{0,2}M\\d{2}:\\d{4}");
		Pattern weekdailyRulePattern = Pattern.compile("\\d\\D\\D:\\d{4}(;\\d\\D\\D:\\d{4})*");

		if (intervalParameter != null && StringUtils.isNotBlank(intervalParameter.getValue())) {
			String interval = intervalParameter.getValue();
			if (interval.contains("*")) {
				form.setIntervalType(IntervalType.Short);
				form.setIntervalDays(new boolean[7]);
			} else if (monthRulePattern.matcher(interval).matches()) {
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

	private void updateMailingsWithDynamicTemplate(Mailing template, HttpServletRequest request) {
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
	protected Mailing loadMailing(MailingBaseForm form, HttpServletRequest req) throws Exception {
		return loadMailing((ComMailingBaseForm) form, req, true, false, null, null);
	}

	protected Mailing loadMailing(ComMailingBaseForm form, HttpServletRequest request, boolean preserveCmListSplit, boolean showTagWarnings, ActionMessages errors, ActionMessages messages) throws Exception {
	    final ComAdmin admin = AgnUtils.getAdmin(request);
	    final int companyId = admin.getCompanyID();

		Mailing aMailing;
		if (form.getMailingID() > 0) {
			aMailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(request));
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
        form.setMailingListFrequencyCountEnabled(mailinglistService.isFrequencyCounterEnabled(admin, aMailing.getMailinglistID()));
        form.setFrequencyCounterDisabled(form.isMailingListFrequencyCountEnabled() && aMailing.isFrequencyCounterDisabled());
        form.setWorldMailingSend(mailingPropertiesRules.mailingIsWorldSentOrActive(aMailing));

        loadMailingGridTemplate(form, request);

        if (aMailing.getPlanDate() != null) {
            SimpleDateFormat dateFormat = admin.getDateFormat();
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

		final String[] labels = { "Text", "FAX", "Post", "MMS", "SMS" };
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

		loadReferenceContentSettings(form, request);

        if (aMailing.getId() > 0) {
            form.setWorkflowId(mailingDao.getWorkflowId(aMailing.getId()));
        }

        if (form.getMailingID() > 0) {
            form.setMailingEditable(isMailingEditable(form, request));
        } else {
            form.setMailingEditable(true);
        }
        
        if (showTagWarnings) {
            try {
                validateMailingTagsAndComponents(aMailing, request, errors, messages);
            } catch (AgnTagException e) {
                request.setAttribute("errorReport", e.getReport());
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags"));
            }
        }

		if (logger.isInfoEnabled()) {
			logger.info("loadMailing: mailing loaded");
		}
		
		return aMailing;
	}

	protected void addExtendedViewParams(final HttpServletRequest request, final ComMailingBaseForm baseForm) {
		// Do nothing
	}

    protected void loadReferenceTables(HttpServletRequest req, ComMailingBaseForm mailingBaseForm) {
	    // Do nothing.
    }

    protected void loadReferenceContentSettings(ComMailingBaseForm mailingBaseForm, HttpServletRequest request) {
	    // Do nothing.
    }

    private void loadMailingGridTemplate(ComMailingBaseForm form, HttpServletRequest request) {
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
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), form.getCompanyID());
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
	protected void copyTemplateSettingsToMailingForm(Mailing template, MailingBaseForm mailingBaseForm, HttpServletRequest req, boolean regularTemplate) {
	    ComMailingBaseForm form = (ComMailingBaseForm) mailingBaseForm;
        MailingComponent tmpComp;
        Integer workflowId = (Integer) req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
		// If we already have a campaign we don't have to override settings inherited from it
		boolean overrideInherited = (workflowId == null || workflowId == 0 || !regularTemplate);

		if (overrideInherited) {
            form.setMailingType(template.getMailingType());
            form.setMailinglistID(template.getMailinglistID());
            form.setCampaignID(template.getCampaignID());
		}

		if (overrideInherited || template.getMailingType() == MailingTypes.DATE_BASED.getCode()) {
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

        NumberFormat numberFormatCount = NumberFormat.getNumberInstance(admin.getLocale());
        DecimalFormat decimalFormatCount = (DecimalFormat) numberFormatCount;
        decimalFormatCount.applyPattern(",###");

        try {
            data.element("count", decimalFormatCount.format(mailingBaseService.calculateRecipients(config)));
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

    /**
     * Validate agn-tags and dyn-tags.
     */
    private void validateMailingModules(Mailing mailing) throws DynTagException {
        validateMailingModule(mailing.getTextTemplate());
        validateMailingModule(mailing.getHtmlTemplate());
    }

    private void validateMailingModule(MailingComponent template) throws DynTagException {
        if (template != null) {
            agnTagService.getDynTags(template.getEmmBlock(), agnDynTagGroupResolverFactory.create(template.getCompanyID(), template.getMailingID()));
        }
    }

    private boolean validateMailingListId(Mailing mailing, HttpServletRequest request, ActionMessages errors) {
        if (mailinglistService.existAndEnabled(AgnUtils.getAdmin(request), mailing.getMailinglistID())) {
            return true;
        } else {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.noMailinglist"));
            return false;
        }
    }

    private void validateMailingTagsAndComponents(Mailing mailing, HttpServletRequest request, ActionMessages errors, ActionMessages messages) throws Exception {
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
        } else {
            Map<String, MailingComponent> components =  mailing.getComponents();
            if (validateDeprecatedTags(mailing.getCompanyID(), components, messages) && mailing.getId() > 0) {
                // Only use backend/preview agn syntax check if mailing was already stored in database before (= not new mailing)
                Vector<String> outFailures = new Vector<>();
                TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailing.getCompanyID(), mailing.getId(), mailing.getMailinglistID(), AgnUtils.getLocale(request));

                try {
                    components.forEach((name, component) -> {
                        StringBuffer reportContents = new StringBuffer();
                        if (component.getEmmBlock() != null && !tagCheck.checkContent(component.getEmmBlock(), reportContents, outFailures)) {
                            appendErrorsToList(name, errorReports, reportContents);
                        }
                    });
                } finally {
                    tagCheck.done();
                }
            }
        }

        if (CollectionUtils.isNotEmpty(errorReports)) {
            throw new AgnTagException("error.template.dyntags", errorReports);
        }
    }

    private boolean validateDeprecatedTags(final int companyId, final Map<String, MailingComponent> components, final ActionMessages messages) {
        boolean valid = true;
        for (MailingComponent component : components.values()) {
            if (component.getEmmBlock() != null) {
                final Set<String> deprecatedTagsNames = agnTagService.parseDeprecatedTagNamesFromString(component.getEmmBlock(), companyId);
                if (CollectionUtils.isNotEmpty(deprecatedTagsNames)) {
                    deprecatedTagsNames.forEach(el -> messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.agntag.deprecated", el)));
                    valid = false;
                }
            }
        }
        return valid;
    }

    protected void loadMailingDataForNewMailing(MailingBaseForm form, HttpServletRequest req) {
        final ComMailingBaseForm aForm = (ComMailingBaseForm) form;
        final int mailingOriginId = aForm.getMailingID();

        try {
            Mailing origin = null;
            if (mailingOriginId > 0) {
                origin = mailingDao.getMailing(mailingOriginId, AgnUtils.getCompanyID(req));
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
            Mailing origin = null;
            if (mailingOriginId > 0) {
                origin = mailingDao.getMailing(mailingOriginId, AgnUtils.getCompanyID(req));
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
                aForm.setDescription("");
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

                aForm.setMailingType(MailingTypes.FOLLOW_UP.getCode());
                MediatypeEmail mediatype = origin.getEmailParam();
                mediatype.setFollowupFor(Integer.toString(origin.getId()));
                mediatype.setFollowUpMethod(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey());
                Map<Integer, Mediatype> mediatypes = new HashMap<>();
                mediatypes.put(0, mediatype);
                aForm.setMediatypes(mediatypes);
            }

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
        aForm.setSplitBase(splitId == Mailing.YES_SPLIT_ID ? Mailing.YES_SPLIT : Mailing.NONE_SPLIT);
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
			Mailing template = mailingDao.getMailing(aForm.getTemplateID(), AgnUtils.getCompanyID(req));
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
                    storage.setSearchQueryText(form.getSearchQueryText());
                    storage.setSelectedFields(Arrays.asList(form.getSelectedFields()));
                    storage.setMailingTypeNormal(form.getMailingTypeNormal());
                    storage.setMailingTypeDate(form.getMailingTypeDate());
                    storage.setMailingTypeEvent(form.getMailingTypeEvent());
                    storage.setMailingTypeFollowup(form.isMailingTypeFollowup());
                    storage.setMailingTypeInterval(form.isMailingTypeInterval());
                } else {
                    form.setSearchQueryText("");
                }
            } else {
                form.setNumberOfRows(storage.getRowsCount());

                if (!form.isIsTemplate()) {
                    form.setSearchQueryText(storage.getSearchQueryText());
                    form.setSelectedFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    form.setMailingTypeNormal(storage.isMailingTypeNormal());
                    form.setMailingTypeDate(storage.isMailingTypeDate());
                    form.setMailingTypeEvent(storage.isMailingTypeEvent());
                    form.setMailingTypeFollowup(storage.isMailingTypeFollowup());
                    form.setMailingTypeInterval(storage.isMailingTypeInterval());
                } else {
                    form.setSearchQueryText("");
                }
            }
        });
    }
	
	private void addUiControlAttributes(final ComAdmin admin, final HttpServletRequest request, final MailingBaseForm form) {
        int altgID = adminService.getAccessLimitTargetId(admin);
        request.setAttribute("ALTG_ENABLED", altgID > 0);
		
		if (altgID > 0) {
			form.setTargetMode(Mailing.TARGET_MODE_AND);
		}
	}
	
	private final boolean isTargetModeCheckboxDisabled(final ComAdmin admin, final ComMailingBaseForm form) {
		return isMailingWorkflowDriven(form) || isMailingWorldSent(form) || isAltgEnabled(admin);
	}
	
	private final boolean isTargetModeCheckboxVisible(final ComMailingBaseForm form) {
		return !(isComplexTargetExpression(form) || ((isMailingWorkflowDriven(form) || isMailingWorldSent(form)) && targetExpressionSize(form) < 2));
	}
	
	private final boolean isMailingWorkflowDriven(final ComMailingBaseForm form) {
		return form.getWorkflowId() > 0;
	}
	
	private final boolean isMailingWorldSent(final ComMailingBaseForm form) {
		return form.isWorldMailingSend();
	}
	
	private final boolean isAltgEnabled(final ComAdmin admin) {
		return adminService.getAccessLimitTargetId(admin) > 0;
	}
	
	private final boolean isComplexTargetExpression(final ComMailingBaseForm form) {
		return form.isComplexTargetExpression();
	}
	
	private final int targetExpressionSize(final ComMailingBaseForm form) {
		return form.getTargetGroupIds().length;
	}
    
    @Required
    public void setGridService(GridServiceWrapper gridServiceWrapper) {
	    this.gridService = gridServiceWrapper;
    }

	@Required
	public void setMailingLightService(ComMailingLightService mailingLightService) {
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

    @Required
    public final void setMailingStopService(final MailingStopService service) {
    	this.mailingStopService = Objects.requireNonNull(service, "Mailing stop service is empty");
    }

    @Required
    public void setAgnTagService(final AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }

    @Required
    public void setAgnDynTagGroupResolverFactory(AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory) {
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
    }

    @Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}
    
    @Required
    public final void setMailingPropertiesRules(final MailingPropertiesRules rules) {
    	this.mailingPropertiesRules = Objects.requireNonNull(rules, "MailingPropertiesRules is null");
    }
    
    @Required
    public final void setRequestAttributesHelper(final RequestAttributesHelper helper) {
    	this.requestAttributesHelper = Objects.requireNonNull(helper, "RequestAttributesHelper is null");
    }

    @Required
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
}
