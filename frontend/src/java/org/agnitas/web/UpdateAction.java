/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.Permission;

/**
 * Implementation of <strong>Action</strong> that handles Account Admins
 */
public final class UpdateAction extends StrutsActionBase {
	private static final transient Logger logger = Logger.getLogger(UpdateAction.class);
	
	protected ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    // ---------------------------------------- Public Methods

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * ACTION_LIST: launches Automatic update of OpenEMM, <br>
     *          forwards to success or error page <br>
     * <br><br>
     * ACTION_VIEW: forwards to jsp with question to confirm update
     * <br><br>
     * ACTION_NEW: forwards to administration list page
     * <br><br>
     * Any other ACTION_* would cause a forward to "list"
     * <br><br>
     * @param mapping The ActionMapping used to select this instance
     * @param form
     * @param req
     * @param res
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination
     */
    @Override
	public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest req,
            HttpServletResponse res)
            throws IOException, ServletException {

        UpdateForm aForm = null;
        ActionMessages errors = new ActionMessages();
        ActionForward destination=null;

        if(!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }
        
        if(form!=null) {
            aForm=(UpdateForm)form;
        } else {
            aForm=new UpdateForm();
        }

        try {
            switch(aForm.getAction()) {
                case UpdateAction.ACTION_LIST:
                    if (AgnUtils.allowed(req, Permission.UPDATE_SHOW)) {
                    	String cmd = "/home/openemm/bin/upgrade.sh start";
                    	int rc;
                    	Runtime rtime = Runtime.getRuntime ();
                        Process proc = rtime.exec (cmd);
                        rc = proc.waitFor ();
                        if (rc == 0) {
                        	destination=mapping.findForward("success");
                        }
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
                        destination=mapping.findForward("error");
                    }
                    break;
                    
                case UpdateAction.ACTION_VIEW:
                	destination=mapping.findForward("question");
                	break;
                	
                case UpdateAction.ACTION_NEW:
                	aForm.setAction(UpdateAction.ACTION_VIEW);
                	destination=mapping.findForward("list");
                	break;

                default:
                    aForm.setAction(UpdateAction.ACTION_VIEW);
                    destination=mapping.findForward("list");
            }
        } catch (Exception e) {
            logger.error("execute: "+e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            throw new ServletException(e);
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
        }
        return destination;
    }
}
