/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.n8n.web;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.service.LogonServiceException;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.restful.RestfulAuthentificationException;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/n8n")
public class N8nController {

    private static final Logger logger = LogManager.getLogger(N8nController.class);

    private final LogonService logonService;
    private final RecipientFieldService fieldService;
    private final MailinglistApprovalService mailinglistApprovalService;

    public N8nController(LogonService logonService, RecipientFieldService fieldService, MailinglistApprovalService mailinglistApprovalService) {
        this.logonService = logonService;
        this.fieldService = fieldService;
        this.mailinglistApprovalService = mailinglistApprovalService;
    }

    @ExceptionHandler(RestfulAuthentificationException.class)
    public ResponseEntity<String> onRestfulAuthenticationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    @GetMapping("/basic-auth-test.action")
    public ResponseEntity<String> testAuth(HttpServletRequest req) {
        tryGetAdmin(req);
        return ResponseEntity.ok("Authentication tested");
    }

    private Admin tryGetAdmin(HttpServletRequest req) {
        try {
            String username = HttpUtils.getBasicAuthenticationUsername(req);
            String password = HttpUtils.getBasicAuthenticationPassword(req);
            Admin admin = logonService.getAdminByCredentials(username, password, req.getRemoteAddr());
            if (admin == null) {
                throw new RestfulAuthentificationException();
            }
            return admin;
        } catch (LogonServiceException e) {
            logger.error("Error during n8n basic auth: {}", e.getMessage(), e);
            throw new RestfulAuthentificationException();
        }
    }

    @GetMapping("/recipient-fields.action")
    public Map<String, String> getEditableRecipientFields(HttpServletRequest req) {
        return fieldService.getEditableFieldsMap(tryGetAdmin(req).getCompanyID());
    }

    @GetMapping("/mailinglists.action")
    public Map<Integer, String> getMailingLists(HttpServletRequest req) {
        return mailinglistApprovalService.getMailinglistsMap(tryGetAdmin(req));
    }
}
