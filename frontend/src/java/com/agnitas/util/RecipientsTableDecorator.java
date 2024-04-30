/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.displaytag.decorator.TableDecorator;
import org.displaytag.model.Row;

import com.agnitas.beans.ComRecipientHistory;

public class RecipientsTableDecorator extends TableDecorator {
    private static final String EVEN_STYLE = "recipientsTable-even";
    private static final String ODD_STYLE = "recipientsTable-odd";

    private List<Date> dates;

    private boolean isLastOdd = true;

    @Override
    public String addRowClass() {
        if(tableModel.getSortedColumnNumber() > 0) {
            return processUsualLogic();
        }

        return groupByDate();
    }

    private String groupByDate() {
        if(dates == null) {
            initDates();
        }
        final Date currentChangeDate = ((ComRecipientHistory) getCurrentRowObject()).getChangeDate();
        final int index = dates.indexOf(currentChangeDate);
        return getStyle(index % 2 == 0);
    }

    private void initDates() {
        this.dates = new ArrayList<>();
        for (Object item: tableModel.getRowListPage()) {
            final Row row = (Row) item;
            final ComRecipientHistory history = (ComRecipientHistory) row.getObject();
            final Date changeDate = history.getChangeDate();
            if (!dates.contains(changeDate)) {
                dates.add(changeDate);
            }
        }
        Collections.sort(dates);
    }

    private String processUsualLogic() {
        isLastOdd = !isLastOdd;
        return getStyle(isLastOdd);
    }

    private String getStyle(boolean odd) {
        if (odd) {
            return ODD_STYLE;
        } else {
            return EVEN_STYLE;
        }
    }
}
