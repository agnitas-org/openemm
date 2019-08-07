/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Campaign;
import org.agnitas.beans.factory.CampaignFactory;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.CampaignForm;
import org.agnitas.web.forms.FormUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComTargetDao;


public class CampaignAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(CampaignAction.class); 
    
	public static final String FUTURE_TASK = "GET_CAMPAIGN_LIST";
    public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 3;
    public static final int ACTION_SECOND_LAST = ACTION_LAST + 3;
    public static final int ACTION_NEW_MAILING = ACTION_LAST + 5;

    protected ComCampaignDao campaignDao;
    protected ComCompanyDao companyDao;
    protected ExecutorService workerExecutorService;
    protected MailingDao mailingDao;
    protected CampaignFactory campaignFactory;
    protected ComTargetDao targetDao;

    protected WebStorage webStorage;
	protected ConfigService configService;

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
	 * ACTION_LIST: loads list of campaigns into request and forwards to campaign list page.
	 * <br><br>
	 * ACTION_SAVE: saves campaign entry and forwards to the campaign list page.
	 * <br><br>
     * ACTION_VIEW: resets campaign form, loads data of chosen campaign into form,
     *     loads list of campaign mailings into request, forwards to campaign view page
     * <br><br>
     * ACTION_NEW: creates new campaign db entry; reloads form data; loads list of mailings into request;
     *     forwards to campaign list page.
     * <br><br>
     * <br><br>
     * ACTION_VIEW_WITHOUT_LOAD: is used after failing form validation for loading essential data into request
     *     before returning to the view page. Does not reload form data.
     * <br><br>
	 * ACTION_CONFIRM_DELETE: loads campaign data into form; forwards to jsp with question to confirm deletion
	 * <br><br>
	 * ACITON_DELETE: deletes the entry of certain campaign, forwards to campaign list page.
	 * <br><br>
	 * Any other ACTION_* would cause a forward to "list"
     * <br><br>
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

        if (logger.isInfoEnabled()) logger.info("Action: "+aForm.getAction());
        
        try {
            switch(aForm.getAction()) {
                case CampaignAction.ACTION_VIEW_WITHOUT_LOAD:
                    loadCampaignFormData(aForm, req);
                    aForm.setAction(CampaignAction.ACTION_SAVE);
                    destination=mapping.findForward("view");
					if ( aForm.getColumnwidthsList() == null) {
                		aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                	}
                	break;
                default:
                    aForm.setAction(CampaignAction.ACTION_LIST);
                    destination=mapping.findForward("list");                    
            }
            
        } catch (Exception e) {
            logger.error("execute: "+e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

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
            order = new Integer(request.getParameter(new ParamEncoder("campaign").encodeParameterName(TableTagParameters.PARAMETER_ORDER)));
        }

        return campaignDao.getCampaignList(AgnUtils.getCompanyID(request), sort, order);
    }

    @Required
    public void setCampaignDao(ComCampaignDao campaignDao) {
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
