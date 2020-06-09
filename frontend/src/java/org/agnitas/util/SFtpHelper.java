/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * The Class SFtpHelper.
 */
public class SFtpHelper implements RemoteFileHelper {

	/** The Constant logger. */
	private static final transient Logger logger = Logger.getLogger(SFtpHelper.class);

	/** The host. */
	private String host = null;
	
	/** The optional hostKeyFingerprint. */
	private String hostKeyFingerprint = null;
	
	/** The user. */
	private String user = null;
	
	/** The password. */
	private String password = null;
	
	/** The privateSshKeyFile. */
	private File privateSshKeyFile = null;
	
	/** The privateSshKeyData. */
	private String privateSshKeyData = null;

	/** The privateSshKeyPassphrase. */
	private byte[] privateSshKeyPassphrase = null;
	
	/** The port. */
	private int port = 22; // Default port for SFTP is 22
	
	private boolean allowUnknownHostKeys = false;
	
	/** The jsch. */
	private JSch jsch = new JSch();
	
	/** The session. */
	private Session session = null;
	
	/** The channel. */
	private ChannelSftp channel = null;
	
	/** The baseDir. */
	private String baseDir = null;
	
	/**
	 * Instantiates a new sftp helper.
	 * Default port for SFTP is 22.
	 *
	 * @param host the host
	 * @param user the user
	 * @param password the password
	 */
	public SFtpHelper(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
	/**
	 * Instantiates a new sftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param password the password
	 * @param port the port
	 */
	public SFtpHelper(String host, String user, String password, int port) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.port = port;
	}
	
