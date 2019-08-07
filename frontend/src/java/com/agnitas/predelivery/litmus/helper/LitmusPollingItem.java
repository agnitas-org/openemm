/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.predelivery.litmus.helper;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Helper class for information required to poll a litmus test
 */
public class LitmusPollingItem {
	private int litmusTestID;
	private int mailingID;
	private int companyID;
	/**
	 * @param litmusTestID the litmusTestID to set
	 */
	public void setLitmusTestID(int litmusTestID) {
		this.litmusTestID = litmusTestID;
	}
	/**
	 * @return the litmusTestID
	 */
	public int getLitmusTestID() {
		return litmusTestID;
	}
	/**
	 * @param mailingID the mailingID to set
	 */
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}
	/**
	 * @return the mailingID
	 */
	public int getMailingID() {
		return mailingID;
	}
	/**
	 * @param companyID the companyID to set
	 */
	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}
	/**
	 * @return the companyID
	 */
	public int getCompanyID() {
		return companyID;
	}
	
}
