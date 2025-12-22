/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import com.agnitas.messages.I18nString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SafeString {
	
	private static final Logger logger = LogManager.getLogger(SafeString.class);
	
	/**
	 * Check String to be a safe DbTableName and in case return this String.
	 * Otherwise throws an Exception
	 * 
	 * Allowed characters: a-z0-9_ (min 1, max 32)
	 */
	public static String getSafeDbTableName(String tableName) {
		if (StringUtils.isBlank(tableName)) {
			logger.error("Found invalid empty tablename");
			throw new IllegalArgumentException("Found invalid empty tablename");
		} else {
			tableName = tableName.toLowerCase().trim();
			Pattern tableNamePattern = Pattern.compile("^[a-z0-9_]{1,30}$");
			if (tableNamePattern.matcher(tableName).find()) {
				return tableName;
			} else {
				logger.error("Found invalid tablename '{}'", tableName);
				throw new IllegalArgumentException("Found invalid tablename '" + tableName + "'");
			}
		}
    }

	/**
	 * Check String to be a safe DbColumnName and in case return this String.
	 * Otherwise throws an Exception
	 * 
	 * Allowed characters: a-z0-9_ (min 1, max 32)
	 */
	public static String getSafeDbColumnName(String columnName) {
		if (StringUtils.isBlank(columnName)) {
			logger.error("Found invalid empty columnname");
			throw new IllegalArgumentException("Found invalid empty columnname");
		} else {
			columnName = columnName.toLowerCase().trim();
			Pattern tableNamePattern = Pattern.compile("^[a-z0-9_]{1,30}$");
			if (tableNamePattern.matcher(columnName).find()) {
				return columnName;
			} else {
				logger.error("Found invalid columnname '{}'", columnName);
				throw new IllegalArgumentException("Found invalid columnname '" + columnName + "'");
			}
		}
    }
    
    /**
     * Cuts the length of the string to a fixed length.
     *
     * @param len Fixed length.
     */
    public static String cutByteLength(String input, long len) {
        while (input.getBytes(StandardCharsets.UTF_8).length > len) {
            input=input.substring(0, input.length() - 1);
        }
        return input;
    }
    
    /**
     * Cuts the string length into the line length.
     */
    public static String cutLineLength(String input, int lineLength) {
        int posA, posB, posC;
        StringBuffer tmpBuf=null;
        
        posA=0;
        posB=input.indexOf('\n', posA);
        if(posB==-1) {
			posB=input.length();
		}
        
        while(true) {
            if((posB-posA) >= lineLength) {
                posC=input.lastIndexOf(' ', posA+lineLength+1);
                if((posC==-1) || (posC<posA)) {
                    posC=input.indexOf(' ', posA);
                    if((posC<posB) && (posC!=-1)) {
                        tmpBuf=new StringBuffer(input);
                        tmpBuf.insert(posC+1, '\n');
                        input=tmpBuf.toString();
                        posA=posC+2;
                    } else {
                        posA=posB+2;
                    }
                } else {
                    tmpBuf=new StringBuffer(input);
                    tmpBuf.insert(posC+1, '\n');
                    input=tmpBuf.toString();
                    posA=posC+2;
                }
            } else {
                posA=posB+1;
            }
            if(posA+lineLength >= input.length()) {
				break;
			}
            
            posB=input.indexOf('\n', posA);
            if(posB==-1) {
				posB=input.length();
			}
        }
        return input;
    }
    
    /**
     * Gets a locale string.
     */
	public static String getLocaleString(String key, Locale locale) {
		return I18nString.getLocaleString(key, locale);
	}
    
    /**
     * Removes HTML tags from an input string.
     */
    public static String removeHTMLTags(String input) {
        StringBuffer output=new StringBuffer(input);
        int posA, posB=0;
        while((posA=input.indexOf("<"))!=-1) {
            posB=input.indexOf(">", posA);
            if(posB<posA) {
                break;
            }
            output.delete(posA, posB+1);
            input=output.toString();
        }
        
        return output.toString();
    }
}
