/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.context.request.async.DeferredResult;

import com.agnitas.beans.PollingUid;
import com.agnitas.service.PollingService;
import com.agnitas.web.mvc.Pollable;

public class PollingServiceImpl implements PollingService {
    private Map<PollingUid, ListenableFuture<?>> pendingTasksMap = new ConcurrentHashMap<>();
    private ExecutorService workerExecutorService;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void submit(Pollable<?> pollable) {
        pendingTasksMap.computeIfAbsent(pollable.getUid(), uid -> enqueue(pollable))
                .addCallback(new Callback(pollable.getDeferredResult()));
    }

    private ListenableFuture<?> enqueue(Pollable<?> pollable) {
        ListenableFutureTask<?> task = new ListenableFutureTask<>(new CallablePollable<>(pollable));
        workerExecutorService.submit(task);
        return task;
    }

    @Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    private static class Callback<T> implements ListenableFutureCallback<T> {
        private final DeferredResult<T> result;

        public Callback(DeferredResult<T> result) {
            this.result = result;
        }

        @Override
        public void onFailure(Throwable throwable) {
            result.setErrorResult(throwable);
        }

        @Override
        public void onSuccess(T value) {
            result.setResult(value);
        }
    }

    private class CallablePollable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final PollingUid uid;

        public CallablePollable(Pollable<T> pollable) {
            callable = pollable.getCallable();
            uid = pollable.getUid();
        }

        @Override
        public T call() throws Exception {
            try {
                return callable.call();
            } finally {
                pendingTasksMap.remove(uid);
            }
        }
    }
}
