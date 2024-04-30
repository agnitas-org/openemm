/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.agnitas.dao.exception.UnknownUserStatusException;

public enum UserStatus {
	Active(1, "active"),
	Bounce(2, "bounce"),
	AdminOut(3, "opt_out"),	// Same webhook identifier as UserOut. Distinguished in webhook messge by a separate flag
	UserOut(4, "out_out"),	// Same webhook identifier as AdminOut. Distinguished in webhook messge by a separate flag
	WaitForConfirm(5, "wait_for_confirm"),
	Blacklisted(6, "blacklisted"),
	Suspend(7, "suspended"); // Sometimes also referred to as status "supended" or "pending"
	
	/*
	 *  Status 7 is used for single test delivery, when
	 *  - a customer has been created by entering an unknown mail address or
	 *  - when a mail address of an existing customer is entered, but the customer
	 *    doesn't have a binding on the mailinglist of the mailing
	 */
	
	private int statusCode;
	
	/** Identifier used in webhook messages. */
	private final String webhookIdentifier;
	
	UserStatus(int statusCode, final String webhookIdentifier) {
		this.statusCode = statusCode;
		this.webhookIdentifier = Objects.requireNonNull(webhookIdentifier);
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public static UserStatus getUserStatusByID(int id) throws UnknownUserStatusException {
		for (UserStatus userStatus : UserStatus.values()) {
			if (userStatus.statusCode == id) {
				return userStatus;
			}
		}
		throw new UnknownUserStatusException(id);
	}
	
	public static UserStatus getUserStatusByName(String name) {
		for (UserStatus userStatus : UserStatus.values()) {
			if (userStatus.name().equalsIgnoreCase(name)) {
				return userStatus;
			}
		}
		return null;
	}
	
	/**
	 * @see #getAvailableStatusCodes()
	 */
	@Deprecated
	public static List<Integer> getAvailableStatusCodeList() {
		return Arrays.stream(UserStatus.values()).map(UserStatus::getStatusCode).collect(Collectors.toList());
	}
	
	public static List<UserStatus> getAvailableStatusCodes() {
		return Arrays.asList(UserStatus.values());
	}

	/**
	 * Returns the identifier used in webhook messages.
	 * 
	 * @return identifier used in webhook messages
	 */
	public final String getWebhookIdentifier() {
		return this.webhookIdentifier;
	}
}
