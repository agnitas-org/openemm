/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.backend.beans;

import java.util.HashMap;
import java.util.Map;

public class ClickStatInfo {

    private Map<Integer, Integer> clicks;

    private Map<Integer, Integer> clicksOverall;

    private Map<Integer, Double> percentClicks;

    private Map<String, Integer> clicksPerPosition;

    private Map<String, Double> percentClicksPerPosition;

    public ClickStatInfo() {
        clicks = new HashMap<>();
        clicksOverall = new HashMap<>();
        percentClicks = new HashMap<>();
	clicksPerPosition = new HashMap<>();
	percentClicksPerPosition = new HashMap<>();
    }

    public void addURLInfo(int urlId, int position, int clickNum, int clicksOverallNum, double clickPercent) {
	if (clicks.containsKey (urlId)) {
		clicks.put (urlId, clicks.get (urlId) + clickNum);
	        percentClicks.put(urlId, percentClicks.get (urlId) + clickPercent);
	} else {
	        clicks.put(urlId, clickNum);
	        clicksOverall.put(urlId, clicksOverallNum);
	        percentClicks.put(urlId, clickPercent);
	}
	String	key = String.format ("%d-%d", urlId, position);

	clicksPerPosition.put (key, clickNum);
	percentClicksPerPosition.put (key, clickPercent);
    }

    public Map<Integer, Integer> getClicks() {
        return clicks;
    }

    public void setClicks(Map<Integer, Integer> clicks) {
        this.clicks = clicks;
    }

    public Map<Integer, Integer> getClicksOverall() {
        return clicksOverall;
    }

    public void setClicksOverall(Map<Integer, Integer> clicksOverall) {
        this.clicksOverall = clicksOverall;
    }

    public Map<Integer, Double> getPercentClicks() {
        return percentClicks;
    }

    public void setPercentClicks(Map<Integer, Double> percentClicks) {
        this.percentClicks = percentClicks;
    }
    public Map<String, Integer> getClicksPerPosition() {
	return clicksPerPosition;
    }
    public Map<String, Double> getPercentClicksPerPosition() {
	return percentClicksPerPosition;
    }
}
