/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.messages.Message;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceResult<T> {
    private final T result;
    private final boolean success;
    private final Collection<Message> successMessages;
    private final Collection<Message> warningMessages;
    private final Collection<Message> errorMessages;
    private final Collection<Message> infoMessages;

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

    public static <R> ServiceResult<R> errorKeys(final String... messageKeys) {
        Collection<Message> messages = Stream.of(messageKeys)
                .map(Message::of)
                .collect(Collectors.toList());

        return ServiceResult.error(messages);
    }

    public static <R> ServiceResult<R> error(final Collection<Message> errorMessages) {
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

    public ServiceResult(T result, boolean success, Collection<Message> messages) {
        this.result = result;
        this.success = success;
        if(success) {
            this.successMessages = Collections.unmodifiableCollection(messages);
            this.warningMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
            this.errorMessages = Collections.emptyList();
        } else {
            this.successMessages = Collections.emptyList();
            this.warningMessages = Collections.emptyList();
            this.infoMessages = Collections.emptyList();
            this.errorMessages = Collections.unmodifiableCollection(messages);
        }
    }

    public ServiceResult (final T result, final boolean success, final Collection<Message> successMessages, final Collection<Message> warningMessages, final Collection<Message> errorMessages) {
        this(result, success, successMessages, warningMessages, errorMessages, Collections.emptyList());
    }

    public ServiceResult (final T result, final boolean success, final Collection<Message> successMessages, final Collection<Message> warningMessages, final Collection<Message> errorMessages, final Collection<Message> infoMessages) {
        this.result = result;
        this.success = success;

        if (successMessages == null) {
            this.successMessages = Collections.emptyList();
        } else {
            this.successMessages = Collections.unmodifiableCollection(successMessages);
        }

        if (warningMessages == null) {
            this.warningMessages = Collections.emptyList();
        } else {
            this.warningMessages = Collections.unmodifiableCollection(warningMessages);
        }

        if (errorMessages == null) {
            this.errorMessages = Collections.emptyList();
        } else {
            this.errorMessages = Collections.unmodifiableCollection(errorMessages);
        }

        if (infoMessages == null) {
            this.infoMessages = Collections.emptyList();
        } else {
            this.infoMessages = Collections.unmodifiableCollection(infoMessages);
        }
    }

    public static <R> ServiceResult<R> from(ServiceResult<?> result) {
        return new ServiceResult<>(
                null,
                result.isSuccess(),
                result.getSuccessMessages(),
                result.getWarningMessages(),
                result.getErrorMessages(),
                result.getInfoMessages()
        );
    }

    public Collection<Message> getSuccessMessages() {
        return successMessages;
    }

    public Collection<Message> getWarningMessages() {
        return warningMessages;
    }

    public Collection<Message> getErrorMessages() {
        return errorMessages;
    }

    public Collection<Message> getInfoMessages() {
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
