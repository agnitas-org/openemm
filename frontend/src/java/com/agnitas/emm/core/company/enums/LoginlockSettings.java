/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.enums;

import java.util.Optional;

public enum LoginlockSettings {

	LOGINLOCK_3_5(3, 5),
	LOGINLOCK_3_1(3, 1),
	LOGINLOCK_5_5(5, 5),
	LOGINLOCK_5_1(5, 1),
	LOGINLOCK_10_5(10, 5),
	LOGINLOCK_10_1(10, 1);
	
	public static final LoginlockSettings DEFAULT = LOGINLOCK_10_1;
	
	private final int maxFailedAttempts;
	private final int lockTimeMinutes;
	
	LoginlockSettings(final int maxFailedAttempts, final int lockTimeMinutes) {
		this.maxFailedAttempts = maxFailedAttempts;
		this.lockTimeMinutes = lockTimeMinutes;
	}
	
	public final int getMaxFailedAttempts() {
		return this.maxFailedAttempts;
	}
	
	public final int getLockTimeMinutes() {
		return this.lockTimeMinutes;
	}
	
	public final String getName() {
		return this.name();
	}
	
	public static final Optional<LoginlockSettings> fromSettings(final int maxFailedAttempts, final int lockTimeMinutes) {
		for(final LoginlockSettings settings : values()) {
			if(settings.maxFailedAttempts == maxFailedAttempts && settings.lockTimeMinutes == lockTimeMinutes) {
				return Optional.of(settings);
			}
		}
		
		return Optional.empty();
	}
	
	public static final Optional<LoginlockSettings> fromName(final String name) {
		for(final LoginlockSettings settings : values()) {
			if(settings.name().equals(name)) {
				return Optional.of(settings);
			}
		}
		
		return Optional.empty();
	}
}
