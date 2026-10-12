/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.dto;

import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.tomcat.jakartaee.commons.lang3.StringUtils.isNotBlank;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.settings.EmailMediatypePatch;
import com.agnitas.emm.core.mailing.settings.MediatypePatch;
import com.agnitas.emm.core.mailing.validation.MailingConstraints;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.validator.MailingValidationField;

public record PatchMailingSettingsCommand(
        PatchField<String> name,
        PatchField<String> description,
        PatchField<MailingType> mailingType,
        PatchField<MailingContentType> contentType,
        PatchField<Instant> planDate,
        PatchField<Integer> mailinglistId,
        PatchField<String> targetExpression,
        PatchField<Integer> splitId,
        PatchField<Boolean> needsTarget,
        PatchField<Boolean> useDynamicTemplate,
        PatchField<Integer> openTriggerId,
        PatchField<Integer> clickTriggerId,
        PatchField<Integer> archiveId,
        PatchField<Boolean> archived,
        PatchField<List<MediaTypes>> activeMediatypesOrder,
        PatchField<Map<MediaTypes, MediatypePatch>> mediatypes,
        PatchField<List<MailingParameter>> parameters,
        PatchField<Boolean> frequencyCounterDisabled, // extended scope
        PatchField<ReferenceContentSettings> referenceContentSettings // extended scope
) {
    public PatchMailingSettingsCommand {
        name.check(n -> length(n) >= MailingConstraints.NAME_MIN_LENGTH, MailingValidationField.NAME);
        description.check(
                d -> length(d) <= MailingConstraints.DESCRIPTION_MAX_LENGTH,
                MailingValidationField.DESCRIPTION
        );
        mailinglistId.check(id -> id != null && id > 0, MailingValidationField.MAILINGLIST);
        mailingType.check(Objects::nonNull, MailingValidationField.MAILING_TYPE);

        openTriggerId.check(id -> id == null || id > 0, MailingValidationField.OPEN_TRIGGER);
        clickTriggerId.check(id -> id == null || id > 0, MailingValidationField.CLICK_TRIGGER);
        archiveId.check(id -> id == null || id >= 0, MailingValidationField.ARCHIVE);
        parameters.check(
                params -> params == null || params.stream().allMatch(p -> isNotBlank(p.getName())),
                MailingValidationField.PARAMETERS
        );

//        todo mediatypes validation
//        subject.check(s -> length(s) >= MailingConstraints.SUBJECT_MIN_LENGTH, "subject");
//        senderAddress.check(v -> v == null || AgnUtils.isEmailValid(v), "senderAddress");
//        replyAddress.check(v -> v == null || AgnUtils.isEmailValid(v), "replyAddress");
    }

    public Optional<EmailMediatypePatch> getEmailPatch() {
        if (!mediatypes.present()) {
            return Optional.empty();
        }
        MediatypePatch patch = mediatypes.value().get(MediaTypes.EMAIL);
        return patch instanceof EmailMediatypePatch emailPatch
                ? Optional.of(emailPatch)
                : Optional.empty();
    }
}
