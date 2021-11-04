/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

public class ProfileImportCsvException extends Exception {
	private static final long serialVersionUID = -6519878605641730345L;

	public static enum ReasonCode {
		MissingMandatory("error.mandatory.missing"),
		InvalidEmail("error.email.invalid"),
		InvalidMailtype("error.mailtype.invalid"),
		InvalidGender("GWUA.error.gender.invalid.short"),
		InvalidDate("error.date.invalid"),
		InvalidNumber("error.number.invalid"),
		InvalidEncryption("error.encryption.invalid"),
		ValueTooLarge("error.value.toolarge"),
		NumberTooLarge("error.value.numbertoolarge"),
		Unknown("error.value.unknownReason");
		
		private String messageKey;
		
		ReasonCode(String messageKey) {
			this.messageKey = messageKey;
		}

		public static ReasonCode getFromString(String reasonString) {
			for (ReasonCode reasonCode : ReasonCode.values()) {
				if (reasonCode.toString().equalsIgnoreCase(reasonString)) {
					return reasonCode;
				}
			}
			return null;
		}

		public String getMessageKey() {
			return messageKey;
		}
	}

	private ReasonCode reasonCode;
	private String csvFieldName;

	public ReasonCode getReasonCode() {
		return reasonCode;
	}

	public String getCsvFieldName() {
		return csvFieldName;
	}
	
	public ProfileImportCsvException(ReasonCode reasonCode, String csvFieldName, String message, Throwable e) {
		super(message, e);
		
		this.reasonCode = reasonCode;
		this.csvFieldName = csvFieldName;
	}
	
	public ProfileImportCsvException(ReasonCode reasonCode, String csvFieldName, String message) {
		super(message);
		
		this.reasonCode = reasonCode;
		this.csvFieldName = csvFieldName;
	}
}
