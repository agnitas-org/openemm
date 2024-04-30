/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.converter;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.agnitas.beans.MediaTypeStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MediatypeToMediatypeFormConverter implements Converter<Mediatype, MediatypeForm> {

    @Override
    public MediatypeForm convert(Mediatype mediatype) {
        return convertMediatype(mediatype);
    }

    protected MediatypeForm convertMediatype(Mediatype mediatype) {
        if (mediatype.getMediaType() == MediaTypes.EMAIL) {
            return convertEmailMediatypeToForm((MediatypeEmailImpl) mediatype);
        }
        throw new IllegalArgumentException("Invalid mediatype" + mediatype.getMediaType());
    }

    protected EmailMediatypeForm convertEmailMediatypeToForm(MediatypeEmailImpl mediatype) {
        EmailMediatypeForm form = new EmailMediatypeForm();
        form.setSubject(mediatype.getSubject());
        form.setCharset(mediatype.getCharset());
        form.setLinefeed(mediatype.getLinefeed());
        form.setOnepixel(mediatype.getOnepixel());
        form.setPriority(mediatype.getPriority());
        form.setTextTemplate(mediatype.getTemplate());
        form.setFromEmail(mediatype.getFromEmail());
        form.setReplyEmail(getReplyEmailFromMediatype(mediatype));
        form.setMailFormat(mediatype.getMailFormat());
        form.setHtmlTemplate(mediatype.getHtmlTemplate());
        form.setFromFullname(mediatype.getFromFullname());
        form.setReplyFullname(getReplyToFullNameFromMediatype(mediatype));
        form.setEnvelopeEmail(getEnvelopeEmailFromMediatype(mediatype));
        form.setActive(mediatype.getStatus() == MediaTypeStatus.Active.getCode());
        trySetBccRecipients(mediatype, form);
        return form;
    }

    private String getEnvelopeEmailFromMediatype(MediatypeEmailImpl mediatype) {
        try {
            return new InternetAddress(mediatype.getEnvelopeEmail()).getAddress();
        } catch (AddressException e) {
            return mediatype.getEnvelopeEmail();
        }
    }

    private String getReplyToFullNameFromMediatype(MediatypeEmailImpl mediatype) {
        try {
            return new InternetAddress(mediatype.getReplyAdr()).getPersonal();
        } catch (Exception e) {
            return mediatype.getReplyFullname();
        }
    }

    private String getReplyEmailFromMediatype(MediatypeEmailImpl mediatype) {
        try {
            return new InternetAddress(mediatype.getReplyAdr()).getAddress();
        } catch (Exception e) {
            return mediatype.getReplyEmail();
        }
    }

    private void trySetBccRecipients(MediatypeEmailImpl mediatype, EmailMediatypeForm form) {
        try {
            form.setBccRecipients(mediatype.getBccRecipients());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid e-mail in address lists");
        }
    }
}
