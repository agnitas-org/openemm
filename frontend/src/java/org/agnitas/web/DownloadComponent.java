/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.Map;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComRecipientDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class DownloadComponent extends HttpServlet {

    /**
     * Logger.
     */
    private static final transient Logger logger = Logger.getLogger(ShowComponent.class);

    private static final long serialVersionUID = 663420929616439014L;

    private UserActivityLogService userActivityLogService;
    private MailingComponentDao mailingComponentDao;
    private PreviewFactory previewFactory;
    private ComRecipientDao recipientDao;

    @Override
    public void init() throws ServletException {
        super.init();

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        userActivityLogService = ctx.getBean("UserActivityLogService", UserActivityLogService.class);
        mailingComponentDao = ctx.getBean("MailingComponentDao", MailingComponentDao.class);
        previewFactory = ctx.getBean("PreviewFactory", PreviewFactory.class);
        recipientDao = ctx.getBean("RecipientDao", ComRecipientDao.class);
    }

    /**
     * Download mailing components or attachments.
     * Write component into response.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
        long len = 0;
        int compId = 0;

        if (!AgnUtils.isUserLoggedIn(req)) {
            return;
        }

        try {
            compId = Integer.parseInt(req.getParameter("compID"));
        } catch (Exception e) {
            logger.warn("Error converting " + (req.getParameter("compID") != null ? "'" + req.getParameter("compID") + "'" : req.getParameter("compID")) + " to integer", e);
            return;
        }

        if (compId == 0) {
            return;
        }

        int customerID = NumberUtils.toInt(req.getParameter("customerID"), 0);

        MailingComponent comp = mailingComponentDao.getMailingComponent(compId, AgnUtils.getCompanyID(req));

        if (comp != null) {
            HttpUtils.setDownloadFilenameHeader(response, comp.getComponentName());
            response.setContentType(comp.getMimeType());
            try (ServletOutputStream out = response.getOutputStream()) {
                Preview preview = previewFactory.createPreview();

                byte[] attachment = null;
                int mailingID = comp.getMailingID();

                if (comp.getType() == MailingComponentType.PersonalizedAttachment) {
                    int targetID = 0;
                    if (customerID == 0) {
                        targetID = comp.getTargetID();
                    }

                    if (customerID == 0 && targetID == 0) { // no customerID and targetID are available, take the 1st available test recipient
                        Map<Integer, String> recipientList = recipientDao.getAdminAndTestRecipientsDescription(comp.getCompanyID(), mailingID);
                        customerID = recipientList.keySet().iterator().next();
                    }

                    Page page = preview.makePreview(mailingID, customerID, targetID);
                    attachment = page.getAttachment(comp.getComponentName());

                } else {
                    attachment = comp.getBinaryBlock();
                }

                len = ArrayUtils.getLength(attachment);
                response.setContentLength((int) len);
                out.write(attachment);
                out.flush();
                userActivityLogService.writeUserActivityLog(AgnUtils.getAdmin(req), "component download",
                        String.format("downloaded component (%d) for mailing (%d)", compId, mailingID));
            }
        }
    }
}
