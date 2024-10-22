/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.component;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.action.bean.ActionSendMailingToUserStatus;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.messages.I18nString;
import org.agnitas.actions.EmmAction;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.agnitas.util.UserActivityUtil.addChangedFieldLog;
import static org.agnitas.util.UserActivityUtil.addSetFieldLog;

@Component
public class EmmActionChangesCollector {

    private static final Logger LOGGER = LogManager.getLogger(EmmActionChangesCollector.class);

    public String collectChanges(EmmAction newAction, EmmAction oldAction) {
        return StringUtils.join(getChangesDescriptions(newAction, oldAction), '\n');
    }

    private List<String> getChangesDescriptions(EmmAction newAction, EmmAction oldAction) {
        List<String> descriptions = new ArrayList<>();

        if (oldAction == null) {
            descriptions.add(String.format("Set type to %s.", getTypeAsString(newAction.getType())));
            descriptions.add(String.format("Made %s.", newAction.getIsActive() ? "active" : "inactive"));

            for (AbstractActionOperationParameters operation : newAction.getActionOperations()) {
                String operationDescription = getOperationChanges(operation, null);
                // Missing description means "nothing changed".
                if (StringUtils.isNotBlank(operationDescription)) {
                    descriptions.add(operationDescription);
                }
            }
        } else {
            if (!StringUtils.equals(newAction.getShortname(), oldAction.getShortname())) {
                descriptions.add(String.format("Renamed %s to %s.", oldAction.getShortname(), newAction.getShortname()));
            }

            if (newAction.getType() != oldAction.getType()) {
                descriptions.add(String.format("Changed type from %s to %s.", getTypeAsString(oldAction.getType()), getTypeAsString(newAction.getType())));
            }

            if (newAction.getIsActive() != oldAction.getIsActive()) {
                descriptions.add(String.format("Made %s.", newAction.getIsActive() ? "active" : "inactive"));
            }

            List<AbstractActionOperationParameters> newOperations = newAction.getActionOperations();
            List<AbstractActionOperationParameters> oldOperations = oldAction.getActionOperations();

            // Order by id to join both lists by id below.
            newOperations.sort(Comparator.comparingInt(AbstractActionOperationParameters::getId));
            oldOperations.sort(Comparator.comparingInt(AbstractActionOperationParameters::getId));

            int newIndex = 0;
            int oldIndex = 0;

            while (newIndex < newOperations.size() || oldIndex < oldOperations.size()) {
                AbstractActionOperationParameters newOperation = null;
                AbstractActionOperationParameters oldOperation = null;

                if (newIndex < newOperations.size()) {
                    newOperation = newOperations.get(newIndex++);
                }

                if (oldIndex < oldOperations.size()) {
                    oldOperation = oldOperations.get(oldIndex++);

                    if (newOperation != null) {
                        // Join if ids match or roll back a side having greater id otherwise.
                        if (newOperation.getId() < oldOperation.getId()) {
                            oldOperation = null;
                            oldIndex--;
                        } else if (newOperation.getId() > oldOperation.getId()) {
                            newOperation = null;
                            newIndex--;
                        }
                    }
                }

                String operationDescription = getOperationChanges(newOperation, oldOperation);
                if (StringUtils.isNotBlank(operationDescription)) {
                    // Missing description means "nothing changed".
                    descriptions.add(operationDescription);
                }
            }
        }

        return descriptions;
    }

