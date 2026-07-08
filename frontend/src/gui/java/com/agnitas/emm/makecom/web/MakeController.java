/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.makecom.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.logon.service.LogonServiceException;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.restful.RestfulAuthentificationException;
import com.agnitas.service.MailingContentService;
import com.agnitas.util.DbColumnType;
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
@RequestMapping("/make")
public class MakeController {

    private static final Logger logger = LogManager.getLogger(MakeController.class);

    private static final Set<RecipientStandardField> DEFAULT_FIELDS = Set.of(
            RecipientStandardField.Email,
            RecipientStandardField.Firstname,
            RecipientStandardField.Lastname,
            RecipientStandardField.Gender,
            RecipientStandardField.Mailtype
    );

    private final LogonService logonService;
    private final RecipientFieldService fieldService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final MailingService mailingService;
    private final EmmActionService actionService;
    private final MailingContentService mailingContentService;

    public MakeController(
            LogonService logonService,
            RecipientFieldService fieldService,
            MailinglistApprovalService mailinglistApprovalService,
            MailingService mailingService,
            EmmActionService actionService,
            MailingContentService mailingContentService
    ) {
        this.logonService = logonService;
        this.fieldService = fieldService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.mailingService = mailingService;
        this.actionService = actionService;
        this.mailingContentService = mailingContentService;
    }

    @ExceptionHandler(RestfulAuthentificationException.class)
    public ResponseEntity<String> onRestfulAuthenticationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    @GetMapping("/basic-auth-test.action")
    public ResponseEntity<String> testAuth(HttpServletRequest req) {
        authenticateAdmin(req);
        return ResponseEntity.ok("Authentication tested");
    }

    @GetMapping("/recipient-fields.action")
    public List<Map<String, Object>> getEditableRecipientFields(HttpServletRequest req) {
        List<RecipientFieldDescription> fields = fieldService.getEditableFields(authenticateAdmin(req).getCompanyID());

        return fields.stream()
                .filter(f -> DEFAULT_FIELDS.stream().noneMatch(sf -> f.getColumnName().equals(sf.getColumnName())))
                .map(this::convertRecipientField)
                .toList();
    }

    @GetMapping("/mailinglists.action")
    public List<Map<String, Object>> getMailinglists(HttpServletRequest req) {
        return mailinglistApprovalService.getEnabledMailinglistsForAdmin(authenticateAdmin(req))
                .stream()
                .map(m -> Map.<String, Object>of("label", m.getShortname(), "value", m.getId()))
                .toList();
    }

    @GetMapping("/triggers.action")
    public List<Map<String, Object>> getTriggers(HttpServletRequest req) {
        int companyID = authenticateAdmin(req).getCompanyID();
        return actionService.getEmmNotFormActionsMap(companyID)
                .entrySet()
                .stream()
                .map(e -> Map.<String, Object>of("label", e.getValue(), "value", e.getKey()))
                .toList();
    }

    @GetMapping("/templates.action")
    public List<Map<String, Object>> getMailingTemplateNames(HttpServletRequest req) {
        Admin admin = authenticateAdmin(req);
        return mailingService.getTemplateNames(admin).entrySet()
                .stream()
                .map(e -> Map.<String, Object>of("label", e.getValue(), "value", e.getKey()))
                .toList();
    }

    @GetMapping("/content-targets/names.action")
    public List<Map<String, Object>> getTargetNamesForContent(HttpServletRequest req) {
        Admin admin = authenticateAdmin(req);
        return mailingContentService.getNamesOfAvailableTargetsForContent(admin)
                .entrySet()
                .stream()
                .map(e -> Map.<String, Object>of("label", e.getValue(), "value", e.getKey()))
                .toList();
    }

    private Admin authenticateAdmin(HttpServletRequest req) {
        try {
            String username = HttpUtils.getBasicAuthenticationUsername(req);
            String password = HttpUtils.getBasicAuthenticationPassword(req);
            Admin admin = logonService.getAdminByCredentials(username, password, req.getRemoteAddr());
            if (admin == null) {
                throw new RestfulAuthentificationException();
            }
            return admin;
        } catch (LogonServiceException e) {
            logger.error("Error during make.com basic auth: {}", e.getMessage(), e);
            throw new RestfulAuthentificationException();
        }
    }

    private Map<String, Object> convertRecipientField(RecipientFieldDescription field) {
        String type;

        switch (field.getSimpleDataType()) {
            case Numeric -> type = "integer";
            case Float -> type = "number";
            case Date, DateTime -> type = "date";
            default -> type = "text";
        }
        return Map.of(
                "key", field.getColumnName(),
                "label", field.getShortName(),
                "type", type,
                "time", field.getSimpleDataType().equals(DbColumnType.SimpleDataType.DateTime)
        );
    }
}
