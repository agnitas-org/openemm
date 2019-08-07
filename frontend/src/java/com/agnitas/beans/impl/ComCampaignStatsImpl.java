/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Hashtable;
import java.util.Map;

import org.agnitas.beans.impl.CampaignStatsImpl;

import com.agnitas.beans.ComCampaignStats;

public class ComCampaignStatsImpl extends CampaignStatsImpl implements ComCampaignStats {

	// HashMap contains Key=MailingID, and Value=Revenue for that mailing.
	private Map<Integer, Double> revenueMap = null;
	// contains the biggest Revenues of this Campaign.
	private double biggestRevenues = 0.0;
	// contains the sum of all Revenues of this Campaign
	private double totalRevenues = 0.0;
	
	@Override
	public Map<Integer, Double> getRevenues() {
		if (revenueMap == null)
			revenueMap = new Hashtable<>();
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
