/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.exception.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.action.ActionNotFoundException;
import com.agnitas.emm.core.admin.AdminNotFoundException;
import com.agnitas.emm.core.binding.exception.BindingNotExistException;
import com.agnitas.emm.core.blacklist.exception.BlocklistEntryNotFoundException;
import com.agnitas.emm.core.commons.exception.EntityAlreadyExistsException;
import com.agnitas.emm.core.commons.exception.EntityNotFoundException;
import com.agnitas.emm.core.mailing.exception.MailingCopyException;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailingcontent.exception.MailingContentNotFoundException;
import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.core.recipient.exception.RecipientNotExistException;
import com.agnitas.emm.core.target.exception.TargetGroupNotFoundException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.workflow.exception.WorkflowNotFoundException;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpAuthenticationException;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpPermissionDeniedException;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpUserNotFoundException;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssException;
import com.agnitas.emm.mcp.v1.mailing.exception.MailingTemplateNotFoundException;
import com.agnitas.emm.mcp.v1.mailing.send.exception.MailingSendException;
import com.agnitas.emm.mcp.v1.mailinglist.exception.MailinglistDeletionException;
import com.agnitas.emm.mcp.v1.recipient.exception.RecipientDeletionException;
import com.agnitas.emm.mcp.v1.target.exception.TargetGroupDeletionException;
import com.agnitas.emm.mcp.v1.trigger.exception.TriggerExecutionException;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.exception.UiMessageException;
import com.agnitas.messages.Message;
import com.agnitas.web.perm.FieldChangeNotAllowedException;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class McpToolErrorMessageResolverImpl implements McpToolErrorMessageResolver {

    private static final Logger logger = LogManager.getLogger(McpToolErrorMessageResolverImpl.class);

    @Override
    public String resolve(Throwable ex) {
        if (ex instanceof tools.jackson.databind.exc.ValueInstantiationException && ex.getCause() != null) {
            // Thrown when a request record's compact constructor rejects invalid input during deserialization;
            // unwrap to resolve the real cause (e.g. ValidationException) instead of falling through to the generic "Internal error" branch below.
            return resolve(ex.getCause());
        }

        if (ex instanceof ConstraintViolationException) {
            logger.warn("Validation failed: {}", ex.getMessage());
            return "Request validation failed: " + ex.getMessage();
        }

        if (ex instanceof McpXssException e) {
            logger.warn("XSS check failed: {}", e.getMessage());
            String details = e.getErrors().entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue().stream()
                            .map(error -> error.toMessage().resolve(Locale.ENGLISH))
                            .sorted()
                            .collect(Collectors.joining(", ")))
                    .collect(Collectors.joining("; "));
            return "Unsafe HTML found: " + details;
        }

        if (ex instanceof ValidationException e) {
            logger.warn("Validation failed: {}", e.getResult());
            Map<String, Message> fieldErrors = new LinkedHashMap<>();
            e.getFieldErrors().forEach((field, msg) -> fieldErrors.put(field.name(), msg));
            return "Validation failed: " + formatMessages(e.getErrors(), fieldErrors);
        }

        if (ex instanceof UiMessageException e) {
            return formatMessages(e.getErrors(), e.getFieldsErrors());
        }

        if (ex instanceof MailinglistNotExistException e) {
            logger.warn("Mailinglist [{}] not found", e.getMailinglistID(), ex);
            return "Mailinglist not found";
        }

        if (ex instanceof MailinglistDeletionException e) {
            logger.warn("Mailinglist cannot be deleted: {}", e.getMessages());
            return formatMessages(e.getMessages());
        }

        if (ex instanceof MailingNotExistException e) {
            logger.warn("Mailing [{}] not found", e.getMailingID(), ex);
            return "Mailing not found";
        }

        if (ex instanceof MailingCopyException e) {
            if (e.getCause() instanceof MailingNotExistException cause) {
                return resolve(cause);
            }
            logger.warn("Mailing copy failed: {}", ex.getMessage(), ex);
            return ex.getMessage();
        }

        if (ex instanceof MailingTemplateNotFoundException e) {
            logger.warn("Mailing template [{}] not found", e.getTemplateId(), ex);
            return "Template not found";
        }

        if (ex instanceof MailingSendException e) {
            logger.warn("Mailing send failed: {}", e.getMessages());
            return formatMessages(e.getMessages());
        }

        if (ex instanceof MailingContentNotFoundException) {
            logger.warn("Mailing content not found", ex);
            return "Mailing content not found";
        }

        if (ex instanceof TargetGroupNotFoundException e) {
            logger.warn("Target group [{}] not found", e.getTargetGroupId(), ex);
            return "Target group not found";
        }

        if (ex instanceof TargetGroupDeletionException e) {
            logger.warn("Target group cannot be deleted: {}", e.getMessages());
            return formatMessages(e.getMessages());
        }

        if (ex instanceof RecipientNotExistException) {
            logger.warn("Recipient not found: {}", ex.getMessage());
            return "Recipient not found";
        }

        if (ex instanceof RecipientDeletionException e) {
            logger.warn("Recipient cannot be deleted: {}", e.getMessages());
            return formatMessages(e.getMessages());
        }

        if (ex instanceof BindingNotExistException) {
            logger.warn("Binding not found: {}", ex.getMessage());
            return "Binding not found";
        }

        if (ex instanceof BlocklistEntryNotFoundException) {
            logger.warn("Blocklist entry not found: {}", ex.getMessage());
            return "Blocklist entry not found";
        }

        if (ex instanceof FormNotFoundException) {
            logger.warn("Form not found: {}", ex.getMessage());
            return "Form not found";
        }

        if (ex instanceof EntityAlreadyExistsException) {
            logger.warn("Entity already exists: {}", ex.getMessage());
            return ex.getMessage();
        }

        if (ex instanceof ActionNotFoundException) {
            logger.warn("Trigger not found: {}", ex.getMessage());
            return "Trigger not found";
        }

        if (ex instanceof TriggerExecutionException) {
            logger.warn("Trigger execution failed: {}", ex.getMessage());
            return ex.getMessage();
        }

        if (ex instanceof WorkflowNotFoundException e) {
            logger.warn("Workflow [{}] not found", e.getWorkflowId(), ex);
            return "Workflow not found";
        }

        if (ex instanceof AdminNotFoundException) {
            logger.warn("User not found: {}", ex.getMessage());
            return "User not found";
        }

        if (ex instanceof McpAuthenticationException) {
            logger.warn("MCP request authentication failed: {}", ex.getMessage());
            return "Authentication failed: " + ex.getMessage();
        }

        if (ex instanceof McpUserNotFoundException) {
            logger.warn("MCP user resolution failed: {}", ex.getMessage());
            return "User not found: " + ex.getMessage();
        }

        if (ex instanceof McpPermissionDeniedException) {
            logger.warn("MCP permission check failed: {}", ex.getMessage());
            return "Permission denied: " + ex.getMessage();
        }

        if (ex instanceof TrackableLinkUnknownLinkIdException e) {
            logger.warn("Link [{}] not found", e.getLinkId(), ex);
            return "Link not found";
        }

        if (ex instanceof FieldChangeNotAllowedException e) {
            logger.warn("Change of field [{}] is not allowed", e.getField().name(), ex);
            return "Permission denied: changing field '" + e.getField().name() + "' is not allowed";
        }

        if (ex instanceof EntityNotFoundException) {
            logger.warn("Entity not found: {}", ex.getMessage());
            return ex.getMessage();
        }

        logger.error("Unexpected error during MCP tool execution", ex);
        return "Internal error while executing tool";
    }

    private String formatMessages(Collection<Message> errors) {
        return formatMessages(errors, Collections.emptyMap());
    }

    private String formatMessages(Collection<Message> errors, Map<String, Message> fieldErrors) {
        StringBuilder builder = new StringBuilder();
        fieldErrors.forEach((field, msg) ->
                builder.append(field).append(": ").append(msg.resolve(Locale.ENGLISH)).append("; "));
        errors.forEach(msg -> builder.append(msg.resolve(Locale.ENGLISH)).append("; "));

        String result = builder.toString().stripTrailing();
        return result.endsWith(";")
                ? result.substring(0, result.length() - 1).stripTrailing()
                : result;
    }

}
