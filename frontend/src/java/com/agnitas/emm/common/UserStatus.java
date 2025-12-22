/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.exception.InvalidUserStatusException;

public enum UserStatus {

	Active(1, "active", "recipient.MailingState1"),
	Bounce(2, "bounce", "recipient.MailingState2"),
	AdminOut(3, "opt_out", "recipient.OptOutAdmin"),	// Same webhook identifier as UserOut. Distinguished in webhook messge by a separate flag
	UserOut(4, "opt_out", "recipient.OptOutUser"),	// Same webhook identifier as AdminOut. Distinguished in webhook messge by a separate flag
	WaitForConfirm(5, "wait_for_confirm", "recipient.MailingState5"), // Sometimes also referred as status "pending"
	Blacklisted(6, "blacklisted", "recipient.MailingState6"),
	Suspend(7, "suspended", "recipient.MailingState7"); // Sometimes also referred to as status "supended"
	
	/*
	 *  Status 7 is used for single test delivery, when
	 *  - a customer has been created by entering an unknown mail address or
	 *  - when a mail address of an existing customer is entered, but the customer
	 *    doesn't have a binding on the mailinglist of the mailing
	 */
	
	private final int statusCode;
	/** Identifier used in webhook messages. */
	private final String webhookIdentifier;
	private final String messageKey;
	
	UserStatus(int statusCode, final String webhookIdentifier, final String messageKey) {
		this.statusCode = statusCode;
		this.webhookIdentifier = Objects.requireNonNull(webhookIdentifier);
		this.messageKey = messageKey;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public static Optional<UserStatus> findByCode(int code) {
		return Stream.of(UserStatus.values())
				.filter(s -> s.statusCode == code)
				.findFirst();
	}

	public static UserStatus getByCode(int code) {
		return findByCode(code)
				.orElseThrow(() -> new InvalidUserStatusException(code));
    }

	public static boolean existsWithId(int id) {
        return findByCode(id).isPresent();
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
