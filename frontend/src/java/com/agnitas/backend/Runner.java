/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.HashMap;
import java.util.Map;
import com.agnitas.util.Log;
import com.agnitas.util.ParameterParser;

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
		if (command.equals("fire")) {
			fire();
		} else {
			message(Log.ERROR, "unknown command " + command);
		}
	}

	private Map <String, Object> parse (String source) {
		Map <String, Object>	opts = new HashMap <> ();
		
		if ((source != null) && (! source.isBlank ())) {
			try {
				for (Map.Entry <String, String> entry : (new ParameterParser (source)).parse ().entrySet ()) {
					opts.put (entry.getKey (), entry.getValue ());
				}
			} catch (Exception e) {
				message(Log.ERROR, "Unparsable option \"" + source + "\": " + e.toString ());
				return null;
			}
		}
		return opts;
	}
		
	private void fire() {
		String[]		parts = option.trim ().split ("\\s+", 2);
		String			status_id;
		Map <String, Object>	opts;
		
		if ((parts.length == 0) || parts[0].isBlank ()) {
			message(Log.ERROR, "Missing arguments (<status_id> <optios>* expected, got \"" + option + "\")");
			return;
		}
		status_id = parts[0];
		opts = parse (parts.length == 2 ? parts[1] : null);
		if (opts == null) {
			return;
		}

		MailgunImpl mailout = null;
		try {
			mailout = new MailgunImpl();

			message(Log.INFO, mailout.fire(status_id, opts));
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
			result = str;
		}
	}

	private void message(int loglvl, String str) {
		message(loglvl, str, null);
	}
}
