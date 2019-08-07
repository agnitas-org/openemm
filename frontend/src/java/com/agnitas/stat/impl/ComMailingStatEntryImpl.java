/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.stat.impl;

import java.util.Map;

import org.agnitas.stat.URLStatEntry;

import com.agnitas.stat.ComMailingDeepStatEntry;
import com.agnitas.stat.ComMailingStatEntry;

public class ComMailingStatEntryImpl implements ComMailingStatEntry {
	private int opened;
	private int optouts;
	private int bounces;
	private int totalMails;
	private int totalClicks;
	private int totalClicksNetto;
	private int totalClickSubscribers;
	private Map<Integer, URLStatEntry> clickStatValues;

	/**
	 * Holds value of property targetName.
	 */
	private String targetName;
	
	private Map<String, URLStatEntry> deeptrackStatValues;

	/**
	 * Holds value of property deepAlphaValues.
	 */
	private Map<String, ComMailingDeepStatEntry> deepAlphaValues;

	/**
	 * Holds value of property deepNumValues.
	 */
Map<String, ComMailingDeepStatEntry> deepNumValues;

	private double revenue;
	
	@Override
	public int getOpened() {
		return opened;
	}

	@Override
	public void setOpened(int opened) {
		this.opened = opened;
	}

	@Override
	public int getOptouts() {
		return optouts;
	}

	@Override
	public void setOptouts(int optouts) {
		this.optouts = optouts;
	}

	@Override
	public int getBounces() {
		return bounces;
	}

	@Override
	public void setBounces(int bounces) {
		this.bounces = bounces;
	}

	@Override
	public int getTotalMails() {
		return totalMails;
	}

	@Override
	public void setTotalMails(int totalMails) {
		this.totalMails = totalMails;
	}

	@Override
	public Map<Integer, URLStatEntry> getClickStatValues() {
		return clickStatValues;
	}

	@Override
	public void setClickStatValues(Map<Integer, URLStatEntry> clickStatValues) {
		this.clickStatValues = clickStatValues;
	}

	@Override
	public int getTotalClicks() {
		return totalClicks;
	}

	@Override
	public void setTotalClicks(int totalClicks) {
		this.totalClicks = totalClicks;
	}

	@Override
	public int getTotalClickSubscribers() {
		return totalClickSubscribers;
	}

	@Override
	public void setTotalClickSubscribers(int totalClickSubscribers) {
		this.totalClickSubscribers = totalClickSubscribers;
	}

	@Override
	public int getTotalClicksNetto() {
		return totalClicksNetto;
	}

	@Override
	public void setTotalClicksNetto(int totalClicksNetto) {
		this.totalClicksNetto = totalClicksNetto;
	}

	/**
	 * Getter for property targetName.
	 * 
	 * @return Value of property targetName.
	 */
	@Override
	public String getTargetName() {
		return targetName;
	}

	/**
	 * Setter for property targetName.
	 * 
	 * @param targetName
	 *            New value of property targetName.
	 */
	@Override
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	@Override
	public Map<String, URLStatEntry> getDeeptrackStatValues() {
		return deeptrackStatValues;
	}

	@Override
	public void setDeeptrackStatValues(Map<String, URLStatEntry> deeptrackStatValues) {
		this.deeptrackStatValues = deeptrackStatValues;
	}

	/**
	 * Getter for property deepAlphaValues.
	 * 
	 * @return Value of property deepAlphaValues.
	 */
	@Override
	public Map<String, ComMailingDeepStatEntry> getDeepAlphaValues() {
		return deepAlphaValues;
	}

	/**
	 * Setter for property deepAlphaValues.
	 * 
	 * @param deepAlphaValues
	 *            New value of property deepAlphaValues.
	 */
	@Override
	public void setDeepAlphaValues(Map<String, ComMailingDeepStatEntry> deepAlphaValues) {
		this.deepAlphaValues = deepAlphaValues;
	}

	/**
	 * Getter for property deepNumValues.
	 * 
	 * @return Value of property deepNumValues.
	 */
	@Override
	public Map<String, ComMailingDeepStatEntry> getDeepNumValues() {
		return deepNumValues;
	}

	/**
	 * Setter for property deepNumValues.
	 * 
	 * @param deepNumValues
	 *            New value of property deepNumValues.
	 */
	@Override
	public void setDeepNumValues(Map<String, ComMailingDeepStatEntry> deepNumValues) {
		this.deepNumValues = deepNumValues;
	}

	@Override
	public double getRevenue() {
		return revenue;
	}

	@Override
	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}
}
