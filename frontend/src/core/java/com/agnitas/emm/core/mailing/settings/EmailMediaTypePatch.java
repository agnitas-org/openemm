/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.settings;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.MailType;

public record EmailMediaTypePatch(
        PatchField<String> subject,
        PatchField<String> preHeader,
        PatchField<String> senderAddress,
        PatchField<String> replyAddress,
        PatchField<String> senderName,
        PatchField<String> replyName,
        PatchField<Charset> charset,
        PatchField<MailType> format,
        PatchField<String> onePixel,
        PatchField<String> envelopeEmail,
        PatchField<String> htmlTemplate,
        PatchField<String> textTemplate,
        PatchField<FollowUpType> followupType,
        PatchField<Integer> followupForId
) implements MediaTypePatch {

    public EmailMediaTypePatch {
        if (charset.present() && charset.value() == null) {
            charset = PatchField.of(Charset.UTF_8);
        }
        if (format.present() && format.value() == null) {
            format = PatchField.of(MailType.HTML_OFFLINE);
        }
        if (onePixel.present() && onePixel.value() == null) {
            onePixel = PatchField.of(MediatypeEmail.ONEPIXEL_TOP);
        }
    }

    @Override
    public void applyTo(Mediatype mediaType) {
        if (!(mediaType instanceof MediatypeEmail email)) {
            throw new IllegalStateException("Expected MediatypeEmail but got " + mediaType.getClass());
        }
        subject().ifPresent(email::setSubject);
        preHeader().ifPresent(email::setPreHeader);
        senderAddress().ifPresent(email::setFromEmail);
        replyAddress().ifPresent(email::setReplyEmail);
        senderName().ifPresent(email::setFromFullname);
        replyName().ifPresent(email::setReplyFullname);
        charset().ifPresent(c -> email.setCharset(c.getCharsetName()));
        format().ifPresent(email::setMailFormat);
        onePixel().ifPresent(email::setOnepixel);
        envelopeEmail().ifPresent(email::setEnvelopeEmail);
        htmlTemplate().ifPresent(email::setHtmlTemplate);
        textTemplate().ifPresent(email::setTemplate);
        followupType().ifPresent(t -> email.setFollowUpMethod(t == null ? null : t.getKey()));
        followupForId().ifPresent(id -> email.setFollowupFor(id == null ? null : String.valueOf(id)));
    }

    @Override
    public boolean hasTemplateChange() {
        return textTemplate.present() || htmlTemplate.present();
    }

    public EmailMediaTypePatch withHtmlTemplate(String value) {
        return new EmailMediaTypePatch(
                subject, preHeader, senderAddress, replyAddress, senderName,
                replyName, charset, format, onePixel, envelopeEmail,
                PatchField.of(value), textTemplate, followupType, followupForId
        );
    }

    @Override
    public void applyAlwaysAllowedFieldsTo(Mediatype mediaType) {
        if (!(mediaType instanceof MediatypeEmail email)) {
            throw new IllegalStateException("Expected MediatypeEmail but got " + mediaType.getClass());
        }
        htmlTemplate().ifPresent(email::setHtmlTemplate);
        textTemplate().ifPresent(email::setTemplate);
    }
}
