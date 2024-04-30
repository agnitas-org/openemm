/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

/**
 * The Class FtpHelper.
 */
public class FtpHelperBySimpleHttp {
	
	/** The host. */
	private String host = null;
	
	/** The user. */
	private String user = null;
	
	/** The password. */
	private String password = null;
	
	/** The port. */
	private int port = 21; // Default port for FTP is 21
	
	/**
	 * Instantiates a new ftp helper.
	 * Default port for FTP is 21.
	 *
	 * @param host the host
	 * @param user the user
	 * @param password the password
	 */
	public FtpHelperBySimpleHttp(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
	/**
	 * Instantiates a new ftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param password the password
	 * @param port the port
	 */
	public FtpHelperBySimpleHttp(String host, String user, String password, int port) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.port = port;
	}

	/**
	 * Instantiates a new ftp helper.
	 * Default port for FTP is 21
	 *
	 * @param fileServerAndAuthConfigString like "ftp://[username[:password]@]server[:port]"
	 */
	public FtpHelperBySimpleHttp(String fileServerAndAuthConfigString) throws Exception {
		if (fileServerAndAuthConfigString.toLowerCase().startsWith("sftp://")) {
			throw new Exception("Invalid protocol for FtpHelper");
		} else if (fileServerAndAuthConfigString.toLowerCase().startsWith("ftp://")) {
			fileServerAndAuthConfigString = fileServerAndAuthConfigString.substring(6);
		}
		
		if (fileServerAndAuthConfigString.contains("@")) {
			String[] parts = fileServerAndAuthConfigString.split("@");
			String authPart = parts[0];
			String serverPart = parts[1];
			
			if (authPart.contains(":")) {
				user = authPart.substring(0, authPart.indexOf(":"));
				password = authPart.substring(authPart.indexOf(":") + 1);
			} else {
				user = authPart;
			}
			
			if (serverPart.contains(":")) {
				host = serverPart.substring(0, serverPart.indexOf(":"));
				port = Integer.parseInt(serverPart.substring(serverPart.indexOf(":") + 1));
			} else {
				host = serverPart;
			}
		} else {
			host = fileServerAndAuthConfigString;
		}
	}

	/**
	 * Connect.
	 *
	 * @throws Exception the exception
	 */
	public void connect() throws Exception {
		URL url = new URL(getUrlString());
		URLConnection conn = url.openConnection();
		try (InputStream inputStream = conn.getInputStream()) {
			if (inputStream == null) {
				throw new Exception("Connection failed");
			}
		}
	}
	
	/**
	 * Check if file exists on ftp server
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception on invalid directory
	 */
	public boolean exists(String filePath) throws Exception {
		URL url = new URL(getUrlString() + "/" + filePath + ";type=i");
		URLConnection conn = url.openConnection();
		try (InputStream inputStream = conn.getInputStream()) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Put a file on the server.
	 *
	 * @param in the in
	 * @param dst the dst
	 */
	public void put(InputStream inputStream, String destination) throws Exception {
		URL url = new URL(getUrlString() + "/" + destination + ";type=i");
		URLConnection conn = url.openConnection();
		try (OutputStream outputStream = conn.getOutputStream()) {
			IOUtils.copy(inputStream, outputStream);
		}
	}

	/**
	 * Gets the file.
	 *
	 * @param name the name
	 * @return the input stream
	 */
	public InputStream get(String name) throws Exception {
		URL url = new URL(getUrlString() + "/" + name + ";type=i");
		URLConnection conn = url.openConnection();
		return conn.getInputStream();
	}

	private String getUrlString() {
		String urlString = "ftp://" + user + ":" + password + "@" + host;
		if (port != 21) {
			urlString = urlString + ":" + port;
		}
		return urlString;
	}

	/**
	 * Close. Does nothing
	 */
	public void close() {
		// do nothing
	}
}
