/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.AgnDBTagError;

public class AgnDBTagErrorImpl implements AgnDBTagError {

	private String invalidTag;
	private String errorDescription;
	
	public AgnDBTagErrorImpl(String invalidTag, String errorDescription) {
		this.invalidTag = invalidTag;
		this.errorDescription = errorDescription;
	}

	@Override
	public String getInvalidTag() {
		return invalidTag;
	}
	public void setInvalidTag(String invalidTag) {
		this.invalidTag = invalidTag;
	}
	@Override
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	 
	
}
