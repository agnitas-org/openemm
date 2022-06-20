/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import java.util.Optional;

public enum PasswordExpireSettings {
	
	EXPIRE_30_DAYS(30),
	EXPIRE_90_DAYS(90),
	EXPIRE_180_DAYS(180),
	EXPIRE_360_DAYS(360)
	;

	public static final PasswordExpireSettings DEFAULT = EXPIRE_90_DAYS;
	
	private final int expireDays;
	
	private PasswordExpireSettings(final int days) {
		this.expireDays = days;
	}
	
	public final int getExpireDays() {
		return this.expireDays;
	}
	
	public static final Optional<PasswordExpireSettings> findByDays(final int days) {
		for (final PasswordExpireSettings settings : values()) {
			if (settings.expireDays == days) {
				return Optional.of(settings);
			}
		}
		
		return Optional.empty();
	}
}
