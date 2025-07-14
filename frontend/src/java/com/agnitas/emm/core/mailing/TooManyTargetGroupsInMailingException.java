/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing;

public class TooManyTargetGroupsInMailingException extends MailingDataException {
	private static final long serialVersionUID = -2559779296771712210L;
	
	private final int mailingID;
	
	public TooManyTargetGroupsInMailingException(final int mailingID) {
		super(String.format("Mailing %d has too many target groups", mailingID));
		
		this.mailingID = mailingID;
	}
	
	public int getMailingID() {
		return this.mailingID;
	}
}
