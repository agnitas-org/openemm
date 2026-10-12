/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.service;

import java.text.ParseException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.settings.MailingSettingsForm;
import com.agnitas.emm.core.mailing.settings.MediatypePatch;
import com.agnitas.emm.core.mailing.settings.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.settings.mediatype.MediatypeForm;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.validator.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface MailingSettingsUiMapper {

    @SuppressWarnings("unused")
    default <T> PatchField<T> mapToPatchField(T value) {
        return PatchField.of(value);
    }

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) // todo fix
    @Mapping(target = "referenceContentSettings", expression = "java(PatchField.absent())")
    @Mapping(target = "frequencyCounterDisabled", expression = "java(PatchField.absent())")
    @Mapping(target = "clickTriggerId", expression = "java(PatchField.absent())")
    @Mapping(target = "openTriggerId", expression = "java(PatchField.absent())")
    @Mapping(target = "parameters", source = "form.params")
    @Mapping(target = "name", source = "form.shortname")
    @Mapping(target = "archived", source = "form.archived")
    @Mapping(target = "contentType", source = "form.mailingContentType")
    @Mapping(target = "targetExpression", source = "targetExpression")
    @Mapping(target = "mediatypes", expression = "java(mapToPatchField(mapMediatypes(form)))")
    @Mapping(target = "planDate", expression = "java(mapToPatchField(mapPlanDate(form.getPlanDate(), admin)))")
    PatchMailingSettingsCommand internalMapToPatchCommand(MailingSettingsForm form, int splitId, String targetExpression, @Context Admin admin);

    default PatchMailingSettingsCommand toPatchCommand(MailingSettingsForm form, int splitId, String targetExpression, @Context Admin admin) {
        return internalMapToPatchCommand(form, splitId, targetExpression, admin);
    }

    @SuppressWarnings("unused")
    default EnumMap<MediaTypes, MediatypePatch> mapMediatypes(MailingSettingsForm form) {
        EnumMap<MediaTypes, Mediatype> enumMap = new EnumMap<>(MediaTypes.class);
        for (Map.Entry<Integer, MediatypeForm> entry : form.getMediatypes().entrySet()) {
            MediaTypes key = MediaTypes.getMediaTypeForCode(entry.getKey());
            if (key != null) {
                enumMap.put(key, toMediatype(form, entry.getValue()));
            }
        }
        return null; // todo fix
    }

    default Mediatype toMediatype(MailingSettingsForm form, MediatypeForm mediatypeForm) {
        return switch (mediatypeForm) {
            case null -> null;
            case EmailMediatypeForm emailForm -> toEmailMediatype(form, emailForm);
            default -> throw new IllegalArgumentException("Unknown MediatypeForm type: " + mediatypeForm.getClass());
        };
    }

    @Mapping(target = "followupFor", ignore = true)
    @Mapping(target = "followupExcludeProxyOpeners", ignore = true)
    @Mapping(target = "followUpMethod", ignore = true)
    @Mapping(target = "doublechecking", ignore = true)
    @Mapping(target = "cleanupTestsBeforeDelivery", ignore = true)
    @Mapping(target = "encryptedSend", ignore = true)
    @Mapping(target = "skipempty", ignore = true)
    @Mapping(target = "mailingID", ignore = true)
    @Mapping(target = "approvalRequester", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvers", ignore = true)
    @Mapping(target = "companyID", ignore = true)
    @Mapping(target = "param", ignore = true)
    @Mapping(target = "status", expression = "java(getStatus(mediatypeForm))")
    @Mapping(target = "template", source = "mediatypeForm.textTemplate")
    MediatypeEmailImpl toEmailMediatype(MailingSettingsForm form, EmailMediatypeForm mediatypeForm);

    default int getStatus(MediatypeForm form) {
        return form.isActive() ? MediaTypeStatus.Active.getCode() : MediaTypeStatus.Inactive.getCode();
    }

    @SuppressWarnings("unused")
    default Instant mapPlanDate(String formPlanDate, Admin admin) {
        if (StringUtils.isBlank(formPlanDate)) {
            return null;
        }
        try {
            String planDateFromDto = StringUtils.trimToEmpty(formPlanDate);
            if (planDateFromDto.split(" ").length == 2) { // date and time is set
                Instant planDate = admin.getDateTimeFormat().parse(planDateFromDto).toInstant();
                if ("00:00".equals(planDateFromDto.split(" ")[1])) {
                    return planDate.plusSeconds(1); // add a second to indicate in db DATE column that the time was not left empty and user set 00:00 by intention
                }
                return planDate;
            }
            return admin.getDateFormat().parse(planDateFromDto).toInstant(); // date is set, time is not
        } catch (ParseException e) {
            throw new ValidationException("error.mailing.wrong.plan.date.format");
        }
    }
}
