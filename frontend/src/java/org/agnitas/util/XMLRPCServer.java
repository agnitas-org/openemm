/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package	org.agnitas.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * Wrap up the apache XML-RPC library for easy implementing
 * a server process
 * 
 * To use this class one should subclass it, override the
 * constructor and use "config" to optional adjust the XML-RPC
 * configuration and add calling points using "phm".
 */
public class XMLRPCServer {
	private String				host = null;
	private int				port = 8080;
	private WebServer			server = null;
	private XmlRpcServer			xserver = null;
	protected XmlRpcServerConfigImpl	config = null;
	protected PropertyHandlerMapping	phm = null;
	
	public XMLRPCServer (String nhost, int nport) throws UnknownHostException {
		if (nport != 0) {
			port = nport;
		}
		host = nhost;
		if (host != null) {
			server = new WebServer (port, InetAddress.getByName (host));
		} else {
			server = new WebServer (port);
		}
		xserver = server.getXmlRpcServer ();
		config = new XmlRpcServerConfigImpl ();
		phm = new PropertyHandlerMapping ();
		config.setEnabledForExceptions (false);
		phm.setVoidMethodEnabled (false);
	}
	
	public XMLRPCServer (int nport) throws UnknownHostException {
		this (null, nport);
	}
	
	public XMLRPCServer () throws UnknownHostException {
		this (null, 0);
	}

	/**
	 * Hand over the control to the server
	 * 
	 * @throws Exception
	 */
	public void start () throws Exception {
		xserver.setConfig (config);
		xserver.setHandlerMapping (phm);
		server.start ();
	}
}
