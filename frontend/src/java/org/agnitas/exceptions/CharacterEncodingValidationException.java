/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.exceptions;

import java.util.Set;

public class CharacterEncodingValidationException extends Exception {
	private static final long serialVersionUID = 6720694281684517713L;
	
	private final boolean subjectValid;
	private final Set<String> failedMailingComponents;
	private final Set<String> failedDynamicTags;
	
	public CharacterEncodingValidationException( boolean subjectValid, Set<String> failedMailingComponents, Set<String> failedDynamicTags) {
		this.subjectValid = subjectValid;
		this.failedMailingComponents = failedMailingComponents;
		this.failedDynamicTags = failedDynamicTags;
	}

	public CharacterEncodingValidationException( Set<String> failedMailingComponents, Set<String> failedDynamicTags) {
		this( true, failedMailingComponents, failedDynamicTags);
	}

	public Set<String> getFailedMailingComponents() {
		return this.failedMailingComponents;
	}
	
	public Set<String> getFailedDynamicTags() {
		return this.failedDynamicTags;
	}
	
	public boolean isSubjectValid() {
		return this.subjectValid;
	}
}
