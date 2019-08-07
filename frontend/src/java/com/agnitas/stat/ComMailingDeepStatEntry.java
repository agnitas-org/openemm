/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.stat;

import org.springframework.context.ApplicationContext;

public interface ComMailingDeepStatEntry {
	/**
	 * Getter for property date.
	 *
	 * @return Value of property date.
	 */
	public String getDate();

	/**
	 * Setter for property date.
	 *
	 * @param date
	 *            New value of property date.
	 */
	public void setDate(String date);

	/**
	 * Getter for property sum.
	 *
	 * @return Value of property sum.
	 */
	public double getSum();

	/**
	 * Setter for property sum.
	 *
	 * @param sum
	 *            New value of property sum.
	 */
	public void setSum(double sum);

	/**
	 * Getter for property average.
	 *
	 * @return Value of property average.
	 */
	public double getAverage();

	/**
	 * Setter for property average.
	 *
	 * @param average
	 *            New value of property average.
	 */
	public void setAverage(double average);

	/**
	 * Getter for property minimum.
	 *
	 * @return Value of property minimum.
	 */
	public double getMinimum();

	/**
	 * Setter for property minimum.
	 *
	 * @param minimum
	 *            New value of property minimum.
	 */
	public void setMinimum(double minimum);

	/**
	 * Getter for property maximum.
	 *
	 * @return Value of property maximum.
	 */
	public double getMaximum();

	/**
	 * Setter for property maximum.
	 *
	 * @param maximum
	 *            New value of property maximum.
	 */
	public void setMaximum(double maximum);

	/**
	 * Getter for property requests.
	 *
	 * @return Value of property requests.
	 */
	public int getRequests();

	/**
	 * Setter for property requests.
	 *
	 * @param requests
	 *            New value of property requests.
	 */
	public void setRequests(int requests);

	/**
	 * Getter for property requestsNetto.
	 *
	 * @return Value of property requestsNetto.
	 */
	public int getRequestsNetto();

	/**
	 * Setter for property requestsNetto.
	 *
	 * @param requestsNetto
	 *            New value of property requestsNetto.
	 */
	public void setRequestsNetto(int requestsNetto);

	/**
	 * Setter for property applicationContext.
	 *
	 * @param applicationContext
	 *            New value of property applicationContext.
	 */
	public void setApplicationContext(ApplicationContext applicationContext);

}
