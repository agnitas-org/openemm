/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Date;

import org.displaytag.decorator.TableDecorator;

import com.agnitas.beans.ComRecipientHistory;

public class RecipientsTableDecorator extends TableDecorator {
    private static final String EVEN_STYLE = "recipientsTable-even";
    private static final String ODD_STYLE = "recipientsTable-odd";

    private Date lastDate;

    @Override
    public String addRowClass() {
        boolean odd = true;
        int sortedColumn = tableModel.getSortedColumnNumber();
        ComRecipientHistory currentItem = (ComRecipientHistory) getCurrentRowObject();
        if (sortedColumn <= 0) {
            if (lastDate != null && !lastDate.equals(currentItem.getChangeDate())) {
                odd = !odd;
            }
        } else {
            odd = lastDate == null ? odd : !odd;
        }
        lastDate = currentItem.getChangeDate();
        return getStyle(odd);
    }

    private String getStyle(boolean odd) {
        if (odd) {
            return ODD_STYLE;
        } else {
            return EVEN_STYLE;
        }
    }
}
