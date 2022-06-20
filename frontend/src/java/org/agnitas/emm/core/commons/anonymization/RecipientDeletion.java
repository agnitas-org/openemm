/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.anonymization;

import java.util.Optional;

public enum RecipientDeletion {
	
	EXPIRE_2_YEARS(730),
	EXPIRE_1_YEAR(365),
	EXPIRE_6_MONTHS(180),
	EXPIRE_90_DAYS(90),
	EXPIRE_30_DAYS(30),
	EXPIRE_14_DAYS(14),
	EXPIRE_7_DAYS(7);
	
	private final int recipientDeletion;
	
	private RecipientDeletion(final int days) {
		this.recipientDeletion = days;
	}
	
	public final int getRecipientDeletion() {
		return this.recipientDeletion;
	}
	
	public static final Optional<RecipientDeletion> findByDays(final int days) {
		for (final RecipientDeletion settings : values()) {
			if (settings.recipientDeletion == days) {
				return Optional.of(settings);
			}
		}
		return Optional.empty();
	}
}
