/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.backend;

import	java.sql.SQLException;
import	java.util.regex.Matcher;
import	java.util.regex.Pattern;
import	com.agnitas.backend.dao.MessagesDAO;
import	com.agnitas.util.Str;

public class Messages {
	private static Pattern		parameterPattern = Pattern.compile ("\\{([0-9]+)\\}", Pattern.MULTILINE | Pattern.DOTALL);
	private MessagesDAO		messagesDao;
	
	public Messages (Data data) throws SQLException {
		messagesDao = new MessagesDAO (data.dbase);
	}
	
	public String get (String messageKey, String language, Object ... parameter) {
		String	message = messagesDao.get (messageKey, language);
		
		if (message != null) {
			StringBuffer	result = new StringBuffer ();
			Matcher		matcher = parameterPattern.matcher (message);
			int		position = 0;
			
			while (matcher.find (position)) {
				int	start = matcher.start ();
				
				if (start > position) {
					result.append (message, position, start);
				}
				int	parameterNumber = Str.atoi (matcher.group (1));
				String	replace = (parameterNumber >= 0 && parameterNumber < parameter.length) ? parameter[parameterNumber].toString () : null;
				
				if (replace != null) {
					result.append (replace);
				}
				position = matcher.end ();
			}
			if (position < message.length ()) {
				result.append (message.substring (position));
			}
			return result.toString ();
		}
		return messageKey;
	}
}
