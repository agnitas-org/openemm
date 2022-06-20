/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.web;

import com.agnitas.emm.core.logon.service.HostAuthenticationServiceException;

/**
 * Exception indicating an error in sending security code.
 */
public class CannotSendSecurityCodeException extends HostAuthenticationServiceException {

	/** Serial version UID. */
	private static final long serialVersionUID = 5013607063955159495L;

	/** Receiver of security code. */
	private final String receiver;
	
	/**
	 * Creates a new exception for given receiver of security code.
	 * 
	 * @param receiver receiver of security code
	 */
	public CannotSendSecurityCodeException( String receiver) {
		super( "Cannot send security code to " + receiver);
		
		this.receiver = receiver;
	}
	
	/**
	 * Creates a new exception for given receiver of security code with given
	 * cause.
	 * 
	 * @param receiver receiver of security code
	 * @param cause cause
	 */
	public CannotSendSecurityCodeException( String receiver, Throwable cause) {
		super( "Cannot send security code to " + receiver, cause);
		
		this.receiver = receiver;
	}
	
	/**
	 * Returns the receiver of the security code.
	 * 
	 * @return receiver of security code
	 */
	public String getReceiver() {
		return this.receiver;
	}
	
}
