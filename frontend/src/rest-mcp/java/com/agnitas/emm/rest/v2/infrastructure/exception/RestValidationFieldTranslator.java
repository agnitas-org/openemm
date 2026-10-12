/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.exception;

import com.agnitas.emm.core.validator.AttachmentValidationField;
import com.agnitas.emm.core.validator.DynTagValidationField;
import com.agnitas.emm.core.validator.EmmFormValidationField;
import com.agnitas.emm.core.validator.LinkValidationField;
import com.agnitas.emm.core.validator.MailingImageValidationField;
import com.agnitas.emm.core.validator.MailingValidationField;
import com.agnitas.emm.core.validator.MailinglistValidationField;
import com.agnitas.emm.core.validator.MediapoolValidationField;
import com.agnitas.emm.core.validator.BindingValidationField;
import com.agnitas.emm.core.validator.RefTableColumnValidationField;
import com.agnitas.emm.core.validator.RefTableValidationField;
import com.agnitas.emm.core.validator.TargetValidationField;
import com.agnitas.emm.core.validator.TriggerValidationField;
import com.agnitas.emm.core.validator.UserValidationField;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.emm.core.validator.ValidationFieldTranslator;
import org.springframework.stereotype.Component;

@Component
public class RestValidationFieldTranslator implements ValidationFieldTranslator {

    public String translate(ValidationField field) {
        return switch (field) {
            case MailingValidationField mailing -> translate(mailing);
            case LinkValidationField link -> translate(link);
            case AttachmentValidationField attachment -> translate(attachment);
            case MailingImageValidationField mailingImage -> translate(mailingImage);
            case DynTagValidationField dynTag -> translate(dynTag);
            case EmmFormValidationField emmForm -> translate(emmForm);
            case MailinglistValidationField mailinglist -> translate(mailinglist);
            case MediapoolValidationField mediapool -> translate(mediapool);
            case BindingValidationField binding -> translate(binding);
            case RefTableValidationField refTable -> translate(refTable);
            case RefTableColumnValidationField refTableColumn -> translate(refTableColumn);
            case TargetValidationField target -> translate(target);
            case TriggerValidationField trigger -> translate(trigger);
            case UserValidationField user -> translate(user);
        };
    }

    private static String translate(MailingValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case DESCRIPTION -> "description";
            case MAILINGLIST -> "mailinglistId";
            case PLAN_DATE -> null;
            case SUBJECT -> "subject";
            case REPLY_FULLNAME -> "replyName";
            case SENDER_FULLNAME -> "senderName";
            case SENDER_EMAIL -> "senderAddress";
            case REPLY_EMAIL -> "replyAddress";
            case ENVELOPE_EMAIL -> "envelopeEmail";
            case MAILING_TYPE -> "mailingType";
            case TARGETS, ALTG -> "targetExpression";
            case FOLLOWUP_FOR -> "followupForId";
            case FOLLOWUP_TYPE -> "followupType";
            case OPEN_TRIGGER -> "openTriggerId";
            case CLICK_TRIGGER -> "clickTriggerId";
            case PARAMETERS -> "parameters";
            case ARCHIVE -> "archiveId";
            case SMS_SENDER_ADDRESS -> "smsSenderAddress";
        };
    }

    private static String translate(LinkValidationField field) {
        return switch (field) {
            case URL -> "url";
            case TRACKING -> "tracking";
            case RETARGETING -> "retargeting";
            case TRIGGER -> "triggerId";
        };
    }

    private static String translate(AttachmentValidationField field) {
        return switch (field) {
            case TARGET -> "targetId";
        };
    }

    private static String translate(MailingImageValidationField field) {
        return switch (field) {
            case MOBILE_VERSION_OF -> "mobileVersionOf";
        };
    }

    private static String translate(DynTagValidationField field) {
        return switch (field) {
            case CONTENT -> "content";
            case TARGET -> "targetId";
        };
    }

    private static String translate(TriggerValidationField field) {
        return switch (field) {
            case TYPE -> "type";
            case NAME -> "name";
        };
    }

    private static String translate(MailinglistValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case DESCRIPTION -> "description";
            case SENDER_EMAIL, REPLY_EMAIL -> "";
        };
    }

    private static String translate(EmmFormValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case START_TRIGGER -> "startTriggerId";
            case END_TRIGGER -> "endTriggerId";
            case SUCCESS_SETTINGS -> "successSettings";
            case ERROR_SETTINGS -> "errorSettings";
        };
    }

    private static String translate(UserValidationField field) {
        return switch (field) {
            case GENDER -> "gender";
            case USERNAME -> "username";
            case PASSWORD -> "password";
            case COMPANY_NAME -> "company";
            case FIRST_NAME -> "firstName";
            case LAST_NAME -> "lastName";
            case EMAIL -> "email";
            case STATISTICS_EMAIL -> "statisticsEmail";
            case LANGUAGE -> "language";
            case TIMEZONE -> "timezone";
            case GROUPS -> "groupIds";
            case ALMLS -> "disabledMailingListIds";
            case ALTGS -> "accessLimitingTargetGroupIds";
        };
    }

    private static String translate(TargetValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case EQL -> "eql";
        };
    }

    private static String translate(RefTableValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case KEY_COLUMN -> "keyColumn";
            case REFERENCE_CONDITION -> "referenceCondition";
        };
    }

    private static String translate(RefTableColumnValidationField field) {
        return switch (field) {
            case NAME -> "name";
            case TYPE -> "type";
            case LENGTH -> "length";
        };
    }

    private static String translate(BindingValidationField field) {
        return switch (field) {
            case USER_STATUS -> "userStatus";
            case ENTRY_MAILING_ID -> "entryMailingId";
            case EXIT_MAILING_ID -> "exitMailingId";
        };
    }

    private static String translate(MediapoolValidationField field) {
        return switch (field) {
            case FILENAME, MIMETYPE -> "";
            case CONTENT -> "content";
            case TITLE -> "title";
            case CATEGORY -> "categoryId";
            case LINK -> "link";
        };
    }
}
