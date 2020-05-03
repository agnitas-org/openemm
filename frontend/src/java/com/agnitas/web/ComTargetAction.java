/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.DateFormatFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.InvalidTypeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownLinkIdFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownMailingIdFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownProfileFieldFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownReferenceTableColumnFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownReferenceTableFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnsupportedOperatorForDataTypeException;
import com.agnitas.emm.core.target.eql.emm.legacy.EqlToTargetRepresentationConversionException;
import com.agnitas.emm.core.target.eql.emm.legacy.TargetRepresentationToEqlConversionException;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxErrorException;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingClickedOnSpecificLink;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingRevenue;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.forms.ComTargetForm;
import org.agnitas.dao.TrackableLinkDao;
import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.dao.exception.target.TargetGroupTooLargeException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.exception.TargetGroupIsInUseException;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.WebStorage;
import org.agnitas.target.TargetFactory;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeFactory;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.TargetRepresentationFactory;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeMailingClicked;
import org.agnitas.target.impl.TargetNodeMailingOpened;
import org.agnitas.target.impl.TargetNodeMailingReceived;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.TargetForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Required;

/**
 * Struts {@link Action} dealing with target groups.
 */
public class ComTargetAction extends StrutsActionBase {

	/**
	 * Internally used class to store structure data of target groups in session.
	 */
	private static class SavedTargetStructureData {
		/** ID of target group. */
		public final int id;

		/** TargetNode-based structure of target grocheckup. */
		public final TargetRepresentation structure;

		/** EQL-based structure of target group. */
		public final String eql;

		/** Flag, if target group is of simple structure. */
		public final boolean simpleStructure;

