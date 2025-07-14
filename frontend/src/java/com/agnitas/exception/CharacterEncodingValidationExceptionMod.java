/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.exception;

import java.util.Set;

public class CharacterEncodingValidationExceptionMod extends Exception{
	private static final long serialVersionUID = 3367244877120178236L;
	
	private final Set<EncodingError> subjectErrors;
	private final Set<EncodingError> failedMailingComponents;
	private final Set<EncodingError> failedDynamicTags;

	public CharacterEncodingValidationExceptionMod(Set<EncodingError> subjectErrors, Set<EncodingError> failedMailingComponents, Set<EncodingError> failedDynamicTags) {
		this.subjectErrors = subjectErrors;
		this.failedMailingComponents = failedMailingComponents;
		this.failedDynamicTags = failedDynamicTags;
	}

	public Set<EncodingError> getFailedMailingComponents() {
		return this.failedMailingComponents;
	}

	public Set<EncodingError> getFailedDynamicTags() {
		return this.failedDynamicTags;
	}

	public Set<EncodingError> getSubjectErrors() {
        return this.subjectErrors;
    }

}
