/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsOverviewFilter;
import com.agnitas.messages.I18nString;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.service.GenericExportWorker;
import org.agnitas.util.SqlPreparedStatementManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MailingRecipientsExportWorker extends GenericExportWorker {

    private int companyID;
    private int mailingID;
    private RemoteFile remoteFile = null;
    private MailingRecipientsOverviewFilter filter;
    private MailingRecipientsService mailingRecipientsService;

    public RemoteFile getRemoteFile() {
        return remoteFile;
    }

    public int getCompanyID() {
        return companyID;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setRemoteFile(RemoteFile remoteFile) {
        this.remoteFile = remoteFile;
    }

    public MailingRecipientsExportWorker(MailingRecipientsOverviewFilter filter, int mailingID, int companyID, MailingRecipientsService service, Locale locale) {
        super();

        this.filter = filter;
        this.mailingRecipientsService = service;
        this.mailingID = mailingID;
        this.companyID = companyID;
        this.locale = locale;
    }

    @Override
    public GenericExportWorker call() throws Exception {
        try {
            List<String> csvheaders = new ArrayList<>(filter.getSelectedFields());
            csvheaders.remove("title");

            csvheaders.add(I18nString.getLocaleString("Title", locale));
            csvheaders.add(I18nString.getLocaleString("Firstname", locale));
            csvheaders.add(I18nString.getLocaleString("Lastname", locale));
			csvheaders.add(I18nString.getLocaleString("mailing.MediaType.0", locale));

            csvheaders.add(I18nString.getLocaleString("target.rule.mailingReceived", locale));
            csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_opened", locale));
            csvheaders.add(I18nString.getLocaleString("statistic.openings", locale));
            csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_clicked", locale));
            csvheaders.add(I18nString.getLocaleString("statistic.Clicks", locale));
            csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_bounced", locale));
            csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_unsubscribed", locale));

            SqlPreparedStatementManager sqlStatement = mailingRecipientsService.prepareSqlStatement(filter, mailingID, companyID);

            selectStatement = sqlStatement.getPreparedSqlString();
            selectParameters = new ArrayList<>(List.of(sqlStatement.getPreparedSqlParameters()));

            setCsvFileHeaders(csvheaders);

            excludedColumns = new ArrayList<>();
            excludedColumns.add("customer_id");

            // Execute export
            super.call();

            if (error != null) {
                throw error;
            }

            if (remoteFile != null) {
                remoteFile = new RemoteFile(remoteFile.getRemoteFilePath(), new File(exportFile), remoteFile.getDownloadDurationMillis());
            }
        } catch (Exception e) {
            error = e;
        }

        return this;
    }
}
