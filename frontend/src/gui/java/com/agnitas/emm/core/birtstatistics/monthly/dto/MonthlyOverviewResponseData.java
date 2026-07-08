/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.dto;

import java.util.List;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.dto.MonthlyMailingAmountStatistics;
import com.agnitas.emm.core.birtstatistics.dto.MonthlyMailingDetailStatistics;

public record MonthlyOverviewResponseData(
        double averageMailSizeKb,
        List<MailingTypeRow> mailingTypesData,
        List<MonthlyMailingDetailStatistics> detailRows,
        List<MonthlyMailingAmountStatistics> amountRows
) {

    public record MailingTypeRow(MailingType mailingType, int mailingsCount, int emailsCount, boolean isTotal) {

        public MailingTypeRow(MailingType mailingType, int mailingsCount, int emailsCount) {
            this(mailingType, mailingsCount, emailsCount, false);
        }

        public MailingTypeRow(int mailingsCount, int emailsCount) {
            this(null, mailingsCount, emailsCount, true);
        }
    }

}
