/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.agnitas.messages.Message;

public class ServiceResult<T> {
    private final T result;
    private final boolean success;
    private final List<Message> successMessages;
    private final List<Message> warningMessages;
    private final List<Message> errorMessages;
    private final List<Message> infoMessages;

    public static <R> ServiceResult<R> success(final R result, final Message... successMessages) {
        return new ServiceResult<>(result, true, Arrays.asList(successMessages), null, null);
    }

    public static <R> ServiceResult<R> warning(final boolean success, final Message... warningMessages) {
        return new ServiceResult<>(null, success, null, Arrays.asList(warningMessages), null);
    }

    public static <R> ServiceResult<R> warning(final R result, final boolean success, final Message... warningMessages) {
        return new ServiceResult<>(result, success, null, Arrays.asList(warningMessages), null);
    }

    public static <R> ServiceResult<R> info(final R result, final boolean success, final Message... infoMessages) {
        return new ServiceResult<>(result, success, null, null, null, Arrays.asList(infoMessages));
    }

    public static <R> ServiceResult<R> error(final Message... errorMessages) {
        return new ServiceResult<>(null, false, null, null, Arrays.asList(errorMessages));
    }

    public static <R> ServiceResult<R> error(final List<Message> errorMessages) {
        return new ServiceResult<>(null, false, null, null,  errorMessages);
    }

    public ServiceResult(T result, boolean success, Message... messages) {
        this.result = result;
        this.success = success;
        if(success) {
            this.successMessages = Collections.unmodifiableList(Arrays.asList(messages));
            this.warningMessages = Collections.emptyList();
            this.errorMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
        } else {
            this.successMessages = Collections.emptyList();
            this.warningMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
            this.errorMessages = Collections.unmodifiableList(Arrays.asList(messages));
        }
    }

    public ServiceResult(T result, boolean success, List<Message> messages) {
        this.result = result;
        this.success = success;
        if(success) {
            this.successMessages = Collections.unmodifiableList(messages);
            this.warningMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
            this.errorMessages = Collections.emptyList();
        } else {
            this.successMessages = Collections.emptyList();
            this.warningMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
            this.errorMessages = Collections.unmodifiableList(messages);
        }
    }

    public ServiceResult (final T result, final boolean success, final List<Message> successMessages, final List<Message> warningMessages, final List<Message> errorMessages) {
        this(result, success, successMessages, warningMessages, errorMessages, Collections.emptyList());
    }

    public ServiceResult (final T result, final boolean success, final List<Message> successMessages, final List<Message> warningMessages, final List<Message> errorMessages, final List<Message> infoMessages) {
        this.result = result;
        this.success = success;

        if (successMessages == null) {
            this.successMessages = Collections.emptyList();
        } else {
            this.successMessages = Collections.unmodifiableList(successMessages);
        }

        if (warningMessages == null) {
            this.warningMessages = Collections.emptyList();
        } else {
            this.warningMessages = Collections.unmodifiableList(warningMessages);
        }

        if (errorMessages == null) {
            this.errorMessages = Collections.emptyList();
        } else {
            this.errorMessages = Collections.unmodifiableList(errorMessages);
        }

        if (infoMessages == null) {
            this.infoMessages = Collections.emptyList();
        } else {
            this.infoMessages = Collections.unmodifiableList(infoMessages);
        }
    }

    public List<Message> getSuccessMessages() {
        return successMessages;
    }

    public List<Message> getWarningMessages() {
        return warningMessages;
    }

    public List<Message> getErrorMessages() {
        return errorMessages;
    }

    public List<Message> getInfoMessages() {
        return infoMessages;
    }

    public boolean hasErrorMessages() {
        return CollectionUtils.isNotEmpty(errorMessages);
    }

    public T getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

}
