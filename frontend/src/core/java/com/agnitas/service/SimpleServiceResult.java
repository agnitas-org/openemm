/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.service;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.messages.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class SimpleServiceResult extends ServiceResult<Void> {

    public SimpleServiceResult(
            boolean success,
            Collection<Message> successMessages,
            Collection<Message> warningMessages,
            Collection<Message> errorMessages,
            Collection<Message> infoMessages,
            Map<ValidationField, Message> fieldErrors
    ) {
        super(
                null,
                success,
                successMessages,
                warningMessages,
                errorMessages,
                infoMessages,
                fieldErrors
        );
    }

    public SimpleServiceResult(final boolean success, final Collection<Message> successMessages, final Collection<Message> warningMessages, final Collection<Message> errorMessages) {
        this(success, successMessages, warningMessages, errorMessages, List.of());
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

    public static SimpleServiceResult simpleWarning(boolean success, Message ... warningMessages) {
        return new SimpleServiceResult(success, null, Set.of(warningMessages), null);
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

    public static SimpleServiceResult simpleFieldError(Map<ValidationField, Message> fieldErrors) {
        return new SimpleServiceResult(fieldErrors.isEmpty(), null, null, null, null, fieldErrors);
    }

    public static SimpleServiceResult of(ServiceResult<?> result) {
        return new SimpleServiceResult(result.isSuccess(),
                result.getSuccessMessages(),
                result.getWarningMessages(),
                result.getErrorMessages(),
                result.getInfoMessages());
    }

    public SimpleServiceResult merge(ServiceResult<?>... others) {
        boolean s = isSuccess();
        List<Message> succ = new ArrayList<>(getSuccessMessages());
        List<Message> warn = new ArrayList<>(getWarningMessages());
        List<Message> err  = new ArrayList<>(getErrorMessages());
        List<Message> info = new ArrayList<>(getInfoMessages());
        Map<ValidationField, Message> fe = new LinkedHashMap<>(getFieldErrors());

        for (ServiceResult<?> o : others) {
            s &= o.isSuccess();
            succ.addAll(o.getSuccessMessages());
            warn.addAll(o.getWarningMessages());
            err.addAll(o.getErrorMessages());
            info.addAll(o.getInfoMessages());
            fe.putAll(o.getFieldErrors());
        }
        return new SimpleServiceResult(s, succ, warn, err, info, fe);
    }

    public static SimpleServiceResult of(
            Collection<Message> warnings, Collection<Message> errors,
            Map<ValidationField, Message> fieldErrors
    ) {
        boolean success = CollectionUtils.isEmpty(errors) && MapUtils.isEmpty(fieldErrors);
        return new SimpleServiceResult(success, emptyList(), warnings, errors, emptyList(), fieldErrors);
    }

    public static SimpleServiceResult of(Collection<Message> warnings, Collection<Message> errors) {
        return of(warnings, errors, Map.of());
    }

    public SimpleServiceResult throwIfFailed() {
        if (!isSuccess()) {
            throw ValidationException.of(this);
        }
        return this;
    }
}
