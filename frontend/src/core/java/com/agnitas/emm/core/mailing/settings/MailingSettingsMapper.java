/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.settings;

import java.util.EnumMap;
import java.util.Map;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailingSettingsMapper {

    @Mapping(target = "referenceContentSettings", ignore = true)
    @Mapping(target = "splitId", source = "splitID")
    @Mapping(target = "name", source = "shortname")
    @Mapping(target = "mailinglistId", source = "mailinglistID")
    @Mapping(target = "clickActionId", source = "clickActionID")
    @Mapping(target = "openActionId", source = "openActionID")
    @Mapping(target = "archiveId", source = "campaignID")
    @Mapping(target = "contentType", source = "mailingContentType")
    @Mapping(target = "archived", expression = "java(mailing.getArchived() == 1)")
    @Mapping(target = "mediatypes", expression = "java(mapMediatypes(mailing.getMediatypes()))")
    MailingSettingsDto toSettingsDto(Mailing mailing);

    @SuppressWarnings("unused")
    default EnumMap<MediaTypes, Mediatype> mapMediatypes(Map<Integer, Mediatype> mediatypes) {
        EnumMap<MediaTypes, Mediatype> result = new EnumMap<>(MediaTypes.class);
        for (Map.Entry<Integer, Mediatype> entry : mediatypes.entrySet()) {
            MediaTypes key = MediaTypes.getMediaTypeForCode(entry.getKey());
            if (key != null) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }
}
