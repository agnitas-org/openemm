/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Base class of exception indicating an errors in voucher code feature.
 */
public class VoucherException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 8520500229769039817L;

	/**
	 * Instantiates a new voucher exception.
	 */
	public VoucherException() {
		// Nothing to do
	}

	/**
	 * Instantiates a new voucher exception.
	 *
	 * @param message the message
	 */
	public VoucherException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new voucher exception.
	 *
	 * @param cause the cause
	 */
	public VoucherException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new voucher exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public VoucherException(String message, Throwable cause) {
		super(message, cause);
	}

}
