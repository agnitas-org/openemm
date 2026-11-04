/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class MailingResources {

    private static final String AGN_TAGS_REFERENCE = """
            # agn tags in mailings

            agn tags are placeholders in mailing templates (htmlTemplate, textTemplate, smsTemplate, \
            rcsTemplate) and content blocks that EMM replaces when the mailing is generated for each recipient. \
            Always use them instead of hard-coded values: never paste an absolute image URL, a \
            bare image filename or a recipient's actual data into the content.

            ## agnDYN - content blocks (placeholders in templates)
            A template defines the layout; editable parts are marked with agnDYN tags. Each tag names a content \
            block whose content is managed separately with get_mailing_content / update_mailing_content (one \
            content per target group, so different recipients can get different content).
            - Standalone form: `[agnDYN name="headline" /]` is replaced by the content of block `headline`.
            - Enclosing form, rendered only if the block has content (otherwise the whole section is omitted); \
            `[agnDVALUE name="..."]` marks where the content goes:
              ```
              [agnDYN name="teaser"]<div class="teaser">[agnDVALUE name="teaser"]</div>[/agnDYN name="teaser"]
              ```
            - A block is created when a mailing with the new agnDYN reference in its template is saved; it \
            cannot be created via update_mailing_content alone.
            - To change text inside a block, update the block's content, not the template.

            ## agnIMAGE - images
            Generates the URL of a hosted image. Use it wherever the image URL is needed, usually the `src` \
            attribute of an `<img>` tag:
            - Mailing image (uploaded with create_mailing_image, listed by get_mailing_images; use its fileName): \
            `<img src="[agnIMAGE name="logo.png"]" alt="Logo">`
            - Mediapool image (create_mediapool_element / get_mediapool_elements): \
            `<img src="[agnIMAGE name="logo.png" source="mediapool"]" alt="Logo">`
            - To replace an image, upload the new one and change only the `name` in the existing agnIMAGE tag, \
            keeping the surrounding HTML. If a content block is placed inside a `src` attribute in the template \
            (e.g. `<img src="[agnDYN name="header-image" /]">`), the block's content is just the agnIMAGE tag.

            ## agnDB - recipient profile field values (personalization)
            Inserts the value of a profile field of the recipient the mailing is sent to: \
            `[agnDB column="<profile field name>"]`, e.g. `Dear [agnDB column="firstname"] [agnDB column="lastname"],`.
            - `column` is the profile field's column name (e.g. firstname, lastname, email or a custom field).
            - Optional `format` for date/number fields, e.g. `[agnDB column="birthday" format="dd.MM.yyyy"]`.
            - For content that depends on a field value (e.g. salutation by gender), prefer an agnDYN block with \
            different content per target group.

            ## More tags
            EMM supports many more agn tags (agnFORM for links to forms, agnUNSUBSCRIBE, agnDATE, agnTITLE for \
            salutations, etc.). See the full reference at \
            https://docs.agnitas.de/manual/chapter_17_appendix_a_agnitas_tags/
            """;

    @McpResource(
            uri = "docs://mailings/agn-tags",
            name = "mailing_agn_tags",
            description = "How to use agn tags in mailing templates and content: agnDYN (content block "
                    + "placeholders), agnIMAGE (image URLs for mailing and mediapool images) and agnDB "
                    + "(recipient profile field values), with examples.",
            mimeType = "text/markdown"
    )
    public String agnTagsReference() {
        return AGN_TAGS_REFERENCE;
    }

}
