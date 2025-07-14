/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.agnitas.backend.DBase;
import com.agnitas.backend.Data;

public class ConfigDAO {
	private Map<String, Map<String, String>> config;

	public ConfigDAO (DBase dbase) throws SQLException {
		config = new HashMap <> ();
		reread (dbase);
	}
	
	public void reread (DBase dbase) throws SQLException {
		config.clear ();
		try (DBase.With with = dbase.with ()) {
			Set <String>	seenByHost = new HashSet <> ();
			
			for (Map <String, Object> row : dbase.query (with.cursor (), "SELECT class, name, value, hostname FROM config_tbl")) {
				String	configClass = dbase.asString (row.get ("class"));
				String	configName = dbase.asString (row.get ("name"));
				String	configValue = dbase.asString (row.get ("value"));
				String	configHostname = dbase.asString (row.get ("hostname"));
				
				if ((configClass != null) && (configName != null) && (configValue != null) && ((configHostname == null) || Data.selection.match (configHostname))) {
					String			key = configClass + ":" + configName;
					
					if ((configHostname != null) || (! seenByHost.contains (key))) {
						Map <String, String>	entry = config.get (configClass);
					
						if (entry == null) {
							entry = new HashMap<>();
							config.put(configClass, entry);
						}
						entry.put(configName, configValue);
						if (configHostname != null) {
							seenByHost.add(key);
						}
					}
				}
			}
		}
	}

	public Map<String, String> getClassEntry(String className) {
		return config.get(className);
	}
	
	public void add (DBase dbase, String className, String name, String value, String hostname, String description) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			if (dbase.update (with.cursor (),
					  "UPDATE config_tbl " +
					  "SET value = :value, change_date = current_timestamp " +
					  "WHERE class = :class AND name = :name AND hostname = :hostname",
					  "value", value,
					  "class", className,
					  "name", name,
					  "hostname", hostname) == 0) {
				dbase.update (with.cursor (),
					      "INSERT INTO config_tbl " +
					      "       (class, name, value, hostname, description, creation_date, change_date) " +
					      "VALUES " +
					      "       (:class, :name, :value, :hostname, :description, current_timestamp, current_timestamp)",
					      "class", className,
					      "value", value,
					      "name", name,
					      "hostname", hostname,
					      "description", description);
			}
		}
	}
}
