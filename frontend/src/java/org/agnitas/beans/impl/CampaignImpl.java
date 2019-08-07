/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.Campaign;
import org.agnitas.beans.CampaignStats;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class CampaignImpl implements Campaign {

	// hold the Stats of this Campaign.
	private CampaignStats campaignStatsImpl = null;
	
	// getter for campaignStats, creates the CampaignStat-Bean with
	// lazy creation.

    @Override
    public CampaignStats getCampaignStats() {
		if (campaignStatsImpl == null) {
			campaignStatsImpl = new CampaignStatsImpl();
		}
		return this.campaignStatsImpl;
    }


    // setter for CampaignStats.
	public void setCampaignStats(CampaignStatsImpl in_CampaignStats) {
		this.campaignStatsImpl = in_CampaignStats;
	}
    
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
    
    /** Holds value of property csvfile. */
    private String csvfile="";

    @Override
	public String getCsvfile() {
        return csvfile;
    }
    
    @Override
	public void setCsvfile(String csvfile) {
		this.csvfile = csvfile;
	}

	// CONSTRUCTORS:
    public CampaignImpl() {
        id = 0;
        companyID = 0;
    }
    
    // automatically generated
    // get & set methods:

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
        return this.shortname;
    }
    
    @Override
	public String getDescription() {
        return description;
    }

    /** Getter for property netto.
     * @return Value of property netto.
     *
     */
    @Override
	public boolean isNetto() {
        return this.netto;
    }
    
    /** Setter for property netto.
     * @param netto New value of property netto.
     *
     */
    @Override
	public void setNetto(boolean netto) {
        this.netto = netto;
    }    
    
    @Override
	public void setCompanyID( @VelocityCheck int companyID) {
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
