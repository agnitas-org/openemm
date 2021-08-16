/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.log4j.Logger;

@Deprecated // Not for common use. Used by EMM-8126 only to measure timings.
public class TimingLogger {

	private final Logger logger;
	private final UUID loggingID;
	private final ZonedDateTime startTime;
	
	public TimingLogger(final Logger logger, final String initialMessage) {
		this.logger = logger;
		this.loggingID = UUID.randomUUID();
		this.startTime = ZonedDateTime.now();
		
		log(initialMessage);
	}
	
	public final void log(final String message) {
		if(logger != null && logger.isInfoEnabled()) {
			final Duration duration = Duration.between(startTime, ZonedDateTime.now());
			
			logger.info(String.format(
					"Timing measurement %s (%s / %dms): %s ", 
					loggingID.toString(),
					duration.toString(),
					duration.toMillis(),
					message));
		}
	}
	
}
