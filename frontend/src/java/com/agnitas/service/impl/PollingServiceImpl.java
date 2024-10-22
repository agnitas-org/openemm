/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.agnitas.emm.core.export.web.ExportController;
import com.agnitas.emm.core.recipient.imports.wizard.web.RecipientImportWizardController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.context.request.async.DeferredResult;

import com.agnitas.beans.PollingUid;
import com.agnitas.service.PollingService;
import com.agnitas.web.mvc.Pollable;

public class PollingServiceImpl implements PollingService {
    
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private static final Set<String> LONG_RUNNING_TASKS = Set.of(ExportController.EXPORT_KEY, RecipientImportWizardController.RECIPIENTS_IMPORT_WIZARD_KEY);

    private Map<PollingUid, ListenableFuture<?>> pendingTasksMap = new ConcurrentHashMap<>();
    private ExecutorService workerExecutorService;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void submit(Pollable<?> pollable) {
        pendingTasksMap.computeIfAbsent(pollable.getUid(), uid -> enqueue(pollable))
                .addCallback(new Callback(pollable.getDeferredResult())); // suppress warning for this unchecked call
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
                // If the task is executed longer than the pollable timeout, it may finished at the same time
                // when a repeated request is just sent from the client (e.g. when using data-form="loading").
                // In this case, another task with the equal uid may be generated again. So same task working twice.
                // To prevent second time execution, the retention delay was added for removing the task,
                // so that when such a situation occurs, the result of the initial execution is returned.
                long retentionTimeout = LONG_RUNNING_TASKS.contains(uid.getName())
                            ? Pollable.LONG_RETENTION_TIMEOUT
                            : Pollable.SHORT_RETENTION_TIMEOUT;

                scheduledExecutorService.schedule(
                        () -> pendingTasksMap.remove(uid),
                        retentionTimeout,
                        TimeUnit.MILLISECONDS);
            }
        }
    }
}
