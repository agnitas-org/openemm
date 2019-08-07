/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat.impl;

import org.agnitas.stat.CampaignStatEntry;

public class CampaignStatEntryImpl implements CampaignStatEntry {
    private static final long serialVersionUID = -4863870455420608269L;
	protected String name;        
    protected String shortname;
    protected int clicks;
    protected int opened;
    protected int optouts;
    protected int bounces;
    protected int totalMails;
    protected double revenue;
        
    /**
     * Holds value of property openRate.
     */
    protected double openRate;
    
    /**
     * Holds value of property clickRate.
     */
    protected double clickRate;
    
    public CampaignStatEntryImpl() {
        name=" ";
        shortname=" ";
        clicks=0;
        opened=0;
        optouts=0;
        bounces=0;
        totalMails=0;
    }

    @Override
	public String getShortname() {
        return shortname;
    }
    
    @Override
	public int getClicks() {
        return clicks;
    }
    
    @Override
	public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    @Override
	public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    @Override
	public int getOpened() {
        return opened;
    }

    @Override
	public void setOpened(int opened) {
        this.opened = opened;
    }

    @Override
	public int getOptouts() {
        return optouts;
    }

    @Override
	public void setOptouts(int optouts) {
        this.optouts = optouts;
    }

    @Override
	public int getBounces() {
        return bounces;
    }

    @Override
	public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    @Override
	public int getTotalMails() {
        return totalMails;
    }

    @Override
	public void setTotalMails(int totalMails) {
        this.totalMails = totalMails;
    }

    /**
     * Getter for property openRate.
     * @return Value of property openRate.
     */
    @Override
	public double getOpenRate() {
        return openRate;
    }

    /**
     * Setter for property openRate.
     * @param openRate New value of property openRate.
     */
    @Override
	public void setOpenRate(double openRate) {
        this.openRate = openRate;
    }

    /**
     * Getter for property clickRate.
     * @return Value of property clickRate.
     */
    @Override
	public double getClickRate() {
        return clickRate;
    }

    /**
     * Setter for property clickRate.
     * @param clickRate New value of property clickRate.
     */
    @Override
	public void setClickRate(double clickRate) {
        this.clickRate = clickRate;
    }

    /**
     * Getter for property revenue.
     * @return Value of property revenue.
     */
    @Override
	public double getRevenue() {
        return revenue;
    }

    /**
     * Setter for property revenue.
     * @param revenue New value of property revenue.
     */
    @Override
	public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
