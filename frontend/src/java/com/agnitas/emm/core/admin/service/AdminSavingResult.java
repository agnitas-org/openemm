/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.Objects;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComAdmin;

public class AdminSavingResult {
    private final ComAdmin result;
    private final boolean success;
    private final boolean isPasswordChanged;
    private final ActionMessages errors;

    private AdminSavingResult(ComAdmin result, boolean success, boolean isPasswordChanged, ActionMessages errors) {
        this.result = result;
        this.success = success;
        this.isPasswordChanged = isPasswordChanged;
        this.errors = errors;
    }

    private AdminSavingResult(ComAdmin result, boolean isPasswordChanged) {
        this(result, true, isPasswordChanged, null);
    }

    private AdminSavingResult(ActionMessages errors) {
        Objects.requireNonNull(errors);

        this.result = null;
        this.success = false;
        this.isPasswordChanged = false;
        this.errors = errors;
    }

    private AdminSavingResult(String errorProperty, ActionMessage error) {
        this(new ActionMessages());
        this.errors.add(errorProperty, error);
    }

    public ComAdmin getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isPasswordChanged() {
        return isPasswordChanged;
    }

    public ActionMessages getErrors() {
        return errors;
    }

    public static AdminSavingResult success(ComAdmin admin, boolean isPasswordChanged) {
        return new AdminSavingResult(admin, isPasswordChanged);
    }

    public static AdminSavingResult error(ActionMessage error) {
        return new AdminSavingResult(ActionMessages.GLOBAL_MESSAGE, error);
    }
}
