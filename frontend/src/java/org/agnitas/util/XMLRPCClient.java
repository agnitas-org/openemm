/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package	org.agnitas.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcLiteHttpTransportFactory;

/**
 * Wrap up the apache XML-RPC library to a more easy to use
 * version. Remote errors and exceptions are not propagated
 * to the caller but can be collected by implementing a
 * class using the "interface Report".
 * 
 * For convenient it also provides a class method for
 * a single invokation of a remote message which contains
 * all the setup and teardown for the remote call.
 */
public class XMLRPCClient {
	public interface Report {
		public void error (Exception e, String msg);
		public void remote (Throwable t);
	}

	private XmlRpcClient		client = null;
	private Report			report = null;

	private void error (Exception e, String msg) {
		if (report != null) {
			report.error (e, msg);
		}
	}
	
	/**
	 * Constructor for a new instance, all parameter are optional
	 * and are replaced by a default, if not set (e.g. set to null or 0)
	 * 
	 * @param proto the transport protocol to use, default is "http"
	 * @param host  the host to make the connection to, default is "localhost"
	 * @param port  the port to connect, default is provied bey the transport library and depends on the protocol
	 * @param path  the path for the call, default is "/RPC2"
	 * @param rep   an instance of a class implementing the Report interface to collect possible errors and exceptions
	 */
	public XMLRPCClient (String proto, String host, int port, String path, Report rep) {
		report = rep;

		try {
			XmlRpcClientConfigImpl	config;
			String			addr;
			
			config = new XmlRpcClientConfigImpl ();
			if (proto == null) {
				proto = "http";
			}
			if (host == null) {
				host = "localhost";
			}
			if (path == null) {
				path = "/RPC2";
			}
			if (port <= 0) {
				addr = host;
			} else {
				addr = host + ":" + port;
			}
			config.setServerURL (new URL (proto + "://" + addr + path));
			client = new XmlRpcClient ();
			client.setTransportFactory (new XmlRpcLiteHttpTransportFactory (client));
			client.setConfig (config);
		} catch (MalformedURLException e) {
			error (e, "url: " + e.toString ());
			client = null;
		}
	}
	public XMLRPCClient (String host, int port, Report rep) {
		this (null, host, port, null, rep);
	}
	public XMLRPCClient (String host, int port) {
		this (null, host, port, null, null);
	}

	/**
	 * Returns if the client setup had been successful
	 * 
	 * @return true, if the setup had been successful, false otherwise
	 */
	public boolean valid () {
		return client != null;
	}
	
	private Object call (long timeout, String method, Object[] param) {
		Object	rc = null;

		try {
			if (timeout > 0) {
				TimingOutCallback 	callback = new TimingOutCallback (timeout);

				client.executeAsync (method, param, callback);
				rc = callback.waitForResponse ();
			} else {
				rc = client.execute (method, param);
			}
		} catch (TimingOutCallback.TimeoutException e) {
			error (e, "timeout");
		} catch (XmlRpcException e) {
			error (e, "xml-rpc: " + e.toString ());
		} catch (Exception e) {
			error (e, "failure: " + e.toString ());
		} catch (Throwable t) {
			if (report != null) {
				report.remote (t);
			}
		}
		return rc;
	}

	/**
	 * Invoke the remote "method", passing the arguments for this method
	 * as a variable number of arguments
	 * 
	 * @param timeout the timeout in miliseconds to wait for the remote to complete and answer, 0 means no timeout at all
	 * @param method  the name of the method to invoke
	 * @param param   the arguments for the method
	 * @return        the result from the method call (or null on failure)
	 */
	public Object invoke (long timeout, String method, Object ... param) {
		return call (timeout, method, param);
	}
	
	public Object invokeParam (long timeout, String method, Object[] param) {
		return call (timeout, method, param);
	}

	/**
	 * Convenient method for a single call. Collection exceptions are
	 * thrown in the case of an error occures
	 * 
	 * @param host    the host to connect to
	 * @param port    the port to connect to
	 * @param timeout the timeout in miliseconds
	 * @param method  the name of the method to invoke
	 * @param param   a variable list of arguments for the method to call
	 * @returns       the result from the method call
	 * @thtrows       Exception
	 */
	public static Object invoke (String host, int port, long timeout, String method, Object ... param) throws Exception {
		class Reporter implements Report {
			private Exception	err = null;
			private Throwable	rem = null;
		
			@Override
			public void error (Exception e, String msg) {
				err = e;
			}
			@Override
			public void remote (Throwable t) {
				rem = t;
			}
			public void post () throws Exception {
				if (err != null) {
					throw err;
				}
				if (rem != null) {
					throw new Exception (rem);
				}
			}
		}
		Reporter	r = new Reporter ();
		XMLRPCClient	x = new XMLRPCClient (host, port, r);
		Object		rc;

		r.post ();
		rc = x.invokeParam (timeout, method, param);
		r.post ();
		return rc;
	}
}
