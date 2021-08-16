/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

/**
 * The Class FtpHelper.
 * It uses FTP passive mode by default
 */
public class FtpHelper implements RemoteFileHelper {

	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(FtpHelper.class);

	/** The host. */
	private String host = null;
	
	/** The user. */
	private String user = null;
	
	/** The password. */
	private String password = null;
	
	/** The port. */
	private int port = 21; // Default port for FTP is 21
	
	/**
	 * usePassiveMode
	 */
	private boolean usePassiveMode = true;
	
	/**
	 * Currently connected client
	 */
	private FTPClient ftpClient = null;
	
	/**
	 * Instantiates a new ftp helper.
	 * Default port for FTP is 21.
	 *
	 * @param host the host
	 * @param user the user
	 * @param password the password
	 */
	public FtpHelper(String host, String user, String password) {
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
	public FtpHelper(String host, String user, String password, int port) {
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
	public FtpHelper(String fileServerAndAuthConfigString) throws Exception {
		if (fileServerAndAuthConfigString.toLowerCase().startsWith("sftp://")) {
			throw new Exception("Invalid protocol for FtpHelper");
		} else if (fileServerAndAuthConfigString.toLowerCase().startsWith("ftp://")) {
			fileServerAndAuthConfigString = fileServerAndAuthConfigString.substring(6);
		}
		
		if (fileServerAndAuthConfigString.contains("@")) {
			int hostSeparatorIndex = fileServerAndAuthConfigString.lastIndexOf("@");
			String authPart = fileServerAndAuthConfigString.substring(0, hostSeparatorIndex);
			String serverPart = fileServerAndAuthConfigString.substring(hostSeparatorIndex + 1);

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
	@Override
	public void connect() throws Exception {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(host, port);
			if (usePassiveMode) {
				ftpClient.enterLocalPassiveMode();
			}
			if (!ftpClient.login(user, password)) {
				ftpClient.logout();
				throw new Exception("Authentication failed");
			}
		} catch (Exception e) {
			close();
			throw new Exception("Connection failed");
		}
	}

	/**
	 * Cd.
	 *
	 * @param path the path
	 */
	@Override
	public void cd(String path) throws Exception {
		checkForConnection();
		ftpClient.changeWorkingDirectory(path);
	}

	/**
	 * Ls.
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> ls(String path) throws Exception {
		checkForConnection();
		String[] fileNames = ftpClient.listNames(path);
		if (fileNames != null) {
			List<String> returnList = new ArrayList<>();
			for (String fileEntry : fileNames) {
				String fileEntryName;
				if (fileEntry.contains("/")) {
					fileEntryName = fileEntry.substring(fileEntry.lastIndexOf("/") + 1);
				} else {
					fileEntryName = fileEntry;
				}
				if (!".".equals(fileEntryName) && ! "..".equals(fileEntryName)) {
					returnList.add(fileEntryName);
				}
			}
			return returnList;
		} else {
			// Some FTP servers can only execute "ls" on directories
			if (!path.endsWith("/")) {
				String parentDirectory;
				String fileName;
				if (path.contains("/")) {
					parentDirectory = path.substring(0, path.lastIndexOf("/"));
					fileName = path.substring(path.lastIndexOf("/") + 1);
				} else {
					parentDirectory = "/";
					fileName = path;
				}
				fileNames = ftpClient.listNames(parentDirectory);
				if (fileNames != null) {
					List<String> returnList = new ArrayList<>();
					Pattern fileNamePattern = Pattern.compile("^" + fileName.replace("^", "\\^").replace("$", "\\$").replace(".", "\\.").replace("*", ".*").replace("?", ".") + "$");
					for (String fileEntry : fileNames) {
						String fileEntryName;
						if (fileEntry.contains("/")) {
							fileEntryName = fileEntry.substring(fileEntry.lastIndexOf("/") + 1);
						} else {
							fileEntryName = fileEntry;
						}
						if (fileNamePattern.matcher(fileEntryName).find()) {
							returnList.add(fileEntryName);
						}
					}
					return returnList;
				} else {
					return new ArrayList<>();
				}
			} else {
				return new ArrayList<>();
			}
		}
	}
	
	/**
	 * Check if directory exists on sftp server
	 * 
	 * @param directoryPath
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean directoryExists(String directoryPath) throws Exception {
		checkForConnection();
		ftpClient.changeWorkingDirectory(directoryPath);
		int returnCode = ftpClient.getReplyCode();
		if (returnCode == 550) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Check if file exists on sftp server
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception on invalid directory
	 */
	@Override
	public boolean fileExists(String filePath) throws Exception {
		checkForConnection();
		return ftpClient.listFiles(filePath).length > 0;
	}

	/**
	 * Put a file on the server.
	 *
	 * @param in the in
	 * @param dst the dst
	 */
	@Override
	public void put(InputStream inputStream, String destination, boolean useTempFileNameWhileUploading) throws Exception {
		checkForConnection();
		if (useTempFileNameWhileUploading) {
			ftpClient.storeFile(destination + ".tmp", inputStream);
			ftpClient.rename(destination + ".tmp", destination);
		} else {
			ftpClient.storeFile(destination, inputStream);
		}
	}

	/**
	 * Gets the file.
	 *
	 * @param filePathAndName the path and name
	 * @return the input stream
	 */
	@Override
	public InputStream get(String filePathAndName) throws Exception {
		checkForConnection();
		InputStream inputStream = ftpClient.retrieveFileStream(filePathAndName);
		int returnCode = ftpClient.getReplyCode();
		if (inputStream != null && returnCode != 550) {
			return new FtpInputStream(inputStream, this);
		} else {
			throw new Exception("File not found");
		}
	}

	@Override
	public Date getModifyDate(String filePathAndName) throws Exception {
		checkForConnection();
		FTPFile file = ftpClient.mlistFile(filePathAndName);
		if (file != null) {
			return file.getTimestamp().getTime();
		} else {
			throw new Exception("File not found");
		}
	}

	@Override
	public long getFileSize(String filePathAndName) throws Exception {
		checkForConnection();
		FTPFile file = ftpClient.mlistFile(filePathAndName);
		if (file != null) {
			return file.getSize();
		} else {
			throw new Exception("File not found");
		}
	}
	
	private void checkForConnection() throws Exception {
		if (ftpClient == null) {
			throw new Exception("FtpHelper is not connected");
		} else if (!ftpClient.isConnected()) {
			ftpClient = null;
			throw new Exception("FtpHelper is not connected anymore");
		}
	}

	/**
	 * Close connection
	 */
	@Override
	public void close() {
		if (ftpClient != null) {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (Exception f) {
					// do nothing
				}
			}
			ftpClient = null;
		}
	}

	public boolean completePendingCommand() {
		if (ftpClient != null) {
			if (ftpClient.isConnected()) {
				try {
					return ftpClient.completePendingCommand();
				} catch (Exception f) {
					// do nothing
				}
			}
		}
		return false;
	}
	
	class FtpInputStream extends FilterInputStream {
		FtpHelper ftpHelper;
		boolean isClosed = false;
		
		protected FtpInputStream(InputStream in, FtpHelper ftpHelper) {
			super(in);
			this.ftpHelper = ftpHelper;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			if (!isClosed) {
				ftpHelper.completePendingCommand();
			}
			
			isClosed = true;
		}
		
	}

	@Override
	public void deleteFile(String filePath) throws Exception {
		ftpClient.deleteFile(filePath);
	}
}
