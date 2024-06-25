/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import org.agnitas.util.Log;

/**
 * This class provides the method that can be called via XML-RPC
 * as a replacement for the no more supported RMI version
 */
public class Merger {
	/**
	 * Logging interface
	 */
	private Log log;

	/**
	 * Constructor
	 */
	public Merger() {
		log = new Log("merger", Log.DEBUG, 0);
	}

	/**
	 * Controls the behaviour of the merger process; see the
	 * Runner class for valid values of the parametere
	 *
	 * @param command the command to be executed
	 * @param option  the option for this command
	 * @return a status string
	 */
	public String remote_control(String command, String option) {
		Runner run = new Runner(log, command, option);

		run.start();
		return run.result();
	}
}
