/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class RecipientDetailRow {
	private String mydate;
	
	private int active;
	private int bounced;
	private int allout;
	private int blacklisted;
	private int doubleOptIn;
	
	private int maxActive;
	private int maxBounced;
	private int maxOut;
	private int maxBlacklisted;
	private int maxDoubleOptIn;
	
	public RecipientDetailRow() {
	}
	
	public RecipientDetailRow(String mydate) {
		this.mydate = mydate;
	}
	
	public String getMydate() {
		return mydate;
	}

	public void setMydate(String mydate) {
		this.mydate = mydate;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getBounced() {
		return bounced;
	}

	public void setBounced(int bounced) {
		this.bounced = bounced;
	}

	public int getAllout() {
		return allout;
	}

	public void setAllout(int allout) {
		this.allout = allout;
	}

	public int getBlacklisted() {
		return blacklisted;
	}

	public void setBlacklisted(int blacklisted) {
		this.blacklisted = blacklisted;
	}

	public int getDoubleOptIn() {
		return doubleOptIn;
	}

	public void setDoubleOptIn(int doubleOptIn) {
		this.doubleOptIn = doubleOptIn;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxBounced() {
		return maxBounced;
	}

	public void setMaxBounced(int maxBounced) {
		this.maxBounced = maxBounced;
	}

	public int getMaxOut() {
		return maxOut;
	}

	public void setMaxOut(int maxOut) {
		this.maxOut = maxOut;
	}

	public int getMaxBlacklisted() {
		return maxBlacklisted;
	}

	public void setMaxBlacklisted(int maxBlacklisted) {
		this.maxBlacklisted = maxBlacklisted;
	}

	public int getMaxDoubleOptIn() {
		return maxDoubleOptIn;
	}

	public void setMaxDoubleOptIn(int maxDoubleOptIn) {
		this.maxDoubleOptIn = maxDoubleOptIn;
	}
	
	public void setRecipientMaxData(RecipientMaxValues commonData) {
		setMaxActive(commonData.getMaxActive());
		setMaxBounced(commonData.getMaxBounced());
		setMaxDoubleOptIn(commonData.getMaxDoubleOptIn());
		setMaxBlacklisted(commonData.getMaxBlacklisted());
		setMaxOut(commonData.getMaxAllOut());
		
	}
}
