/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.bean;

public final class LoginTrackSettings {

	private final int observationTimeSeconds;
	private final int maxFailedLogins;
	private final int lockTimeSeconds;
	
	public LoginTrackSettings(final int observationTimeSeconds, final int maxFailedLogins, final int lockTimeSeconds) {
		this.observationTimeSeconds = observationTimeSeconds;
		this.maxFailedLogins = maxFailedLogins;
		this.lockTimeSeconds = lockTimeSeconds;
	}

	public int getObservationTimeSeconds() {
		return observationTimeSeconds;
	}

	public int getMaxFailedLogins() {
		return maxFailedLogins;
	}

	public int getLockTimeSeconds() {
		return lockTimeSeconds;
	}
}
