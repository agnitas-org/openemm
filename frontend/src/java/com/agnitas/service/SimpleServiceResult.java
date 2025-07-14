/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.messages.Message;

import java.util.Arrays;
import java.util.Collection;

public class SimpleServiceResult extends ServiceResult<Void> {

    public SimpleServiceResult(final boolean success, final Collection<Message> successMessages, final Collection<Message> warningMessages, final Collection<Message> errorMessages) {
        this(success, successMessages, warningMessages, errorMessages, null);
    }

    public SimpleServiceResult(final boolean success, final Collection<Message> successMessages, final Collection<Message> warningMessages, final Collection<Message> errorMessages, final Collection<Message> infoMessages) {
        super(null, success, successMessages, warningMessages, errorMessages, infoMessages);
    }
    public SimpleServiceResult(boolean success, Message... messages) {
        super(null, success, messages);
    }

    public SimpleServiceResult(boolean success, Collection<Message> messages) {
        super(null, success, messages);
    }

    public static SimpleServiceResult simpleSuccess(final Message... successMessages) {
        return new SimpleServiceResult(true, Arrays.asList(successMessages), null, null);
    }

    public static SimpleServiceResult simpleWarning(final Message... warningMessages) {
        return new SimpleServiceResult(true, null, Arrays.asList(warningMessages), null);
    }

    public static SimpleServiceResult simpleWarning(final Collection<Message> warningMessages) {
        return simpleWarning(true, warningMessages);
    }

    public static SimpleServiceResult simpleWarning(boolean success, final Collection<Message> warningMessages) {
        return new SimpleServiceResult(success, null, warningMessages, null);
    }

    public static SimpleServiceResult simpleError(final Message... errorMessages) {
        return new SimpleServiceResult(false, null, null, Arrays.asList(errorMessages));
    }

    public static SimpleServiceResult simpleError(final Collection<Message> errorMessages) {
        return new SimpleServiceResult(false, null, null, errorMessages);
    }

    public static SimpleServiceResult simpleInfo(boolean success, Message ... messages) {
        return new SimpleServiceResult(success, null, null, null, Arrays.asList(messages));
    }

    public static SimpleServiceResult of(ServiceResult<?> result) {
        return new SimpleServiceResult(result.isSuccess(),
                result.getSuccessMessages(),
                result.getWarningMessages(),
                result.getErrorMessages(),
                result.getInfoMessages());
    }
}
