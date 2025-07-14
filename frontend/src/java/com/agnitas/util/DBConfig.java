/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class reads the global database access configuration file and
 * parses it and provides methods to retrieve these values.
 */
public class DBConfig {
	private static final String	defaultConfigPath = "/opt/agnitas.com/etc/dbcfg";
	private String			configPath = null;
	private HashMap <String, HashMap <String, String>>	
					content = null;
	private HashMap <String, String>
					selected = null;
	private String			lastError = null;

	/**
	 * If an error had occured, return the last error
	 * 
	 * @return the last error occured or null if no error had occured
	 */
	public String getLastError () {
		return lastError;
	}
	
	/**
	 * Set the path to the configuration file. If this had not been set
	 * the default configuration file will be used.
	 * 
	 * @param path the new path to the configuration file.
	 */
	public void setConfigPath (String path) {
		configPath = path;
		content = null;
	}

	/**
	 * Resets the path of the configuration file to its default location
	 */
	public void resetConfigPath () {
		setConfigPath (null);
	}
	
	/**
	 * Search and return the configuration for the provided database id.
	 * 
	 * All configuration are a key/value pair stored in a map which will
	 * be returned. The available entries depend on the configuration and
	 * from the used dbms.
	 * 
	 * @param id the database id for which the record should be retrieved
	 * @return   the record for the id, if found, null otherwise
	 */
	public HashMap <String, String> find (String id) {
		readConfig ();
		return content != null ? content.get (id) : null;
	}
	
	/**
	 * Returns a list of all known database ids from this configuration
	 * 
	 * @return a list (array of strings) of all known database ids or null, if the configuration file is not accessable
	 */
	public String[] ids () {
		String[]	rc = null;
		
		readConfig ();
		if (content != null) {
			Set <String>	keys = content.keySet ();
			
			rc = keys.toArray (new String[0]);
		}
		return rc;
	}
	
	/**
	 * Select a dabatase id for further access the entries using the findInRecord methods
	 * 
	 * @param id the database id to look for
	 * @return   true if the record had been found, false otherwise
	 */
	public boolean selectRecord (String id) {
		selected = find (id);
		return selected != null;
	}
	
	/**
	 * Select the value for key for the selected database id, return null if
	 * no id had been selected or the key is not found
	 * 
	 * @param key the key to look up
	 * @return    the value of the key, if found, otherwise null
	 */
	public String findInRecord (String key) {
		return selected != null ? selected.get (key) : null;
	}

	/**
	 * Select the value for key for the selected database id, return dflt if
	 * no id had been selected or the key is not found
	 * 
	 * @param key  the key to look up
	 * @param dflt the default value to return
	 * @return     the value of the key, if found, otherwise dflt
	 */
	public String findInRecord (String key, String dflt) {
		String	rc = findInRecord (key);
		
		return rc != null ? rc : dflt;
	}
	public boolean findInRecord (String key, boolean dflt) {
		return Str.atob (findInRecord (key), dflt);
	}

	private static Pattern	parseLine = Pattern.compile ("([a-z0-9._+-]+):[ \t]*(.*)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private boolean readConfig () {
		if (content == null) {
			String	filename = configPath != null ? configPath : System.getenv ("DBCFG_PATH");
			File	fdesc;
			
			if (filename == null) {
				filename = Str.makePath ("$home", "etc", "dbcfg");
				fdesc = new File (filename);
				if (! fdesc.exists ()) {
					fdesc = new File (defaultConfigPath);
				}
			} else {
				fdesc = new File (filename);
			}
			try (FileInputStream stream = new FileInputStream (fdesc)) {
				byte[]	buffer = new byte[(int) fdesc.length ()];
				int	n = stream.read (buffer);
				
				if (n == buffer.length) {
					String	data = new String (buffer, "UTF8");
					content = new HashMap<>();
					
					for (String line : data.split ("\n")) {
						line = line.trim ();
						if ((line.length () == 0) || line.startsWith ("#")) {
							continue;
						}
						Matcher	m = parseLine.matcher (line);
						if (m.find ()) {
							String	id = m.group (1);
							String	tokens = m.group (2);
							HashMap	<String, String>
								entity = new HashMap<>();
							
							for (String token : tokens.split (", [ \t]*")) {
								String[]	parts = token.split ("[ \t]*=[ \t]*", 2);
								
								if (parts.length == 2) {
									entity.put (parts[0], parts[1]);
								}
							}
							content.put (id, entity);
						}
					}
				} else {
					lastError = "File " + filename + " incomplete, expected " + buffer.length + " bytes, but read " + n + " bytes";
				}
			} catch (FileNotFoundException e) {
				lastError = "File not found: " + filename;
			} catch (UnsupportedEncodingException e) {
				lastError = "File " + filename + " has invalid content (expecting UTF8): " + e.toString ();
			} catch (IOException e) {
				lastError = "File " + filename + " read error: " + e.toString ();
			}
		}
		return content != null;
	}
}
