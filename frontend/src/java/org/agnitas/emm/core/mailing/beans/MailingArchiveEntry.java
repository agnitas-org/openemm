/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.beans;

import java.util.Objects;

public final class MailingArchiveEntry {

    private final int mailingId;
    private final String shortname;
    private final String emailSubject;

    public MailingArchiveEntry(final int mailingId, final String shortname, final String emailSubject) {
        this.mailingId = mailingId;
        this.shortname = Objects.requireNonNull(shortname,"mailing shortname");
        this.emailSubject = Objects.requireNonNull(emailSubject, "email subject");
    }

    public final int getMailingId() {
        return mailingId;
    }

    public final String getShortname() {
        return shortname;
    }

    public final String getEmailSubject() {
        return emailSubject;
    }
}
