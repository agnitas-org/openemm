/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;

import com.agnitas.reporting.birt.external.dataset.CommonKeys;

public class MailingComparisonForm extends BulkMailingComparisonForm {
    
    private String recipientType = CommonKeys.TYPE_ALL_SUBSCRIBERS;
    
    private String reportFormat = "html";
    
    public String getRecipientType() {
        return recipientType;
    }
    
    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
    
    public String getReportFormat() {
        return reportFormat;
    }
    
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }
}
