/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.converter;

import com.agnitas.beans.MailingSendOptions;
import com.agnitas.emm.core.components.form.MailingSendForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MailingSendFormToMailingSendOptionsConverter implements Converter<MailingSendForm, MailingSendOptions> {

    @Override
    public MailingSendOptions convert(MailingSendForm form) {
        return MailingSendOptions.builder()
                .setReportSendAfter24h(form.isReportSendAfter24h())
                .setReportSendAfter48h(form.isReportSendAfter48h())
                .setReportSendAfter1Week(form.isReportSendAfter1Week())
                .setReportEmails(form.getReportEmails())
                .setCheckForDuplicateRecords(form.isCheckForDuplicateRecords())
                .setSkipWithEmptyTextContent(form.isSkipWithEmptyTextContent())
                .setCleanupTestsBeforeDelivery(form.isCleanupTestsBeforeDelivery())
                .setGenerationOptimization(form.getGenerationOptimization())
                .setMaxRecipients(Integer.parseInt(form.getMaxRecipients()))
                .setStepping(form.getStepping())
                .setBlockSize(form.getBlocksize())
                .setRequiredAutoImport(form.getAutoImportId())
                .setDate(form.getDate())
                .setActivateAgainToday(form.isActivateAgainToday())
                .build();
    }
}
