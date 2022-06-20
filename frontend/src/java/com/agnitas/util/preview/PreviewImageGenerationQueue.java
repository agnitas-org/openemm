/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.preview;

import java.util.Collection;

/**
 * Designed to help to optimize long-running tasks execution. The main idea is to avoid adding new
 * task to a queue if there's another task having the same ID (see {@link PreviewImageGenerationTask#getId()}) waiting
 * for execution but not started yet.
 */
public interface PreviewImageGenerationQueue {
    /**
     * Check if a schedule contains a task having the same ID (see {@link PreviewImageGenerationTask#getId()}) as a {@code task} argument.
     * If so the supplied {@code task} will never be executed. Otherwise it will be scheduled for synchronous/immediate or asynchronous execution.
     *
     * @param task a task to be (conditionally) added to a queue (asynchronous) or executed immediately (synchronous).
     * @param async whether a task should be scheduled for synchronous/immediate ({@code true}) or asynchronous ({@code false}) execution.
     */
    void enqueue(PreviewImageGenerationTask task, boolean async);

    /**
     * Effectively works the same way as if {@link #enqueue(PreviewImageGenerationTask, boolean)} was called for each task
     * in a collection but provides some overhead reduction.
     *
     * @param tasks a collection of tasks to be (conditionally) added to a queue (asynchronous) or executed immediately (synchronous).
     * @param async whether tasks should be scheduled for synchronous/immediate ({@code true}) or asynchronous ({@code false}) execution.
     */
    void enqueue(Collection<PreviewImageGenerationTask> tasks, boolean async);
}
