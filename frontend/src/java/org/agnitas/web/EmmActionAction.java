/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.factory.ActionOperationFactory;
import org.agnitas.beans.factory.EmmActionFactory;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.scriptvalidator.IllegalVelocityDirectiveException;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import org.agnitas.emm.core.velocity.scriptvalidator.VelocityDirectiveScriptValidator;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.forms.EmmActionForm;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTrackpointDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.action.service.UnableConvertException;

/**
 * Implementation of <strong>Action</strong> that handles Targets
 */
public class EmmActionAction extends StrutsActionBase {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(EmmActionAction.class);
    
    public static final int ACTION_ADD_MODULE = 9;
    public static final int ACTION_REMOVE_MODULE = 10;

    private ComCampaignDao campaignDao;
    private EmmActionDao emmActionDao;
    private EmmActionFactory emmActionFactory;
    private ActionOperationFactory actionOperationFactory;
    private MailingDao mailingDao;
    protected VelocityDirectiveScriptValidator velocityDirectiveScriptValidator;
    protected ComEmmActionService emmActionService;
    protected ComRecipientDao recipientDao;
    protected BlacklistService blacklistService;
    protected ComTrackpointDao trackpointDao;
    
	protected ConfigService configService;
	protected WebStorage webStorage;
	
    @Override
	public String subActionMethodName(int subAction) {
		switch (subAction) {
			case ACTION_LIST:
				return "list";
			case ACTION_VIEW:
				return "view";
			case ACTION_SAVE:
				return "save";
			case ACTION_NEW:
				return "new";
			case ACTION_DELETE:
				return "delete";
			case ACTION_CONFIRM_DELETE:
				return "confirm_delete";
            case ACTION_ADD_MODULE:
                return "add_module";
            case ACTION_REMOVE_MODULE:
                return "remove_module";
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
     * <br>
	 * ACTION_LIST: initializes columns width list if necessary, forwards to emm action list page.
	 * <br><br>
	 * ACTION_SAVE: saves emm action data in database; sets new emm action id in form field; forwards
     *     to emm action view page.
	 * <br><br>
     * ACTION_VIEW: loads data of chosen emm action into form, forwards to emm action view page
     * <br><br>
     * ACTION_ADD_MODULE: adds new action operation module to the given emm action, forwards to emm action view page.
     * <br><br>
	 * ACTION_CONFIRM_DELETE: loads data of chosen emm action into form, forwards to jsp with question to confirm deletion
	 * <br><br>
	 * ACTION_DELETE: deletes the entry of certain emm action, forwards to emm action list page
	 * <br><br>
	 * Any other ACTION_* would cause a forward to "list"
     * <br><br>
     * If the forward is "list" - loads list of emm-actions to request; also loads list of campaigns and list of
     * sent actionbased-mailings (sets that to form)
     *
     * @param form ActionForm object, data for the action filled by the jsp
     * @param req  HTTP request
     * @param res HTTP response
     * @param mapping The ActionMapping used to select this instance
     * @return destination specified in struts-config.xml to forward to next jsp
     * @throws Exception
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse res) throws Exception {

        EmmActionForm aForm = null;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;
        ComAdmin admin = AgnUtils.getAdmin(request);

        if (Objects.isNull(admin)) {
            return mapping.findForward("logon");
        }

        aForm = (EmmActionForm) form;

        request.setAttribute("oplist", getActionOperations());

        if (logger.isInfoEnabled()) {
			logger.info("Action: " + aForm.getAction());
		}
        try {
            switch (aForm.getAction()) {
            	case EmmActionAction.ACTION_NEW:
            		aForm.setShortname(SafeString.getLocaleString("default.Name", AgnUtils.getLocale(request)));
            		aForm.setDescription(SafeString.getLocaleString("default.description", AgnUtils.getLocale(request)));
            		aForm.setActionID(0);
            		aForm.setActions(null);
            		aForm.setDeleteModule(0);
            		aForm.setType(0);

                    aForm.setAction(EmmActionAction.ACTION_SAVE);

                    // Some deserialized Actions need the mailings to show their configuration data
                    aForm.setMailings(mailingDao.getMailingsByStatusE(AgnUtils.getCompanyID(request)));
                    aForm.setCampaigns(campaignDao.getCampaignList(AgnUtils.getCompanyID(request), "lower(shortname)", 1));

                    destination = mapping.findForward("success");
            		break;
                case EmmActionAction.ACTION_LIST:
                    //loadActionUsed(aForm, req);
                    if (aForm.getColumnwidthsList() == null) {
                        aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                    }
                    AgnUtils.setAdminDateTimeFormatPatterns(request);
                    destination = mapping.findForward("list");
                    break;
                case EmmActionAction.ACTION_VIEW:
                    if (aForm.getActionID() != 0) {
                        aForm.setAction(EmmActionAction.ACTION_SAVE);
                        loadAction(aForm, request);
                    } else {
                        aForm.setAction(EmmActionAction.ACTION_SAVE);
                    }

                    // Some deserialized Actions need the mailings to show their configuration data
                    aForm.setMailings(mailingDao.getMailingsByStatusE(AgnUtils.getCompanyID(request)));
                    aForm.setCampaigns(campaignDao.getCampaignList(AgnUtils.getCompanyID(request), "lower(shortname)", 1));

                    destination = mapping.findForward("success");
                    break;
                case EmmActionAction.ACTION_SAVE:
                    saveAction(aForm, admin, errors);
                    if (errors.isEmpty()) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    }
                    destination = mapping.findForward("success");
                    break;
                case EmmActionAction.ACTION_CONFIRM_DELETE:
                    loadAction(aForm, request);
                    destination = mapping.findForward("delete");
                    aForm.setAction(EmmActionAction.ACTION_DELETE);
                    break;
                case EmmActionAction.ACTION_DELETE:
                    deleteAction(aForm, request);

                    // Show "changes saved"
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    aForm.setAction(EmmActionAction.ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;
                case EmmActionAction.ACTION_ADD_MODULE:
                    addActionModule(aForm);
                    aForm.setAction(EmmActionAction.ACTION_SAVE);
                    destination = mapping.findForward("success");
                    break;
                case EmmActionAction.ACTION_REMOVE_MODULE:
                    removeActionModule(aForm);
                    aForm.setAction(EmmActionAction.ACTION_SAVE);
                    destination = mapping.findForward("success");
                    break;
                default:
                    if (aForm.getColumnwidthsList() == null) {
                        aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                    }
                    destination = mapping.findForward("list");
                    break;
            }
        } catch (UnableConvertException e) {
            logger.warn("Attempt to edit old action without converter", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception.actop.convert"));
        } catch (Exception e) {
            logger.error("execute", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        if (destination != null && "list".equals(destination.getName())) {
            try {
                FormUtils.syncNumberOfRows(webStorage, WebStorage.ACTION_OVERVIEW, aForm);
                request.setAttribute("emmactionList", getActionList(request));
                aForm.setMailings(mailingDao.getMailingsByStatusE(AgnUtils.getCompanyID(request)));
            } catch (Exception e) {
                logger.error("getActionList", e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            }
        }


        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return (new ActionForward(mapping.getInput()));
        }

        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }

        return destination;
    }

    /**
     * Loads an emm action data from DB into the form.
     *
     * @param aForm EmmActionForm object
     * @param req  HTTP request
     */
    protected void loadAction(EmmActionForm aForm, HttpServletRequest req) {
        EmmAction aAction = emmActionService.getEmmAction(aForm.getActionID(), AgnUtils.getCompanyID(req));

        if (aAction != null && aAction.getId() != 0) {
            aForm.setShortname(aAction.getShortname());
            aForm.setDescription(aAction.getDescription());
            aForm.setType(aAction.getType());
            aForm.setActions(aAction.getActionOperations());
            aForm.setIsActive(aAction.getIsActive());

            if (logger.isInfoEnabled()) {
            	logger.info("loadAction: action "+aForm.getActionID()+" loaded");
            }
            writeUserActivityLog(AgnUtils.getAdmin(req), "view action", aForm.getShortname());
        } else {
			logger.warn("loadAction: could not load action " + aForm.getActionID());
        }
    }

    private void saveAction(EmmActionForm aForm, ComAdmin admin, ActionMessages errors) throws Exception {
        if (isValidActions(aForm, errors)) {
            EmmAction aAction = emmActionFactory.newEmmAction();

            int companyId = admin.getCompanyID();
            aAction.setCompanyID(companyId);
            aAction.setId(aForm.getActionID());
            aAction.setType(aForm.getType());
            aAction.setShortname(aForm.getShortname());
            aAction.setDescription(aForm.getDescription());
            aAction.setIsActive(aForm.getIsActive());
            List<AbstractActionOperationParameters> operations = aForm.getActions();
            if (operations == null) {
                operations = new ArrayList<>();
            }
            
            for (AbstractActionOperationParameters operation : operations) {
				operation.setCompanyId(companyId);
				
				try {
					operation.validate(errors, admin.getLocale(), recipientDao, trackpointDao);
				} catch (Exception e) {
					logger.error("Cannot validate AbstractActionOperationParameters: " + e.getMessage(), e);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error", e.getMessage()));
				}
                
                if (ActionOperationType.SEND_MAILING.equals(operation.getOperationType())) {
                	String bccAddress = ((ActionOperationSendMailingParameters) operation).getBcc();
                	if (StringUtils.isNotBlank(bccAddress) && blacklistService.blacklistCheckCompanyOnly(bccAddress, companyId)) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.action.blacklistedBcc", bccAddress));
                        return;
                	}
                } else if (ActionOperationType.SERVICE_MAIL.equals(operation.getOperationType())) {
                	String toAddress = ((ActionOperationServiceMailParameters) operation).getToAddress();
                	if (StringUtils.isNotBlank(toAddress)) {
	                	for (String singleAdr : toAddress.split(";|,| ")) {
	            			singleAdr = AgnUtils.normalizeEmail(singleAdr);
	            			if (StringUtils.isNotBlank(singleAdr) && blacklistService.blacklistCheckCompanyOnly(singleAdr, companyId)) {
	            				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.action.blacklistedTo", singleAdr));
	            				return;
	            			}
	            		}
                	}
                	
                	String fromAddress = ((ActionOperationServiceMailParameters) operation).getFromAddress();
                	if (StringUtils.isNotBlank(fromAddress)) {
	                	if (!fromAddress.toLowerCase().startsWith("$requestparameters.")
	                			&& !fromAddress.toLowerCase().startsWith("$customerdata.")) {
		                	fromAddress = AgnUtils.normalizeEmail(fromAddress);
							if (StringUtils.isNotBlank(fromAddress) && blacklistService.blacklistCheckCompanyOnly(fromAddress, companyId)) {
								errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.action.blacklistedFrom", fromAddress));
		        				return;
							}
						}
                	}
					
                	String replyAddress = ((ActionOperationServiceMailParameters) operation).getReplyAddress();
                	if (StringUtils.isNotBlank(replyAddress)) {
	                	if (!replyAddress.toLowerCase().startsWith("$requestparameters.")
	                			&& !replyAddress.toLowerCase().startsWith("$customerdata.")) {
		                	replyAddress = AgnUtils.normalizeEmail(replyAddress);
		    				if (StringUtils.isNotBlank(replyAddress) && blacklistService.blacklistCheckCompanyOnly(replyAddress, companyId)) {
		    					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.action.blacklistedReply", replyAddress));
		        				return;
		    				}
	                	}
                	}
                }
            }
            aAction.setActionOperations(operations);

            List<UserAction> userActions = new ArrayList<>();
            aForm.setActionID(emmActionService.saveEmmAction(aAction, userActions));

            for (UserAction userAction : userActions) {
                writeUserActivityLog(admin, userAction);
            }
        }
    }

    private boolean isValidActions(EmmActionForm form, ActionMessages errors){
    	List<AbstractActionOperationParameters> list =  form.getActions();
    	
    	if (list != null) {
	    	for ( Object action : list) {
	    		if (action instanceof ActionOperationExecuteScriptParameters) {
	    			ActionOperationExecuteScriptParameters scriptAction = (ActionOperationExecuteScriptParameters) action;
                    try {
                        this.velocityDirectiveScriptValidator.validateScript(scriptAction.getScript());
                    } catch(ScriptValidationException e) {
                        String directive = ((IllegalVelocityDirectiveException) e).getDirective();
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.action.illegal_directive", directive));
                    }
                }
	    	}

	    	if(isInvalidBcc(list)){
	    	    errors.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.action.invalid.bbc"));
            }
    	}

    	return errors.isEmpty();
    }

    private boolean isInvalidBcc(List<AbstractActionOperationParameters> actions) {
        return actions.stream()
                .filter(ActionOperationSendMailingParameters.class::isInstance)
                .map(ActionOperationSendMailingParameters.class::cast)
                .map(ActionOperationSendMailingParameters::getBcc)
                .filter(StringUtils::isNotBlank)
                .anyMatch(emails->!AgnUtils.isEmailsListValid(emails));
    }

    /**
     * Deletes an action.
     *
     * @param aForm EmmActionForm object
     * @param req HTTP request
     */
    protected void deleteAction(EmmActionForm aForm, HttpServletRequest req) {
    	emmActionService.deleteEmmAction(aForm.getActionID(), AgnUtils.getCompanyID(req));
        writeUserActivityLog(AgnUtils.getAdmin(req), "delete action", aForm.getShortname());
    }

    /**
     * Gets action operations map.
     *
     * @return Map object contains emm action operations
     */
    protected Map<String, String> getActionOperations() {
		Map<String, String> mapMessageKeyToActionClass = new TreeMap<>();
		String[] names = actionOperationFactory.getTypes();
		for (String name : names) {
			mapMessageKeyToActionClass.put("action.op." + name, name);
		}
		return mapMessageKeyToActionClass;
    }

    protected void addActionModule(EmmActionForm form) {
        AbstractActionOperationParameters aMod = actionOperationFactory.newActionOperation(form.getNewModule());
        List<AbstractActionOperationParameters> actions = form.getActions();
        if (actions == null) {
            actions = new ArrayList<>();
            form.setActions(actions);
        }
        actions.add(aMod);
    }

    protected void removeActionModule(EmmActionForm form) {
        List<AbstractActionOperationParameters> actions = form.getActions();
        if (actions == null) {
            actions = new ArrayList<>();
            form.setActions(actions);
        }

        int index = form.getDeleteModule();
        if (index >= 0 && index < actions.size()) {
            actions.remove(index);
        }
    }

    public List<EmmAction> getActionList(HttpServletRequest request) {
        String[] columns = new String[] { "shortname", "shortname", "description", "shortname", "creation_date", "change_date", "active", "" };

        int sortcolumnindex = 0;
        String sortParam = request.getParameter(new ParamEncoder("emmaction").encodeParameterName(TableTagParameters.PARAMETER_SORT));
        if (sortParam != null && !sortParam.equals("null")) {
            sortcolumnindex = Integer.parseInt(sortParam);
        }

        String sort = columns[sortcolumnindex];

        int order = 1;
        String orderParam = request.getParameter(new ParamEncoder("emmaction").encodeParameterName(TableTagParameters.PARAMETER_ORDER));
        if (orderParam != null && !orderParam.equals("null")) {
            order = new Integer(orderParam);
        }

        Boolean activenessFilter = null;
        String activenessFilterString = request.getParameter("activenessFilter");

        if (StringUtils.equalsIgnoreCase(activenessFilterString, "active")) {
            activenessFilter = Boolean.TRUE;
        } else if (StringUtils.equalsIgnoreCase(activenessFilterString, "inactive")) {
            activenessFilter = Boolean.FALSE;
        }

        return emmActionDao.getActionList(AgnUtils.getCompanyID(request), sort, order == 1, activenessFilter);
    }

    @Required
    public void setCampaignDao(ComCampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    @Required
    public void setEmmActionDao(EmmActionDao emmActionDao) {
        this.emmActionDao = emmActionDao;
    }

    @Required
    public void setEmmActionFactory(EmmActionFactory emmActionFactory) {
        this.emmActionFactory = emmActionFactory;
    }

    @Required
    public void setActionOperationFactory(ActionOperationFactory actionOperationFactory) {
        this.actionOperationFactory = actionOperationFactory;
    }

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setVelocityDirectiveScriptValidator( VelocityDirectiveScriptValidator validator) {
    	this.velocityDirectiveScriptValidator = validator;
    }

    @Required
	public void setEmmActionService(ComEmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }
    
    @Required
    public void setBlacklistService(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }
    
    // not required for OpenEMM
    public void setTrackpointDao(ComTrackpointDao trackpointDao) {
        this.trackpointDao = trackpointDao;
    }
}
