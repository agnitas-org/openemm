/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.dao.MessageDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDaoImpl extends BaseDaoImpl implements MessageDao {
	
	@Override
    public Map<String, Map<String, String>> getAllMessages(boolean isIncludeDeleted) {
		// keep SELECT * because of different available languages
        String sql = "SELECT * FROM messages_tbl";
        if (!isIncludeDeleted) {
            sql += " WHERE deleted = 0";
        }
        List<Map<String, Object>> resultList = select(sql);

        List<String> locales = new ArrayList<>();
        locales.add("default");
        locales.addAll(Arrays.asList(AgnUtils.SUPPORTED_LOCALES));

        Map<String, Map<String, String>> result = new HashMap<>();
        for (String locale : locales) {
            result.put(locale, new HashMap<>());
        }

        for (Map<String, Object> row : resultList) {
            String messageKey = (String) row.get("message_key");
            for (String locale : locales) {
                String message = (String) row.get("value_" + locale);
                result.get(locale).put(messageKey, message);
            }
        }

        return result;
    }
	
	/**
	 * Returns a sorted list of all available keys. Deleted keys first
	 */
	@Override
    public List<String> getAllMessageKeys(boolean isIncludeDeleted) {
        return select("SELECT message_key FROM messages_tbl" + (!isIncludeDeleted ? " WHERE deleted = 0" : "") + " ORDER BY deleted DESC, change_date", StringRowMapper.INSTANCE);
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void markAsDeleted(String key, String language) {
    	language = AgnUtils.toLowerCase(language);
    	if (StringUtils.isBlank(language)) {
    		// Execute global delete operation
	        String sql = "UPDATE messages_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP WHERE message_key = ?";
	        update(sql, key);
    	} else if (language.equals("en")) {
    		// Execute single language delete operation
	        String sql = "UPDATE messages_tbl SET value_default = NULL, change_date = CURRENT_TIMESTAMP WHERE message_key = ?";
	        update(sql, key);
    	} else {
    		// Execute single language delete operation
	        String sql = "UPDATE messages_tbl SET value_" + language + " = NULL, change_date = CURRENT_TIMESTAMP WHERE message_key = ?";
	        update(sql, key);
    	}
    }

    @Override
	@DaoUpdateReturnValueCheck
    public boolean insertMessage(String key, String language, String value) {
    	language = AgnUtils.toLowerCase(language);
    	
    	if (StringUtils.isBlank(language) || language.equals("en")) {
    		// Delete any existing previously deleted value
    		if (selectInt("SELECT COUNT(*) FROM messages_tbl WHERE message_key = ? AND deleted = 1", key) > 0) {
    			update("DELETE FROM messages_tbl WHERE message_key = ? AND deleted = 1", key);
    		}
    		
	    	if (selectInt("SELECT COUNT(*) FROM messages_tbl WHERE message_key = ?", key) == 0) {
	    		// Only create value if it doesn't already exist
	    		return update("INSERT INTO messages_tbl (message_key, value_default, creation_date, change_date, deleted) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)", key, value) == 1;
	    	} else {
	    		if (selectInt("SELECT COUNT(*) FROM messages_tbl WHERE message_key = ? AND value_default IS NULL AND deleted = 0", key) == 1) {
		    		// Only set value if it doesn't already exist
		    		return update("UPDATE messages_tbl SET value_default = ?, change_date = CURRENT_TIMESTAMP WHERE message_key = ?", value, key) == 1;
		    	} else {
		            return false;
		    	}
	    	}
    	} else {
    		if (selectInt("SELECT COUNT(*) FROM messages_tbl WHERE message_key = ? AND value_" + language + " IS NULL AND deleted = 0", key) == 1) {
	    		// Only set value if it doesn't already exist
	    		return update("UPDATE messages_tbl SET value_" + language + " = ?, change_date = CURRENT_TIMESTAMP WHERE message_key = ?", value, key) == 1;
	    	} else {
	            return false;
	    	}
    	}
    }
    
    @Override
    public int getMessageKeysQuantity(String prefix, boolean isIncludeDeleted) {
        String escapedPrefix = DbUtilities.escapeLikeExpression(prefix, '\\');
        return selectInt("SELECT count(message_key) FROM messages_tbl WHERE message_key LIKE ? " +
				(!isIncludeDeleted ? " AND deleted = 0" : "") +
				" ORDER BY deleted DESC",escapedPrefix + "%");
    }
}
