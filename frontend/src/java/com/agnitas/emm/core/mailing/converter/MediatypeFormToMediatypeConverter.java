/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.converter;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.emm.core.mediatypes.factory.MediatypeFactoryImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MediatypeFormToMediatypeConverter implements Converter<MediatypeForm, Mediatype> {

    @Override
    public Mediatype convert(MediatypeForm mediatypeForm) {
        return convertFormToMediatype(mediatypeForm);
    }

    protected Mediatype convertFormToMediatype(MediatypeForm form) {
        if (form instanceof EmailMediatypeForm) {
            return convertFormToEmailMediatype((EmailMediatypeForm) form);
        }
        throw new IllegalArgumentException("Invalid mediatype" + form.getClass());
    }

    protected MediatypeEmail convertFormToEmailMediatype(EmailMediatypeForm form) {
        MediatypeEmail mediatype = (MediatypeEmail) new MediatypeFactoryImpl().create(MediaTypes.EMAIL.getMediaCode());
        mediatype.setSubject(form.getSubject());
        mediatype.setPreHeader(form.getPreHeader());
        mediatype.setCharset(form.getCharset());
        mediatype.setLinefeed(form.getLinefeed());
        mediatype.setOnepixel(form.getOnepixel());
        mediatype.setPriority(form.getPriority());
        mediatype.setFromEmail(form.getFromEmail());
        mediatype.setReplyEmail(form.getReplyEmail());
        mediatype.setMailFormat(form.getMailFormat());
        mediatype.setTemplate(form.getTextTemplate());
        mediatype.setHtmlTemplate(form.getHtmlTemplate());
        mediatype.setFromFullname(form.getFromFullname());
        mediatype.setReplyFullname(form.getReplyFullname());
        mediatype.setEnvelopeEmail(form.getEnvelopeEmail());
        mediatype.setBccRecipients(form.getBccRecipients());
        mediatype.setStatus(getStatus(form));
        return mediatype;
    }
    
    protected int getStatus(MediatypeForm form) {
        return form.isActive() ? MediaTypeStatus.Active.getCode() : MediaTypeStatus.Inactive.getCode();
    }
}
