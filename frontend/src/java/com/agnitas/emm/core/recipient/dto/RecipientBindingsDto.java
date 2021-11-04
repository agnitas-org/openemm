/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import java.util.List;

public class RecipientBindingsDto {
    private List<RecipientBindingDto> bindings;
    private boolean isOldBlacklistedEmail;
    private boolean isNewBlacklistedEmail;

    public List<RecipientBindingDto> getBindings() {
        return bindings;
    }

    public void setBindings(List<RecipientBindingDto> bindings) {
        this.bindings = bindings;
    }

    public boolean isOldBlacklistedEmail() {
        return isOldBlacklistedEmail;
    }

    public void setOldBlacklistedEmail(boolean oldBlacklistedEmail) {
        isOldBlacklistedEmail = oldBlacklistedEmail;
    }

    public boolean isNewBlacklistedEmail() {
        return isNewBlacklistedEmail;
    }

    public void setNewBlacklistedEmail(boolean newBlacklistedEmail) {
        isNewBlacklistedEmail = newBlacklistedEmail;
    }
}
