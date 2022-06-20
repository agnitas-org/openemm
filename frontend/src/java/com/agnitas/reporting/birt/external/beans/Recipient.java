/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import com.agnitas.messages.I18nString;

public class Recipient {
	
	
	//Geschlecht des Empfängers: 0 = Herr, 1 = Frau, 2 = Unbekannt, 3 = ??
	private final static int MALE = 0;
	private final static int FEMALE = 1;
		
	private String name;
	private String lastname;
	private String email;
	private String salutation = " ";
	private int gender;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSalutation() {
		return salutation;
	}
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	
	public void setGender(int gender,String language) {
		setGender(gender);
		if (gender == MALE ) {
			setSalutation(I18nString.getLocaleString("recipient.gender.0.short", language).trim());
		}
		if (gender == FEMALE ) {
			setSalutation(I18nString.getLocaleString("recipient.gender.1.short", language).trim());
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getSalutation() );
		buffer.append(" ");
		buffer.append(getName());
		buffer.append(" ");
		buffer.append(getLastname());
		buffer.append(" ");
		buffer.append(getEmail());		
		return buffer.toString();
	}
}
