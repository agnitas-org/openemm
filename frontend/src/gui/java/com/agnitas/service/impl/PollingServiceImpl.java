/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.export.web.ExportController;
import com.agnitas.emm.core.recipient.imports.wizard.web.RecipientImportWizardController;
import com.agnitas.service.PollingService;
import com.agnitas.web.mvc.Pollable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class PollingServiceImpl implements PollingService {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private static final Set<String> LONG_RUNNING_TASKS = Set.of(ExportController.EXPORT_KEY, RecipientImportWizardController.RECIPIENTS_IMPORT_WIZARD_KEY);

    private final ExecutorService workerExecutorService;
    private final Map<PollingUid, CompletableFuture<?>> pendingTasksMap = new ConcurrentHashMap<>();

    public PollingServiceImpl(@Qualifier("WorkerExecutorService") ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @SuppressWarnings({ "unchecked"})
    @Override
    public void submit(Pollable<?> pollable) {
        CompletableFuture<?> future = pendingTasksMap.computeIfAbsent(
                pollable.getUid(),
                uid -> enqueue(pollable)
        );

        future.whenComplete((value, throwable) -> {
            DeferredResult<Object> result = (DeferredResult<Object>) pollable.getDeferredResult();

            if (throwable != null) {
                result.setErrorResult(throwable);
            } else {
                result.setResult(value);
            }
        });
    }

    private <T> CompletableFuture<T> enqueue(Pollable<T> pollable) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return pollable.getCallable().call();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    } finally {
                        scheduleRemoval(pollable.getUid());
                    }
                },
                workerExecutorService
        );
    }

    private void scheduleRemoval(PollingUid uid) {
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
                TimeUnit.MILLISECONDS
        );
    }

}
