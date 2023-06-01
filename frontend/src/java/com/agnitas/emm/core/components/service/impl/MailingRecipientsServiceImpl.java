/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.components.entity.RecipientEmailStatus;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("MailingRecipientsServiceImpl")
public class MailingRecipientsServiceImpl implements MailingRecipientsService {

    private final ComMailingDao mailingDao;

    @Autowired
    public MailingRecipientsServiceImpl(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Override
    public RecipientEmailStatus saveStatusMailRecipients(int mailingId, String statusmailRecipients) {
        RecipientEmailStatus status = RecipientEmailStatus.OK;
        ArrayList<String> validatedEmails = new ArrayList<>();

        if (statusmailRecipients != null) {
            if (!statusmailRecipients.isBlank()) {
                StringBuilder validatedRecipients = new StringBuilder();
                String regex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
                String[] recipients = statusmailRecipients.split("\\s+");

                for (String recipient : recipients) {
                    if (recipient.length() > 0) {
                        if (!recipient.matches(regex)) {
                            status = RecipientEmailStatus.WRONG;
                        } else if (validatedEmails.contains(recipient)) {
                            status = RecipientEmailStatus.DUPLICATED;
                        } else {
                            validatedRecipients.append(" ").append(recipient);
                            validatedEmails.add(recipient);
                        }
                    }
                }

                mailingDao.saveStatusmailRecipients(mailingId, validatedRecipients.toString());
            } else {
                mailingDao.saveStatusmailRecipients(mailingId, statusmailRecipients.trim());
            }
        }

        return status;
    }
}
