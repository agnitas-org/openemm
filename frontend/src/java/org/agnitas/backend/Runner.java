/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.StringTokenizer;

import org.agnitas.util.Log;

/**
 * This class is the anchor for a new thread in the mailout server
 */
public class Runner extends Thread {
	/**
	 * logger
	 */
	private Log log;
	/**
	 * the command to execute
	 */
	private String command;
	/**
	 * the options for this command
	 */
	private String option;
	/**
	 * the standard response on fire
	 */
	private String result = "Mailout started";

	/**
	 * Constructor
	 *
	 * @param log     a reference to valid logger
	 * @param command the command to execute
	 * @param option  its options
	 */
	public Runner(Log log, String command, String option) {
		this.log = log;
		this.command = command;
		this.option = option;
	}

	/**
	 * Returns the message to return from an invocation
	 *
	 * @return the message string
	 */
	public String result() {
		return result;
	}

	/**
	 * Thread starting point
	 */
	@Override
	public void run() {
		StringTokenizer tok;
		int tokCount;

		if (option != null) {
			tok = new StringTokenizer(option);
			tokCount = tok.countTokens();
		} else {
			tok = null;
			tokCount = 0;
		}
		if (command.equals("fire")) {
			fire(tok, tokCount);
		} else {
			message(Log.ERROR, "unknown command " + command);
		}
	}

	private void fire(StringTokenizer tok, int tokCount) {
		if ((tokCount < 1) || (tokCount > 2)) {
			message(Log.ERROR, "invalid number of arguments (<status_id> [<customerid>] expected, got " + tokCount + ")");
			return;
		}

		String status_id, custid;

		status_id = tok.nextToken();
		if (tokCount > 1) {
			custid = tok.nextToken();
		} else {
			custid = null;
		}

		MailgunImpl mailout = null;
		try {
			mailout = new MailgunImpl();

			message(Log.INFO, mailout.fire(status_id, custid));
		} catch (Exception e) {
			message(Log.ERROR, "Error during starting: " + e.toString(), e);
			if (mailout == null) {
				message(Log.ERROR, "Failed to cleanup mailout: Nullpointer mailout not successfully initialized", e);
			} else {
				try {
					mailout.done();
				} catch (Exception e2) {
					message(Log.ERROR, "Failed to cleanup mailout: " + e.toString(), e);
				}
			}
		}
	}

	/**
	 * output error message
	 *
	 * @param loglvl the log level
	 * @param str    the message
	 */
	private void message(int loglvl, String str, Throwable th) {
		if (str != null) {
			if (option != null) {
				str = "[" + option + "] " + str;
			}
			log.out(loglvl, (command != null ? command : "runner"), str, th);
		}
	}

	private void message(int loglvl, String str) {
		message(loglvl, str, null);
	}
}
