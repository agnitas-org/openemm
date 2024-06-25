/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.fullview;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpServletResponse;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;

@Controller
@Anonymous
public class FullviewController {

    private static final Logger logger = LogManager.getLogger(FullviewController.class);

    private final MailingService mailingService;
    private final PreviewFactory previewFactory;
    private final ExtensibleUIDService extensibleUIDService;

    public FullviewController(MailingService mailingService, PreviewFactory previewFactory, ExtensibleUIDService extensibleUIDService) {
        this.mailingService = mailingService;
        this.previewFactory = previewFactory;
        this.extensibleUIDService = extensibleUIDService;
    }

    @GetMapping("/fullview.action")
    public void view(@RequestParam("agnUID") String uid, HttpServletResponse response) throws Exception {
        response.setContentType("text/html");

        ComExtensibleUID extensibleUID = decodeUidString(uid);
        int mailingID = extensibleUID.getMailingID();
        int customerID = extensibleUID.getCustomerID();

        if (mailingService.exists(mailingID, extensibleUID.getCompanyID())) {
            try (PrintWriter writer = response.getWriter()) {
                Preview preview = previewFactory.createPreview();
                Page page = preview.makePreview(mailingID, customerID, null, false, false, extensibleUID.getSendDate());
                if (page.getError() != null) {
                    logger.error("Error generating preview for fullview. mailingID: {}, customerID: {}, \n Preview error: {}",
                            mailingID, customerID, page.getError());
                }
                writer.write(page.getHTML());
            }
        }
    }

    private ComExtensibleUID decodeUidString(String uidString) {
        try {
            return extensibleUIDService.parse(uidString);
        } catch (DeprecatedUIDVersionException e) {
            logInfo(String.format("Deprecated UID version of UID: %s", uidString), e);
        } catch (UIDParseException e) {
            logInfo(String.format("Error parsing UID: %s", uidString), e);
        } catch (InvalidUIDException e) {
            logInfo(String.format("Invalid UID: %s", uidString), e);
        }

        return null;
    }

    private void logInfo(String message, Exception cause) {
        if (logger.isInfoEnabled()) {
            logger.info(message, cause);
        }
    }
}
