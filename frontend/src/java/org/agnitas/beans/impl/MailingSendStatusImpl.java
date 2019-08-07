/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.MailingSendStatus;

public class MailingSendStatusImpl implements MailingSendStatus {
    private boolean hasMailtracks;
    private int expirationDays;
    private boolean hasMailtrackData;

    @Override
    public boolean getHasMailtracks() {
        return hasMailtracks;
    }

    @Override
    public void setHasMailtracks(boolean hasMailtracks) {
        this.hasMailtracks = hasMailtracks;
    }

    @Override
    public int getExpirationDays() {
        return expirationDays;
    }

    @Override
    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    @Override
    public boolean getHasMailtrackData() {
        return hasMailtrackData;
    }

    @Override
    public void setHasMailtrackData(boolean hasMailtrackData) {
        this.hasMailtrackData = hasMailtrackData;
    }
}
