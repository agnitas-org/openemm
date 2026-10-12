/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.settings;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;

import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dto.ReferenceContentSettings;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public record MailingSettingsDto(
        String name,
        String description,
        Instant planDate,
        int mailinglistId,
        String targetExpression,
        int archiveId,
        boolean archived,
        boolean needsTarget,
        boolean useDynamicTemplate,
        MailingType mailingType,
        MailingContentType contentType,
        List<MailingParameter> parameters,
        EnumMap<MediaTypes, Mediatype> mediatypes,
        int splitId,
        int openActionId,
        int clickActionId,
        boolean frequencyCounterDisabled, // extended scope
        ReferenceContentSettings referenceContentSettings // extended scope
) {
    public MailingSettingsDto {
        if (mailingType == null) {
            mailingType = MailingType.NORMAL;
        }
    }

    public MediatypeEmail getEmailMediatype() {
        if (!mediatypes.containsKey(MediaTypes.EMAIL)) {
            return new MediatypeEmailImpl();
        }
        return (MediatypeEmail) mediatypes.get(MediaTypes.EMAIL);
    }
}
