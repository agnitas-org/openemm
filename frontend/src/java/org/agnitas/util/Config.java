/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * general class to read configuration files
 */
public class Config {
	/** optional logger	 */
	private Log		log;
	/** the name of the configuration file */
	private String		filename;
	/** Contains all values for configuration */
	private Properties	config;

	/**
	 * Constructor for the class
	 */
	public Config () {
		log = null;
		filename = null;
		config = new Properties ();
	}
	/**
	 * Constructor for the class
	 * 
	 * @param nLog optional Log instance for logging
	 */
	public Config (Log nLog) {
		this ();
		log = nLog;
	}

	/**
	 * Returns the source from where the configuration had been provided
	 * 
	 * @return the filename of the file or the static string "rsc", if configuration had been provied by a resource
	 */
	public String getSource () {
		return filename == null ? "rsc" : filename;
	}

	/**
	 * Return all keys of the configuration storage
	 * 
	 * @return a set of keys
	 */
	public Set <String> getKeys () {
		return config.stringPropertyNames ();
	}

	/**
	 * Loads a configuration file
	 *
	 * @param nFilename the basename of the file
	 * @return true on success, false otherwise
	 */
	public boolean loadConfig (String nFilename) {
		if (! fileExists (nFilename)) {
			logging (Log.DEBUG, "Requested file " + nFilename + " not found, search alternatives");

			String		classPath = System.getProperty ("java.class.path");
			String		filesep = System.getProperty ("file.separator");

			if (filesep == null) {
				filesep = java.io.File.separator;
				if (filesep == null) {
					filesep = "/";
				}
			}
			int		n = nFilename.lastIndexOf (filesep);

			if (n != -1) {
				nFilename = nFilename.substring (n + 1);
			}

			filename = null;
			if (classPath != null) {
				String		pathsep = System.getProperty ("path.separator");

				if (pathsep == null) {
					pathsep = java.io.File.pathSeparator;
					if (pathsep == null) {
						pathsep = ":";
					}
				}

				String[]	parts = classPath.split (pathsep);

				for (n = 0; n < parts.length; ++n) {
					if (isDirectory (parts[n])) {
						String	fname = parts[n] + "/" + nFilename;

						if (fileExists (fname)) {
							logging (Log.VERBOSE, "Found config file " + fname + " in classpath");
							filename = fname;
							break;
						}
					}
				}
			} else {
				logging (Log.DEBUG, "No class path found to search config file in");
			}

			if (filename == null) {
				String	home = System.getProperty ("user.home");

				if (home != null) {
					filename = scanForConfig (home, filesep, nFilename);
					if (filename != null) {
						logging (Log.VERBOSE, "Found config file " + filename + " while starting search in home directory " + home);
					}
				}
			}
		} else {
			filename = nFilename;
		}
		boolean rc = false;

		if (filename != null) {
			try (FileInputStream fd = new FileInputStream (filename)) {
				config.load (fd);
				rc = true;
			} catch (IOException e) {
				logging (Log.ERROR, "Failed to read config file " + filename + ": " + e.toString ());
			}
		}
		return rc;
	}

	/**
	 * Loads a configuration from a resource
	 * 
	 * This allows geting the configuration from a resource bundle
	 * To retrieve only a selected part of the bundle, you can provide
	 * one or more prefixes of the resource keys to be used. The matching
	 * prefix is removed from the key and the key is converted to upper
	 * case before adding the entry to the configuration storage.
	 *
	 * @param rsc the resource bundle to read data from
	 * @param prefixes the prefixes for the keys
	 * @return true on success, false otherwise
	 */
	public boolean loadConfig (ResourceBundle rsc, String ... prefixes) {
		boolean rc;

		rc = true;
		try {
			for (String key : rsc.keySet ()) {
				try {
					String	value = rsc.getString (key);
					int	pos = 0;

					if ((prefixes.length == 0) || ((pos = matchPrefixes (key, prefixes)) > 0)) {
						if (value.startsWith ("::")) {
							String	ref = value.substring (2).trim ();

							if (rsc.containsKey (ref)) {
								value = rsc.getString (ref);
							} else {
								logging (Log.WARNING, key + " references " + value + " which is not found in resource bundle");
							}
						}
						if (pos > 0) {
							key = key.substring (pos);
						}
						config.setProperty (key.toUpperCase (), value);
					}
				} catch (Exception ex) {
					logging (Log.ERROR, "Failed to parse key " + key + " in resource bundle: " + ex.toString ());
					rc = false;
				}
			}
		} catch (Exception ex) {
			logging (Log.ERROR, "Failed to parse resource bundle: " + ex.toString ());
			rc = false;
		}
		return rc;
	}

