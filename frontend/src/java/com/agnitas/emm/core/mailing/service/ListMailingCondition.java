/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ListMailingCondition {

	private ListMailingCondition() {
		// Empty;
	}
	
	public static abstract class SendDateCondition extends ListMailingCondition {
		private final ZonedDateTime timestamp;
		private final boolean inclusive;
		
		private SendDateCondition(final ZonedDateTime timestamp, final boolean inclusive) {
			this.timestamp = Objects.requireNonNull(timestamp);
			this.inclusive = inclusive;
		}

		public final ZonedDateTime getTimestamp() {
			return timestamp;
		}

		public final boolean isInclusive() {
			return inclusive;
		}
	}
	
	public static abstract class MailingPropertyCondition extends ListMailingCondition {
		private MailingPropertyCondition() {
			// Empty. Defined to avoid construction for outside class.
		}
	}
	
	public static final class SentBeforeCondition extends SendDateCondition {
		private SentBeforeCondition(final ZonedDateTime timestamp, final boolean inclusive) {
			super(timestamp, inclusive);
		}
	}
	
	public static final class SentAfterCondition extends SendDateCondition {
		private SentAfterCondition(final ZonedDateTime timestamp, final boolean inclusive) {
			super(timestamp, inclusive);
		}
	}
	
	public static final class MailingStatusCondition extends MailingPropertyCondition {
		private final String status;
		
		private MailingStatusCondition(final String status) {
			this.status = Objects.requireNonNull(status);
		}
		
		public final String getStatus() {
			return this.status;
		}
	}
	
	public static final SentBeforeCondition sentBefore(final ZonedDateTime timestamp) {
		return sentBefore(timestamp, false);
	}
	
	public static final SentBeforeCondition sentBefore(final ZonedDateTime timestamp, final boolean inclusive) {
		return new SentBeforeCondition(timestamp, inclusive);
	}
	
	public static final SentAfterCondition sentAfter(final ZonedDateTime timestamp) {
		return sentAfter(timestamp, false);
	}
	
	public static final SentAfterCondition sentAfter(final ZonedDateTime timestamp, final boolean inclusive) {
		return new SentAfterCondition(timestamp, inclusive);
	}
	
	public static final MailingStatusCondition mailingStatus(final String status) {
		return new MailingStatusCondition(status);
	}
}
