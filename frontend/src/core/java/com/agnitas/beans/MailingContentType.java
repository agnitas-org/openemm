/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.agnitas.emm.common.MailingType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public enum MailingContentType {
	advertising,
	transaction;
	
	public static MailingContentType getFromString(String mailingContentTypeString) {
		for (MailingContentType value : MailingContentType.values()) {
			if (value.name().equalsIgnoreCase(mailingContentTypeString)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid MailingContentType: " + mailingContentTypeString);
	}

	public static class NameDeserializer extends JsonDeserializer<MailingContentType> {
		@Override
		public MailingContentType deserialize(
				JsonParser p,
				DeserializationContext ctxt
		) throws IOException {
			String value = p.getText();
			if (value == null) {
				return null;
			}
			try {
				return getFromString(value);
			} catch (Exception e) {
				throw InvalidFormatException.from(
						p,
						"Invalid MailingType code. Allowed values: " + getAllowedValuesStr(),
						value,
						MailingType.class
				);
			}
		}

		private static String getAllowedValuesStr() {
			return Arrays.stream(MailingContentType.values())
					.map(MailingContentType::name)
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}
}
