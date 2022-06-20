/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat;

import java.io.Serializable;

public interface CampaignStatEntry extends Serializable {
   
     /**
     * Getter for property bounces.
     * 
     * @return Value of property bounces.
     */
    int getBounces();

    /**
     * Getter for property clickRate.
     * 
     * @return Value of property clickRate.
     */
    double getClickRate();

    /**
     * Getter for property clicks.
     * 
     * @return Value of property clicks.
     */
    int getClicks();

    /**
     * Getter for property name.
     * 
     * @return Value of property mane.
     */
    String getName();

    /**
     * Getter for property openRate.
     * 
     * @return Value of property openRate.
     */
    double getOpenRate();

    /**
     * Getter for property opened.
     * 
     * @return Value of property opened.
     */
    int getOpened();

    /**
     * Getter for property optOuts.
     * 
     * @return Value of property optOuts.
     */
    int getOptouts();

    /**
     * Getter for property shortname.
     * 
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property totalMails.
     * 
     * @return Value of property totalMails.
     */
    int getTotalMails();

    /**
     * Getter for property revenue.
     *
     * @return Value of property revenue.
     */
    double getRevenue();

    /**
     * Setter for property bounces.
     * 
     * @param bounces New value of property bounces.
     */
    void setBounces(int bounces);

    /**
     * Setter for property clickRate.
     * 
     * @param clickRate New value of property clickRate.
     */
    void setClickRate(double clickRate);

    /**
     * Setter for property clicks.
     * 
     * @param clicks New value of property clicks.
     */
    void setClicks(int clicks);

    /**
     * Setter for property name.
     * 
     * @param name New value of property name.
     */
    void setName(String name);

    /**
     * Setter for property openRate.
     * 
     * @param openRate New value of property openRate.
     */
    void setOpenRate(double openRate);

    /**
     * Setter for property opened.
     * 
     * @param opened New value of property opened.
     */
    void setOpened(int opened);

    /**
     * Setter for property optouts.
     * 
     * @param optouts New value of property optouts.
     */
    void setOptouts(int optouts);

    /**
     * Setter for property shortname.
     * 
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property totalMails.
     * 
     * @param totalMails New value of property totalMails.
     */
    void setTotalMails(int totalMails);

    /**
     * Setter for property revenue.
     *
     * @param revenue New value of property revenue.
     */
    void setRevenue(double revenue);
    
}
