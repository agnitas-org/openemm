/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.dto;

import java.util.Locale;
import java.util.Optional;

import com.agnitas.emm.core.birtreport.bean.ReportFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MailingReportRequest.class, name = "mailing"),
        @JsonSubTypes.Type(value = ComparisonReportRequest.class, name = "comparison"),
        @JsonSubTypes.Type(value = RecipientReportRequest.class, name = "recipient"),
        @JsonSubTypes.Type(value = TopDomainsReportRequest.class, name = "topDomains")
})
public sealed interface StatisticsReportRequest
        permits ComparisonReportRequest, MailingReportRequest, RecipientReportRequest, TopDomainsReportRequest {

    ReportFormat format();

    String language();

    String reportName();

    boolean darkmode();

    String figures();

    default Locale locale() {
        return Optional.ofNullable(language())
                .map(Locale::forLanguageTag)
                .orElse(Locale.ENGLISH);
    }

}
