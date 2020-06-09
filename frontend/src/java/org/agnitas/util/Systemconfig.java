/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import	java.io.File;
import	java.io.FileInputStream;
import	java.io.IOException;
import	java.io.InputStream;
import	java.nio.charset.Charset;
import	java.util.HashMap;
import	java.util.Map;

import	com.fasterxml.jackson.core.JsonFactory;
import	com.fasterxml.jackson.core.JsonParser;
import	com.fasterxml.jackson.core.JsonToken;

/**
 * Class to read and parse the system configuration, either
 * from a file or from the enviroment
 * 
 * Please keep the logic in sync with other implementations
 * of processing the system config file to ensure consistency
 */
public class Systemconfig {
	static final private String	SYSTEM_CONFIG_LEGACY_PATH = "/opt/agnitas.com/etc/licence.cfg";
	static final private String	SYSTEM_CONFIG_PATH = "/opt/agnitas.com/etc/system.cfg";
	static final private String	SYSTEM_CONFIG_PATH_ENV = "SYSTEM_CONFIG_PATH";
	static final private String	SYSTEM_CONFIG_ENV = "SYSTEM_CONFIG";
	private Map <String, String> cfg;

	public Systemconfig (String systemConfigPath) {
		String	content = System.getenv (SYSTEM_CONFIG_ENV);
		
		if (content == null) {
			String	path = systemConfigPath != null ? systemConfigPath : System.getenv (SYSTEM_CONFIG_PATH_ENV);
			if (path == null) {
				path = SYSTEM_CONFIG_PATH;
				if ((! fileExists (path)) && fileExists (SYSTEM_CONFIG_LEGACY_PATH)) {
					path = SYSTEM_CONFIG_LEGACY_PATH;
				}
			}

			if ((! path.equals ("-")) && fileExists (path)) {
				File	file = new File (path);
				
				try (InputStream fd = new FileInputStream (file)) {
					byte[]	buffer = new byte[(int) file.length ()];
				
					if (fd.read (buffer) == file.length ()) {
						content = new String (buffer, Charset.forName ("UTF-8"));
					}
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		parseSystemconfig (content);
	}
	public Systemconfig () {
		this (null);
	}

	/**
	 * get whole configuration
	 */
	public Map <String, String> get () {
		return cfg;
	}
	
	/**
	 * get a configuration value, null if not existing
	 */
	public String get (String key) {
		return cfg.get (key);
	}
	
	/**
	 * get a configuration value, dflt if not existing
	 */
	public String get (String key, String dflt) {
		String	rc = get (key);
		
		return rc != null ? rc : dflt;
	}
	
	private boolean fileExists (String path) {
		return (new File (path)).exists ();
	}

	private void parseSystemconfig (String content) {
		cfg = new HashMap <> ();
		
		if (content != null) {
			if (! parseJson (content)) {
				parsePlain (content);
			}
		}
	}
	
	private boolean parseJson (String content) {
		boolean	rc = false;
		
		try (JsonParser parser  = (new JsonFactory ()).createParser (content)) {
			String	name = null;
			int	indent = 0;
			
			while (! parser.isClosed ()) {
				JsonToken	jsonToken = parser.nextToken ();
				
				if (jsonToken == JsonToken.START_OBJECT) {
					++indent;
				} else if (jsonToken == JsonToken.END_OBJECT) {
					--indent;
				} else if (indent == 1) {
					if (jsonToken == JsonToken.FIELD_NAME) {
						name = parser.getValueAsString ();
					} else if (name != null) {
						if (jsonToken == JsonToken.VALUE_NULL) {
							cfg.put (name, "");
						} else if (jsonToken == JsonToken.VALUE_FALSE) {
							cfg.put (name, "false");
						} else if (jsonToken == JsonToken.VALUE_TRUE) {
							cfg.put (name, "true");
						} else if ((jsonToken == JsonToken.VALUE_NUMBER_FLOAT) || (jsonToken == JsonToken.VALUE_NUMBER_INT)) {
							cfg.put (name, parser.getNumberValue ().toString ());
						} else if (jsonToken == JsonToken.VALUE_STRING) {
							cfg.put (name, parser.getText ());
						}
						name = null;
					}
				}
			}
			rc = true;
		} catch (IOException e) {
			// do nothing
		}
		return rc;
	}
	
	private void parsePlain (String content) {
		String	multiLineContent = null;
		String	multiLineName = null;
		
		for (String line : content.split ("(\r?\n)+")) {
			if (multiLineName != null) {
				if (line.equals ("}")) {
					if (multiLineContent == null) {
						throw new RuntimeException("Unexpected empty multiLineContent");
					}
					cfg.put (multiLineName, multiLineContent.trim ());
					multiLineName = null;
				} else {
					multiLineContent += " " + line.trim ();
				}
			} else if ((line.length () > 0) && (! line.startsWith ("#"))) {
				String[]	parsed = line.trim ().split (" *= *", 2);
				
				if (parsed.length == 2) {
					if (parsed[1].equals ("{")) {
						multiLineName = parsed[0];
						multiLineContent = "";
					} else {
						cfg.put (parsed[0], parsed[1]);
					}
				}
			}
		}
	}
}
