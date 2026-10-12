/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.config.json;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.bulk.ImportMode;
import com.agnitas.emm.rest.v2.mailing.dto.FollowupType;
import com.agnitas.emm.rest.v2.mailing.dto.MailFormat;
import com.agnitas.emm.rest.v2.mailing.dto.OnePixelPosition;
import com.agnitas.emm.rest.v2.mailing.dto.RestCharset;
import com.agnitas.emm.rest.v2.mailing.dto.RestMediatype;
import com.agnitas.emm.rest.v2.mailing.links.LinkRetargeting;
import com.agnitas.emm.rest.v2.mailing.links.LinkTracking;
import com.agnitas.emm.rest.v2.table.RefTableColumnType;
import com.agnitas.util.importvalues.Gender;

public class RestEnumAllowedValues {

    public static final String MEDIATYPES = allowedPairs(MediaTypes.values(), MediaTypes::getMediaCode);
    public static final String REST_MEDIATYPES = allowedValues(RestMediatype.class, RestMediatype::name);
    public static final String USER_STATUSES = allowedPairs(UserStatus.values(), UserStatus::getStatusCode);
    public static final String USER_TYPES = allowedPairs(UserType.values(), UserType::getTypeCode);
    public static final String MAILING_TYPES = allowedPairs(MailingType.values(), MailingType::getCode);
    public static final String MAILING_STATUSES = allowedValues(MailingStatus.class, MailingStatus::name);
    public static final String TRIGGER_TYPES = allowedPairs(EmmActionType.values(), EmmActionType::getActionTypeCode);
    public static final String REF_TABLE_COLUMN_TYPES = allowedValues(
            RefTableColumnType.class,
            RefTableColumnType::name
    );
    public static final String IMPORT_MODES = allowedValues(ImportMode.class, ImportMode::getValue);
    public static final String MAILING_CONTENT_TYPES = allowedValues(MailingContentType.class, Enum::name);
    public static final String GENDERS = allowedValues(Gender.class, g -> String.valueOf(g.getStorageValue()));
    public static final String CHARSETS = allowedPairs(
            RestCharset.values(),
            restCharset -> restCharset.getCoreVal().getCharsetName()
    );
    public static final String FOLLOWUP_TYPES = allowedValues(FollowupType.class, Enum::name);
    public static final String MAIL_FORMATS = allowedValues(MailFormat.class, Enum::name);
    public static final String LINK_TRACKINGS = allowedValues(LinkTracking.class, Enum::name);
    public static final String LINK_RETARGETINGS = allowedValues(LinkRetargeting.class, Enum::name);
    public static final String ATTACHMENT_TYPES = allowedValues(AttachmentType.class, Enum::name);
    public static final String ONE_PIXEL_POSITIONS = allowedPairs(
            OnePixelPosition.values(),
            n -> n.toString().toLowerCase()
    );

    private static final Map<Class<? extends Enum<?>>, String> ALLOWED_VALUES = Map.ofEntries(
            Map.entry(MediaTypes.class, MEDIATYPES),
            Map.entry(RestMediatype.class, REST_MEDIATYPES),
            Map.entry(UserStatus.class, USER_STATUSES),
            Map.entry(UserType.class, USER_TYPES),
            Map.entry(MailingType.class, MAILING_TYPES),
            Map.entry(MailingStatus.class, MAILING_STATUSES),
            Map.entry(EmmActionType.class, TRIGGER_TYPES),
            Map.entry(RefTableColumnType.class, REF_TABLE_COLUMN_TYPES),
            Map.entry(ImportMode.class, IMPORT_MODES),
            Map.entry(MailingContentType.class, MAILING_CONTENT_TYPES),
            Map.entry(Gender.class, GENDERS),
            Map.entry(RestCharset.class, CHARSETS),
            Map.entry(FollowupType.class, FOLLOWUP_TYPES),
            Map.entry(MailFormat.class, MAIL_FORMATS),
            Map.entry(LinkTracking.class, LINK_TRACKINGS),
            Map.entry(LinkRetargeting.class, LINK_RETARGETINGS),
            Map.entry(AttachmentType.class, ATTACHMENT_TYPES),
            Map.entry(OnePixelPosition.class, ONE_PIXEL_POSITIONS)
    );

    private RestEnumAllowedValues() {
    }

    private static String allowedValues(String values) {
        return "Allowed values: [%s]".formatted(values);
    }

    private static <E extends Enum<E>> String allowedPairs(E[] values, Function<E, ?> codeExtractor) {
        return allowedValues(Arrays.stream(values)
                .map(it -> "%s(%s)".formatted(it.name(), codeExtractor.apply(it)))
                .collect(Collectors.joining(", ")));
    }

    private static <E extends Enum<E>> String allowedValues(Class<E> enumClass, Function<E, String> mapper) {
        return allowedValues(Arrays.stream(enumClass.getEnumConstants())
                .map(mapper)
                .collect(Collectors.joining(", ")));
    }

    public static String getAllowedValue(Class<? extends Enum<?>> enumClass) {
        return ALLOWED_VALUES.get(enumClass);
    }
}