	/**
	 * Instantiates a new sftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param port the port
	 */
	public SFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase) {
		this.host = host;
		this.user = user;
		this.privateSshKeyFile = privateSshKeyFile;
		this.privateSshKeyPassphrase = privateSshKeyPassphrase;
	}
	
	/**
	 * Instantiates a new sftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param port the port
	 */
	public SFtpHelper(String host, String user, File privateSshKeyFile, byte[] privateSshKeyPassphrase, int port) {
		this.host = host;
		this.user = user;
		this.privateSshKeyFile = privateSshKeyFile;
		this.privateSshKeyPassphrase = privateSshKeyPassphrase;
		this.port = port;
	}
	
	/**
	 * Instantiates a new sftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param port the port
	 */
	public SFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase) {
		this.host = host;
		this.user = user;
		this.privateSshKeyData = privateSshKeyData;
		this.privateSshKeyPassphrase = privateSshKeyPassphrase;
	}
	
	/**
	 * Instantiates a new sftp helper.
	 *
	 * @param host the host
	 * @param user the user
	 * @param port the port
	 */
	public SFtpHelper(String host, String user, String privateSshKeyData, byte[] privateSshKeyPassphrase, int port) {
		this.host = host;
		this.user = user;
		this.privateSshKeyData = privateSshKeyData;
		this.privateSshKeyPassphrase = privateSshKeyPassphrase;
		this.port = port;
	}

	/**
	 * Instantiates a new sftp helper.
	 * Default port for SFTP is 22
	 *
	 * @param fileServerAndAuthConfigString like "[username[:password]@]server[:port][;hostKeyFingerprint][/baseDirectory]". The hostKeyFingerprint should be given without ":"-Characters
	 * @throws Exception
	 */
	public SFtpHelper(String fileServerAndAuthConfigString) throws Exception {
		if (fileServerAndAuthConfigString.toLowerCase().startsWith("ftp://")) {
			throw new Exception("Invalid protocol for SFtpHelper");
		} else if (fileServerAndAuthConfigString.toLowerCase().startsWith("sftp://")) {
			fileServerAndAuthConfigString = fileServerAndAuthConfigString.substring(7);
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
			
			if (serverPart.contains("/")) {
				baseDir = serverPart.substring(serverPart.indexOf("/") + 1);
				serverPart = serverPart.substring(0, serverPart.indexOf("/"));
			}
			
			if (serverPart.contains(";")) {
				hostKeyFingerprint = serverPart.substring(serverPart.indexOf(";") + 1);
				serverPart = serverPart.substring(0, serverPart.indexOf(";"));
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
	 * Set the private/public key auth data and use the already configured password if set before
	 * @param privateSshKeyData
	 * @throws Exception
	 */
	public void setPrivateSshKeyData(String privateSshKeyData) throws Exception {
		this.privateSshKeyData = privateSshKeyData;
		if (StringUtils.isNotBlank(password)) {
			privateSshKeyPassphrase = password.getBytes("UTF-8");
		}
	}
	
	/**
	 * Override the known_hosts file
	 * 
	 * @param allowUnknownHostKeys
	 */
	public void setAllowUnknownHostKeys(boolean allowUnknownHostKeys) {
		this.allowUnknownHostKeys = allowUnknownHostKeys;
	}
	
	/**
	 * Set a expected specific hostKeyFingerprint for this connection
	 * 
	 * @param hostKeyFingerprint
	 */
	public void setHostKeyFingerPrint(String hostKeyFingerprint) {
		this.hostKeyFingerprint = hostKeyFingerprint;
	}

	/**
	 * Set a starting directory
	 * 
	 * @param baseDir
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Connect.
	 *
	 * @throws Exception the exception
	 */
	@Override
	public void connect() throws Exception {
		try {
			if (privateSshKeyFile != null) {
				jsch.addIdentity(privateSshKeyFile.getAbsolutePath(), privateSshKeyPassphrase);
			} else if (privateSshKeyData != null) {
				jsch.addIdentity(user, privateSshKeyData.getBytes("UTF-8"), null, privateSshKeyPassphrase);
			} else if (password == null) {
				String homeDir = AgnUtils.getUserHomeDir();
				if (new File(homeDir + "/.ssh/id_dsa").exists()) {
					jsch.addIdentity(homeDir + "/.ssh/id_dsa");
				} else if (new File(homeDir + "/.ssh/id_rsa").exists()) {
					jsch.addIdentity(homeDir + "/.ssh/id_rsa");
				}
			}
	
			session = jsch.getSession(user, host, port);
			
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", allowUnknownHostKeys ? "no" : "yes");
			session.setConfig(config);
			
			session.setUserInfo(new JSchUserInfo(password));
			
			session.connect();
			
			if (hostKeyFingerprint != null) {
				String actualHostKeyFingerprint = session.getHostKey().getFingerPrint(jsch);
				if (!hostKeyFingerprint.replace(":", "").equalsIgnoreCase(actualHostKeyFingerprint.replace(":", ""))) {
					throw new Exception("Unexpected fingerprint of hostkey. Expected: " + hostKeyFingerprint + " Host: " + actualHostKeyFingerprint);
				}
			}
	
			Channel chan = session.openChannel("sftp");
			chan.connect();
			channel = (ChannelSftp) chan;
			
			if (StringUtils.isNotBlank(baseDir)) {
				cd(baseDir);
			}
		} catch (Exception ex) {
			if (channel != null) {
				try {
					channel.disconnect();
				} catch (Exception e) {
					// do nothing
					e.printStackTrace();
				}
				channel = null;
			}
			
			if (session != null) {
				try {
					session.disconnect();
				} catch (Exception e) {
					// do nothing
					e.printStackTrace();
				}
				session = null;
			}
			throw ex;
		}
	}

	/**
	 * Cd.
	 *
	 * @param path the path
	 * @throws SftpException the sftp exception
	 */
	@Override
	public void cd(String path) throws Exception {
		checkForConnection();
		channel.cd(path);
	}

	/**
	 * Mkdir.
	 *
	 * @param path the path
	 * @throws SftpException the sftp exception
	 */
	public void mkdir(String path) throws Exception {
		checkForConnection();
		channel.mkdir(path);
	}

	/**
	 * Put.
	 *
	 * @param srcFile the srcFile
	 * @param dstFile the dstFile
	 * @param mode the mode (see ChannelSftp.xxx for allowed modes)
	 * @throws SftpException the sftp exception
	 */
	public void put(String srcFile, String dstFile, int mode) throws Exception {
		checkForConnection();
		channel.put(srcFile, dstFile, mode);
	}

	/**
	 * Put.
	 *
	 * @param inputStream the inputStream
	 * @param dstFile the dstFile
	 * @throws SftpException the sftp exception
	 */
	@Override
	public void put(InputStream inputStream, String dstFile) throws Exception {
		checkForConnection();
		channel.put(inputStream, dstFile, ChannelSftp.OVERWRITE);
	}

	/**
	 * Gets the.
	 *
	 * @param name the name
	 * @return the input stream
	 * @throws SftpException the sftp exception
	 */
	@Override
	public InputStream get(String name) throws Exception {
		checkForConnection();
		return channel.get(name);
	}

	/**
	 * Gets the mofidy date.
	 *
	 * @param name the name
	 * @return the mofidy date
	 * @throws SftpException the sftp exception
	 * @throws ParseException the parse exception
	 */
	public Date getModifyDate(String name) throws Exception {
		checkForConnection();
		SftpATTRS attrs = channel.lstat(name);
		SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		return format.parse(attrs.getMtimeString());
	}

	/**
	 * Ls.
	 *
	 * @param path the path
	 * @return the vector
	 * @throws SftpException the sftp exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> ls(String path) throws Exception {
		if (StringUtils.isEmpty(path)) {
			path = ".";
		} else if (!path.trim().startsWith("/")) {
			path = "./" + path;
		}
		
		checkForConnection();
		Vector<LsEntry> returnVector = channel.ls(path);
		List<String> returnList = new ArrayList<>();
		for (LsEntry item : returnVector) {
			returnList.add(item.getFilename());
		}
		return returnList;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<LsEntry> ls(String path, String fileNameWithWildcard) throws Exception {
		if (StringUtils.isEmpty(path)) {
			path = ".";
		} else if (!path.trim().startsWith("/")) {
			path = "./" + path;
		}
		
		checkForConnection();
		Vector<LsEntry> returnVector = channel.ls(path + "/" + fileNameWithWildcard);
		return returnVector;
	}
	
	public void rename(final String from, final String to) throws Exception {
		checkForConnection();
		channel.rename(from, to);
	}

	/**
	 * Close.
	 */
	@Override
	public void close() {
		if (channel != null) {
			try {
				channel.quit();
			} catch (Exception e) {
				logger.error("Cannot close SFtpHelper channel: " + e.getMessage(), e);
			}
		}
		if (session != null) {
			try {
				session.disconnect();
			} catch (Exception e) {
				logger.error("Cannot close SFtpHelper session: " + e.getMessage(), e);
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
		try {
			if (StringUtils.isNotBlank(directoryPath)) {
				if (directoryPath.endsWith("/")) {
					ls(directoryPath);
				} else {
					ls(directoryPath + "/");
				}
				return true;
			} else {
				throw new Exception("Invalid directory");
			}
		} catch (Exception e) {
			return false;
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
		String directoryPath = "";
		String filename = filePath;
		
		int lastSlash = filename.lastIndexOf("/");
		if (lastSlash > -1) {
			directoryPath = filename.substring(0, lastSlash);
			if ("".equals(directoryPath)) {
				directoryPath = "/";
			}
			filename = filename.substring(lastSlash + 1);
		}
		
		List<String> directoryEntries;
		try {
			if (StringUtils.isNotBlank(directoryPath)) {
				directoryEntries = ls(directoryPath);
			} else {
				directoryEntries = ls(".");
			}
		} catch (Exception e) {
			throw new Exception("Cannot read directory: " + directoryPath, e);
		}
		
		boolean fileFound = false;
		if (directoryEntries != null) {
			for (String fileName : directoryEntries) {
				if (fileName.equals(filename)) {
					fileFound = true;
					break;
				}
			}
		}
		return fileFound;
	}

	public List<String> scanForFiles(String fileNameFilterRegEx, boolean scanSubDirectories) throws Exception {
		return scanForFiles(".", fileNameFilterRegEx, scanSubDirectories);
	}

	public List<String> scanForFiles(String dir, String fileNameFilterRegEx, boolean scanSubDirectories) throws Exception {
		checkForConnection();
		List<String> returnList = new ArrayList<>();

		if (StringUtils.isEmpty(dir)) {
			dir = ".";
		} else {
			if (!dir.endsWith("/")) {
				dir += "/";
			}
		}

		@SuppressWarnings("unchecked")
		Vector<LsEntry> entries = channel.ls(dir);
		for (LsEntry entry : entries) {
			if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) {
				continue;
			} else if (entry.getAttrs().isDir() && scanSubDirectories) {
				for (String subFilePath : scanForFiles(dir + entry.getFilename(), fileNameFilterRegEx, true)) {
					returnList.add(entry.getFilename() + "/" + subFilePath);
				}
			} else {
				if (StringUtils.isEmpty(fileNameFilterRegEx) || entry.getFilename().matches(fileNameFilterRegEx)) {
					returnList.add(entry.getFilename());
				}
			}
		}
		return returnList;
	}

	private void checkForConnection() throws Exception {
		if (channel == null) {
			throw new Exception("SFtpHelper is not connected");
		}
	}

	/**
	 * Show setup data of SFtpHelper without password
	 */
	@Override
	public String toString() {
		String diplayString = "sftp://" + user + ":";
		if (privateSshKeyFile != null) {
			diplayString += "<keyFile>";
		} else if (privateSshKeyData != null) {
			diplayString += "<keyData>";
		} else {
			diplayString += "<password>";
		}
		diplayString += "@" + host + ":" + port;
		if (hostKeyFingerprint != null) {
			diplayString += ";" + hostKeyFingerprint;
		}
		if (baseDir != null) {
			diplayString += "/" + baseDir;
		}
		return diplayString;
	}

	/**
	 * Fix bug with mock in line: expect(helper.toString()).andReturn("something");
	 * @return {@link SFtpHelper#toString()}
     */
	public String getSetUpDataWithoutString(){
		return toString();
	}
}
