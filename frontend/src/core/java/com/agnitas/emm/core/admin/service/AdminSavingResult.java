/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import com.agnitas.beans.Admin;
import com.agnitas.messages.Message;

public class AdminSavingResult {
    private final Admin result;
    private final boolean success;
    private final boolean isPasswordChanged;
    private final Message error;

    private AdminSavingResult(Admin result, boolean success, boolean isPasswordChanged, Message error) {
        this.result = result;
        this.success = success;
        this.isPasswordChanged = isPasswordChanged;
        this.error = error;
    }

    private AdminSavingResult(Admin result, boolean isPasswordChanged) {
        this(result, true, isPasswordChanged, null);
    }

    private AdminSavingResult(Message error) {
        this.result = null;
        this.success = false;
        this.isPasswordChanged = false;
        this.error = error;
    }

    public Admin getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isPasswordChanged() {
        return isPasswordChanged;
    }

    public Message getError() {
        return error;
    }

    public static AdminSavingResult success(Admin admin, boolean isPasswordChanged) {
        return new AdminSavingResult(admin, isPasswordChanged);
    }

    public static AdminSavingResult error(Message error) {
        return new AdminSavingResult(error);
    }
}
