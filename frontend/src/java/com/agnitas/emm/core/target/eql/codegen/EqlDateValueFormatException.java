/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

public class EqlDateValueFormatException extends IllegalArgumentException {
    private static final long serialVersionUID = -4330164517148553518L;

    private final String dateFormat;
    private final String dateValue;

    public EqlDateValueFormatException(String dateFormat, String dateValue) {
        super(String.format("Failed to parse date '%s' using date format '%s'", dateValue, dateFormat));

        this.dateFormat = dateFormat;
        this.dateValue = dateValue;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getDateValue() {
        return dateValue;
    }
}
