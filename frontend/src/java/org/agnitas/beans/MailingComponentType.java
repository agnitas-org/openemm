/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

public enum MailingComponentType {
	Template(0),
	Image(1), // Image hosted by some foreign server
	Attachment(3),
	PersonalizedAttachment(4),
	HostedImage(5), // Image hosted on by the EMM systems
	Font(6),
	PrecAAttachement(7), // Precoded Attachment encoded in base64 (for eABO and others)
	ThumbnailImage(8);
	
	private final int code;
	
	private MailingComponentType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static MailingComponentType getMailingComponentTypeByCode(int code) throws Exception {
		for (MailingComponentType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		throw new Exception("Invalid MailingComponentType code: " + code);
	}
	
	public static MailingComponentType getMailingComponentTypeByName(String name) throws Exception {
		for (MailingComponentType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		throw new Exception("Invalid MailingComponentType name: " + name);
	}
}
