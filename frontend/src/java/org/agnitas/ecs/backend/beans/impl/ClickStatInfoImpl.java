/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans.impl;

import java.util.HashMap;
import java.util.Map;

import org.agnitas.ecs.backend.beans.ClickStatInfo;

/**
 * Implementation of {@link org.agnitas.ecs.backend.beans.ClickStatInfo} bean
 */
public class ClickStatInfoImpl implements ClickStatInfo {

	private Map<Integer, Integer> clicks;

	private Map<Integer, Integer> clicksOverall;

	private Map<Integer, Double> percentClicks;

	public ClickStatInfoImpl() {
		clicks = new HashMap<>();
		clicksOverall = new HashMap<>();
		percentClicks = new HashMap<>();
	}

	@Override
	public void addURLInfo(int urlId, int clickNum, int clicksOverallNum, double clickPercent) {
		clicks.put(urlId, clickNum);
		clicksOverall.put(urlId, clicksOverallNum);
		percentClicks.put(urlId, clickPercent);
	}

	@Override
	public Map<Integer, Integer> getClicks() {
		return clicks;
	}

	@Override
	public void setClicks(Map<Integer, Integer> clicks) {
		this.clicks = clicks;
	}

	@Override
	public Map<Integer, Integer> getClicksOverall() {
		return clicksOverall;
	}

	@Override
	public void setClicksOverall(Map<Integer, Integer> clicksOverall) {
		this.clicksOverall = clicksOverall;
	}

	@Override
	public Map<Integer, Double> getPercentClicks() {
		return percentClicks;
	}

	@Override
	public void setPercentClicks(Map<Integer, Double> percentClicks) {
		this.percentClicks = percentClicks;
	}
}
