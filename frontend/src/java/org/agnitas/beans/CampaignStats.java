/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Map;

import org.agnitas.stat.CampaignStatEntry;

public interface CampaignStats {
	int getBounces();
    int getClicks();
    int getOpened();
    int getOptouts();
    int getSubscribers();
	Map<Integer, CampaignStatEntry> getMailingData();
    int getMaxBounces();
    int getMaxClicks();
    int getMaxOpened();
    int getMaxOptouts();
    int getMaxSubscribers();

    void setMaxBounces(int maxBounces);
	void setMaxClicks(int maxClicks);
	void setMaxOpened(int maxOpened);
	void setMaxOptouts(int maxOptouts);
	void setMaxSubscribers(int maxSubscribers);
    void setSubscribers(int subscribers);
    void setBounces(int bounces);
    void setOptouts(int optouts);
    void setOpened(int opened);
    void setClicks(int clicks);

    void setMaxClickRate(double maxClickRate);
    void setMaxOpenRate(double maxOpenRate);
}
