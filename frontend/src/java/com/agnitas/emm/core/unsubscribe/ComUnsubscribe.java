/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.BindingEntry;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.util.SafeString;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComMailing;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

//import org.agnitas.util.TimeoutLRUMap;

public class ComUnsubscribe extends HttpServlet {
	private static final long serialVersionUID = -1335116656304676065L;
	
	private static final transient Logger logger = Logger.getLogger(ComUnsubscribe.class);
	
    /**
     * Service-Method, gets called everytime a User calls the servlet
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { 
        ApplicationContext con = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ExtensibleUIDService uidService = (ExtensibleUIDService) con.getBean( ExtensibleUIDConstants.SERVICE_BEAN_NAME);
        
        // log informations about the calling client.
        //accessLogging(req);        

        String param = req.getParameter("uid");
        try {
            
        	
        	if (param == null) {
        		logger.error("got no UID parameter");
    			return;
        	} 

        	final ComExtensibleUID uid = uidService.parse( param);
            
        	if(uid == null) {
    			logger.error("got no UID for " + param);
    			return;
    		}
        	ComMailingDao mailingDao = (ComMailingDao) con.getBean("MailingDao");
        	ComBindingEntryDao bindingEntryDao = (ComBindingEntryDao) con.getBean("BindingEntryDao");
        	
        	ComMailing mailing = (ComMailing) mailingDao.getMailing(uid.getMailingID(), uid.getCompanyID());
        	int mailinglist = mailing.getMailinglistID();
        	BindingEntry entry = bindingEntryDao.get(uid.getCustomerID(), uid.getCompanyID(), mailinglist, 0);
        	entry.setUserStatus(UserStatus.UserOut.getStatusCode());
        	entry.setExitMailingID(mailing.getId());
        	
        	Locale loc = req.getLocale();
        	entry.setUserRemark(SafeString.getLocaleString("recipient.csa.optout.remark", loc));
        	bindingEntryDao.updateBinding(entry, uid.getCompanyID());

        } catch (Exception e) {
            logger.error("Exception in RDIR", e);
        }
    }
}
