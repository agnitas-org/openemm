/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password.util;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.commons.password.PasswordCheckHandler;
import com.agnitas.emm.core.commons.password.PolicyViolation;
import com.agnitas.emm.core.commons.password.PolicyViolationException;
import com.agnitas.messages.Message;

public final class PasswordCheckUtil {

	/**
	 * Invoke handler methods by policy violation indicators.
	 *  
	 * @param ex password exception
	 * @param handler handler
	 */
	public static final void invokeHandler(final PolicyViolationException ex, final PasswordCheckHandler handler) {
		for(final PolicyViolation violation : ex.getViolations()) {
			switch(violation) {
			case TOO_SHORT:
				handler.handlePasswordTooShort();
				break;
					
			case NO_DIGITS:
				handler.handleNoDigitsException();
				break;
				
			case NO_LOWER_CASE:
				handler.handleNoLowerCaseLettersException();
				break;
				
			case NO_UPPER_CASE:
				handler.handleNoUpperCaseLettersException();
				break;
				
			case NO_SPECIAL:
				handler.handleNoPunctuationException();
				break;
				
			case MATCHES_CURRENT_PASSWORD:
				handler.handleMatchesCurrentPassword();
				break;
				
			default:
				handler.handleGenericError();
				break;
			}
		}
	}

	public static final List<Message> policyViolationsToMessages(final PolicyViolationException exception) {
		final List<Message> messages = new ArrayList<>();
		
		final PasswordCheckHandler handler = new PasswordCheckHandler() {
			
			@Override
			public void handlePasswordTooShort() {
				messages.add(Message.of("error.password.tooShort"));
			}
			
			@Override
			public void handleNoUpperCaseLettersException() {
				messages.add(Message.of("error.password_no_uppercase_letters"));
			}
			
			@Override
			public void handleNoPunctuationException() {
				messages.add(Message.of("error.password_no_special_chars"));
			}
			
			@Override
			public void handleNoLowerCaseLettersException() {
				messages.add(Message.of("error.password_no_lowercase_letters"));
			}
			
			@Override
			public void handleNoDigitsException() {
				messages.add(Message.of("error.password_no_digits"));
			}
			
			@Override
			public void handleMatchesCurrentPassword() {
				messages.add(Message.of("error.password_must_differ"));
			}
			
			@Override
			public void handleGenericError() {
				messages.add(Message.of("error.password.general"));
			}
		};
		
		invokeHandler(exception, handler);
		
		return messages;
	}
	

}
