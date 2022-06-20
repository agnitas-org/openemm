/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

public class UnknownMailingIdFaultyCodeException extends FaultyCodeException {
	private static final long serialVersionUID = 1326905785453720362L;
	
	private final int mailingId;
	
	public UnknownMailingIdFaultyCodeException(int mailingId, CodeLocation startLocation, Throwable cause) {
		super(startLocation, "Unknown mailing ID " + mailingId, cause);
		
		this.mailingId = mailingId;
	}
	
	public int getMailingId() {
		return this.mailingId;
	}
}
