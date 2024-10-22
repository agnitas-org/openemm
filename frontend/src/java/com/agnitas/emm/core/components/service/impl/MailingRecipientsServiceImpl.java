/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.company.service.ComCompanyService;
import com.agnitas.emm.core.components.entity.RecipientEmailStatus;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.dao.MailingRecipientsDao;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsOverviewFilter;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.web.MailingRecipientsAdditionalColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDateTimeValue;
import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDateValue;
import static org.agnitas.emm.core.recipient.RecipientUtils.formatRecipientDoubleValue;

@Service("MailingRecipientsServiceImpl")
public class MailingRecipientsServiceImpl implements MailingRecipientsService {

    private final MailingDao mailingDao;
    private final MailingRecipientsDao mailingRecipientsDao;
    private final ComCompanyService companyService;

    @Autowired
    public MailingRecipientsServiceImpl(MailingDao mailingDao, MailingRecipientsDao mailingRecipientsDao, ComCompanyService companyService) {
        this.mailingDao = mailingDao;
        this.mailingRecipientsDao = mailingRecipientsDao;
        this.companyService = companyService;
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

    @Override
    public PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(MailingRecipientsOverviewFilter filter, int mailingId, Map<String, ProfileField> profileFields, Admin admin) throws Exception {
        Set<String> selectedRecipientsFields = filterSelectedRecipientsFields(filter);
        int maxRecipients = companyService.getCompany(admin.getCompanyID()).getMaxRecipients();

        PaginatedListImpl<MailingRecipientStatRow> recipients = mailingRecipientsDao.getMailingRecipients(filter, selectedRecipientsFields, maxRecipients, mailingId, admin.getCompanyID());

        for (MailingRecipientStatRow recipient : recipients.getList()) {
            for (String col : selectedRecipientsFields) {
                recipient.setVal(col, getFieldFormattedValue(recipient.getVal(col), profileFields.get(col), admin));
            }
        }

        return recipients;
    }

    private String getFieldFormattedValue(Object val, ProfileField profileField, Admin admin) {
        if (profileField == null) {
            return "";
        }

        switch (profileField.getSimpleDataType()) {
            case Float:
                return formatRecipientDoubleValue(admin, ((Number) val).doubleValue());
            case Date:
                return formatRecipientDateValue(admin, (Date) val);
            case DateTime:
                return formatRecipientDateTimeValue(admin, (Date) val);
            default:
                return Objects.isNull(val) ? "" : val.toString();
        }
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatement(MailingRecipientsOverviewFilter filter, int mailingId, int companyId) {
        return mailingRecipientsDao.prepareSqlStatement(filter, filterSelectedRecipientsFields(filter), mailingId, companyId);
    }

    private Set<String> filterSelectedRecipientsFields(MailingRecipientsOverviewFilter filter) {
        final Set<String> fields = new HashSet<>(filter.getSelectedFields());
        fields.removeAll(MailingRecipientsAdditionalColumn.getColumns());

        return fields;
    }
}
