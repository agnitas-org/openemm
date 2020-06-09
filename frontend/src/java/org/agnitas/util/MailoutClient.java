/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package	org.agnitas.util;

import java.util.ResourceBundle;

/**
 * MailoutClient
 *
 * - connects to MailgoutServer via XML-RPC and starts Mailout with two arguments:
 *   status_id and output (file|zip)
 * - the connected MailoutServer starts a new thread, therefore the function
 *   returns instantly
 */
public class MailoutClient {
	/** name of the host to connect to. optional with port number, name:8834 */
	private static String	hostname = "nfsserver";
	private static int	portnumber = 8089;
	private Log		log = null;
	
	private void setupLogger (int loglevel) {
		log = new Log ("mailoutclient", loglevel);
	}

	/**
	 * Constructor
	 * 
	 * Setup logging with INFO level
	 */
	public MailoutClient () {
		this (Log.INFO);
	}
	
	/**
	 * Constructor
	 * 
	 * @param loglevel loglevel to setup internal logger
	 */
	public MailoutClient (int loglevel) {
		setupLogger (loglevel);
	}

	/** general method to trigger remote mailout
	 * @param command the command itself
	 * @param option  command depended options
	 */
	public void invoke (String command, String option) {
		String		message = "blank";
		String		host = null;
		int		port = -1;

		try {
			ResourceBundle	rsc;
			String		portValue;

			rsc = ResourceBundle.getBundle ("emm");
			if (rsc.containsKey ("system.mailout")) {
				host = rsc.getString ("system.mailout");
			} else 	if (rsc.containsKey ("system.mailgun")) {
				host = rsc.getString ("system.mailgun");
			} else {
				host = "nfsserver";
			}
			if (rsc.containsKey ("port.mailout")) {
				portValue = rsc.getString ("port.mailout");
			} else if (rsc.containsKey ("port.mailgun")) {
				portValue = rsc.getString ("port.mailgun");
			} else {
				portValue = null;
			}
			if (portValue != null) {
				port = Integer.parseInt (portValue);
				if (port <= 0) {
					port = -1;
				}
			}
		} catch (java.util.MissingResourceException e) {
			log.out (Log.DEBUG, "invoke", "No emm resource found, using default " + hostname + ":" + portnumber);
		} catch (Exception e) {
			log.out (Log.VERBOSE, "invoke", "Failed parsing resources: " + e.toString ());
		}
		if (host == null) {
			host = hostname;
		}
		if (port == -1) {
			port = portnumber;
		}
		log.out (Log.INFO, "invoke", "Connecting to " + host);
		
		try {
			message = (String) XMLRPCClient.invoke (host, port, 30 * 1000, "Merger.remote_control", command, option);
		} catch (Exception e) {
			log.out (Log.ERROR, "invoke", "MailoutClient exception: " + e.getMessage());
		}
		log.out (Log.INFO, "invoke", "Message: " + message);
	}

	/** Wrapper for invoke for starting a mailing
	 * @param status_id the status_id in maildrop_status_tbl to start
	 * @param custid the customer_id to start mailout for
	 */
	public void run(String status_id, String custid) {
		String	option;

		option = status_id;
		if (custid != null) {
			option += " " + custid;
		}
		invoke ("fire", option);
	}

	/** Interface to start this class as application
	 * @param args the commandline arguments
	 * @throws Exception any error occuring during invokation
	 */
	public static void main(String[] args) throws Exception {
		MailoutClient mclient = new MailoutClient(Log.DEBUG);

		if (args.length < 1) {
			throw new Exception ("Usage: MailoutClient <command> [<options>]");
		}
		String	cmd = args[0];
		String	opts = null;

		if (args.length > 1) {
			for (int n = 1; n < args.length; ++n) {
				if (n == 1) {
					opts = args[n];
				} else {
					opts += " " + args[n];
				}
			}
		}
		mclient.invoke (cmd, opts);
	}
}
