/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

public class SubscriberLimitCheckResult {
	private int currentNumberOfCustomers;
	private int maximumNumberOfCustomers = -1;
	private int gracefulLimitExtension = -1;
	private boolean isWithinGraceLimitation = false;
	
	public int getCurrentNumberOfCustomers() {
		return currentNumberOfCustomers;
	}

	public int getMaximumNumberOfCustomers() {
		return maximumNumberOfCustomers;
	}

	public int getGracefulLimitExtension() {
		return gracefulLimitExtension;
	}

	public boolean isWithinGraceLimitation() {
		return isWithinGraceLimitation;
	}
	
	public SubscriberLimitCheckResult(int currentNumberOfCustomers, int maximumNumberOfCustomers, int gracefulLimitExtension, boolean isWithinGraceLimitation) {
		this.currentNumberOfCustomers = currentNumberOfCustomers;
		this.maximumNumberOfCustomers = maximumNumberOfCustomers;
		this.gracefulLimitExtension = gracefulLimitExtension;
		this.isWithinGraceLimitation = isWithinGraceLimitation;
	}
}
