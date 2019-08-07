/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import com.agnitas.beans.MailingSendingProperties;

public interface WorkflowMailing extends WorkflowMailingAware, MailingSendingProperties {

    int AUTOMATIC_REPORT_NONE = 0;
    int AUTOMATIC_REPORT_1DAY = 1;
    int AUTOMATIC_REPORT_2DAYS = 2;
    int AUTOMATIC_REPORT_7DAYS = 7;

    int getAutoReport();

    void setAutoReport(int autoReport);

    boolean isSkipEmptyBlocks();

    void setSkipEmptyBlocks(boolean skipEmptyBlocks);

    boolean isDoubleCheck();

    void setDoubleCheck(boolean doubleCheck);

    @Override
	int getMaxRecipients();

    void setMaxRecipients(int maxRecipients);

    @Override
	int getBlocksize();

    void setBlocksize(int blocksize);
}
