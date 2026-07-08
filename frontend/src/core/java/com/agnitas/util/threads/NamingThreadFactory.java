/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.threads;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper to name threads created by another ThreadFactory.
 */
public class NamingThreadFactory implements ThreadFactory {

	/** Wrapped ThreadFactory. */
	private final ThreadFactory threadFactory;
	
	/** Prefix of thread names. */
	private final String threadNamePrefix;
	
	/** Thread-safe thread counter. */
	private final AtomicInteger threadCounter;
	
	/**
	 * Creates a new instance wrapping given {@link ThreadFactory}.
	 * 
	 * @param factory ThreadFactory to wrap
	 * @param threadNamePrefix prefix for thread names
	 */
	public NamingThreadFactory(final ThreadFactory factory, final String threadNamePrefix) {
		this.threadFactory = Objects.requireNonNull(factory, "Thread factory is null");
		this.threadNamePrefix = Objects.requireNonNull(threadNamePrefix, "Prefix of thread name is null");
		this.threadCounter = new AtomicInteger();
	}
	
	@Override
	public final Thread newThread(final Runnable runnable) {
		final Thread thread = this.threadFactory.newThread(runnable);
		
		final int threadNumber = threadCounter.getAndIncrement();
		thread.setName(String.format("%s-#%d", threadNamePrefix, threadNumber));
		
		return thread;
	}

}
