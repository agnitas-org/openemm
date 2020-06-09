/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.Arrays;
import java.util.List;

import com.agnitas.messages.Message;

public class SimpleServiceResult extends ServiceResult<Void> {

    public SimpleServiceResult(final boolean success, final List<Message> successMessages, final List<Message> warningMessages, final List<Message> errorMessages) {
        super(null, success, successMessages, warningMessages, errorMessages);
    }
    public SimpleServiceResult(boolean success, Message... messages) {
        super(null, success, messages);
    }

    public static SimpleServiceResult simpleSuccess(final Message... successMessages) {
        return new SimpleServiceResult(true, Arrays.asList(successMessages), null, null);
    }

    public static SimpleServiceResult simpleWarning(final Message... warningMessages) {
        return new SimpleServiceResult(true, null, Arrays.asList(warningMessages), null);
    }

    public static SimpleServiceResult simpleWarning(final List<Message> warningMessages) {
        return new SimpleServiceResult(true, null, warningMessages, null);
    }

    public static SimpleServiceResult simpleError(final List<Message> errorMessages) {
        return new SimpleServiceResult(false, null, null, errorMessages);
    }
}
