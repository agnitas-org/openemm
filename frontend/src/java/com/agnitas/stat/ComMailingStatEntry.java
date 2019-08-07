/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.stat;

import java.util.Map;

import org.agnitas.stat.URLStatEntry;

public interface ComMailingStatEntry {
	/**
	 * Getter for property bounces.
	 * 
	 * @return Value of property bounces.
	 */
	int getBounces();

	/**
	 * Getter for property clickStatsValues.
	 * 
	 * @return Value of property clickStatsValues.
	 */
	Map<Integer, URLStatEntry> getClickStatValues();

	/**
	 * Getter for property opened.
	 * 
	 * @return Value of property opened.
	 */
	int getOpened();

	/**
	 * Getter for property optouts.
	 * 
	 * @return Value of property optouts.
	 */
	int getOptouts();

	/**
	 * Getter for property targetName.
	 * 
	 * @return Value of property targetName.
	 */
	String getTargetName();

	/**
	 * Getter for property totalClickSubscribers.
	 * 
	 * @return Value of property totalClickSubscribers.
	 */
	int getTotalClickSubscribers();

	/**
	 * Getter for property totalClicks.
	 * 
	 * @return Value of property totalClicks.
	 */
	int getTotalClicks();

	/**
	 * Getter for property totalClicksNetto.
	 * 
	 * @return Value of property totalClicksNetto.
	 */
	int getTotalClicksNetto();

	/**
	 * Getter for property totalMails.
	 * 
	 * @return Value of property totalMails.
	 */
	int getTotalMails();

	/**
	 * Setter for property bounces.
	 * 
	 * @param bounces
	 *            New value of property bounces.
	 */
	void setBounces(int bounces);

	/**
	 * Setter for property clickStatValues.
	 * 
	 * @param clickStatValues
	 *            New value of property clickStatValues.
	 */
	void setClickStatValues(Map<Integer, URLStatEntry> clickStatValues);

	/**
	 * Setter for property opnened.
	 *
	 * @param opened
	 *            New value of property opened.
	 */
	void setOpened(int opened);

	/**
	 * Setter for property optouts.
	 *
	 * @param optouts
	 *            New value of property optouts.
	 */
	void setOptouts(int optouts);

	/**
	 * Setter for property targetName.
	 * 
	 * @param targetName
	 *            New value of property targetName.
	 */
	void setTargetName(String targetName);

	/**
	 * Setter for property totalClickSubscribers.
	 *
	 * @param totalClickSubscribers
	 *            New value of property totalClickSubscribers.
	 */
	void setTotalClickSubscribers(int totalClickSubscribers);

	/**
	 * Setter for property totalClicks.
	 *
	 * @param totalClicks
	 *            New value of property totalClicks.
	 */
	void setTotalClicks(int totalClicks);

	/**
	 * Setter for property totalClicksNetto.
	 *
	 * @param totalClicksNetto
	 *            New value of property totlClicksNetto.
	 */
	void setTotalClicksNetto(int totalClicksNetto);

	/**
	 * Setter for property totalMails.
	 *
	 * @param totalMails
	 *            New value of property totalMails.
	 */
	void setTotalMails(int totalMails);

	Map<String, URLStatEntry> getDeeptrackStatValues();

    void setDeeptrackStatValues(Map<String, URLStatEntry> deeptrackStatValues);

    /**
     * Getter for property deepAlphaValues.
     * @return Value of property deepAlphaValues.
     */
    Map<String, ComMailingDeepStatEntry> getDeepAlphaValues();

    /**
     * Setter for property deepAlphaValues.
     * @param deepAlphaValues New value of property deepAlphaValues.
     */
    void setDeepAlphaValues(Map<String, ComMailingDeepStatEntry> deepAlphaValues);

    /**
     * Getter for property deepNumValues.
     * @return Value of property deepNumValues.
     */
    Map<String, ComMailingDeepStatEntry> getDeepNumValues();

    /**
     * Setter for property deepNumValues.
     * @param deepNumValues New value of property deepNumValues.
     */
    void setDeepNumValues(Map<String, ComMailingDeepStatEntry> deepNumValues);

    double getRevenue();

    void setRevenue(double revenue);
}
