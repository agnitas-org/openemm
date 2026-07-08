/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MailingComponentDto(

        int id,
        @NotBlank
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String description,
        @Pattern(
                regexp = "[012345678]",
                message = """
                        Allowed values: [\
                                Template(0), \
                                Image(1), \
                                Attachment(3), \
                                PersonalizedAttachment(4), \
                                HostedImage(5), \
                                PrecAAttachement(7), \
                                ThumbnailImage(8)
                        ]"""
        )
        String type,
        int target_id,
        String url,
        int urlId,
        String mimetype,
        String emm_block,
        String bin_block
) {
}
