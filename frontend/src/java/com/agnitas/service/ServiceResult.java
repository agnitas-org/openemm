/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.agnitas.messages.Message;

public class ServiceResult<T> {
    private final T result;
    private final boolean success;
    private final List<Message> messages;

    public ServiceResult(T result, boolean success, Message... messages) {
        this.result = result;
        this.success = success;
        this.messages = Arrays.asList(messages);
    }

    public ServiceResult(T result, boolean success, List<Message> messages) {
        this.result = result;
        this.success = success;
        this.messages = new ArrayList<>(messages);
    }

    public T getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
