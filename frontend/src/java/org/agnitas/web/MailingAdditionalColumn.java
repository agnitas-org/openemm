/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

public enum MailingAdditionalColumn {
    CREATION_DATE("default.creationDate", "creation_date"),
    TEMPLATE("Template", "template_name"),
    SUBJECT("mailing.Subject", "subject"),
    TARGET_GROUPS("Target-Groups", "target_group"),
    MAILING_ID("MailingId", "mailingID"),
    RECIPIENTS_COUNT("Recipients", "recipients_count");

    public final String messageKey;
    public final String sortColumn;

    MailingAdditionalColumn(String messageKey, String sortColumn) {
        this.messageKey = messageKey;
        this.sortColumn = sortColumn;
    }

    public static MailingAdditionalColumn getColumn(String sortColumn) {
        for (MailingAdditionalColumn column : values()) {
            if (column.sortColumn.equals(sortColumn)) {
                return column;
            }
        }
        return null;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getSortColumn() {
        return sortColumn;
    }
}
