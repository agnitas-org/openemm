/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import com.agnitas.emm.core.velocity.event.MessageEventHandlerImpl;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link VelocityResult}.
 */
class VelocityResultImpl implements VelocityResult {

	/** Indicates successful executing of the Velocity script. */
	private final boolean successful;
	
	/** Velocity event handler collecting error messages. */
	private final MessageEventHandlerImpl eventHandler;
	
	/**
	 * Creates a new VelocityresultImpl.
	 * 
	 * @param successful true, if executing of script was successful
	 * @param eventHandler Event handler that collected the script errors
	 */
	public VelocityResultImpl( boolean successful, MessageEventHandlerImpl eventHandler) {
		this.successful = successful;
		this.eventHandler = eventHandler;
	}
	
	@Override
	public boolean wasSuccessful() {
		return this.successful;
	}

	@Override
	public boolean hasErrors() {
		return eventHandler.getErrors() != null && !eventHandler.getErrors().isEmpty();
	}

	@Override
	public List<Message> getErrors() {
		if( hasErrors())
			return eventHandler.getErrors();
		else
			return null;
	}

	@Override
	public List<String> getErrorMessages() {
		if (!hasErrors()) {
			return null;
		}

		return eventHandler.getErrors().stream()
				.map(msg -> msg.isResolvable()
						? I18nString.getLocaleString(msg.getCode(), LocaleContextHolder.getLocale(), msg.getArguments())
						: msg.getCode())
				.collect(Collectors.toList());
	}
}
