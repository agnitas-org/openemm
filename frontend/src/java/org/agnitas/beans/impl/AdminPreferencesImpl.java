/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.AdminPreferences;
import org.apache.log4j.Logger;

public class AdminPreferencesImpl implements AdminPreferences {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(AdminPreferencesImpl.class);

    private static final long serialVersionUID = -6728182529383684326L;

    protected int adminId;
    protected int mailingContentView;

    @Override
    public int getAdminID() {
        return this.adminId;
    }

    @Override
    public void setAdminID(int adminID) {
        this.adminId = adminID;
    }

    @Override
    public int getMailingContentView() {
        return this.mailingContentView;
    }

    @Override
    public void setMailingContentView(int mailingContentView) {
        this.mailingContentView = mailingContentView;
    }
}
