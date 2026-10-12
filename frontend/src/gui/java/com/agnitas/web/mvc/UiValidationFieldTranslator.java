/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.web.mvc;

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
public class UiValidationFieldTranslator implements ValidationFieldTranslator {

    @Override
    public String translate(ValidationField field) {
        return switch (field) {
            case MailingValidationField mailing -> translate(mailing);
            case LinkValidationField link -> translate(link);
            case AttachmentValidationField attachment -> translate(attachment);
            case MailingImageValidationField mailingImage -> translate(mailingImage);
            case DynTagValidationField ignored -> "";
            case EmmFormValidationField emmForm -> translate(emmForm);
            case MailinglistValidationField mailinglist -> translate(mailinglist);
            case MediapoolValidationField ignored -> "";
            case BindingValidationField ignored -> "";
            case RefTableValidationField refTable -> translate(refTable);
            case RefTableColumnValidationField refTableColumn -> translate(refTableColumn);
            case TargetValidationField target -> translate(target);
            case TriggerValidationField trigger -> translate(trigger);
            case UserValidationField user -> translate(user);
        };
    }

    private static String translate(MailingValidationField mailing) {
        return switch (mailing) {
            case NAME -> "shortname";
            case DESCRIPTION -> "description";
            case MAILINGLIST -> "mailinglistId";
            case PLAN_DATE -> "planDate";
            case SUBJECT -> "emailMediatype.subject";
            case REPLY_FULLNAME -> "emailMediatype.replyFullname";
            case SENDER_FULLNAME -> "emailMediatype.fromFullname";
            case SENDER_EMAIL -> "emailMediatype.fromEmail";
            case REPLY_EMAIL -> "emailMediatype.replyEmail";
            case ENVELOPE_EMAIL -> "emailMediatype.envelopeEmail";
            case MAILING_TYPE -> "mailingType";
            case TARGETS -> "targetGroupIds";
            case ALTG -> "altgIds";
            case FOLLOWUP_FOR -> "followupFor";
            case FOLLOWUP_TYPE -> "followUpMailingType";
            case OPEN_TRIGGER, CLICK_TRIGGER, PARAMETERS -> "";
            case ARCHIVE -> "archiveId";
            case SMS_SENDER_ADDRESS -> "smsMediatype.fromAdr";
        };
    }

    private static String translate(LinkValidationField link) {
        return switch (link) {
            case URL -> "url";
            case TRACKING -> "usage";
            case RETARGETING -> "deepTracking";
            case TRIGGER -> "actionId";
        };
    }

    private static String translate(AttachmentValidationField attachment) {
        return switch (attachment) {
            case TARGET -> "targetId";
        };
    }

    private static String translate(MailingImageValidationField field) {
        return switch (field) {
            case MOBILE_VERSION_OF -> "";
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
            case NAME -> "shortname";
            case DESCRIPTION -> "description";
            case SENDER_EMAIL -> "senderEmail";
            case REPLY_EMAIL -> "replyEmail";
        };
    }

    private static String translate(EmmFormValidationField field) {
        return switch (field) {
            case NAME -> "formName";
            case START_TRIGGER -> "successSettings.startActionId";
            case END_TRIGGER -> "successSettings.finalActionId";
            case SUCCESS_SETTINGS, ERROR_SETTINGS -> "";
        };
    }

    private static String translate(UserValidationField field) {
        return switch (field) {
            case GENDER -> "gender";
            case USERNAME -> "username";
            case PASSWORD -> "password";
            case COMPANY_NAME -> "companyName";
            case FIRST_NAME -> "firstname";
            case LAST_NAME -> "fullname";
            case EMAIL -> "email";
            case STATISTICS_EMAIL -> "statEmail";
            case LANGUAGE -> "language";
            case TIMEZONE -> "adminTimezone";
            case GROUPS -> "groupIDs";
            case ALMLS -> "";
            case ALTGS -> "altgIds";
        };
    }

    private static String translate(TargetValidationField field) {
        return switch (field) {
            case NAME -> "shortname";
            case EQL -> "";
        };
    }

    private static String translate(RefTableValidationField field) {
        return switch (field) {
            case NAME -> "shortname";
            case KEY_COLUMN -> "keyColumn";
            case REFERENCE_CONDITION -> "refSource";
        };
    }

    private static String translate(RefTableColumnValidationField field) {
        return switch (field) {
            case NAME -> "newName";
            case TYPE -> "type";
            case LENGTH -> "length";
        };
    }
}
