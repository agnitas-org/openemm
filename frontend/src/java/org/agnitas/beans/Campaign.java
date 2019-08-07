/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;


public interface Campaign {
	
//	public interface Stats {
//    	public int getBounces();
//        public int getClicks();
//        public int getOpened();
//        public int getOptouts();
//        public int getSubscribers();
//    	public Hashtable getMailingData();
//        public int getMaxBounces();
//        public int getMaxClicks();
//        public int getMaxOpened();
//        public int getMaxOptouts();
//        public int getMaxSubscribers();
//
//        public void setMaxClickRate(double maxClickRate);
//        public void setMaxOpenRate(double maxOpenRate);
//    };

    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    int getCompanyID();
    
    /**
     * Getter for property shortname.
     * 
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    String getDescription();

    /**
     * Setter for property campaignID.
     *
     * @param id New value of property campaignID.
     */
    void setId(int id);

    /**
     * Setter for property companyID.
     * 
     * @param companyID New value of property companyID.
     */
    void setCompanyID( @VelocityCheck int companyID);
    
    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    void setDescription(String description);
    
    /** Getter for property netto.
     * @return Value of property netto.
     *
     */
    public boolean isNetto();
    
    
    /** Setter for property netto.
     * @param netto New value of property netto.
     *
     */
    public void setNetto(boolean netto);
    
    public String getCsvfile();
    
    public void setCsvfile(String csvfile);

    public CampaignStats getCampaignStats();
        
}
