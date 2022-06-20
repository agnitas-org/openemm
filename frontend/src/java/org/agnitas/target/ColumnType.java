/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

@Deprecated
public final class ColumnType {
    public static final int COLUMN_TYPE_STRING = 0;
    public static final int COLUMN_TYPE_NUMERIC = 1;
    public static final int COLUMN_TYPE_DATE = 2;
    public static final int COLUMN_TYPE_INTERVAL_MAILING = 3;
    public static final int COLUMN_TYPE_MAILING_RECEIVED = 4;
    public static final int COLUMN_TYPE_MAILING_OPENED = 5;
    public static final int COLUMN_TYPE_MAILING_CLICKED = 6;
    public static final int COLUMN_TYPE_MAILING_REVENUE = 7;
    public static final int COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK = 8;
}
