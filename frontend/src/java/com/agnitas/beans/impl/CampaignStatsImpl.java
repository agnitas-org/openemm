/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.agnitas.stat.CampaignStatEntry;

import com.agnitas.beans.CampaignStats;

public class CampaignStatsImpl implements CampaignStats {

	// HashMap contains Key=MailingID, and Value=Revenue for that mailing.
	private Map<Integer, Double> revenueMap = null;
	// contains the biggest Revenues of this Campaign.
	private double biggestRevenues = 0.0;
	// contains the sum of all Revenues of this Campaign
	private double totalRevenues = 0.0;
	int clicks=0;
    int opened=0;
    int optouts=0;
    int bounces=0;
    int subscribers=0;
    Map<Integer, CampaignStatEntry> mailingData = new HashMap<>();
    int maxBounces=0;
    int maxClicks=0;
    int maxOpened=0;
    int maxOptouts=0;
    int maxSubscribers=0;
    double maxClickRate=0.0;
    double maxOpenRate=0.0;

    @Override
	public int getBounces() {
        return bounces;
    }

    @Override
	public int getClicks() {
        return clicks;
    }

    public double getMaxClickRate() {
		return maxClickRate;
	}

	public double getMaxOpenRate() {
		return maxOpenRate;
	}

	@Override
	public void setBounces(int bounces) {
		this.bounces = bounces;
	}

	@Override
	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	public void setMailingData(Map<Integer, CampaignStatEntry> mailingData) {
		this.mailingData = mailingData;
	}

	@Override
	public void setMaxBounces(int maxBounces) {
		this.maxBounces = maxBounces;
	}

	@Override
	public void setMaxClicks(int maxClicks) {
		this.maxClicks = maxClicks;
	}

	@Override
	public void setMaxOpened(int maxOpened) {
		this.maxOpened = maxOpened;
	}

	@Override
	public void setMaxOptouts(int maxOptouts) {
		this.maxOptouts = maxOptouts;
	}

	@Override
	public void setMaxSubscribers(int maxSubscribers) {
		this.maxSubscribers = maxSubscribers;
	}

	@Override
	public void setOpened(int opened) {
		this.opened = opened;
	}

	@Override
	public void setOptouts(int optouts) {
		this.optouts = optouts;
	}

	@Override
	public void setSubscribers(int subscribers) {
		this.subscribers = subscribers;
	}

	@Override
	public int getOpened() {
        return opened;
    }

    @Override
	public int getOptouts() {
        return optouts;
    }

    @Override
	public int getSubscribers() {
        return subscribers;
    }
    
    @Override
	public Map<Integer, CampaignStatEntry> getMailingData() {
        return mailingData;
    }

    @Override
	public int getMaxBounces() {
        return maxBounces;
    }

    @Override
	public int getMaxClicks() {
        return maxClicks;
    }

    @Override
	public int getMaxOpened() {
        return maxOpened;
    }

    @Override
	public int getMaxOptouts() {
        return maxOptouts;
    }

    @Override
	public int getMaxSubscribers() {
        return maxSubscribers;
    }

    @Override
	public void setMaxClickRate(double maxClickRate) {
        this.maxClickRate=maxClickRate;
    }

    @Override
	public void setMaxOpenRate(double maxOpenRate) {
        this.maxOpenRate=maxOpenRate;
    }
	@Override
	public Map<Integer, Double> getRevenues() {
		if (revenueMap == null) {
			revenueMap = new Hashtable<>();
		}
		return revenueMap;
	}

	/**
	 * sets the Revenue-Hastable.
	 * The key is the Mailing-ID and the value are the revenues.
	 */
	@Override
	public void setRevenues(Map<Integer, Double> revenues) {
		this.revenueMap = revenues;
	}
	
	/**
	 * returns the biggest Revenue for this Mailing.
	 */
	@Override
	public double getBiggestRevenue() {
		return this.biggestRevenues;
	}
	
	/**
	 * sets the biggest Revenue for this Campaign.
	 * @param in_BiggestRevenue
	 */
	@Override
	public void setBiggestRevenue(double in_BiggestRevenue) {
		this.biggestRevenues = in_BiggestRevenue;
	}
	
	/**
	 * returns the sum of all Revenues of this campaign.
	 * @return
	 */
	@Override
	public double getTotalRevenue() {
		return this.totalRevenues;
	}
	
	/**
	 * sets the sum of all Revenues of this campaign.
	 * @param in_Revenue
	 */
	@Override
	public void setTotalRevenue(double in_Revenue) {
		this.totalRevenues = in_Revenue;
	}
	
	
	
}
