/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.preview.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import com.agnitas.util.preview.PreviewImageGenerationQueue;
import com.agnitas.util.preview.PreviewImageGenerationTask;

public class PreviewImageGenerationQueueImpl implements PreviewImageGenerationQueue {
    private static final Logger logger = Logger.getLogger(PreviewImageGenerationQueueImpl.class);

    private static final int MIN_THREADS = 0;
    private static final int MAX_THREADS = 1;
    private static final int KEEP_ALIVE_SECONDS = 5;

    private Executor executor = new ThreadPoolExecutor(MIN_THREADS, MAX_THREADS, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private ConcurrentMap<String, PreviewImageGenerationTask> schedule = new ConcurrentHashMap<>();

    @Override
    public void enqueue(PreviewImageGenerationTask task, boolean async) {
        WrapperTask wrapperTask = new WrapperTask(task);
        if (wrapperTask.schedule()) {
            if (async) {
                executor.execute(wrapperTask);
            } else {
                wrapperTask.run();
            }
        }
    }

    @Override
    public void enqueue(Collection<PreviewImageGenerationTask> tasks, boolean async) {
        if (CollectionUtils.isNotEmpty(tasks)) {
            List<WrapperTask> wrapperTasks = tasks.stream()
                    .map(WrapperTask::new)
                    .filter(WrapperTask::schedule)
                    .collect(Collectors.toList());

            if (!wrapperTasks.isEmpty()) {
                if (async) {
                    executor.execute(() -> wrapperTasks.forEach(WrapperTask::run));
                } else {
                    wrapperTasks.forEach(WrapperTask::run);
                }
            }
        }
    }

    /**
     * Guarantees safe schedule use - prevents task from being stuck in a schedule because of improper implementation of
     * {@link com.agnitas.util.preview.PreviewImageGenerationTask#getId()} when different calls return different values.
     *
     * Removes task ID from a schedule right before an actual task's execution. The only case when a new task could be safely
     * rejected (not scheduled) is when a previous task (with the same ID) presents in a schedule but wasn't started yet.
     *
     * Catches all the exceptions in order to make sure that one failing task will not break anything else.
     */
    private class WrapperTask implements Runnable {
        private PreviewImageGenerationTask task;
        private String id;

        WrapperTask(PreviewImageGenerationTask task) {
            this.task = task;
            this.id = task.getId();
        }

        public boolean schedule() {
            return null == schedule.putIfAbsent(id, task);
        }

        @Override
        public void run() {
            schedule.remove(id);
            try {
                task.run();
            } catch (Throwable e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
        }
    }
}
