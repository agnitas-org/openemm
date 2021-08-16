/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.beans.Mailing;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.CampaignAction;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;


public final class ComCampaignAction extends CampaignAction {
	private static final transient Logger logger = Logger.getLogger(ComCampaignAction.class);
    
    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
//        case ACTION_VIEW_RIGHTS:
//            return "view_without_load";
        case ACTION_NEW_MAILING:
            return "new_mailing";
        case ACTION_VIEW_WITHOUT_LOAD:
            return "view_without_load";            
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
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param req The HTTP request we are processing
     * @param res The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    @Override
	public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest req,
            HttpServletResponse res)
            throws IOException, ServletException {

        // Validate the request parameters specified by the user
        ComCampaignForm aForm = (ComCampaignForm) form;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;
        
        if (logger.isInfoEnabled()) {
        	logger.info("Action: " + aForm.getAction());
        }

        try {
            switch(aForm.getAction()) {
            	case CampaignAction.ACTION_LIST:
                    if (aForm.getColumnwidthsList() == null) {
                        aForm.setColumnwidthsList(getInitializedColumnWidthList(3));
                    }
                    destination = mapping.findForward("list");
                    aForm.reset(mapping, req);
                    aForm.setAction(CampaignAction.ACTION_LIST);
                    break;

            	case CampaignAction.ACTION_VIEW:
                    updateForwardParameters(req);
                	aForm.reset(mapping, req);
                    loadCampaign(aForm, req);
                    aForm.setAction(CampaignAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    loadCampaignFormData(aForm, req);
					if ( aForm.getColumnwidthsList() == null) {
                		aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                	}
                    saveToken(req);
                    break;

                case CampaignAction.ACTION_SAVE:
                    if (isTokenValid(req, true)) {
                        saveCampaign(aForm, req);
                        resetToken(req);
                        // Show "changes saved"
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    }
                    aForm.setAction(CampaignAction.ACTION_LIST);
                    destination = mapping.findForward("list");
                    if (errors.size() == 0 && req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID) != null
                            && (Integer) req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID) != 0) {
                        Object workflowId = req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
                        
                        destination = mapping.findForward("workflow_view");
                        String path = destination.getPath().replace("{WORKFLOW_ID}", workflowId.toString());
                        ActionRedirect redirect = new ActionRedirect(path);
                        
                        redirect.addParameter("forwardParams", req.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS).toString()
                                + ";elementValue=" + Integer.toString(aForm.getCampaignID()));
                        return redirect;
                    }
                    break;

                case CampaignAction.ACTION_NEW:
                    updateForwardParameters(req);
                    aForm.reset(mapping, req);
                    aForm.setAction(CampaignAction.ACTION_SAVE);
                    aForm.setCampaignID(0);
                    loadCampaignFormData(aForm, req);
                    destination = mapping.findForward("view");
                    saveToken(req);
                    break;

                case CampaignAction.ACTION_CONFIRM_DELETE:
                    if (aForm.getMailingID() == 0) {
                        loadCampaign(aForm, req);
                    } else {
                        loadMailing(aForm.getMailingID(), req);
                    }
                    aForm.setAction(CampaignAction.ACTION_DELETE);
                    destination = mapping.findForward("delete");
                    break;

                case CampaignAction.ACTION_DELETE:
                    if (aForm.getMailingID() == 0) {
                        if (AgnUtils.allowed(req, Permission.CAMPAIGN_SHOW)) {
                            if (AgnUtils.parameterNotEmpty(req, "kill")) {
                                this.deleteCampaign(aForm, req);
                                aForm.setAction(CampaignAction.ACTION_LIST);
                                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                            }
                        } else {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
                        }
                        destination = mapping.findForward("list");
                    } else {
                        mailingDao.deleteMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));
                        aForm.setMailingID(0);
                        loadCampaignFormData(aForm, req);
                        aForm.setAction(CampaignAction.ACTION_SAVE);
                        destination = mapping.findForward("view");
                    }
                    break;

                case CampaignAction.ACTION_NEW_MAILING:
                    MailingBaseForm mailingBaseForm = (MailingBaseForm)req.getSession().getAttribute("mailingBaseForm");
                    if (mailingBaseForm != null) {
                        mailingBaseForm.setIsTemplate(false);
                    }
                    ActionRedirect redirect = new ActionRedirect(mapping.findForward("mailing_create"));
                    redirect.addParameter(ComMailingBaseAction.MAILING_ID, req.getParameter(ComMailingBaseAction.MAILING_ID));
                    redirect.addParameter(ComMailingBaseAction.CAMPAIGN_ID, req.getParameter(ComMailingBaseAction.CAMPAIGN_ID));
                    return redirect;

                default:                  
               	 destination = super.execute(mapping, form, req, res);	
            }

        } catch (Exception e) {
            logger.error("execute in ComCampaignAction: "+e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        // if the destination is "list" then we load the data for the list-view.
        processListDestination(destination, aForm, req, errors);
        
        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
        }        
        
        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
        	saveMessages(req, messages);
        }
        
        return destination;
    }

    private void updateForwardParameters(HttpServletRequest req) {
        WorkflowUtils.updateForwardParameters(req);
    }

    public void loadMailing(int mailingID, HttpServletRequest req){
        Mailing mailing = mailingDao.getMailing(mailingID, AgnUtils.getCompanyID(req));
        req.setAttribute("tmpMailingID", mailingID);
        req.setAttribute("tmpShortname",mailing.getShortname());
        req.setAttribute("isTemplate",mailing.isIsTemplate() ? 1 : 0);
    }
}
