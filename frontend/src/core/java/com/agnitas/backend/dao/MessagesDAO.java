/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.util.HashMap;
import	java.util.List;
import	java.util.Map;
import	com.agnitas.backend.DBase;

public class MessagesDAO {
	private Map <String, String>	messages;
	
	public MessagesDAO (DBase dbase) throws SQLException {
		messages = new HashMap <> ();
		
		try (DBase.With with = dbase.with ()) {
			List <Map <String, Object>>	rq;
				
			rq = dbase.query (with.cursor (), "SELECT * FROM messages_tbl WHERE deleted = 0");
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				String			messageKey = dbase.asString (row.get ("message_key"));
				String			defaultValue = dbase.asString (row.get ("value_default"));
					
				messages.put (messageKey, defaultValue);
				for (Map.Entry <String, Object> entry : row.entrySet ()) {
					String	key = entry.getKey ().toUpperCase ();
						
					if (key.startsWith ("VALUE_") && key.length () == 8) {
						String	lang = key.substring (6);
						Object	value = entry.getValue ();
							
						if (value != null) {
							messages.put (lang + ":" + messageKey, value.toString ());
						}
					}
				}
			}
		}
	}
	
	
	public String get (String messageKey, String language) {
		String	message = messages.get (language.toUpperCase () + ":" + messageKey);
		
		if (message == null) {
			message = messages.get (messageKey);
		}
		return message;
	}
}
