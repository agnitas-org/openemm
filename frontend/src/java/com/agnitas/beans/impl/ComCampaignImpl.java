/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import com.agnitas.beans.ComCampaign;
import com.agnitas.beans.ComCampaignStats;

public class ComCampaignImpl extends org.agnitas.beans.impl.CampaignImpl implements ComCampaign {
	protected ComCampaignStatsImpl campaignStats = null;
	
    private String csvfile="";

    @Override
	public String getCsvfile() {
        return csvfile;
    }

	@Override
	public void setCsvfile(String csvfile) {
		this.csvfile = csvfile;
	}

	// returns the ComCampaignStats with lazy creation.
	@Override
	public ComCampaignStats getComCampaignStats() {
		if (campaignStats == null) {
			campaignStats = new ComCampaignStatsImpl();
		}
		return campaignStats;
	}
}
