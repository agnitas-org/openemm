/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.beans;

import java.util.Map;

/**
 * Bean that stores mailing link-click statistics
 */
public interface ClickStatInfo {

	/**
	 * Add link statistics info
	 *
	 * @param urlId		id of link url in database
	 * @param clickNum	 number of clicks for link
	 * @param clickPercent percent value of link clicks
	 */
	public void addURLInfo(int urlId, int clickNum, int clicksOverall, double clickPercent);

	/**
	 * Get links click info
	 *
	 * @return map containing click-info (Map structure: urlId -> clicks number)
	 */
	public Map<Integer, Integer> getClicks();

	/**
	 * Set links click info
	 *
	 * @param clicks click map (Map structure: urlId -> clicks number)
	 */
	public void setClicks(Map<Integer, Integer> clicks);

	/**
	 * Get links clicks overall info
	 *
	 * @return map containing click-info (Map structure: urlId -> clicksOverall number)
	 */
	public Map<Integer, Integer> getClicksOverall();

	/**
	 * Set links clicks overall info
	 *
	 * @param clicksOverall click map (Map structure: urlId -> clicksOverall number)
	 */
	public void setClicksOverall(Map<Integer, Integer> clicksOverall);

	/**
	 * Get clicks percentage
	 *
	 * @return click percentage map (Map structure: urlId -> clicks percentage)
	 */
	public Map<Integer, Double> getPercentClicks();

	/**
	 * Get clicks percentage
	 *
	 * @param percentClicks click percentage map (Map structure: urlId -> clicks percentage)
	 */
	public void setPercentClicks(Map<Integer, Double> percentClicks);
}
