/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean.impl;

import org.agnitas.beans.Mailinglist;

import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.emm.core.report.bean.CompositeBindingEntry;

public class CompositeBindingEntryImpl extends PlainBindingEntryImpl implements CompositeBindingEntry {

    private static final long serialVersionUID = -5525752040521977179L;

    private Mailinglist mailingList;

    // todo: replace with full clean entity;
    private ComRecipientLiteImpl recipient;

    @Override
    public Mailinglist getMailingList() {
        return mailingList;
    }

    @Override
    public void setMailingList(Mailinglist mailinglist) {
        this.mailingList = mailinglist;
    }

    @Override
    public ComRecipientLiteImpl getRecipient() {
        return recipient;
    }

    @Override
    public void setRecipient(ComRecipientLiteImpl recipient) {
        this.recipient = recipient;
    }
}
