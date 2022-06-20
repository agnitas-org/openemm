/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.Objects;
import java.util.concurrent.Callable;

import org.springframework.web.context.request.async.DeferredResult;

import com.agnitas.beans.PollingUid;

public class Pollable<T> {
    public static final long DEFAULT_TIMEOUT = 1000L;
    public static final long LONG_TIMEOUT = 10000L;
    public static final long SHORT_TIMEOUT = 500L;

    public static PollingUid uid(String sessionId, String name, Object... arguments) {
        return new PollingUid(sessionId, name, arguments);
    }

    private final DeferredResult<T> deferredResult;
    private final PollingUid uid;
    private final Callable<T> callable;

    public Pollable(PollingUid uid, Callable<T> callable) {
        this.deferredResult = new DeferredResult<>();
        this.uid = Objects.requireNonNull(uid);
        this.callable = Objects.requireNonNull(callable);
    }

    public Pollable(PollingUid uid, long timeout, Callable<T> callable) {
        this.deferredResult = new DeferredResult<>(timeout);
        this.uid = Objects.requireNonNull(uid);
        this.callable = Objects.requireNonNull(callable);
    }

    public Pollable(PollingUid uid, long timeout, T timeoutResult, Callable<T> callable) {
        this.deferredResult = new DeferredResult<>(timeout, timeoutResult);
        this.uid = Objects.requireNonNull(uid);
        this.callable = Objects.requireNonNull(callable);
    }

    public DeferredResult<T> getDeferredResult() {
        return deferredResult;
    }

    public PollingUid getUid() {
        return uid;
    }

    public Callable<T> getCallable() {
        return callable;
    }
}
