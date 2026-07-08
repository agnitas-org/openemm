/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.beans;

public class CampaignStatEntry {

    protected String name;
    protected String shortname;
    protected int clicks;
    protected int opened;
    protected int optouts;
    protected int bounces;
    protected int totalMails;
    protected double revenue;
    protected double openRate;
    protected double clickRate;

    public CampaignStatEntry() {
        name = " ";
        shortname = " ";
        clicks = 0;
        opened = 0;
        optouts = 0;
        bounces = 0;
        totalMails = 0;
    }

    public String getShortname() {
        return shortname;
    }

    public int getClicks() {
        return clicks;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOpened() {
        return opened;
    }

    public void setOpened(int opened) {
        this.opened = opened;
    }

    public int getOptouts() {
        return optouts;
    }

    public void setOptouts(int optouts) {
        this.optouts = optouts;
    }

    public int getBounces() {
        return bounces;
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    public int getTotalMails() {
        return totalMails;
    }

    public void setTotalMails(int totalMails) {
        this.totalMails = totalMails;
    }

    public double getOpenRate() {
        return openRate;
    }

    public void setOpenRate(double openRate) {
        this.openRate = openRate;
    }

    public double getClickRate() {
        return clickRate;
    }

    public void setClickRate(double clickRate) {
        this.clickRate = clickRate;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
