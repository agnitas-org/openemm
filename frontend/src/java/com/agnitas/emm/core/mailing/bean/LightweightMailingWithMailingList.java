/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;

public class LightweightMailingWithMailingList extends LightweightMailing {

    private final int mailingListId;

    public LightweightMailingWithMailingList(int companyID, int mailingID, String shortname, String description, MailingType mailingType, String workStatusOrNull,
                                             MailingContentType mailingContentType, int mailingListId) {
        super(companyID, mailingID, shortname, description, mailingType, workStatusOrNull, mailingContentType);
        this.mailingListId = mailingListId;
    }

    public LightweightMailingWithMailingList(Mailing mailing, int mailingListId) {
        super(mailing);
        this.mailingListId = mailingListId;
    }

    public int getMailingListId() {
        return mailingListId;
    }
}
