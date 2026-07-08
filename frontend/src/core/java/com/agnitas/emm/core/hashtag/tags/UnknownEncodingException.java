/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import com.agnitas.emm.core.commons.encoder.EncodingException;

public class UnknownEncodingException extends EncodingException {

	/** Serial version UID. */
	private static final long serialVersionUID = -6130543721290408361L;
	
	private final String encodingName;
	
	public UnknownEncodingException(final String name) {
		super(String.format("Unknown encoding '%s'", name));
		
		this.encodingName = name;
	}
	
	public String getEncodingName() {
		return this.encodingName;
	}
}
