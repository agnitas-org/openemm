/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.enums;

import java.util.Optional;

public enum MaxAdminMailsSettings {
	
	TESTMAILS_25(25),
	TESTMAILS_50(50),
	TESTMAILS_100(100);

	public static final MaxAdminMailsSettings DEFAULT = TESTMAILS_25;
	
	private final int maxAdminMailsSettings;
	
	private MaxAdminMailsSettings(final int mails) {
		this.maxAdminMailsSettings = mails;
	}
	
	public int getMaxAdminMailsSettings() {
		return maxAdminMailsSettings;
	}

	public static final Optional<MaxAdminMailsSettings> findByMails(final int mails) {
		for (final MaxAdminMailsSettings settings : values()) {
			if (settings.maxAdminMailsSettings == mails) {
				return Optional.of(settings);
			}
		}
		return Optional.empty();
	}
}
