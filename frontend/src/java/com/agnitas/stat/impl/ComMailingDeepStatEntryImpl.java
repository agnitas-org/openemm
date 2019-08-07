/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/*
 * ComMailingDeepStatEntry.java
 *
 * Created on 07. September 2003
 */

package com.agnitas.stat.impl;

import org.springframework.context.ApplicationContext;

import com.agnitas.stat.ComMailingDeepStatEntry;

public class ComMailingDeepStatEntryImpl implements ComMailingDeepStatEntry {
	/**
	 * Holds value of property date.
	 */
	private String date;

	/**
	 * Holds value of property sum.
	 */
	private double sum = 0;

	/**
	 * Holds value of property average.
	 */
	private double average = 0;

	/**
	 * Holds value of property minimum.
	 */
	private double minimum = 0;

	/**
	 * Holds value of property maximum.
	 */
	private double maximum = 0;

	/**
	 * Holds value of property requests.
	 */
	private int requests = 0;

	/**
	 * Holds value of property requestsNetto.
	 */
	private int requestsNetto = 0;

	/**
	 * Getter for property date.
	 *
	 * @return Value of property date.
	 */
	@Override
	public String getDate() {
		return this.date;
	}

	/**
	 * Setter for property date.
	 *
	 * @param date
	 *            New value of property date.
	 */
	@Override
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Getter for property sum.
	 *
	 * @return Value of property sum.
	 */
	@Override
	public double getSum() {
		return this.sum;
	}

	/**
	 * Setter for property sum.
	 *
	 * @param sum
	 *            New value of property sum.
	 */
	@Override
	public void setSum(double sum) {
		this.sum = sum;
	}

	/**
	 * Getter for property average.
	 *
	 * @return Value of property average.
	 */
	@Override
	public double getAverage() {
		return this.average;
	}

	/**
	 * Setter for property average.
	 *
	 * @param average
	 *            New value of property average.
	 */
	@Override
	public void setAverage(double average) {
		this.average = average;
	}

	/**
	 * Getter for property minimum.
	 *
	 * @return Value of property minimum.
	 */
	@Override
	public double getMinimum() {
		return this.minimum;
	}

	/**
	 * Setter for property minimum.
	 *
	 * @param minimum
	 *            New value of property minimum.
	 */
	@Override
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}

	/**
	 * Getter for property maximum.
	 *
	 * @return Value of property maximum.
	 */
	@Override
	public double getMaximum() {
		return this.maximum;
	}

	/**
	 * Setter for property maximum.
	 *
	 * @param maximum
	 *            New value of property maximum.
	 */
	@Override
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}

	/**
	 * Getter for property requests.
	 *
	 * @return Value of property requests.
	 */
	@Override
	public int getRequests() {
		return this.requests;
	}

	/**
	 * Setter for property requests.
	 *
	 * @param requests
	 *            New value of property requests.
	 */
	@Override
	public void setRequests(int requests) {
		this.requests = requests;
	}

	/**
	 * Getter for property requestsNetto.
	 *
	 * @return Value of property requestsNetto.
	 */
	@Override
	public int getRequestsNetto() {
		return this.requestsNetto;
	}

	/**
	 * Setter for property requestsNetto.
	 *
	 * @param requestsNetto
	 *            New value of property requestsNetto.
	 */
	@Override
	public void setRequestsNetto(int requestsNetto) {
		this.requestsNetto = requestsNetto;
	}

	/**
	 * Holds value of property applicationContext.
	 */
	protected ApplicationContext applicationContext;

	/**
	 * Setter for property applicationContext.
	 *
	 * @param applicationContext
	 *            New value of property applicationContext.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {

		this.applicationContext = applicationContext;
	}

}
