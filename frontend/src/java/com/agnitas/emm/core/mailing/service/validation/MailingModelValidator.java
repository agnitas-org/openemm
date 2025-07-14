/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.validation;

import org.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.emm.core.validator.BaseValidator;

public class MailingModelValidator extends BaseValidator {
    
    private static final int SHORTNAME_MAX_LENGTH = 99;
    private static final int SHORTNAME_MIN_LENGTH = 3;
    private static final String FORMAT = "format";
    private static final String SUBJECT = "subject";
    private static final String CHARSET = "charset";
    private static final String STEPPING = "stepping";
    private static final String ONE_PIXEL = "onePixel";
    private static final String SEND_DATE = "sendDate";
    private static final String SHORTNAME = "shortname";
    private static final String TARGET_ID = "target.id";
    private static final String BLOCK_SIZE = "blocksize";
    private static final String COMPANY_ID = "company.id";
    private static final String MAILING_ID = "mailing.id";
    private static final String TARGET_MODE = "targetMode";
    private static final String TEMPLATE_ID = "template.id";
    private static final String MAILING_TYPE = "mailingType";
    private static final String SENDER_ADDRESS = "senderAddress";
    private static final String MAILINGLIST_ID = "mailinglist.id";
    private static final String REPLY_TO_ADDRESS = "replyToAddress";
    private static final String MAILDROP_STATUS = "maildropStatus";

    public MailingModelValidator(String propertiesFile) {
        super(propertiesFile);
    }

    public void assertIsValidToAdd(MailingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertIsNotBlank(model.getShortname(), SHORTNAME);
        assertMaxLength(model.getShortname(), SHORTNAME, SHORTNAME_MAX_LENGTH);
        assertMinLength(model.getShortname(), SHORTNAME, SHORTNAME_MIN_LENGTH);
        assertPositive(model.getMailinglistId(), MAILINGLIST_ID);
        assertContainsPositiveOrZeroElementsOnly(model.getTargetIDList(), TARGET_ID);
        assertIsNotBlank(model.getMailingTypeString(), MAILING_TYPE);
        assertIsMailingType(model.getMailingTypeString());
        assertIsNotBlank(model.getSubject(), SUBJECT);
        assertIsNotBlank(model.getSenderAddress(), SENDER_ADDRESS);
        assertIsEmail(model.getSenderAddress(), SENDER_ADDRESS);
        assertIsNotBlank(model.getReplyToAddress(), REPLY_TO_ADDRESS);
        assertIsEmail(model.getReplyToAddress(), REPLY_TO_ADDRESS);
        assertIsNotBlank(model.getCharset(), CHARSET);
        assertNotNull(model.getTargetMode(), TARGET_MODE, "err.isTargetMode");
        assertNotNull(model.getOnePixel(), ONE_PIXEL, "err.isOnePixel");
        assertNotNull(model.getFormat(), FORMAT, "err.isMailingFormat");
    }

    public void assertIsValidToUpdate(MailingModel model) {
        assertIsValidToAdd(model);
        assertPositive(model.getMailingId(), MAILING_ID);
    }

    public void assertIsValidToAddFromTemplate(MailingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertIsNotBlank(model.getShortname(), SHORTNAME);
        assertPositive(model.getTemplateId(), TEMPLATE_ID);
    }

    public void assertIsValidToGet(MailingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getMailingId(), MAILING_ID);
    }

    public void assertIsValidToGetForMLId(MailingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getMailinglistId(), MAILINGLIST_ID);
    }

    public void assertIsValidToSend(MailingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getMailingId(), MAILING_ID);
        assertNotNull(model.getMaildropStatus(), MAILDROP_STATUS, "err.isMaildropStatus");
        assertNotNull(model.getSendDate(), SEND_DATE);
        assertIsPositiveOrZero(model.getBlocksize(), BLOCK_SIZE);
        assertIsPositiveOrZero(model.getStepping(), STEPPING);
    }

    public void assertCompany(int companyId) {
        assertPositive(companyId, COMPANY_ID);
    }

    private void assertIsMailingType(String value) {
        try {
            if (value != null && com.agnitas.emm.common.MailingType.fromName(value) == null) {
                throwException("err.isMailingType", MAILING_TYPE);
            }
        } catch (Exception e) {
            throwException("err.isMailingType", MAILING_TYPE);
        }
    }
}