		/**
		 * Creates new structure data.
		 *
		 * @param id ID of target group
		 * @param structure {@link TargetRepresentation} of target group
		 * @param eql EQL of target group
		 * @param simpleStructure flag indicating simple / complex target group structure
		 */
		public SavedTargetStructureData(final int id, final TargetRepresentation structure, final String eql, boolean simpleStructure) {
			this.id = id;
			this.structure = structure;
			this.eql = eql;
			this.simpleStructure = simpleStructure;
		}

	}

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTargetAction.class);

	/** Name of session attribute holding structure of target group currently edited by user. */
	private static final String SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME = ComTargetAction.class.getCanonicalName() + "#SAVED_TARGET_STRUCTURE";

	public static final int ACTION_BULK_CONFIRM_DELETE = ACTION_LAST + 6;
	public static final int ACTION_BULK_DELETE = ACTION_LAST + 7;
	public static final int ACTION_LOCK_TARGET_GROUP = ACTION_LAST + 8;
	public static final int ACTION_UNLOCK_TARGET_GROUP = ACTION_LAST + 9;
	public static final int ACTION_REBUILD_STRUCTURE_DATA = ACTION_LAST + 10;
	public static final int ACTION_VIEW_MAILINGS = ACTION_LAST + 11;

    private BirtStatisticsService birtStatisticsService;
    private ComBirtReportDao birtReportDao;
    private MailinglistApprovalService mailinglistApprovalService;
    
    /** DAO accessing target groups. */
	protected ComTargetDao targetDao;
    private ComRecipientDao recipientDao;
    private TargetNodeFactory targetNodeFactory;
	protected TargetRepresentationFactory targetRepresentationFactory;
	protected TargetFactory targetFactory;
	protected ConfigService configService;
	protected ComMailingDao mailingDao;
	protected TrackableLinkDao trackableLinkDao;
	private WebStorage webStorage;

    /** Facade providing full EQL functionality. */
    private EqlFacade eqlFacade;
    
    protected ColumnInfoService columnInfoService;
	
    public static final int ACTION_CREATE_ML = ACTION_LAST + 1;

	public static final int ACTION_CLONE = ACTION_LAST + 2;
	
	public static final int ACTION_DELETE_RECIPIENTS_CONFIRM = ACTION_LAST + 3;
	
	public static final int ACTION_DELETE_RECIPIENTS = ACTION_LAST + 4;
	
	public static final int ACTION_BACK_TO_MAILINGWIZARD = ACTION_LAST + 5;
	
	public static final int ACTION_DELETE_FROM_MAILINGWIZARD = ACTION_LAST + 6;
    
    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
        case ACTION_LOCK_TARGET_GROUP:
            return "lock_target_group";
        case ACTION_UNLOCK_TARGET_GROUP:
            return "unlock_target_group";
        case ACTION_REBUILD_STRUCTURE_DATA:
            return "rebuild_structure_data";
            
        case ACTION_CREATE_ML:
            return "create_ml";
        case ACTION_CLONE:
            return "clone";
        case ACTION_DELETE_RECIPIENTS_CONFIRM:
            return "delete_recipients_confirm";
        case ACTION_DELETE_RECIPIENTS:
            return "delete_recipients";
        case ACTION_BACK_TO_MAILINGWIZARD:
            return "back_to_mailingwizard";
        case ACTION_VIEW_MAILINGS:
            return "view_mailings";
            
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
     * ACTION_LIST: loads list of target groups into request. Initializes columns width list for the table if
     *     necessary. Forwards to "list" which leads to targets list page.
     * <br><br>
     * ACTION_VIEW: If the target group ID of form is 0 - clears all the rules data in form.<br><br>
     *     Otherwise loads target from database, sets name and description to form and fills form with data from
     *     targetRepresentation property of target group. TargetRepresentation is an object containing list of
     *     target-nodes; each target-node contains information about one rule of target group such as: profile-fields
     *     used, primary operator, chain operator, brackets for current rule etc. For filling the form action
     *     iterates through the target-nodes of targetRepresentation and puts each node's data to form properties at
     *     separate index (for each target rule we have its own index in form properties)<br><br>
     *     Forwards to target group view page.
     * <br><br>
     * ACTION_SAVE: checks if there wasn't adding new rule or deleting existing rule performed.
     *     If the check is passed - performs saving of target group:<br>
     *         creates target representation from form (iterates through rules and creates TargetNode for each rule
     *         contained in form and puts all target nodes to target representation);<br>
     *         generates targetSQL from target representation (this targetSQL is used for creating SQL queries for
     *         filtering recipients matching the target group);<br>
     *         if it is a new target - creates new target object with ID 0;<br>
     *         saves name and description to target group object;<br>
     *         finally saves target group to database;<br>
     *     If there was any problem while saving target-group (target was locked, target wasn't saved in db etc.) -
     *     forwards to target view page with appropriate error message. If the saving was ok - forwards to "success"
     *     (which is currently target view page)
     * <br><br>
     * ACTION_NEW: if there wasn't adding of new rule performed - saves target to database (the detailed description
     *     of that process can be found above in description of ACTION_SAVE). Forwards to target view page.
     * <br><br>
     * ACTION_CONFIRM_DELETE: loads data into form (the detailed description of loading target to form can be found
     *     above in description of ACTION_VIEW), forwards to jsp with question to confirm deletion.
     * <br><br>
     * ACTION_DELETE: marks target group as deleted in database, loads list of target groups into request,
     *     forwards to "after_delete" (currently target groups list page)
     * <br><br>
     * ACTION_CREATE_ML: forwards to jsp with question to confirm of creation new mailing list
     * <br><br>
     * ACTION_CLONE: loads data of chosen target group data into form (see description of loading above). Creates a new
     *     target group in database with that data. Forwards to target view page.
     * <br><br>
     * ACTION_DELETE_RECIPIENTS_CONFIRM: loads target group data into form (see description in ACTION_VIEW). Calculates
     *     number of recipients matching target group and sets that number to form. Forwards to jsp with question to
     *     confirm deletion of recipients of chosen target group.
     * <br><br>
     * ACTION_DELETE_RECIPIENTS: loads target group data into form (see description in ACTION_VIEW). Deletes the
     *     recipients matching target group from database. Loads list of target groups into request. Forwards to
     *     "after_delete" (currently target groups list page)
     * <br><br>
     *
     * Also, with each call of execute, the method updateTargetFormProperties is called (independent of current value
     * of "action" property). That method performs the following functions:<br>
     * If addTargetNode property of form is set - the data of new target node is taken from form properties for new
     * rule and is put to form properties containing data of all rules of target group. The new rule is put at the end
     * of rules list.<br>
     * Method updates the list of possible operations for each rule according to rule type and updates the type of
     * data for each rule;<br>
     * Method removes the target rule with selected index which is set by property targetNodeToRemove (chosen by user
     * on the view page)<br><br>
     *
     * If the destination is "success" - loads list of targets to request.
     *
	 * @param form ActionForm object
     * @param req request
     * @param res response
	 * @param mapping
	 *            The ActionMapping used to select this instance
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet exception occurs
	 * @return destination
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		ComTargetForm targetForm = (ComTargetForm) form;
        targetForm.setSearchEnabled(false);
        
        if (logger.isInfoEnabled()) {
			logger.info("Action: " + targetForm.getAction());
		}

        boolean rebuildStructureData = false;

        ActionForward destination = null;
        ActionMessages errors = new ActionErrors();
        ActionMessages messages = new ActionMessages();
        ActionMessages rulesValidationErrors = new ActionMessages();

		try {
			switch (targetForm.getAction()) {
        		case ACTION_REBUILD_STRUCTURE_DATA:
        			rebuildStructureData = true;

        			// 	Fall-through is intended
        			// $FALL-THROUGH$

        		case ACTION_VIEW:
        			req.getSession().removeAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME);

        			updateWorkflowForwardParameters(req);
        			Integer forwardTargetItemId = (Integer) req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
        			if (forwardTargetItemId != null && forwardTargetItemId != 0) {
        				targetForm.setTargetID(forwardTargetItemId);
        			}

        			if (targetForm.getTargetID() > 0) {
        				String name = targetService.getTargetName(targetForm.getTargetID(), AgnUtils.getCompanyID(req), true);
        				writeUserActivityLog(AgnUtils.getAdmin(req), "view target group", name + " (" + targetForm.getTargetID() + ")");
        				loadAdditionalData(targetForm, req);
        			} else {
        				// ID 0 -> new mailing
        				targetForm.setSimpleStructure(true);
        			}

        			if (rebuildStructureData) {
        				synchronizeEditorData(targetForm, req);
        				targetForm.addMailings(req, mailingDao, trackableLinkDao);

        				destination = mapping.findForward("view");
        			} else {
        				if (targetForm.getTargetID() != 0) {
        					targetForm.setAction(ACTION_SAVE);
        					loadTarget(targetForm, req, true);
        				} else {
        					targetForm.clearRules();
        					targetForm.setAction(ACTION_NEW);
        				}
        				destination = mapping.findForward("view");
        			}

                errors = checkForEqlErrors(targetForm, AgnUtils.getCompanyID(req), errors);

                if (errors != null && errors.size() > 0) {
                	saveErrors(req, errors);
                }
                break;

            case ACTION_SAVE:
				updateTargetFormProperties(targetForm, req);

            	if (!targetService.checkIfTargetNameIsValid(targetForm.getShortname())) {
        			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namenotallowed"));
        		} else if (targetService.checkIfTargetNameAlreadyExists(AgnUtils.getCompanyID(req), targetForm.getShortname(), targetForm.getTargetID())) {
                	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namealreadyexists"));
            	}

				if (!errors.isEmpty()) {
					saveErrors(req, errors);
					targetForm.addMailings(req, mailingDao, trackableLinkDao);
					return mapping.findForward("view");
				}

            	synchronizeEditorData(targetForm, req);

				boolean ruleAdded = targetForm.getAddTargetNode();
				boolean ruleRemoved = targetForm.getTargetNodeToRemove() != -1;

				if (targetForm.checkParenthesisBalance()) {
					
					updateTargetFormProperties(targetForm, req);
	                if (targetForm.getAddTargetNode() || targetForm.getTargetNodeToRemove() != -1) {
	                    destination = mapping.findForward("success");
	                } else {
						try {
	    					if (saveTarget(targetForm, req, rulesValidationErrors) != 0) {
	    						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
	    						destination = mapping.findForward("success");
	    					} else {
	    						errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.saving"));
	    						destination = mapping.findForward("view");
	    					}
						} catch(TargetGroupTooLargeException e) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.too_large"));
							destination = mapping.findForward("view");
						} catch( TargetGroupLockedException e) {
							errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "target.locked"));
							destination = mapping.findForward("view");
						} catch( TargetGroupPersistenceException e) {
							errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.saving.general"));
							destination = mapping.findForward("view");
						} catch( Exception e) {
							errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.saving.general"));
							destination = mapping.findForward("view");
						}
					}
					
					if (!ruleAdded && !ruleRemoved) {
						// Reload target group to get updated EQL
						loadTarget(targetForm, req, false);
					}
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.bracketbalance"));
					saveErrors(req, errors);
					targetForm.addMailings(req, mailingDao, trackableLinkDao);
					destination = mapping.findForward("view");
				}

                if ("success".equals(destination.getName()) && !ruleAdded && !ruleRemoved) {
					HttpSession session = req.getSession();

					Integer workflowId = (Integer) session.getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
					String forwardParams = (String) session.getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS);

					if (workflowId != null && workflowId != 0) {
						destination = mapping.findForward("workflow_view");
						String path = destination.getPath().replace("{WORKFLOW_ID}", workflowId.toString());
						ActionRedirect redirect = new ActionRedirect(path);
						
						redirect.addParameter("forwardParams", forwardParams + ";elementValue=" + targetForm.getTargetID());
						return redirect;
					}
                }

				loadAdditionalData(targetForm, req);

                if ("success".equals(destination.getName()) && targetForm.isShowStatistic()) {
                    req.setAttribute("statisticUrl", getReportUrl(req, targetForm));
                }

                errors = checkForEqlErrors(targetForm, AgnUtils.getCompanyID(req), errors);

                if (errors != null && errors.size() > 0) {
                	saveErrors(req, errors);
                }
                break;

            case ACTION_CONFIRM_DELETE:
                loadTarget(targetForm, req, false);
                loadDependentEntities(targetForm, req);
                targetForm.setAction(ACTION_DELETE);
                return mapping.findForward("delete");

			case ACTION_DELETE:
				targetForm.setSearchEnabled(targetDao.isBasicFullTextSearchSupported());
				
				try {
					if (deleteTarget(targetForm.getTargetID(), AgnUtils.getCompanyID(req), AgnUtils.getAdmin(req))) {
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
					}
				} catch (TargetGroupLockedException e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("target.locked"));
				} catch (TargetGroupPersistenceException e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.delete"));
				} catch (TargetGroupIsInUseException e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.in_use"));
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.delete"));
				}
				
                if(targetForm.getPreviousAction() == ACTION_DELETE_FROM_MAILINGWIZARD){
					targetForm.setTargetID(0);
					destination = mapping.getInputForward();
					break;
                }
                
				targetForm.setAction(ACTION_LIST);
				destination = mapping.findForward(listTargetGroups(targetForm, req));
				break;
				
            case ACTION_BACK_TO_MAILINGWIZARD:
            	destination = mapping.findForward("back_mailingwizard");
                break;

            case ACTION_BULK_CONFIRM_DELETE:
                if (targetForm.getBulkIds().size() == 0) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.target"));
                    saveErrors(req, errors);

                    destination = mapping.findForward(listTargetGroups(targetForm, req));
                } else {
                    targetForm.setAction(ACTION_BULK_DELETE);

                    destination = mapping.findForward("bulk_delete_confirm");
                }
                break;

            case ACTION_BULK_DELETE:
                errors = new ActionMessages();
                try {
					final int companyId = AgnUtils.getCompanyID(req);

					Map<Integer, String> targetsToDelete = new HashMap<>();
					for (int id : targetForm.getBulkIds()) {
						String name = targetService.getTargetName(id, companyId);
						if (name != null) {
							targetsToDelete.put(id, name);
						}
					}

					if (!targetsToDelete.isEmpty()) {
						targetService.bulkDelete(targetsToDelete.keySet(), companyId);

						for (Map.Entry<Integer, String> e : targetsToDelete.entrySet()) {
							writeUserActivityLog(AgnUtils.getAdmin(req), "delete target group", e.getValue() + " (" + e.getKey() + ")");
						}

						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
						saveMessages(req, messages);
					}
                } catch (TargetGroupLockedException e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("target.locked"));
                } catch (TargetGroupPersistenceException e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.delete"));
                } catch (TargetGroupIsInUseException e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.in_use"));
                } catch (Exception e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.delete"));
                }
                if (!errors.isEmpty()) {
                    saveErrors(req, errors);
                }
                destination = mapping.findForward(listTargetGroups(targetForm, req));
                break;

            case ACTION_LOCK_TARGET_GROUP:
            	if (targetService.lockTargetGroup(AgnUtils.getCompanyID(req), targetForm.getTargetID())) {
					writeUserActivityLog(AgnUtils.getAdmin(req), "do lock target group", targetForm.getShortname() + " (" + targetForm.getTargetID() + ")", logger);
					loadTarget( targetForm, req, false);
					ActionMessages msg = new ActionMessages();
					msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					saveMessages(req, msg);
				} else {
					ActionMessages msg = new ActionMessages();
					msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.not_saved"));
					saveErrors(req, msg);
				}

            	destination = mapping.findForward("view");
            	break;

            case ACTION_UNLOCK_TARGET_GROUP:
            	if (targetService.unlockTargetGroup(AgnUtils.getCompanyID(req), targetForm.getTargetID())) {
					writeUserActivityLog(AgnUtils.getAdmin(req), "do unlock target group", targetForm.getShortname() + " (" + targetForm.getTargetID() + ")", logger);
					loadTarget( targetForm, req, false);
					ActionMessages msg = new ActionMessages();
					msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					saveMessages(req, msg);
				} else {
					ActionMessages msg = new ActionMessages();
					msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.not_saved"));
					saveErrors(req, msg);
				}

				destination = mapping.findForward("view");
				break;
				
			case ACTION_DELETE_RECIPIENTS_CONFIRM:
				loadTarget(targetForm, req, true);
				this.getRecipientNumber(targetForm, req);
				targetForm.addMailings(req, mailingDao, trackableLinkDao);
				destination = mapping.findForward("delete_recipients");
				break;
				
			case ACTION_DELETE_RECIPIENTS:
				loadTarget(targetForm, req, true);
				targetService.deleteRecipients(targetForm.getTargetID(), AgnUtils.getCompanyID(req));
				writeUserActivityLog(AgnUtils.getAdmin(req), "edit target group",
						"All recipients deleted from target group " + targetForm.getShortname() + " (" + targetForm.getTargetID() + ")", logger);
				targetForm.setAction(StrutsActionBase.ACTION_LIST);
				targetForm.addMailings(req, mailingDao, trackableLinkDao);
				destination = mapping.findForward(listTargetGroups(targetForm, req));
				break;
				
            case ACTION_NEW:
            	req.getSession().removeAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME);
            	loadAdditionalData(targetForm, req);
            	if (targetForm.getAddTargetNode() || targetForm.getTargetNodeToRemove() != -1) {
                    updateTargetFormProperties(targetForm, req);
                    targetForm.setAction(ACTION_SAVE);
                } else {
					try {
						if (saveTarget(targetForm, req, rulesValidationErrors) != 0) {
							targetForm.setAction(ACTION_SAVE);
							messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
						} else {
							targetForm.setAction(ACTION_SAVE);
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.saving"));
						}
					} catch (TargetGroupLockedException e) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "target.locked"));
					} catch (TargetGroupPersistenceException e) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.saving.general"));
					} catch (Exception e) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.saving.general"));
					}
				}
            	destination = mapping.findForward("view");
            	break;
            	
            case ACTION_LIST:
                targetForm.setSearchEnabled(targetDao.isBasicFullTextSearchSupported());
                destination = mapping.findForward(listTargetGroups(targetForm, req));
                break;
			
            case ACTION_VIEW_MAILINGS:
				targetForm.setUsedInMailings(mailingDao.getMailingsDependentOnTargetGroup(AgnUtils.getCompanyID(req), targetForm.getTargetID()));
				targetForm.setShortname(targetService.getTargetName(targetForm.getTargetID(), AgnUtils.getCompanyID(req)));
				destination = mapping.findForward("view_mailings");
				break;
				
            case ACTION_CREATE_ML:
            	destination = mapping.findForward("create_ml");
            	break;
            	
            case ACTION_CLONE:
				if (targetForm.getTargetID() != 0) {
					loadTarget(targetForm, req, true);
					cloneTarget(targetForm, req, rulesValidationErrors);
					targetForm.setAction(ACTION_SAVE);
				}
				destination = mapping.findForward("view");
				break;
				
            default:
            	destination = mapping.findForward(listTargetGroups(targetForm, req));
			}
		} catch (Exception e) {
			logger.error("execute: " + e, e);
			//errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}
		
		if (destination != null) {
			if ("success".equals(destination.getName())) {
				prepareListParameters(targetForm);
				req.setAttribute("targetlist", loadTargetList(req, targetForm));
			}
		}

		if (destination != null) {
			targetForm.addMailings(req, mailingDao, trackableLinkDao);
		}

		req.setAttribute("rulesValidationErrors", rulesValidationErrors);

		// Report any errors we have discovered back to the original form
		if (errors != null && !errors.isEmpty()) {
			saveErrors(req, errors);
            if (destination == null) {
                return (new ActionForward(mapping.getInput()));
            }
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

		targetForm.clearNewRuleData();

		return destination;
	}
	
	private String listTargetGroups(ComTargetForm form, HttpServletRequest request) {
		if (form.getColumnwidthsList() == null) {
        	form.setColumnwidthsList(getInitializedColumnWidthList(3));
        }

		prepareListParameters(form);
		request.setAttribute("targetlist", loadTargetList(request, form));

		return "list";
	}

	private void prepareListParameters(ComTargetForm form) {
		webStorage.access(ComWebStorage.TARGET_OVERVIEW, storage -> {
			if (form.getNumberOfRows() > 0) {
				storage.setRowsCount(form.getNumberOfRows());
				storage.setShowWorldDelivery(form.isShowWorldDelivery());
				storage.setShowTestAndAdminDelivery(form.isShowTestAndAdminDelivery());
			} else {
				form.setNumberOfRows(storage.getRowsCount());
				form.setShowWorldDelivery(storage.isShowWorldDelivery());
				form.setShowTestAndAdminDelivery(storage.isShowTestAndAdminDelivery());
			}
		});
	}
	
	/**
	 * Remove an existing target group (if any) and write to a user activity log.
	 * @param targetId an identifier of the target group to delete.
	 * @param companyId an identifier of a company that owns the target group.
	 * @param admin a current user who does a delete action.
	 * @return {@code true} if target group was deleted or {@code false} otherwise.
	 */
	protected boolean deleteTarget(int targetId, int companyId, ComAdmin admin) throws TargetGroupPersistenceException, TargetGroupException {
		String name = targetService.getTargetName(targetId, companyId);
		if (name != null) {
			targetService.deleteTargetGroup(targetId, companyId);
			writeUserActivityLog(admin, "delete target group", name + " (" + targetId + ")");
			return true;
		}
		return false;
	}

	
	/**
	 * add new rule if necessary and update exist target rules and properties of form
	 * <br><br>
	 *
	 * @param form
	 * @param request
	 * @return success or failed result of removing rules from form
	 */
	protected boolean updateTargetFormProperties(TargetForm form, HttpServletRequest request) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		int lastIndex = form.getNumTargetNodes();
        int removeIndex = -1;

        // If "add" was clicked, add new rule
		if (form.getAddTargetNode()) {
            writeUserActivityLog(admin, "edit target group", "Added rule to target group " + form.getShortname() + " (" + form.getTargetID() + ")");
           	form.setColumnAndType(lastIndex, form.getColumnAndTypeNew());
        	form.setChainOperator(lastIndex, form.getChainOperatorNew());
        	form.setParenthesisOpened(lastIndex, form.getParenthesisOpenedNew());
        	form.setPrimaryOperator(lastIndex, form.getPrimaryOperatorNew());
        	form.setPrimaryValue(lastIndex, form.getPrimaryValueNew());
        	form.setParenthesisClosed(lastIndex, form.getParenthesisClosedNew());
        	form.setDateFormat(lastIndex, form.getDateFormatNew());
        	form.setSecondaryOperator(lastIndex, form.getSecondaryOperatorNew());
        	form.setSecondaryValue(lastIndex, form.getSecondaryValueNew());

        	form.setAddTargetNode( false);
        	
        	lastIndex++;
        }

		int nodeToRemove = form.getTargetNodeToRemove();
		form.setTargetNodeToRemove(-1);		// EMM-3504: Quick fix for problem removing two rules

		// Iterate over all target rules
        for(int index = 0; index < lastIndex; index++) {
        	if(index != nodeToRemove) {
        		String column = form.getColumnAndType(index);
        		updateFormFromNode(column, index, form, request);
        	} else {
        		if (removeIndex != -1) {
					throw new RuntimeException( "duplicate remove??? (removeIndex = " + removeIndex + ", index = " + index + ")");
				}
        		removeIndex = index;
        	}
		}
        
        if (removeIndex != -1) {
        	form.removeRule(removeIndex);
			writeUserActivityLog(admin, "edit target group", "Removed rule from target group " + form.getShortname() + " (" + form.getTargetID() + ")");
        	return true;
        } else {
        	return false;
        }
	}
	
	protected void updateFormFromNode(String column, final int index, final TargetForm form, final HttpServletRequest request) {
		if( TargetNodeMailingReceived.PSEUDO_COLUMN_NAME.equalsIgnoreCase( column)) {
			form.setValidTargetOperators(index, TargetNodeMailingReceived.getValidOperators());
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_RECEIVED);
			form.setColumnName(index, TargetNodeMailingReceived.PSEUDO_COLUMN_NAME);
		} else if( TargetNodeMailingOpened.PSEUDO_COLUMN_NAME.equalsIgnoreCase( column)) {
			form.setValidTargetOperators(index, TargetNodeMailingOpened.getValidOperators());
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_OPENED);
			form.setColumnName(index, TargetNodeMailingOpened.PSEUDO_COLUMN_NAME);
		} else if( TargetNodeMailingClicked.PSEUDO_COLUMN_NAME.equalsIgnoreCase( column)) {
			form.setValidTargetOperators(index, TargetNodeMailingClicked.getValidOperators());
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_CLICKED);
			form.setColumnName(index, TargetNodeMailingClicked.PSEUDO_COLUMN_NAME);
		} else if(TargetNodeMailingRevenue.PSEUDO_COLUMN_NAME.equalsIgnoreCase(column)) {
			form.setValidTargetOperators(index, TargetNodeMailingRevenue.getValidOperators());
			form.setColumnType(index, ComTargetForm.COLUMN_TYPE_MAILING_REVENUE);
			form.setColumnName(index, TargetNodeMailingRevenue.PSEUDO_COLUMN_NAME);
		} else if(TargetNodeMailingClickedOnSpecificLink.PSEUDO_COLUMN_NAME.equalsIgnoreCase(column)) {
			form.setValidTargetOperators(index, TargetNodeMailingClickedOnSpecificLink.getValidOperators());
			form.setColumnType(index, ComTargetForm.COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK);
			form.setColumnName(index, TargetNodeMailingClickedOnSpecificLink.PSEUDO_COLUMN_NAME);
  		} else {
    		if (column.contains("#")) {
    			column = column.substring(0, column.indexOf('#'));
    		}
    		String type = "unknownType";
    		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(column)) {
    			type = "DATE";
    		} else {
				try {
					type = columnInfoService.getColumnInfo(AgnUtils.getCompanyID(request), column).getDataType();
				} catch (Exception e) {
					logger.error("Cannot find fieldtype for companyId " + AgnUtils.getCompanyID(request) + " and column '" + column + "'", e);
				}
    		}

			form.setColumnName(index, column);
    		
    		if (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
    			form.setValidTargetOperators(index, TargetNodeString.getValidOperators());
    			form.setColumnType(index, TargetForm.COLUMN_TYPE_STRING);
    		} else if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("NUMBER")) {
    			form.setValidTargetOperators(index, TargetNodeNumeric.getValidOperators());
    			form.setColumnType(index, TargetForm.COLUMN_TYPE_NUMERIC);
    		} else if (type.equalsIgnoreCase("DATE")) {
    			form.setValidTargetOperators(index, TargetNodeDate.getValidOperators());
    			form.setColumnType(index, TargetForm.COLUMN_TYPE_DATE);
    		}
		}
		
	}
	
	/**
	 * Gets number of recipients affected in a target group.
	 */
	protected void getRecipientNumber(TargetForm aForm, HttpServletRequest req) {
		final int companyId = AgnUtils.getCompanyID(req);

		ComTarget target = targetDao.getTarget(aForm.getTargetID(), companyId);
		int numOfRecipients = recipientDao.sumOfRecipients(companyId, target.getTargetSQL());

		aForm.setNumOfRecipients(numOfRecipients);
	}

	/**
	 * Checks form data for EQL errors.
	 * <b>Form data has already to be synchronized with a previous call to {@link #synchronizeEditorData(ComTargetForm, HttpServletRequest)}!</b>
	 *
	 * @param form form bean
	 * @param companyId company ID of current user
	 *
	 * @return ActionMessages containing errors or or null
	 */
	private ActionMessages checkForEqlErrors(ComTargetForm form, int companyId, ActionMessages messages) {
		if (form == null || StringUtils.isBlank(form.getEql())) {
			return null;
		}

		try {
			eqlFacade.convertEqlToSql(form.getEql(), companyId);
		} catch(EqlSyntaxErrorException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Syntax error(s) in EQL expression", e);
			}
			if(messages == null){
				messages = new ActionMessages();
			}
			for(EqlSyntaxError syntaxError : e.getErrors()) {
				messages.add("eqlErrors", new ActionMessage("error.target.eql.syntax", syntaxError.getLine(), syntaxError.getColumn(), syntaxError.getSymbol()));
			}
		} catch(EqlParserException e) {
			return processEqlErrors(messages, e, "Parser error(s) in EQL expression",
					new ActionMessage("error.target.eql"));
		} catch(UnknownLinkIdFaultyCodeException e) {
			return processEqlErrors(messages, e, "Invalid link ID while generating code",
					new ActionMessage("error.target.eql.unknownLinkId", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getMailingId(), e.getLinkId()));
		} catch(UnknownMailingIdFaultyCodeException e) {
			return processEqlErrors(messages, e, "Invalid mailing ID while generating code",
					new ActionMessage("error.target.eql.unknownMailingId", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getMailingId()));
		} catch(UnknownReferenceTableColumnFaultyCodeException e) {
			return processEqlErrors(messages, e, "Unknown reference table column while generating code",
					new ActionMessage("error.target.eql.unknownReferenceTableColumn", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getReferenceTableName(), e.getColumnName()));
		} catch(UnknownReferenceTableFaultyCodeException e) {
			return processEqlErrors(messages, e, "Unknown reference table while generating code",
					new ActionMessage("error.target.eql.unknownReferenceTable", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getReferenceTableName()));
		} catch(UnknownProfileFieldFaultyCodeException e) {
			return processEqlErrors(messages, e, "Unknown profile field while generating code",
					new ActionMessage("error.target.eql.unknownProfileField", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getName()));
		} catch(DateFormatFaultyCodeException e) {
			return processEqlErrors(messages, e, "Invalid date format while generating code",
					new ActionMessage("error.target.eql.invalidDateFormat", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn(), e.getDateFormat()));
		} catch(UnsupportedOperatorForDataTypeException e) {
			return processEqlErrors(messages, e, "Invalid operator for data type while generating code",
					new ActionMessage("error.target.eql.unsupportedOperationForType", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn()));
		} catch(InvalidTypeException e) {
			return processEqlErrors(messages, e, "Invalid data type while generating code",
					new ActionMessage("error.target.eql.invalidType", e.getCodeLocation().getLine(), e.getCodeLocation().getColumn()));
		} catch(Exception e) {
			return processEqlErrors(messages, e, "Code generation error(s) in EQL expression",
					new ActionMessage("error.target.eql"));
		}
		return messages;
	}

	private ActionMessages processEqlErrors(ActionMessages messages, Exception e, String logMessage, ActionMessage message){
		if(logger.isInfoEnabled()) {
			logger.info(logMessage, e);
		}
		if(messages == null) {
			messages = new ActionMessages();
		}
		messages.add("eqlErrors", message);
		return messages;
	}

	protected void loadTarget(TargetForm aForm, HttpServletRequest req, boolean updateForm) {
		ComTarget aTarget = targetDao.getTarget(aForm.getTargetID(), AgnUtils.getCompanyID( req));

		if (aTarget == null || aTarget.getId() == 0) {
			logger.warn( "loadTarget: could not load target " + aForm.getTargetID());
			aTarget = targetFactory.newTarget();
			aTarget.setId(aForm.getTargetID());
		}
		aForm.setShortname(aTarget.getTargetName());
		aForm.setDescription(aTarget.getTargetDescription());
		((ComTargetForm) aForm).setUseForAdminAndTestDelivery(aTarget.isAdminTestDelivery());
		
		if (updateForm) {
			fillFormFromTargetRepresentation(aForm, aTarget.getTargetStructure(), aTarget.getCompanyID());
		}
		
		((ComTargetForm) aForm).setLocked(aTarget.isLocked());
		((ComTargetForm) aForm).setSimpleStructure(aTarget.isSimpleStructured());
		((ComTargetForm) aForm).setEql(aTarget.getEQL());

	    if (logger.isInfoEnabled()) {
	    	logger.info("loadTarget: target " + aForm.getTargetID() + " loaded");
	    }
	}

    /**
     * Loads mailings, templates and BIRT reports that are dependent on this target group
     */
    protected void loadDependentEntities(TargetForm aForm, HttpServletRequest req) {
        ComTarget aTarget = targetDao.getTarget(aForm.getTargetID(), AgnUtils.getCompanyID(req));

        if (aTarget.getId() != 0) {
            List<LightweightMailing> affectedMailings = getMailingDao().getMailingsDependentOnTargetGroup(AgnUtils.getCompanyID(req), aTarget.getId());

            if (CollectionUtils.isNotEmpty(affectedMailings)) {
                req.setAttribute("affectedMailingsLightweight", affectedMailings);
                req.setAttribute("affectedMailingsMessageKey", "warning.target.delete.affectedMailings");
                req.setAttribute("affectedMailingsMessageType", GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
            }

            List<ComLightweightBirtReport> affectedReports = birtReportDao.getLightweightBirtReportsBySelectedTarget(aTarget.getCompanyID(), aTarget.getId());
            if (CollectionUtils.isNotEmpty(affectedReports)) {
                req.setAttribute("affectedReports", affectedReports);
                req.setAttribute("affectedReportsMessageKey", "warning.target.delete.affectedBirtReports");
                req.setAttribute("affectedReportsMessageType", GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
            }
        }
    }

	protected List<TargetLight> loadTargetList(HttpServletRequest request, TargetForm form) {
		ComTargetForm targetForm = (ComTargetForm) form;
        String searchQuery = targetForm.getSearchQueryText();

        return targetDao.getTargetLightsBySearchParameters(AgnUtils.getCompanyID(request), false,
                targetForm.isShowWorldDelivery(), targetForm.isShowTestAndAdminDelivery(), false,
                targetForm.isSearchNameChecked(), targetForm.isSearchDescriptionChecked(), searchQuery
        );
	}

    private void updateWorkflowForwardParameters(HttpServletRequest req) {
		WorkflowUtils.updateForwardParameters(req);
    }

    private void loadAdditionalData(ComTargetForm targetForm, HttpServletRequest req) {
        targetForm.setMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(req)));
    }

    @Required
	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}

    public void setBirtReportDao(ComBirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }

    @Required
    public void setEqlFacade(EqlFacade eqlFacade) {
    	this.eqlFacade = eqlFacade;
    }

    private String getReportUrl(HttpServletRequest request, ComTargetForm form) {
		try {
			RecipientStatusStatisticDto statisticDto = new RecipientStatusStatisticDto();
			statisticDto.setMediaType(0);
			statisticDto.setTargetId(form.getTargetID());
			statisticDto.setMailinglistId(form.getMailinglistId());
			statisticDto.setFormat("html");
			
			return birtStatisticsService.getRecipientStatusStatisticUrl(AgnUtils.getAdmin(request), request.getSession(false).getId(), statisticDto);
		} catch (Exception e) {
			logger.error("Error during generation statistic url " + e);
		}
		
		return null;
	}
    
    /**
	 * Saves target.
	 */
	protected int saveTarget(TargetForm aForm, HttpServletRequest req, ActionMessages rulesValidationErrors) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);

		ComTarget oldTarget = targetDao.getTarget(aForm.getTargetID(), companyId);
        ComTarget newTarget = targetFactory.newTarget();

        newTarget.setId(aForm.getTargetID());
        newTarget.setTargetName(aForm.getShortname());
        newTarget.setTargetDescription(aForm.getDescription());
        newTarget.setTargetStructure(createTargetRepresentationFromForm(aForm, req));
        newTarget.setTargetSQL(eqlFacade.convertTargetRepresentationToEql(newTarget.getTargetStructure(), companyId));
        newTarget.setCompanyID(companyId);
        newTarget.setAdminTestDelivery(((ComTargetForm) aForm).isUseForAdminAndTestDelivery());
        newTarget.setEQL(((ComTargetForm) aForm).getEql());

        int newId = targetService.saveTarget(AgnUtils.getAdmin(req), newTarget, oldTarget, rulesValidationErrors, this::writeUserActivityLog);

        if (aForm.getTargetID() == 0) {
			aForm.setTargetID(newId);
        }

		return newId;
	}
	
	/**
	 * Clone target.
	 */
	protected void cloneTarget(TargetForm aForm, HttpServletRequest req, ActionMessages rulesValidationErrors) throws Exception {
        final String newName = SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(req)) +
				" " + aForm.getShortname();

		aForm.setTargetID(0);
		aForm.setShortname(newName);

        saveTarget(aForm, req, rulesValidationErrors);
        writeUserActivityLog(AgnUtils.getAdmin(req), "create target group", newName + " (" + aForm.getTargetID() + ")");
	}


    /**
     * Synchronizes data between classic editor and EQL editor.
     *
     * @param form FormBean
     * @param request servlet request
     */
    private void synchronizeEditorData(ComTargetForm form, HttpServletRequest request) {
		final HttpSession session = request.getSession();
    	final int companyId = AgnUtils.getCompanyID(request);

    	// Load saved target structure from session or create new one
    	SavedTargetStructureData savedTarget = (SavedTargetStructureData) session.getAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME);
    	if (savedTarget == null || savedTarget.id != form.getTargetID()) {
    		if (form.getTargetID() > 0) {
	    		ComTarget target = targetDao.getTarget(form.getTargetID(), companyId);

	    		savedTarget = new SavedTargetStructureData(target.getId(), target.getTargetStructure(), target.getEQL(), target.isSimpleStructured());
    		} else {
    			savedTarget = new SavedTargetStructureData(0, targetRepresentationFactory.newTargetRepresentation(), "", true);
    		}

    		// Write data to session, will be needed later
			session.setAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME, savedTarget);
    	} else {
    		if (logger.isInfoEnabled()) {
    			logger.info("Session has target group data for target ID " + savedTarget.id);
    		}
    	}

		if (savedTarget.id == form.getTargetID()) {
			// Check changes in EQL only if user has permission to edit EQL. Otherwise target group will be cleared from any rule.
			if (!StringUtils.equals(form.getEql(), savedTarget.eql)) {
				// Build target structure from EQL

				if (logger.isInfoEnabled()) {
					logger.info("EQL has changed - rebuilding TargetRepresentation");
				}

				try {
					TargetRepresentation structure;

					if (StringUtils.isBlank(form.getEql())) {
						structure = targetRepresentationFactory.newTargetRepresentation();
					} else {
						structure = eqlFacade.convertEqlToTargetRepresentation(form.getEql(), companyId);
					}

					// Fill form with structure data
					fillFormFromTargetRepresentation(form, structure, companyId);

					// Signal simple structure
					form.setSimpleStructure(true);

					// Save new data in session
					SavedTargetStructureData newSavedTarget = new SavedTargetStructureData(savedTarget.id, structure, form.getEql(), form.isSimpleStructure());
					session.setAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME, newSavedTarget);
				} catch (EqlToTargetRepresentationConversionException | EqlParserException e) {
					// Log as info, we can handle both types of exception in reasonable ways
					if (logger.isInfoEnabled()) {
						logger.info("Unable to convert EQL to TargetRepresentation", e);
					}

					// Signal complex structure
					form.setSimpleStructure(false);

					// Clear simple structure
					TargetRepresentation emptyStructure = targetRepresentationFactory.newTargetRepresentation();
					fillFormFromTargetRepresentation(form, emptyStructure, companyId);

					// Save new data in session
					SavedTargetStructureData newSavedTarget = new SavedTargetStructureData(savedTarget.id, emptyStructure, form.getEql(), form.isSimpleStructure());
					session.setAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME, newSavedTarget);
				}
			} else {
				try {
					// Conversion from TargetRepresentation to EQL is always possible and therefore always simple-structured!
					TargetRepresentation representationFromRequest = createTargetRepresentationFromForm(form, request);

					// When we edited in simple view, then it must remain simple structured...
					form.setSimpleStructure(true);

					assert(representationFromRequest != null);	// createTargetRepresentationFromForm() always returns a non-null value
					assert(savedTarget.structure != null);		// Target node structure in session is always set to a non-null value

					// Check changes in node structure only if target group previously send to view is simple-structured (-> classic editor is shown). Otherwise target group may be cleared from any rule.
					if (savedTarget.simpleStructure && !representationFromRequest.hasSameNodeStructureAs(savedTarget.structure)) {
						if (logger.isInfoEnabled()) {
							logger.info("TargetRepresentation has changed - rebuilding EQL");
						}

						// Rebuild EQL from target structure from request
						try {
							String eql = savedTarget.eql;

							// Doesn't contain errors?
							if (!targetService.validateTargetRepresentation(representationFromRequest, new ActionMessages(), companyId)) {
								eql = eqlFacade.convertTargetRepresentationToEql(representationFromRequest, companyId);
							}

							// Save new data in session
							SavedTargetStructureData newSavedTarget = new SavedTargetStructureData(savedTarget.id, representationFromRequest, eql, form.isSimpleStructure());
							session.setAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME, newSavedTarget);

							form.setEql(eql);
						} catch (TargetRepresentationToEqlConversionException e) {
							// Log as error, because conversion from TargetRepresentation to EQL must always be possible!
							logger.error("Unable to convert TargetRepresentation to EQL", e);

							// Save new data in session (keep old EQL!)
							SavedTargetStructureData newSavedTarget = new SavedTargetStructureData(savedTarget.id, representationFromRequest, savedTarget.eql, form.isSimpleStructure());
							session.setAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME, newSavedTarget);
						}
					} else {
						form.setSimpleStructure(savedTarget.simpleStructure);
					}
				} catch (Exception e) {
					logger.error("Error creating TargetRepresentation from form data", e);

					form.setSimpleStructure(false);
					// Keep saved target structure data!
				}
			}
		} else {
			logger.error("Mismatching target group ID when rebuilding target structure for editor view (expected: " + form.getTargetID() + ", but found " + savedTarget.id + ")");

			// Keep saved target structure data!
		}

		// Load saved target group from session again to get changes
    	savedTarget = (SavedTargetStructureData) session.getAttribute(SAVED_TARGET_STRUCTURE_DATA_SESSION_ATTRIBUTE_NAME);

    	// Transfer data from session to form
		form.setSimpleStructure(savedTarget.simpleStructure);
		fillFormFromTargetRepresentation(form, savedTarget.structure, companyId);
		form.setEql(savedTarget.eql);
    }
    
    protected TargetRepresentation createTargetRepresentationFromForm(TargetForm form, HttpServletRequest request) throws Exception {
        TargetRepresentation target = targetRepresentationFactory.newTargetRepresentation();
       
        int lastIndex = form.getNumTargetNodes();
       
        for(int index = 0; index < lastIndex; index++) {
    		String column = form.getColumnAndType(index);
    		if (column.contains("#")) {
    			column = column.substring(0, column.indexOf('#'));
    		}

    		final TargetNode node = createNodeFromForm(column, index, form, request);
    		
            target.addNode(node);
        }
        
        return target;
	}
    
    protected TargetNode createNodeFromForm(final String column, final int index, final TargetForm form, final HttpServletRequest request) throws Exception {
		if( column.equalsIgnoreCase(TargetNodeMailingReceived.PSEUDO_COLUMN_NAME)) {
			int companyID = AgnUtils.getCompanyID(request);
			return createMailingReceivedNode(companyID, form, index);
		} else if( column.equalsIgnoreCase(TargetNodeMailingOpened.PSEUDO_COLUMN_NAME)) {
			int companyID = AgnUtils.getCompanyID(request);
			return createMailingOpenedNode(companyID, form, index);
		} else if( column.equalsIgnoreCase(TargetNodeMailingClicked.PSEUDO_COLUMN_NAME)) {
			int companyID = AgnUtils.getCompanyID(request);
			return createMailingClickedNode(companyID, form, index);
		} else if(column.equalsIgnoreCase(TargetNodeMailingRevenue.PSEUDO_COLUMN_NAME)) {
			int companyID = AgnUtils.getCompanyID(request);
			return createMailingRevenueNode(companyID, form, index);
		} else if(column.equalsIgnoreCase(TargetNodeMailingClickedOnSpecificLink.PSEUDO_COLUMN_NAME)) {
			int companyID = AgnUtils.getCompanyID(request);
			return createMailingClickedSpecificLinkNode(companyID, form, index);
		} else {
    		String type = "unknownType";
    		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(column) || "SYSDATE".equalsIgnoreCase(column) || "NOW()".equalsIgnoreCase(column)) {
    			type = "DATE";
    		} else {
				try {
					type = columnInfoService.getColumnInfo(AgnUtils.getCompanyID(request), column).getDataType();
				} catch (Exception e) {
					logger.error("Cannot find fieldtype for companyId " + AgnUtils.getCompanyID(request) + " and column '" + column + "'", e);
				}
    		}
    		
    		if (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
    			return createStringNode(form, column, type, index);
    		} else if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("NUMBER")) {
    			return createNumericNode(form, column, type, index);
    		} else if (type.equalsIgnoreCase("DATE")) {
    			return createDateNode(form, column, type, index);
    		}
		}
   	
		return null;
    }
    
    /**
     * fill the data to form from TargetRepresentation object
     * <br><br>
     *
     * @param form
     * @param target
     */
    protected void fillFormFromTargetRepresentation(TargetForm form, TargetRepresentation target, @VelocityCheck int companyID) {
		// First, remove all previously defined rules from target form
		form.clearRules();
		
		// Now, convert target nodes to form data
		int index = 0;
		for (TargetNode node : target.getAllNodes()) {
			form.setChainOperator(index, node.getChainOperator());
			String primaryField = node.getPrimaryField() == null ? null : node.getPrimaryField().toUpperCase();
	        form.setColumnAndType(index, primaryField);
			form.setPrimaryOperator(index, node.getPrimaryOperator());
			form.setPrimaryValue(index, node.getPrimaryValue());
			form.setColumnName(index, node.getPrimaryField());
			form.setParenthesisOpened(index, node.isOpenBracketBefore() ? 1 : 0);
			form.setParenthesisClosed(index, node.isCloseBracketAfter() ? 1 : 0);
	
			fillFormFromNode(form, index, node);
			
			index++;
		}
    }
    
    protected void fillFormFromNode(final TargetForm form, final int index, final TargetNode node) {
		if (node instanceof TargetNodeString) {
			form.setColumnType(index, TargetForm.COLUMN_TYPE_STRING);
			form.setValidTargetOperators(index, TargetNodeString.getValidOperators());
		} else if (node instanceof TargetNodeNumeric) {
			TargetNodeNumeric numericNode = (TargetNodeNumeric) node;
			
			form.setColumnType(index, TargetForm.COLUMN_TYPE_NUMERIC);
			form.setSecondaryOperator(index, numericNode.getSecondaryOperator());
			form.setSecondaryValue(index, Integer.toString(numericNode.getSecondaryValue()));
			form.setValidTargetOperators(index, TargetNodeNumeric.getValidOperators());
		} else if (node instanceof TargetNodeDate) {
			TargetNodeDate dateNode = (TargetNodeDate) node;
			
			form.setDateFormat(index, dateNode.getDateFormat());
			form.setColumnType(index, TargetForm.COLUMN_TYPE_DATE);
			form.setValidTargetOperators(index, TargetNodeDate.getValidOperators());
		} else if( node instanceof TargetNodeMailingReceived) {
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_RECEIVED);
			form.setValidTargetOperators( index, TargetNodeMailingReceived.getValidOperators());
		} else if( node instanceof TargetNodeMailingOpened) {
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_OPENED);
			form.setValidTargetOperators( index, TargetNodeMailingOpened.getValidOperators());
		} else if( node instanceof TargetNodeMailingClicked) {
			form.setColumnType(index, TargetForm.COLUMN_TYPE_MAILING_CLICKED);
			form.setValidTargetOperators( index, TargetNodeMailingClicked.getValidOperators());
		} else if( node instanceof TargetNodeMailingRevenue) {
			form.setColumnType(index, ComTargetForm.COLUMN_TYPE_MAILING_REVENUE);
			form.setValidTargetOperators(index, TargetNodeMailingRevenue.getValidOperators());
		} else if(node instanceof TargetNodeMailingClickedOnSpecificLink) {
			TargetNodeMailingClickedOnSpecificLink currentNode = (TargetNodeMailingClickedOnSpecificLink) node;
			
			form.setColumnType(index, ComTargetForm.COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK);
			form.setValidTargetOperators(index, TargetNodeMailingClickedOnSpecificLink.getValidOperators());
			form.setSecondaryValue(index, currentNode.getSecondaryValue());
		} else {
			// uh oh. It seems, somebody forgot to add a new target node type here :(
			logger.warn("cannot handle target node class " + node.getClass().getCanonicalName());
			throw new RuntimeException("cannot handle target node class " + node.getClass().getCanonicalName());
		}
   	
    }
    
    private TargetNodeNumeric createNumericNode(TargetForm form, String column, String type, int index) { // TODO: Why not implemented in a factory class?
		int primaryOperator = form.getPrimaryOperator(index);
		int secondaryOperator = form.getSecondaryOperator(index);
		int secondaryValue = 0;
		
    	if(primaryOperator == TargetNode.OPERATOR_MOD.getOperatorCode()) {
            try {
                secondaryOperator = form.getSecondaryOperator(index);
            } catch (Exception e) {
                secondaryOperator = TargetNode.OPERATOR_EQ.getOperatorCode();
            }
            try {
                secondaryValue = Integer.parseInt(form.getSecondaryValue(index));
            } catch (Exception e) {
                secondaryValue = 0;
            }
        }
		
    	return targetNodeFactory.newNumericNode(
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				column,
				type,
				primaryOperator,
				form.getPrimaryValue(index),
				secondaryOperator,
				secondaryValue,
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeDate createDateNode(TargetForm form, String column, String type, int index) { // TODO: Why not implemented in a factory class?
		return targetNodeFactory.newDateNode(
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				column,
				type,
				form.getPrimaryOperator(index),
				form.getDateFormat(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeMailingReceived createMailingReceivedNode(@VelocityCheck int companyID, TargetForm form, int index) throws Exception { // TODO: Why not implemented in a factory class?
		return new TargetNodeMailingReceived(
				companyID,
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				form.getPrimaryOperator(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeMailingOpened createMailingOpenedNode(@VelocityCheck int companyID, TargetForm form, int index) throws Exception { // TODO: Why not implemented in a factory class?
		return new TargetNodeMailingOpened(
				companyID,
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				form.getPrimaryOperator(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeMailingClicked createMailingClickedNode( @VelocityCheck int companyID, TargetForm form, int index) throws Exception { // TODO: Why not implemented in a factory class?
		return new TargetNodeMailingClicked(
				companyID,
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				form.getPrimaryOperator(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeMailingRevenue createMailingRevenueNode( @VelocityCheck int companyID, TargetForm form, int index) throws Exception { // TODO: Why not implemented in a factory class?
		return new TargetNodeMailingRevenue(
				companyID,
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				form.getPrimaryOperator(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeString createStringNode(TargetForm form, String column, String type, int index) { // TODO: Why not implemented in a factory class?
		return targetNodeFactory.newStringNode(
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				column,
				type,
				form.getPrimaryOperator(index),
				form.getPrimaryValue(index),
				form.getParenthesisClosed(index));
	}
	
	private TargetNodeMailingClickedOnSpecificLink createMailingClickedSpecificLinkNode(@VelocityCheck int companyID, TargetForm form, int index) throws Exception { // TODO: Why not implemented in a factory class?
		int mailingId = 0;
		int linkId = 0;
		
		// Try to parse mailing ID
		try {
			mailingId = Integer.parseInt(form.getPrimaryValue(index));
		} catch(NumberFormatException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Mailing ID not numeric?", e);
			}
				
			mailingId = 0;
		}

		// Try to parse link ID
		try {
			linkId = Integer.parseInt(form.getSecondaryValue(index));
		} catch(NumberFormatException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Link ID not numeric?", e);
			}
				
			linkId = 0;
		}
		
		
		
		return new TargetNodeMailingClickedOnSpecificLink(
				companyID,
				form.getChainOperator(index),
				form.getParenthesisOpened(index),
				form.getPrimaryOperator(index),
				mailingId,
				linkId,
				form.getParenthesisClosed(index));
	}

    /**
	 * Service for accessing target groups.
	 */
	protected ComTargetService targetService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Set DAO accessing mailing data.
	 * 
	 * @param mailingDao DAO accessing mailing data
	 */
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	/**
	 * Set service for accessing target groups.
	 * 
	 * @param targetService service for accessing target groups
	 */
	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}
	
	@Required
	public ComMailingDao getMailingDao() {
		return this.mailingDao;
	}

	@Required
    public void setColumnInfoService(ColumnInfoService columnInfoService) {
        this.columnInfoService = columnInfoService;
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

	@Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

	@Required
    public void setTargetRepresentationFactory(TargetRepresentationFactory targetRepresentationFactory) {
        this.targetRepresentationFactory = targetRepresentationFactory;
    }

	@Required
    public void setTargetNodeFactory(TargetNodeFactory targetNodeFactory) {
        this.targetNodeFactory = targetNodeFactory;
    }
	   
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	@Required
    public void setTargetFactory(TargetFactory targetFactory) {
        this.targetFactory = targetFactory;
    }

    @Required
    public void setTrackableLinkDao(TrackableLinkDao dao) {
    	this.trackableLinkDao = dao;
    }

    @Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}
}
