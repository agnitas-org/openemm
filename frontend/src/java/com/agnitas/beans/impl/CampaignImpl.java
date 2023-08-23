/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;


import com.agnitas.beans.Campaign;
import com.agnitas.beans.CampaignStats;

public class CampaignImpl implements Campaign {
	protected CampaignStats campaignStats = null;

	private String csvfile = "";

	/** Holds value of property id. */
	private int id;

	/** Holds value of property companyID. */
	private int companyID;

	/** Holds value of property netto. */
	private boolean netto;

	/** Holds value of property shortname. */
	private String shortname;

	/** Holds value of property description. */
	private String description;

	public CampaignImpl() {
		id = 0;
		companyID = 0;
	}

	/**
	 * Returns the CampaignStats with lazy creation.
	 */
	@Override
	public CampaignStats getCampaignStats() {
		if (campaignStats == null) {
			campaignStats = new CampaignStatsImpl();
		}
		return campaignStats;
	}

	@Override
	public String getCsvfile() {
		return csvfile;
	}

	@Override
	public void setCsvfile(String csvfile) {
		this.csvfile = csvfile;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Getter for property netto.
	 * 
	 * @return Value of property netto.
	 */
	@Override
	public boolean isNetto() {
		return netto;
	}

	/**
	 * Setter for property netto.
	 * 
	 * @param netto
	 *            New value of property netto.
	 */
	@Override
	public void setNetto(boolean netto) {
		this.netto = netto;
	}

	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
}
