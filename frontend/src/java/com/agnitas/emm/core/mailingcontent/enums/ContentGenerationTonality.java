/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.enums;

public enum ContentGenerationTonality {

    NONE(null),
    DYNAMIC("mailing.ai.prompt.tonality.dynamic"),
    MODERN("mailing.ai.prompt.tonality.modern"),
    FACTUAL("mailing.ai.prompt.tonality.factual"),
    RELAXED("mailing.ai.prompt.tonality.relaxed"),
    COMMITTED("mailing.ai.prompt.tonality.committed"),
    SERIOUS("mailing.ai.prompt.tonality.serious"),
    FUN("mailing.ai.prompt.tonality.fun");

	private String messageKey;

	public String getMessageKey() {
		return messageKey;
	}
	
	ContentGenerationTonality(String messageKey) {
		this.messageKey = messageKey;
	}
}
