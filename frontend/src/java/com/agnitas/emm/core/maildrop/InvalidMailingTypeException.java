/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop;

import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;

public class InvalidMailingTypeException extends MaildropException {
	private static final long serialVersionUID = -7174940246835932605L;
	
	private final MailingType expected;
	private final MailingType current;
	
	public InvalidMailingTypeException(final MailingType expected, final MailingType current) {
		super(String.format("Invalid mailing type (expected: %s, current: %s)", expected, current));
		
		this.expected = expected;
		this.current = current;
	}
	
	public final MailingType getExpectedMailingType() {
		return this.expected;
	}
	
	public final MailingType getCurrentMailingType() {
		return this.current;
	}
}
