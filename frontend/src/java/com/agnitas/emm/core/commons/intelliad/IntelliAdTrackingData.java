/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

/**
 * Tracking data for IntelliAd.
 */
public class IntelliAdTrackingData {
	
	/*
	 * A note about IDs:
	 * 
	 * The IDs are pure numeric, but can exceed the range of ints and longs.
	 * So, I was forced to use type String here.
	 */

	/** IntelliAd customer ID. */
	private final String customerId;

	/** IntelliAd market ID. */
	private final String marketId;

	/** IntelliAd channel ID. */
	private final String channelId;

	/** IntelliAd campaign ID. */
	private final String campaignId;

	/** IntelliAd adgroup ID. */
	private final String adgroupId;

	/** IntelliAd criterion ID. */
	private final String criterionId;
	
	/**
	 * Creates new IntelliAd tracking data.
	 * 
	 * @param customerId IntelliAd customer ID
	 * @param marketId IntelliAd market ID
	 * @param channelId IntelliAd channel ID
	 * @param campaignId IntelliAd campaign ID
	 * @param adgroupId IntelliAd adgroup ID
	 * @param criterionId IntelliAd criterion ID
	 */
	public IntelliAdTrackingData( String customerId, String marketId, String channelId, String campaignId, String adgroupId, String criterionId) {
		this.customerId = customerId;
		this.marketId = marketId;
		this.channelId = channelId;
		this.campaignId = campaignId;
		this.adgroupId = adgroupId;
		this.criterionId = criterionId;
	}

	/**
	 * Returns IntelliAd customer ID.
	 * 
	 * @return IntelliAd customer ID.
	 */
	public String getCustomerId() {
		return customerId;
	}

	/**
	 * Returns IntelliAd market ID.
	 * 
	 * @return IntelliAd market ID.
	 */
	public String getMarketId() {
		return marketId;
	}

	/**
	 * Returns IntelliAd channel ID.
	 * 
	 * @return IntelliAd channel ID.
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * Returns IntelliAd campaign ID.
	 * 
	 * @return IntelliAd campaign ID.
	 */
	public String getCampaignId() {
		return campaignId;
	}

	/**
	 * Returns IntelliAd adgroup ID.
	 * 
	 * @return IntelliAd adgroup ID.
	 */
	public String getAdgroupId() {
		return adgroupId;
	}

	/**
	 * Returns IntelliAd criterion ID.
	 * 
	 * @return IntelliAd criterion ID.
	 */
	public String getCriterionId() {
		return criterionId;
	}

}
