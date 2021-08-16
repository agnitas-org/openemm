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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.dao.TrackableLinkDao;
import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.exception.TargetGroupIsInUseException;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.TargetEqlQueryBuilder;
import org.agnitas.service.WebStorage;
import org.agnitas.target.PseudoColumn;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.TargetForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.forms.ComTargetForm;

/**
 * Struts {@link Action} dealing with target groups.
 */
public class ComTargetAction extends StrutsActionBase {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTargetAction.class);

    private ComBirtReportDao birtReportDao;
    
    private ComRecipientDao recipientDao;
	protected TargetFactory targetFactory;
	protected ConfigService configService;
	protected MailingService mailingService;
	protected TrackableLinkDao trackableLinkDao;
	protected TargetEqlQueryBuilder targetEqlQueryBuilder;
	private WebStorage webStorage;

    /** Facade providing full EQL functionality. */
    private EqlFacade eqlFacade;
    
    protected ColumnInfoService columnInfoService;
	
    public static final int ACTION_CREATE_ML = ACTION_LAST + 1;

	public static final int ACTION_CLONE = ACTION_LAST + 2;
	
	public static final int ACTION_DELETE_RECIPIENTS_CONFIRM = ACTION_LAST + 3;
	
	public static final int ACTION_DELETE_RECIPIENTS = ACTION_LAST + 4;
	
	public static final int ACTION_BACK_TO_MAILINGWIZARD = ACTION_LAST + 5;
	
	public static final int ACTION_BULK_CONFIRM_DELETE = ACTION_LAST + 6;
	public static final int ACTION_BULK_DELETE = ACTION_LAST + 7;
	public static final int ACTION_LOCK_TARGET_GROUP = ACTION_LAST + 8;
	public static final int ACTION_UNLOCK_TARGET_GROUP = ACTION_LAST + 9;
	public static final int ACTION_REBUILD_STRUCTURE_DATA = ACTION_LAST + 10;

	public static final int ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD = ACTION_LAST + 11;

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
		case ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD:
			return "confirm_delete";

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
        
        ActionForward destination = null;
        ActionMessages errors = new ActionErrors();
        ActionMessages messages = new ActionMessages();
        ActionMessages rulesValidationErrors = new ActionMessages();

        ComAdmin admin = AgnUtils.getAdmin(req);

        assert admin != null;

		try {
			switch (targetForm.getAction()) {

			case ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD:
            case ACTION_CONFIRM_DELETE:
            	targetForm.setPreviousAction(targetForm.getAction());
                loadTarget(targetForm, req);
                loadDependentEntities(targetForm, req);
                targetForm.setAction(ACTION_DELETE);
                return mapping.findForward("delete");

			case ACTION_DELETE:
				targetForm.setSearchEnabled(targetService.isBasicFullTextSearchSupported());
				
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
				
                if (targetForm.getPreviousAction() == ACTION_CONFIRM_DELETE_FROM_MAILINGWIZARD) {
                	destination = mapping.findForward("mailing_wizard_new_target");
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
					loadTarget( targetForm, req);
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
					loadTarget( targetForm, req);
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
				loadTarget(targetForm, req);
				this.getRecipientNumber(targetForm, req);
				destination = mapping.findForward("delete_recipients");
				break;
				
			case ACTION_DELETE_RECIPIENTS:
				loadTarget(targetForm, req);
				targetService.deleteRecipients(targetForm.getTargetID(), AgnUtils.getCompanyID(req));
				writeUserActivityLog(AgnUtils.getAdmin(req), "edit target group",
						"All recipients deleted from target group " + targetForm.getShortname() + " (" + targetForm.getTargetID() + ")", logger);
				targetForm.setAction(StrutsActionBase.ACTION_LIST);
				destination = mapping.findForward(listTargetGroups(targetForm, req));
				break;
           	
            case ACTION_LIST:
                targetForm.setSearchEnabled(targetService.isBasicFullTextSearchSupported());
                destination = mapping.findForward(listTargetGroups(targetForm, req));
                break;

            case ACTION_CREATE_ML:
            	destination = mapping.findForward("create_ml");
            	break;
            	
            case ACTION_CLONE:
				if (targetForm.getTargetID() != 0) {
					loadTarget(targetForm, req);
					cloneTarget(targetForm, req, rulesValidationErrors);
					targetForm.setAction(ACTION_SAVE);
				}
				destination = mapping.findForward("view");
				break;

			// Here comes some actions, that have been replaced by the QueryBuilder action and are not longer supported by this action
        	case ACTION_REBUILD_STRUCTURE_DATA:
       		case ACTION_VIEW:
       		case ACTION_SAVE:
            case ACTION_NEW:
            	try {
            		throw new RuntimeException(); // Just to get the stack trace
            	} catch(final RuntimeException e) {
            		logger.fatal(String.format("Invoked ComTargetAction with unsupported action %d - forwardind to overview page", targetForm.getAction()), e);
            	}
            	
				//$FALL-THROUGH$
			default:
            	destination = mapping.findForward(listTargetGroups(targetForm, req));
			}
		} catch (Exception e) {
			logger.error("execute: " + e, e);
			//errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}
		
		if (destination != null) {
			if ("success".equals(destination.getName())) {
				prepareListParameters(targetForm, admin);
				req.setAttribute("targetlist", loadTargetList(admin, targetForm));
				targetForm.setTargetComplexities(targetService.getTargetComplexities(AgnUtils.getCompanyID(req)));
			}
		}

		req.setAttribute("rulesValidationErrors", rulesValidationErrors);

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(req, errors);
            if (destination == null) {
                return (new ActionForward(mapping.getInput()));
            }
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

		return destination;
	}

	private String listTargetGroups(ComTargetForm form, HttpServletRequest request) {
		if (form.getColumnwidthsList() == null) {
        	form.setColumnwidthsList(getInitializedColumnWidthList(3));
        }

		final ComAdmin admin = AgnUtils.getAdmin(request);
		prepareListParameters(form, admin);
		request.setAttribute("targetlist", loadTargetList(admin, form));
		form.setTargetComplexities(targetService.getTargetComplexities(AgnUtils.getCompanyID(request)));

		AgnUtils.setAdminDateTimeFormatPatterns(request);
		return "list";
	}

	private void prepareListParameters(ComTargetForm form, ComAdmin admin) {
		synchronized (ComWebStorage.TARGET_OVERVIEW) {
			final boolean isBundlePresented = webStorage.isPresented(ComWebStorage.TARGET_OVERVIEW);
			webStorage.access(ComWebStorage.TARGET_OVERVIEW, storage -> {
				if (form.getNumberOfRows() > 0) {
					storage.setRowsCount(form.getNumberOfRows());
					storage.setShowWorldDelivery(form.isShowWorldDelivery());
					storage.setShowTestAndAdminDelivery(form.isShowTestAndAdminDelivery());
				} else {
					form.setNumberOfRows(storage.getRowsCount());
					form.setShowWorldDelivery(storage.isShowWorldDelivery());
					if (!isBundlePresented && admin.permissionAllowed(Permission.MAILING_SEND_ADMIN_TARGET)) {
						storage.setShowTestAndAdminDelivery(true);
					}
					form.setShowTestAndAdminDelivery(storage.isShowTestAndAdminDelivery());
				}
			});
		}
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
	 * Gets number of recipients affected in a target group.
	 */
	protected void getRecipientNumber(TargetForm aForm, HttpServletRequest req) throws UnknownTargetGroupIdException {
		final int companyId = AgnUtils.getCompanyID(req);

		ComTarget target = targetService.getTargetGroup(aForm.getTargetID(), companyId);
		int numOfRecipients = recipientDao.sumOfRecipients(companyId, target.getTargetSQL());

		aForm.setNumOfRecipients(numOfRecipients);
	}

	protected void loadTarget(ComTargetForm aForm, HttpServletRequest req) throws UnknownTargetGroupIdException {
		ComTarget aTarget = targetService.getTargetGroup(aForm.getTargetID(), AgnUtils.getCompanyID( req));

		if (aTarget == null || aTarget.getId() == 0) {
			logger.warn( "loadTarget: could not load target " + aForm.getTargetID());
			aTarget = targetFactory.newTarget();
			aTarget.setId(aForm.getTargetID());
		}
		aForm.setShortname(aTarget.getTargetName());
		aForm.setDescription(aTarget.getTargetDescription());
		aForm.setUseForAdminAndTestDelivery(aTarget.isAdminTestDelivery());
		aForm.setLocked(aTarget.isLocked());
		aForm.setEql(aTarget.getEQL());

	    if (logger.isInfoEnabled()) {
	    	logger.info("loadTarget: target " + aForm.getTargetID() + " loaded");
	    }
	}

    /**
     * Loads mailings, templates and BIRT reports that are dependent on this target group
     */
    protected void loadDependentEntities(TargetForm aForm, HttpServletRequest req) throws UnknownTargetGroupIdException {
        ComTarget aTarget = targetService.getTargetGroup(aForm.getTargetID(), AgnUtils.getCompanyID(req));

        if (aTarget.getId() != 0) {
            List<LightweightMailing> affectedMailings = mailingService.getMailingsDependentOnTargetGroup(AgnUtils.getCompanyID(req), aTarget.getId());

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

	protected List<TargetLight> loadTargetList(final ComAdmin admin, TargetForm form) {
		ComTargetForm targetForm = (ComTargetForm) form;
        String searchQuery = targetForm.getSearchQueryText();

		TargetLightsOptions options = TargetLightsOptions.builder()
				.setCompanyId(admin.getCompanyID())
				.setWorldDelivery(targetForm.isShowWorldDelivery())
				.setAdminTestDelivery(targetForm.isShowTestAndAdminDelivery())
				.setSearchName(targetForm.isSearchNameChecked())
				.setSearchDescription(targetForm.isSearchDescriptionChecked())
				.setSearchText(searchQuery)
				.setAltgMode(AltgMode.NO_ALTG)
				.build();

        return targetService.getTargetLights(options);
	}
    
    /**
	 * Saves target.
	 */
	protected int saveTarget(ComTargetForm aForm, HttpServletRequest req, ActionMessages rulesValidationErrors) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);

		ComTarget oldTarget = targetService.getTargetGroup(aForm.getTargetID(), companyId);
        ComTarget newTarget = targetFactory.newTarget();

        newTarget.setId(aForm.getTargetID());
        newTarget.setTargetName(aForm.getShortname());
        newTarget.setTargetDescription(aForm.getDescription());
        newTarget.setCompanyID(companyId);
        newTarget.setAdminTestDelivery(aForm.isUseForAdminAndTestDelivery());
        newTarget.setEQL(aForm.getEql());
        newTarget.setTargetSQL(eqlFacade.convertEqlToSql(aForm.getEql(), companyId).getSql());

        int newId = targetService.saveTarget(AgnUtils.getAdmin(req), newTarget, oldTarget, rulesValidationErrors, this::writeUserActivityLog);

        if (aForm.getTargetID() == 0) {
			aForm.setTargetID(newId);
        }

		return newId;
	}

	/**
	 * Clone target.
	 */
	protected void cloneTarget(ComTargetForm aForm, HttpServletRequest req, ActionMessages rulesValidationErrors) throws Exception {
        final String newName = SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(req)) +
				" " + aForm.getShortname();

		aForm.setTargetID(0);
		aForm.setShortname(newName);

        saveTarget(aForm, req, rulesValidationErrors);
        writeUserActivityLog(AgnUtils.getAdmin(req), "create target group", newName + " (" + aForm.getTargetID() + ")");
	}

    /**
	 * Service for accessing target groups.
	 */
	protected ComTargetService targetService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
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
    public void setColumnInfoService(ColumnInfoService columnInfoService) {
        this.columnInfoService = columnInfoService;
    }

    @Required
	public void setTargetEqlQueryBuilder(TargetEqlQueryBuilder targetEqlQueryBuilder) {
		this.targetEqlQueryBuilder = targetEqlQueryBuilder;
	}

	@Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
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
	public void setBirtReportDao(ComBirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }

    @Required
    public void setEqlFacade(EqlFacade eqlFacade) {
    	this.eqlFacade = eqlFacade;
    }

    @Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}

	protected void setPseudoRuleAttributes(HttpServletRequest request) {
		request.setAttribute("COLUMN_INTERVAL_MAILING", PseudoColumn.INTERVAL_MAILING);
	}
}
