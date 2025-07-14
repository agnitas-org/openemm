/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import com.agnitas.util.Log;
import com.agnitas.util.Systemconfig;
import com.agnitas.util.XMLRPCServer;

/**
 * Entrypoint for the server version of the merger process
 */
public class MailoutServerXMLRPC extends XMLRPCServer {
	private static final String HOSTNAME = "nfsserver";
	private static final int PORT = 8089;
	private Log log = null;

	/**
	 * Constructor
	 *
	 * @param hostname the hostname to bind the listening socket to
	 * @param port     the port to listen for incoming connections
	 * @throws Exception
	 */
	public MailoutServerXMLRPC(String hostname, int port) throws Exception {
		super(hostname, port);
		log = new Log("mailoutserver", Log.INFO, 0);
		phm.addHandler("Merger", Merger.class);
		log.out(Log.INFO, "server", "Listening to " + (hostname == null ? "*" : hostname) + ":" + port + " for XML-RPC requests running " + Systemconfig.version + " (with default dbid \"" + Data.defaultDBID + "\")");
	}

	/**
	 * Startup point
	 */
	public static void main(String[] args) throws Exception {
		Systemconfig		syscfg = Systemconfig.create ();
		String			hostname = syscfg.get ("mailout-server", HOSTNAME);
		int			port = syscfg.get ("mailout-port", PORT);
		MailoutServerXMLRPC	svr;

		if (args.length > 0) {
			hostname = args[0];
			if ((hostname.length() == 0) || hostname.equals("*")) {
				hostname = null;
			}
			if (args.length > 1) {
				port = Integer.parseInt(args[1]);
			}
		}
		svr = new MailoutServerXMLRPC(hostname, port);
		svr.start();
	}
}
