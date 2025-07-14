/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.agnitas.util.Str;
import com.agnitas.util.Systemconfig;

/**
 * general class to read configuration files
 */
public class Config {
	/**
	 * Contains all values for configuration
	 */
	private Map <String, String>  config;
	/**
	 * namespace for replacing configuration values
	 */
	private Map<String, String> namespace;
	/**
	 * lock (unchangable) configuration values
	 */
	private Set <String> locked;
	
	/**
	 * Constructor for the class
	 */
	public Config(String systemConfigPrefix) {
		config = new HashMap <> ();
		namespace = new HashMap<>();
		locked = new HashSet <> ();
		namespace.put("home", Systemconfig.home);
		namespace.put("fqdn", Systemconfig.fqdn);
		namespace.put("hostname", Systemconfig.hostname);
		namespace.put("user", Systemconfig.user);
		namespace.put("version", Systemconfig.version);
		String[] versionParts = Systemconfig.version.split("\\.");
		String major, minor, patchlevel, hotfix;

		major = minor = "?";
		patchlevel = hotfix = "000";
		if (versionParts.length > 1) {
			major = versionParts[0];
			minor = versionParts[1];
			if (versionParts.length > 2) {
				patchlevel = versionParts[2];
				if (versionParts.length > 3) {
					hotfix = versionParts[3];
				}
			}
		}
		namespace.put ("major", major);
		namespace.put ("minor", minor);
		namespace.put ("patchlevel", patchlevel);
		namespace.put ("hotfix", hotfix);
		namespace.put("ApplicationMajorVersion", major);
		namespace.put("ApplicationMinorVersion", minor);
		namespace.put("ApplicationMicroVersion", patchlevel);
		namespace.put("ApplicationHotfixVersion", hotfix);
		Data.syscfg
			.get ()
			.entrySet ()
			.stream ()
			.filter (entry -> (systemConfigPrefix == null) || (entry.getKey ().startsWith (systemConfigPrefix)))
			.forEach (entry -> {
				String	key = entry.getKey ();
				
				if (systemConfigPrefix != null) {
					key = key.substring (systemConfigPrefix.length ());
				}
				set (key, entry.getValue ());
			});
	}

	/**
	 * Return all keys of the configuration storage
	 *
	 * @return a set of keys
	 */
	public Set<String> getKeys() {
		return config.keySet ();
	}

/*
	public Map<String, String> namespace() {
		return namespace;
	}
*/	
	public void lock (String key) {
		locked.add (key.toLowerCase ());
	}
	public void unlock (String key) {
		locked.remove (key.toLowerCase ());
	}
	public void set(String key, String value) {
		key = key.toLowerCase ();
		if (! locked.contains (key)) {
			config.put (key, value);
		}
	}

	public Map <String, String> copy () {
		Map <String, String>	rc = new HashMap <> ();
		
		config
			.entrySet ()
			.stream ()
			.forEach (entry -> set (entry.getKey ().toString (), entry.getValue ().toString ()));
		return rc;
	}

	/**
	 * Search for a value
	 *
	 * @param key the key to search for
	 *            (@param dftl default value)
	 * @return data if, null otherwise
	 */
	public String cget(String key) {
		return fill(config.get(key.toLowerCase ()));
	}

	/**
	 * Search for a value
	 *
	 * @param key  the key to search for
	 * @param dflt default value
	 * @return data if, the default otherwise
	 */
	public String cget(String key, String dflt) {
		String temp = cget(key);

		return temp == null ? dflt : temp;
	}

	public int cget(String key, int dflt) {
		String temp = cget(key);

		return temp == null ? dflt : Integer.parseInt(temp);
	}

	public long cget(String key, long dflt) {
		String temp = cget(key);

		return temp == null ? dflt : Long.parseLong(temp);
	}

	public boolean cget(String key, boolean dflt) {
		String temp = cget(key);

		return temp == null ? dflt : convertToBool(temp);
	}

	private boolean convertToBool(String str) {
		return Str.atob (str, false);
	}

	private String fill(String s) {
		return s != null ? Str.fill(s, namespace) : s;
	}
}
