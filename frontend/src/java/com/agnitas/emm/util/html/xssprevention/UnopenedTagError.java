/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention;

import com.agnitas.messages.Message;

public final class UnopenedTagError extends AbstractTagError {
	
	public UnopenedTagError(final String tagName) {
		super(tagName);
	}

	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof UnopenedTagError) {
			final UnopenedTagError error = (UnopenedTagError) obj;
			
			return this.getTagName().equals(error.getTagName());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return this.getTagName().hashCode();
	}

	@Override
	public Message toMessage() {
		return Message.of("error.html.missingStartTag", getTagName());
	}

}