    private String getOperationChanges(AbstractActionOperationParameters newOperation, AbstractActionOperationParameters oldOperation) {
        if (newOperation == null && oldOperation == null) {
            return "";
        }

        if (newOperation == null) {
            return String.format("Removed %s.", getModuleName(oldOperation));
        }

        String description = "";

        if (newOperation instanceof ActionOperationSendMailingParameters) {
            description = getSendActionBasedModuleChanges((ActionOperationSendMailingParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationActivateDoubleOptInParameters) {
            description = getDoubleOptInConfirmationModuleChanges((ActionOperationActivateDoubleOptInParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationContentViewParameters) {
            description = getContentViewModuleChanges((ActionOperationContentViewParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationExecuteScriptParameters) {
            description = getExecuteScriptModuleChanges((ActionOperationExecuteScriptParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationGetArchiveListParameters) {
            description = getArchiveListModuleChanges((ActionOperationGetArchiveListParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationGetArchiveMailingParameters) {
            description = getShowArchivedMailingModuleChanges((ActionOperationGetArchiveMailingParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationGetCustomerParameters) {
            description = getLoadRecipientDataModuleChanges((ActionOperationGetCustomerParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationIdentifyCustomerParameters) {
            description = getIdentifyCustomerModuleChanges((ActionOperationIdentifyCustomerParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationServiceMailParameters) {
            try {
                description = getServiceEmailModuleChanges((ActionOperationServiceMailParameters) newOperation, oldOperation);
            } catch (Exception e) {
                LOGGER.error("Error occurred while getting service email module changes", e);
            }
        } else if (newOperation instanceof ActionOperationSubscribeCustomerParameters) {
            description = getSubscribeCustomerModuleChanges((ActionOperationSubscribeCustomerParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationUnsubscribeCustomerParameters) {
            description = getUnsubscribeCustomerModuleChanges((ActionOperationUnsubscribeCustomerParameters) newOperation, oldOperation);
        } else if (newOperation instanceof ActionOperationUpdateCustomerParameters) {
            description = getUpdateCustomerModuleChanges((ActionOperationUpdateCustomerParameters) newOperation, oldOperation);
        }

        if (StringUtils.isBlank(description)) {
            return oldOperation == null ? String.format("Added %s", getModuleName(newOperation)) : "";
        }

        return String.format(
                "%s %s (%s)",
                oldOperation == null ? "Added " : "Changed ",
                getModuleName(newOperation),
                description
        );
    }

    private String getUpdateCustomerModuleChanges(ActionOperationUpdateCustomerParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationUpdateCustomerParameters) {
            ActionOperationUpdateCustomerParameters oldOperation = (ActionOperationUpdateCustomerParameters) oldModule;

            changes.append(addChangedFieldLog("use tracking point", module.isUseTrack(), oldOperation.isUseTrack()));

            if (module.isUseTrack() && module.getTrackingPointId() != oldOperation.getTrackingPointId()) {
                changes.append(addChangedFieldLog("tracking point", module.getTrackingPointId(), oldOperation.getTrackingPointId()));
            }

            changes.append(addChangedFieldLog("column name", module.getColumnName(), oldOperation.getColumnName()));
            changes.append(addChangedFieldLog("update type", getUpdateCustomerTypeStr(module.getUpdateType()), getUpdateCustomerTypeStr(oldOperation.getUpdateType())));

            if (!module.isUseTrack()) {
                changes.append(addChangedFieldLog("value", module.getUpdateValue(), oldOperation.getUpdateValue()));
            }
        } else {
            changes.append(addSetFieldLog("use tracking point", module.isUseTrack()));

            if (module.isUseTrack()) {
                changes.append(addSetFieldLog("tracking point", module.getTrackingPointId()));
            }

            changes.append(addSetFieldLog("column name", module.getColumnName()));
            changes.append(addSetFieldLog("update type", getUpdateCustomerTypeStr(module.getUpdateType())));

            if (!module.isUseTrack()) {
                changes.append(addSetFieldLog("value", module.getUpdateValue()));
            }
        }

        return changes.toString();
    }

    private String getUnsubscribeCustomerModuleChanges(ActionOperationUnsubscribeCustomerParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationUnsubscribeCustomerParameters) {
            ActionOperationUnsubscribeCustomerParameters oldOperation = (ActionOperationUnsubscribeCustomerParameters) oldModule;

            changes.append(addChangedFieldLog("unsubscribe from mailiglists", module.isAdditionalMailinglists(), oldOperation.isAdditionalMailinglists()));

            if (module.isAdditionalMailinglists()) {
                changes.append(addChangedFieldLog("all mailinglists", module.isAllMailinglistsSelected(), oldOperation.isAllMailinglistsSelected()));

                if (!module.isAllMailinglistsSelected() && !CollectionUtils.isEqualCollection(module.getMailinglistIds(), oldOperation.getMailinglistIds())) {
                    changes.append(addChangedFieldLog("mailinglists", joinIds(module.getMailinglistIds()), joinIds(oldOperation.getMailinglistIds())));
                }
            }
        } else {
            changes.append(addSetFieldLog("unsubscribe from mailiglists", module.isAdditionalMailinglists()));

            if (module.isAdditionalMailinglists()) {
                changes.append(addSetFieldLog("all mailinglists", module.isAllMailinglistsSelected()));
                if (!module.isAllMailinglistsSelected()) {
                    changes.append(addSetFieldLog("mailinglists", joinIds(module.getMailinglistIds())));
                }
            }
        }

        return changes.toString();
    }

    private String joinIds(Collection<Integer> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private String getSubscribeCustomerModuleChanges(ActionOperationSubscribeCustomerParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationSubscribeCustomerParameters) {
            ActionOperationSubscribeCustomerParameters oldOperation = (ActionOperationSubscribeCustomerParameters) oldModule;

            changes.append(addChangedFieldLog("use double opt-in", module.isDoubleOptIn(), oldOperation.isDoubleOptIn()))
                    .append(addChangedFieldLog("duplicates check", module.isDoubleCheck(), oldOperation.isDoubleCheck()))
                    .append(addChangedFieldLog("key column", module.getKeyColumn(), oldOperation.getKeyColumn()));
        } else {
            changes.append(addSetFieldLog("use double opt-in", module.isDoubleOptIn()))
                    .append(addSetFieldLog("duplicates check", module.isDoubleCheck()))
                    .append(addSetFieldLog("key column", module.getKeyColumn()));
        }

        return changes.toString();
    }

    private String getServiceEmailModuleChanges(ActionOperationServiceMailParameters module, AbstractActionOperationParameters oldModule) throws Exception {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationServiceMailParameters) {
            ActionOperationServiceMailParameters oldOperation = (ActionOperationServiceMailParameters) oldModule;

            changes.append(addChangedFieldLog("'To' address", module.getToAddress(), oldOperation.getToAddress()))
                    .append(addChangedFieldLog("'From' address", module.getFromAddress(), oldOperation.getFromAddress()))
                    .append(addChangedFieldLog("reply address", module.getReplyAddress(), oldOperation.getReplyAddress()))
                    .append(addChangedFieldLog("subject", module.getSubjectLine(), oldOperation.getSubjectLine()))
                    .append(addChangedFieldLog("text content", module.getTextMail(), oldOperation.getTextMail()))
                    .append(addChangedFieldLog("html content", module.getHtmlMail(), oldOperation.getHtmlMail()))
                    .append(addChangedFieldLog("format", getMailTypeStr(module.getMailtype()), getMailTypeStr(oldOperation.getMailtype())));
        } else {
            changes.append(addSetFieldLog("'To' address", module.getToAddress()))
                    .append(addSetFieldLog("'From' address", module.getFromAddress()))
                    .append(addSetFieldLog("reply address", module.getReplyAddress()))
                    .append(addSetFieldLog("subject", module.getSubjectLine()))
                    .append(addSetFieldLog("text content", module.getTextMail()))
                    .append(addSetFieldLog("html content", module.getHtmlMail()))
                    .append(addSetFieldLog("format", getMailTypeStr(module.getMailtype())));
        }

        return changes.toString();
    }

    private String getIdentifyCustomerModuleChanges(ActionOperationIdentifyCustomerParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationIdentifyCustomerParameters) {
            ActionOperationIdentifyCustomerParameters oldOperation = (ActionOperationIdentifyCustomerParameters) oldModule;
            changes.append(addChangedFieldLog("username column", module.getKeyColumn(), oldOperation.getKeyColumn()))
                    .append(addChangedFieldLog("password column", module.getPassColumn(), oldOperation.getPassColumn()));
        } else {
            changes.append(addSetFieldLog("username column", module.getKeyColumn()))
                    .append(addSetFieldLog("password column", module.getPassColumn()));
        }

        return changes.toString();
    }

    private String getSendActionBasedModuleChanges(ActionOperationSendMailingParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationSendMailingParameters) {
            ActionOperationSendMailingParameters oldOperation = (ActionOperationSendMailingParameters) oldModule;

            changes.append(addChangedFieldLog("bcc emails", module.getBcc(), oldOperation.getBcc()))
                    .append(addChangedFieldLog("mailingID", module.getMailingID(), oldOperation.getMailingID()))
                    .append(addChangedFieldLog("delay", module.getDelayMinutes(), oldOperation.getDelayMinutes()))
                    .append(addChangedFieldLog("user status", getActionUserStatusStr(module.getUserStatusesOption()), getActionUserStatusStr(oldOperation.getUserStatusesOption())));
        } else {
            changes.append(addSetFieldLog("bcc emails", module.getBcc()))
                    .append(addSetFieldLog("mailingID", module.getMailingID()))
                    .append(addSetFieldLog("delay", module.getDelayMinutes()))
                    .append(addSetFieldLog("user status", getActionUserStatusStr(module.getUserStatusesOption())));
        }

        return changes.toString();
    }

    private String getShowArchivedMailingModuleChanges(ActionOperationGetArchiveMailingParameters module, AbstractActionOperationParameters oldModule) {
        if (oldModule instanceof ActionOperationGetArchiveMailingParameters) {
            return addChangedFieldLog(
                    "expire date",
                    module.getExpireDate(),
                    ((ActionOperationGetArchiveMailingParameters) oldModule).getExpireDate()
            );
        }

        return addSetFieldLog("expire date", module.getExpireDate());
    }

    private String getDoubleOptInConfirmationModuleChanges(ActionOperationActivateDoubleOptInParameters module, AbstractActionOperationParameters oldModule) {
        StringBuilder changes = new StringBuilder();

        if (oldModule instanceof ActionOperationActivateDoubleOptInParameters) {
            ActionOperationActivateDoubleOptInParameters oldOperation = (ActionOperationActivateDoubleOptInParameters) oldModule;

            changes.append(addChangedFieldLog("media type", getMediaTypeStr(module.getMediaTypeCode()), getMediaTypeStr(oldOperation.getMediaTypeCode())))
                    .append(addChangedFieldLog("for all mailinglists", module.isForAllLists(), oldOperation.isForAllLists()));
        } else {
            changes.append(addSetFieldLog("media type", getMediaTypeStr(module.getMediaTypeCode())))
                    .append(addSetFieldLog("for all mailinglists", module.isForAllLists()));
        }

        return changes.toString();
    }

    private String getContentViewModuleChanges(ActionOperationContentViewParameters module, AbstractActionOperationParameters oldModule) {
        if (oldModule instanceof ActionOperationContentViewParameters) {
            return addChangedFieldLog(
                    "name of content block",
                    module.getTagName(),
                    ((ActionOperationContentViewParameters) oldModule).getTagName()
            );
        }

        return addSetFieldLog("name of content block", module.getTagName());
    }

    private String getLoadRecipientDataModuleChanges(ActionOperationGetCustomerParameters module, AbstractActionOperationParameters oldModule) {
        if (oldModule instanceof ActionOperationGetCustomerParameters) {
            ActionOperationGetCustomerParameters oldOperation = (ActionOperationGetCustomerParameters) oldModule;

            if (module.isLoadAlways() != oldOperation.isLoadAlways()) {
                return getBooleanDescription("load always", module.isLoadAlways());
            }
            return "";
        }

        return getBooleanDescription("load always", module.isLoadAlways());
    }

    private String getExecuteScriptModuleChanges(ActionOperationExecuteScriptParameters module, AbstractActionOperationParameters oldModule) {
        if (oldModule instanceof ActionOperationExecuteScriptParameters) {
            ActionOperationExecuteScriptParameters oldOperation = (ActionOperationExecuteScriptParameters) oldModule;
            return addChangedFieldLog("script", truncateStr(module.getScript()), truncateStr(oldOperation.getScript()));
        }

        return addSetFieldLog("script", truncateStr(module.getScript()));
    }

    private String getArchiveListModuleChanges(ActionOperationGetArchiveListParameters module, AbstractActionOperationParameters oldModule) {
        if (oldModule instanceof ActionOperationGetArchiveListParameters) {
            ActionOperationGetArchiveListParameters oldOperation = (ActionOperationGetArchiveListParameters) oldModule;
            return addChangedFieldLog("archiveID", module.getCampaignID(), oldOperation.getCampaignID());
        }

        return addSetFieldLog("archiveID", module.getCampaignID());
    }

    private String getModuleName(AbstractActionOperationParameters module) {
        return String.format("%s module #%d", module.getOperationType().getName(), module.getId());
    }

    private String getTypeAsString(int type) {
        switch (type) {
            case EmmAction.TYPE_LINK:
                return "Links";
            case EmmAction.TYPE_FORM:
                return "Forms";
            case EmmAction.TYPE_ALL:
                return "Links and forms";
            default:
                return "Unknown";
        }
    }

    private String getBooleanDescription(String property, boolean value) {
        return String.format("Made '%s' %s", property, value ? "active" : "inactive");
    }

    private String getUpdateCustomerTypeStr(int code) {
        if (code == ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY) {
            return "INCREMENT";
        }

        if (code == ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY) {
            return "DECREMENT";
        }

        if (code == ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE) {
            return "SET VALUE";
        }

        return "";
    }

    private String getMailTypeStr(int code) throws Exception {
        return translate(MailType.getFromInt(code).getMessageKey());
    }

    private String getMediaTypeStr(int code) {
        return translate("mailing.MediaType." + code);
    }

    private String getActionUserStatusStr(int code) {
        return translate(IntEnum.fromId(ActionSendMailingToUserStatus.class, code).getMessageKey());
    }

    private String truncateStr(String str) {
        return StringUtils.abbreviate(str, 25);
    }

    private String translate(String messageKey) {
        return I18nString.getLocaleString(messageKey, Locale.ENGLISH);
    }
}
