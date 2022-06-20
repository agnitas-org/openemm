/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.factory.impl.CampaignFactory;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;


public final class CampaignAction extends StrutsActionBase {
	private static final transient Logger logger = LogManager.getLogger(CampaignAction.class);
	
	public static final String FUTURE_TASK = "GET_CAMPAIGN_LIST";
    public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 3;
    public static final int ACTION_SECOND_LAST = ACTION_LAST + 3;
    public static final int ACTION_NEW_MAILING = ACTION_LAST + 5;
    
    protected CampaignDao campaignDao;
    protected ComCompanyDao companyDao;
    protected ExecutorService workerExecutorService;
    protected MailingDao mailingDao;
    protected CampaignFactory campaignFactory;
    protected ComTargetDao targetDao;

    protected WebStorage webStorage;
	protected ConfigService configService;
	
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
        CampaignForm aForm = (CampaignForm) form;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;
        
        if (logger.isInfoEnabled()) {
        	logger.info("Action: " + aForm.getAction());
        }

        try {
            switch(aForm.getAction()) {
            	case ACTION_LIST:
                    if (aForm.getColumnwidthsList() == null) {
                        aForm.setColumnwidthsList(getInitializedColumnWidthList(3));
                    }
                    destination = mapping.findForward("list");
                    aForm.reset(mapping, req);
                    aForm.setAction(ACTION_LIST);
                    break;

            	case ACTION_VIEW:
                    updateForwardParameters(req);
                	aForm.reset(mapping, req);
                    loadCampaign(aForm, req);
                    aForm.setAction(ACTION_SAVE);
                    destination = mapping.findForward("view");
                    loadCampaignFormData(aForm, req);
					if ( aForm.getColumnwidthsList() == null) {
                		aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                	}
                    saveToken(req);
                    break;
                    
            	case ACTION_VIEW_WITHOUT_LOAD:
                    loadCampaignFormData(aForm, req);
                    aForm.setAction(ACTION_SAVE);
                    destination=mapping.findForward("view");
					if ( aForm.getColumnwidthsList() == null) {
                		aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                	}
                	break;

                case ACTION_SAVE:
                    if (isTokenValid(req, true)) {
                        saveCampaign(aForm, req);
                        resetToken(req);
                        // Show "changes saved"
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    }
                    aForm.setAction(ACTION_LIST);
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

                case ACTION_NEW:
                    updateForwardParameters(req);
                    aForm.reset(mapping, req);
                    aForm.setAction(ACTION_SAVE);
                    aForm.setCampaignID(0);
                    loadCampaignFormData(aForm, req);
                    destination = mapping.findForward("view");
                    saveToken(req);
                    break;

                case ACTION_CONFIRM_DELETE:
                    if (aForm.getMailingID() == 0) {
                        loadCampaign(aForm, req);
                    } else {
                        loadMailing(aForm.getMailingID(), req);
                    }
                    aForm.setAction(ACTION_DELETE);
                    destination = mapping.findForward("delete");
                    break;

                case ACTION_DELETE:
                    if (aForm.getMailingID() == 0) {
                        if (AgnUtils.allowed(req, Permission.CAMPAIGN_SHOW)) {
                            if (AgnUtils.parameterNotEmpty(req, "kill")) {
                                this.deleteCampaign(aForm, req);
                                aForm.setAction(ACTION_LIST);
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
                        aForm.setAction(ACTION_SAVE);
                        destination = mapping.findForward("view");
                    }
                    break;

                case ACTION_NEW_MAILING:
                    MailingBaseForm mailingBaseForm = (MailingBaseForm)req.getSession().getAttribute("mailingBaseForm");
                    if (mailingBaseForm != null) {
                        mailingBaseForm.setIsTemplate(false);
                    }
                    ActionRedirect redirect = new ActionRedirect(mapping.findForward("mailing_create"));
                    redirect.addParameter(MailingBaseAction.MAILING_ID, req.getParameter(MailingBaseAction.MAILING_ID));
                    redirect.addParameter(MailingBaseAction.CAMPAIGN_ID, req.getParameter(MailingBaseAction.CAMPAIGN_ID));
                    return redirect;

                default:
                	aForm.setAction(ACTION_LIST);
                    destination=mapping.findForward("list");
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
    
    /**
     * Loads list of campaigns sorted by given sort parameters
     *
     * @param request  HTTP request
     * @throws InstantiationException
     * @throws IllegalAccessException
     */

    public List<Campaign> getCampaignList(HttpServletRequest request) throws IllegalAccessException, InstantiationException {
        List<Integer> charColumns = Arrays.asList(0, 1);
        String[] columns = new String[]{"shortname", "description", ""};

        int sortcolumnindex = 0;
        if (request.getParameter(new ParamEncoder("campaign").encodeParameterName(TableTagParameters.PARAMETER_SORT)) != null) {
            sortcolumnindex = Integer.parseInt(request.getParameter(new ParamEncoder("campaign").encodeParameterName(TableTagParameters.PARAMETER_SORT)));
        }

        String sort = columns[sortcolumnindex];
        if (charColumns.contains(sortcolumnindex)) {
            sort = "upper( " + sort + " )";
        }

        int order = 1;
        if (request.getParameter(new ParamEncoder("campaign").encodeParameterName(TableTagParameters.PARAMETER_ORDER)) != null) {
            order = Integer.valueOf(request.getParameter(new ParamEncoder("campaign").encodeParameterName(TableTagParameters.PARAMETER_ORDER)));
        }

        return campaignDao.getCampaignList(AgnUtils.getCompanyID(request), sort, order);
    }
    

    /**
     * prepare form for "list" destination
     */
    protected void processListDestination(ActionForward destination, CampaignForm form, HttpServletRequest req, ActionMessages errors) {
        if (destination != null && "list".equals(destination.getName())) {
            try {
                FormUtils.syncNumberOfRows(webStorage, WebStorage.ARCHIVE_OVERVIEW, form);

                req.setAttribute("campaignlist", getCampaignList(req));
            } catch (Exception e) {
                logger.error("getCampaignList: "+e, e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            }
        }
    }
    
    /**
     * Loads campaign data into form
     * @param aForm CampaignForm object
     * @param req  HTTP request
     */
    protected void loadCampaign(CampaignForm aForm, HttpServletRequest req) {
        int campaignID=aForm.getCampaignID();
        int companyID = AgnUtils.getCompanyID(req);
        Campaign myCamp = campaignDao.getCampaign(campaignID, companyID);
        
        if(myCamp != null) {
            aForm.setShortname(myCamp.getShortname());
            aForm.setDescription(myCamp.getDescription());
        } else {
            logger.error("could not load campaign: "+aForm.getTargetID());
        }
    }

    /**
     * Loads list of mailings in the campaign into request
     * @param aForm CampaignForm object
     * @param req  HTTP request
     */
    protected void loadCampaignFormData(CampaignForm aForm, HttpServletRequest req){
        int campaignID = aForm.getCampaignID();
        int companyID = AgnUtils.getCompanyID(req);
        req.setAttribute("mailinglist", campaignDao.getCampaignMailings(campaignID, companyID));
    }

    /**
     * Saves campaign in db
     * @param aForm CampaignForm object
     * @param req  HTTP request
     */
    protected void saveCampaign(CampaignForm aForm, HttpServletRequest req) {
        int campaignID=aForm.getCampaignID();
        int companyID = AgnUtils.getCompanyID(req);
        Campaign myCamp = campaignDao.getCampaign(campaignID, companyID);
        
        if(myCamp == null) {
            aForm.setCampaignID(0);
            myCamp=campaignFactory.newCampaign();
            myCamp.setCompanyID(companyID);
        }
        
        myCamp.setShortname(aForm.getShortname());
        myCamp.setDescription(aForm.getDescription());
        
        campaignID = campaignDao.save(myCamp);
        myCamp.setId(campaignID);
        aForm.setCampaignID(campaignID);
    }

    /**
     * Deletes campaign
     * @param aForm CampaignForm object
     * @param req  HTTP request
     */
    protected void deleteCampaign(CampaignForm aForm, HttpServletRequest req) {
        int campaignID=aForm.getCampaignID();
        int companyID = AgnUtils.getCompanyID(req);
        Campaign myCamp = campaignDao.getCampaign(campaignID, companyID);
        
        if(myCamp!=null) {
           campaignDao.delete(myCamp);
        }
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
    
    @Required
    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setCampaignFactory(CampaignFactory campaignFactory) {
        this.campaignFactory = campaignFactory;
    }

    /**
     * Set DAO accessing target groups.
     * 
     * @param targetDao DAO accessing target groups
     */
    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
