/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dev.logging;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimingLogger {
	
	private static final AtomicInteger NEXT_LOG_MARK_ID = new AtomicInteger();

	private final Logger logger;
	private final UUID loggingID;
	private final ZonedDateTime startTime;
	
	public static final class TimingLogMark {
		private final int id;
		private final ZonedDateTime logTime;
		
		private TimingLogMark(final int id, final ZonedDateTime logTime) {
			this.id = id;
			this.logTime = logTime;
		}
	}
	
	public TimingLogger(final String initialMessage) {
		this(LogManager.getLogger(TimingLogger.class), initialMessage);
	}
	
	public TimingLogger(final Logger logger, final String initialMessage) {
		this.logger = logger;
		this.loggingID = UUID.randomUUID();
		this.startTime = ZonedDateTime.now();
		
		log(initialMessage);
	}
	
	public final TimingLogMark log(final String message) {
		return log(message, null);
	}
	
	public final TimingLogMark log(final String message, final TimingLogMark measureToMark) {
		final ZonedDateTime now = ZonedDateTime.now();
		final TimingLogMark thisMark = new TimingLogMark(NEXT_LOG_MARK_ID.getAndIncrement(), now);
		
		if(logger != null && logger.isInfoEnabled()) {
			final Duration duration = Duration.between(startTime, now);

			if(measureToMark != null) {
				final Duration lastLogDuration = Duration.between(measureToMark.logTime, now);
				
				logger.info(String.format(
						"Timing measurement %s@%5d (%-12s / %5dms --- %-12s %5dms @ mark:%5d): %s ",
						loggingID.toString(),
						thisMark.id,
						duration.toString(),
						duration.toMillis(),
						lastLogDuration.toString(),
						lastLogDuration.toMillis(),
						measureToMark.id,
						message));
			} else {
				logger.info(String.format(
						"Timing measurement %s@%5d (%-12s / %5dms --- ): %s ",
						loggingID.toString(),
						thisMark.id,
						duration.toString(),
						duration.toMillis(),
						message));
			}
		}
		
		return thisMark;
	}
	
}
