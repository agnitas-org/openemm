/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms.mediatype;

import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.commons.CharsetConstants;
import com.agnitas.util.importvalues.MailType;

public class EmailMediatypeForm extends MediatypeForm {

    private int linefeed = 72;
    private int mailFormat = MailType.HTML_OFFLINE.getIntValue();
    private String subject;
    private String preHeader;
    private String fromEmail;
    private String fromFullname;
    private String replyEmail;
    private String charset = CharsetConstants.UTF_8_CHARSET_NAME;
    private String htmlTemplate;
    private String replyFullname;
    private String envelopeEmail;
	private String onepixel = MediatypeEmail.ONEPIXEL_TOP;
	private String bccRecipients;

    public EmailMediatypeForm() {
        setTextTemplate("[agnDYN name=\"Text\" /]");
        setHtmlTemplate("[agnDYN name=\"HTML-Version\" /]");
    }

    public int getLinefeed() {
        return linefeed;
    }

    public void setLinefeed(int linefeed) {
        this.linefeed = linefeed;
    }

    public int getMailFormat() {
        return mailFormat;
    }

    public void setMailFormat(int mailFormat) {
        this.mailFormat = mailFormat;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromFullname() {
        return fromFullname;
    }

    public void setFromFullname(String fromFullname) {
        this.fromFullname = fromFullname;
    }

    public String getReplyFullname() {
        return replyFullname;
    }

    public void setReplyFullname(String replyFullname) {
        this.replyFullname = replyFullname;
    }

    public String getReplyEmail() {
        return replyEmail;
    }

    public void setReplyEmail(String replyEmail) {
        this.replyEmail = replyEmail;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    public String getEnvelopeEmail() {
        return envelopeEmail;
    }

    public void setEnvelopeEmail(String envelopeEmail) {
        this.envelopeEmail = envelopeEmail;
    }

    public String getOnepixel() {
        return onepixel;
    }

    public void setOnepixel(String onepixel) {
        this.onepixel = onepixel;
    }

    public String getBccRecipients() {
        return bccRecipients;
    }

    public void setBccRecipients(String bccRecipients) {
        this.bccRecipients = bccRecipients;
    }

    public String getPreHeader() {
        return preHeader;
    }

    public void setPreHeader(String preHeader) {
        this.preHeader = preHeader;
    }
}
