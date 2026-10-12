/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.trackablelinks.dto;

import java.util.List;

import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.validator.LinkValidationField;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public record PatchLinkCommand(
        PatchField<String> url,
        PatchField<String> description,
        PatchField<Integer> tracking,
        PatchField<Integer> retargeting,
        PatchField<Integer> triggerId,

        @JsonProperty("static")
        PatchField<Boolean> staticLink,

        PatchField<Boolean> administrative,

        PatchField<List<LinkProperty>> extensions
) {
    public PatchLinkCommand {
        if (staticLink.present() && staticLink.value() == null) {
            staticLink = PatchField.of(false);
        }
        if (administrative.present() && administrative.value() == null) {
            administrative = PatchField.of(false);
        }
        if (triggerId.present() && triggerId.value() == null) {
            triggerId = PatchField.of(0);
        }
        url.check(StringUtils::isNotBlank, LinkValidationField.URL);
        tracking.check(v -> v != null && v >= 0, LinkValidationField.TRACKING);
        retargeting.check(v -> v != null && v >= 0, LinkValidationField.RETARGETING);
        triggerId.check(v -> v != null && v >= 0, LinkValidationField.TRIGGER);
    }
}
