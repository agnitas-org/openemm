/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention;

import java.util.Objects;

public final class ForbiddenTagAttributeError extends AbstractTagError {
	
	private final String attributeName;
	
	public ForbiddenTagAttributeError(final String tagName, final String attributeName) {
		super(tagName);
		
		this.attributeName = Objects.requireNonNull(attributeName, "Attribute name cannot be null");
	}

	public final String getAttributeName() {
		return this.attributeName;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof ForbiddenTagAttributeError) {
			final ForbiddenTagAttributeError error = (ForbiddenTagAttributeError) obj;
			
			return this.getTagName().equals(error.getTagName()) && this.attributeName.equals(error.attributeName);
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return this.getTagName().hashCode() + this.attributeName.hashCode();
	}

}
