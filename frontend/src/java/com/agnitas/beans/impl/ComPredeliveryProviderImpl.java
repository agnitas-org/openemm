/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import com.agnitas.beans.ComPredeliveryProvider;

public class ComPredeliveryProviderImpl implements ComPredeliveryProvider {
	private static final long serialVersionUID = 4864827591431532987L;

	/** Holds value of property providerID. */
	private String	providerID;

	/** Holds value of property email. */
	private String	email;

	/** Holds value of property active. */
	private boolean	active;

	/** Holds value of property received. */
	private java.util.Date	received;

	/** Holds value of property spam. */
	private boolean	spam;

	@Override
	public String	getProviderID() {
		return providerID;
	}

	@Override
	public void	setProviderID(String id) {
		providerID = id;
	}

	@Override
	public String	getEmail() {
		return email;
	}

	@Override
	public void	setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean	getActive() {
		return active;
	}

	@Override
	public void	setActive(boolean active) {
		this.active = active;
	}

	@Override
	public java.util.Date	getReceived() {
		return received;
	}

	@Override
	public void	setReceived(java.util.Date received) {
		this.received = received;
	}

	@Override
	public boolean	getSpam() {
		return spam;
	}

	@Override
	public void	setSpam(boolean spam) {
		this.spam = spam;
	}
}
