/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import	java.io.File;
import	java.io.FileInputStream;
import	java.io.IOException;
import	java.io.InputStream;
import	java.net.InetAddress;
import	java.net.UnknownHostException;
import	java.nio.charset.Charset;
import	java.nio.charset.StandardCharsets;
import	java.util.ArrayList;
import	java.util.HashMap;
import	java.util.HashSet;
import	java.util.List;
import	java.util.Map;
import	java.util.ResourceBundle;
import	java.util.Set;
import	java.util.regex.Matcher;
import	java.util.regex.Pattern;
import	java.util.stream.Stream;

import	org.apache.commons.lang3.StringUtils;

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
	public static String		fqdn, hostname, user, home, version;
	static {
		try {
			InetAddress i = InetAddress.getLocalHost ();

			fqdn = i.getHostName ().toLowerCase ();
			hostname = fqdn.split ("\\.", 2)[0];
		} catch (UnknownHostException e) {
			fqdn = hostname = "localhost";
		}
		user = System.getProperty ("user.name", "");
		home = System.getProperty ("user.home", ".");
		version = System.getenv ("VERSION");
		if (version == null) {
			version = "unknown";
		}
		try {
			File	buildSpec = new File (Str.makePath (home, "scripts", "build.spec"));
			if (buildSpec.exists ()) {
				try (FileInputStream fd = new FileInputStream (buildSpec)) {
					byte[]		raw = new byte[(int) buildSpec.length ()];
					fd.read (raw);
					String[]	elements = (new String (raw, StandardCharsets.UTF_8).trim ()).split (";");
					if (elements.length > 0) {
						version = elements[0];
					}
				} catch (IOException e) {
					// do nothing
				}
			}
			if ("unknown".equals (version)) {
				Pattern versionPattern = Pattern.compile("[0-9]{2}\\.(01|04|07|10)\\.[0-9]{3}(\\.[0-9]{3})?$");
				List<String> seen = new ArrayList<>();

				version = Stream.of((System.getProperty("java.class.path", "") + ":" + System.getenv().getOrDefault("CLASSPATH", ""))
						.split(File.pathSeparator))
						.map(File::new)
						.map(f -> f.isFile() ? f.getParentFile() : f)
						.distinct()
						.map(f -> {
							try {
								return f.getCanonicalPath();
							} catch (IOException e) {
								return f.getAbsolutePath();
							}
						})
						.distinct()
						.flatMap(p -> Stream.of(p.split(File.separator)))
						.distinct()
						.filter(f -> f.length() > 0).peek(seen::add)
						.map(versionPattern::matcher)
						.filter(Matcher::find)
						.map(Matcher::group)
						.findFirst().orElse(version);
				if ("unknown".equals(version)) {
					try {
						ResourceBundle rsc = ResourceBundle.getBundle("emm");
						String appVersion = rsc.getString("ApplicationVersion");

						if (StringUtils.isNotEmpty(appVersion)) {
							version = appVersion;
						}
					} catch (Exception e) {
						// do nothing
					}
					if ("unknown".equals(version)) {
						Log log = new Log("version", Log.INFO, 0);

						log.out(Log.ERROR, "retrieve", "Failed to retrieve version in:");
						for (String v : seen) {
							log.out(Log.ERROR, "retrieve", "\t\"" + v + "\"");
						}
					}
				}
			}
		} catch (Exception e) {
			Log log = new Log ("version", Log.INFO, 0);
			log.out (Log.ERROR, "retrieve", "Failed to retrieve version: " + e.toString(), e);
		}
	}
	public static class Selection {
		private List <String>	selections;
		private Set <String>	selectionsSet;
		public Selection (String user, String fqdn, String host) {
			selections = new ArrayList <> ();
			selections.add (user + "@" + fqdn);
			selections.add (user + "@" + host);
			selections.add (user + "@");
			selections.add (fqdn);
			selections.add (host);
			selectionsSet = new HashSet <> ();
			selectionsSet.addAll (selections);
		}
		
		public boolean match (String hostnameToCheck) {
			return hostnameToCheck != null && selectionsSet.contains(hostnameToCheck.toLowerCase ());
		}
		
		public String pick (Map <String, String> source, String key) {
			String	value;
			
			for (String selection : selections) {
				if ((value = source.get (key + "[" + selection + "]")) != null) {
					return value;
				}
			}
			return source.get (key);
		}
	}

	private static Systemconfig	syscfg = null;
	public static synchronized Systemconfig create () {
		if (syscfg == null) {
			syscfg = new Systemconfig ();
		}
		return syscfg;
	}
		
	private Map <String, String>	cfg;
	private String			path;
	private long			lastModified;
	private Selection		selection;

	private Systemconfig () {
		String	content = System.getenv (SYSTEM_CONFIG_ENV);
		
		cfg = new HashMap <> ();
		if (content != null) {
			parseSystemconfig (content);
		} else {
			path = System.getenv (SYSTEM_CONFIG_PATH_ENV);
			if (path == null) {
				path = SYSTEM_CONFIG_PATH;
				if ((! fileExists (path)) && fileExists (SYSTEM_CONFIG_LEGACY_PATH)) {
					path = SYSTEM_CONFIG_LEGACY_PATH;
				}
			}
			if (path.equals ("-")) {
				path = null;
			} else {
				lastModified = 0;
				check ();
			}
		}
		selection = selection ();
	}
	
	/**
	 * get whole configuration
	 */
	public Map <String, String> get () {
		check ();
		return new HashMap <> (cfg);
	}
	
	/**
	 * get a configuration value, null if not existing
	 */
	public String get (String key) {
		check ();
		return selection.pick (cfg, key);
	}
	
	/**
	 * get a configuration value, dflt if not existing
	 */
	public String get (String key, String dflt) {
		String	rc = get (key);
		
		return rc != null ? rc : dflt;
	}
	public int get (String key, int dflt) {
		String	rc = get (key);
		
		if (rc != null) {
			try {
				return Integer.parseInt (rc);
			} catch (Exception e) {
				// do nothing
			}
		}
		return dflt;
	}
	public double get (String key, double dflt) {
		String	rc = get (key);
		
		if (rc != null) {
			try {
				return Double.parseDouble (rc);
			} catch (Exception e) {
				// do nothing
			}
		}
		return dflt;
	}
	public boolean get (String key, boolean dflt) {
		String	rc = get (key);
		
		if (rc != null) {
			return Str.atob (rc, dflt);
		}
		return dflt;
	}
	
	public Selection selection () {
		return new Selection (Systemconfig.user, Systemconfig.fqdn, Systemconfig.hostname);
	}

	private boolean fileExists (String filePath) {
		return (new File(filePath)).exists ();
	}

	private synchronized void check () {
		if (path != null) {
			File	file = new File (path);
			
			if (file.exists ()) {
				if ((lastModified == 0) || (file.lastModified () > lastModified)) {
					try (InputStream fd = new FileInputStream (file)) {
						byte[]	buffer = new byte[(int) file.length ()];
					
						if (fd.read (buffer) == file.length ()) {
							parseSystemconfig (new String (buffer, Charset.forName ("UTF-8")));
						}
						lastModified = file.lastModified ();
					} catch (IOException e) {
						cfg.clear ();
					}
				}
			} else {
				cfg.clear ();
			}
		}
	}

	private void parseSystemconfig (String content) {
		cfg.clear ();
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
							put (name, "");
						} else if (jsonToken == JsonToken.VALUE_FALSE) {
							put (name, "false");
						} else if (jsonToken == JsonToken.VALUE_TRUE) {
							put (name, "true");
						} else if ((jsonToken == JsonToken.VALUE_NUMBER_FLOAT) || (jsonToken == JsonToken.VALUE_NUMBER_INT)) {
							put (name, parser.getNumberValue ().toString ());
						} else if (jsonToken == JsonToken.VALUE_STRING) {
							put (name, parser.getText ());
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
					put (multiLineName, multiLineContent.trim ());
					multiLineName = null;
				} else {
					multiLineContent += "\n" + line.trim ();
				}
			} else if ((line.length () > 0) && (! line.startsWith ("#"))) {
				String[]	parsed = line.trim ().split (" *= *", 2);
				
				if (parsed.length == 2) {
					if (parsed[1].equals ("{")) {
						multiLineName = parsed[0];
						multiLineContent = "";
					} else {
						put (parsed[0], parsed[1]);
					}
				}
			}
		}
	}
	
	private void put (String name, String value) {
		cfg.put (name, value);

		int	pos = name.indexOf ('[');

		if ((pos > 0) && (name.charAt (name.length () - 1) == ']')) {
			String		pureName = name.substring (0, pos);
			String[]	selections = name.substring (pos + 1, name.length () - 1).split (", *");
			
			if (selections.length > 1) {
				for (String selection : selections) {
					cfg.put (pureName + "[" + selection + "]", value);
				}
			}
		}
	}
}