	/**
	 * Search for a value
	 *
	 * @param key the key to search for
	 * (@param dftl default value)
	 * @return data if, null otherwise
	 */
	public String cget (String key) {
		return config.getProperty (key);
	}
	/**
	 * Search for a value
	 *
	 * @param key the key to search for
	 * @param dflt default value
	 * @return data if, the default otherwise
	 */
	public String cget (String key, String dflt) {
		String	temp = cget (key);

		return temp == null ? dflt : temp;
	}
	public int cget (String key, int dflt) {
		String	temp = cget (key);

		return temp == null ? dflt : Integer.parseInt (temp);
	}
	public long cget (String key, long dflt) {
		String	temp = cget (key);

		return temp == null ? dflt : Long.parseLong (temp);
	}
	public boolean cget (String key, boolean dflt) {
		String	temp = cget (key);

		return temp == null ? dflt : convertToBool (temp);
	}

	private boolean convertToBool (String str) {
		boolean val = false;

		if (str != null) {
			try {
				char	ch = str.charAt (0);

				switch (ch) {
				case 't':
				case 'T':
				case 'y':
				case 'Y':
				case '1':
				case '+':
					val = true;
					break;
				}
			} catch (IndexOutOfBoundsException e) {
				// do nothing
			}
		}
		return val;
	}

	private void logging (int level, String msg) {
		if (log != null) {
			log.out (level, "config", msg);
		}
	}

	/**
	 * Check for existance of file
	 *
	 * @param fname filename to check
	 * @return true if file exists, false otherwise
	 */
	private boolean fileExists (String fname) {
		boolean exists = false;

		try {
			File	f = new File (fname);

			exists = f.exists ();
		} catch (Exception e) {
			logging (Log.ERROR, "Failed to get status for file " + fname + ": " + e.toString ());
		}
		return exists;
	}

	/**
	 * Check if a path is a directory
	 *
	 * @param path the path name
	 * @return true if it is a directory, false otherwise
	 */
	private boolean isDirectory (String path) {
		boolean isdir = false;

		try {
			File	f = new File (path);

			isdir = (f.exists () && f.isDirectory ());
		} catch (Exception e) {
			logging (Log.ERROR, "Failed to get status for directory " + path + ": " + e.toString ());
		}
		return isdir;
	}

	/**
	 * Scans start directory for a file
	 * @param base start directory
	 * @param sep path separater
	 * @param fname filename
	 * @return full path, if file is found
	 */
	private String scanForConfig (String base, String sep, String fname) {
		String	rc = null;

		try {
			File		f = new File (base.equals ("") ? sep : base);
			String[]	flist = f.list ();

			if (flist != null) {
				for (int n = 0; (rc == null) && (n < flist.length); ++n)
					if (fname.equals (flist[n])) {
						try {
							String	test = base + sep + flist[n];
							File	temp = new File (test);
							if (temp.exists () && temp.isFile ()) {
								rc = test;
								logging (Log.DEBUG, "Found config file " + test);
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				if (rc == null) {
					for (int n = 0; (rc == null) && (n < flist.length); ++n) {
						String	down = base + sep + flist[n];

						if (isDirectory (down)) {
							rc = scanForConfig (down, sep, fname);
						}
					}
				}
			}
		} catch (Exception e) {
			logging (Log.ERROR, "Failed to scan for config file " + fname + " in " + base + ": " + e.toString ());
		}
		return rc;
	}

	private int matchPrefixes (String key, String[] prefixes) {
		for (int n = 0; n < prefixes.length; ++n) {
			if (key.startsWith (prefixes[n] + ".")) {
				return prefixes[n].length () + 1;
			}
		}
		return 0;
	}
			

}
