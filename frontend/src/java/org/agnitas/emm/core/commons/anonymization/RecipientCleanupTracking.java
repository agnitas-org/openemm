/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.anonymization;

import java.util.Optional;

public enum RecipientCleanupTracking {
	
	EXPIRE_7_DAYS(7),
	EXPIRE_14_DAYS(14),
	EXPIRE_30_DAYS(30);
	
	private final int recipientCleanupTracking;
	
	private RecipientCleanupTracking(final int days) {
		this.recipientCleanupTracking = days;
	}
	
	public final int getRecipientCleanupTracking() {
		return this.recipientCleanupTracking;
	}
	
	public static final Optional<RecipientCleanupTracking> findByDays(final int days) {
		for (final RecipientCleanupTracking settings : values()) {
			if (settings.recipientCleanupTracking == days) {
				return Optional.of(settings);
			}
		}
		return Optional.empty();
	}
}
