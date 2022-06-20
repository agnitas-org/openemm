/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.messages.I18nString;

public class SafeString {
	
	private static final transient Logger logger = LogManager.getLogger( SafeString.class);
	
	/**
	 * Check String to be a safe DbTableName and in case return this String.
	 * Otherwise throws an Exception
	 * 
	 * Allowed characters: a-z0-9_ (min 1, max 32)
	 * 
	 * @param tableName
	 * @return
	 */
	public static String getSafeDbTableName(String tableName) {
		if (StringUtils.isBlank(tableName)) {
			logger.error("Found invalid empty tablename");
			throw new RuntimeException("Found invalid empty tablename");
		} else {
			tableName = tableName.toLowerCase().trim();
			Pattern tableNamePattern = Pattern.compile("^[a-z0-9_]{1,30}$");
			if (tableNamePattern.matcher(tableName).find()) {
				return tableName;
			} else {
				logger.error("Found invalid tablename '" + tableName + "'");
				throw new RuntimeException("Found invalid tablename '" + tableName + "'");
			}
		}
    }

	
	/**
	 * Check String to be a safe DbColumnName and in case return this String.
	 * Otherwise throws an Exception
	 * 
	 * Allowed characters: a-z0-9_ (min 1, max 32)
	 * 
	 * @param tableName
	 * @return
	 */
	public static String getSafeDbColumnName(String columnName) {
		if (StringUtils.isBlank(columnName)) {
			logger.error("Found invalid empty columnname");
			throw new RuntimeException("Found invalid empty columnname");
		} else {
			columnName = columnName.toLowerCase().trim();
			Pattern tableNamePattern = Pattern.compile("^[a-z0-9_]{1,30}$");
			if (tableNamePattern.matcher(columnName).find()) {
				return columnName;
			} else {
				logger.error("Found invalid columnname '" + columnName + "'");
				throw new RuntimeException("Found invalid columnname '" + columnName + "'");
			}
		}
    }
    
    /**
     * Checks if the email string is in correct email adress syntax.
     */
    public static String getEmailSafeString(String input) {
        int at,pt;
        
        if(input == null) {
			return null;
		}
        input=input.toLowerCase().trim();
        if(input.length() < 1) {
			return null;
		}
        if((at=input.indexOf('@')) < 1) {
			return null;
		}
        if((pt=input.indexOf('.',at)) < (at+2)) {
			return null;
		}
        if(pt >= (input.length()-1)) {
			return null;
		}
        return input;
    }
    
    /**
     * Cuts the length of the string to a fixed length.
     *
     * @param len Fixed length.
     */
    public static String cutLength(String input, int len) {
        if(input.length()>len) {
			input=input.substring(0, len);
		}
        
        return input;
    }
    
    /**
     * Cuts the length of the string to a fixed length.
     *
     * @param len Fixed length.
     */
    public static String cutByteLength(String input, long len) {
        
        try {
            while(input.getBytes("UTF-8").length>len) {
                input=input.substring(0, input.length()-1);
            }
        } catch (Exception e) {
            logger.error("cutByteLength", e);
        }
        return input;
    }
    
    /**
     * Cuts the length of the line to a length of 72 characters.
     */
    public static String cutLineLength(String input) {
        return SafeString.cutLineLength(input, 72);
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
     * Replaces the characters in a substring.
     *
     * @param str Input string.
     * @param pattern repalceable part of str.
     * @param replace String that should be replaced.
     */
    public static String replaceIgnoreCase(String str, String pattern, String replace) {
        StringBuffer regex=new StringBuffer();
        String letter;
        String toLower;
        for(int i=0; i<pattern.length(); i++) {
            letter=Character.toString(pattern.charAt(i)).toUpperCase();
            toLower = letter.toLowerCase();
            if(letter.equals(toLower)) {
                regex.append(letter);
            } else {
                regex.append("["+letter+toLower+"]");
            }
        }
        
        return str.replaceAll(regex.toString(), replace);
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
    
	/**
	 * Check for valid simple name without blanks.
	 *
	 * @param name the name
	 * @param maxLength the max length
	 * @return true, if successful
	 */
	public static boolean checkForValidSimpleNameWithoutBlanks(String name, int maxLength) {
		if (StringUtils.isBlank(name)) {
			return false;
		} else {
			Pattern tableNamePattern = Pattern.compile("^[A-Za-z0-9_-]{1," + maxLength + "}$");
			if (tableNamePattern.matcher(name).find()) {
				return true;
			} else {
				return false;
			}
		}
    }
    
	/**
	 * Check for valid simple name with blanks.
	 *
	 * @param name the name
	 * @param maxLength the max length
	 * @return true, if successful
	 */
	public static boolean checkForValidSimpleNameWithBlanks(String name, int maxLength) {
		if (StringUtils.isBlank(name)) {
			return false;
		} else {
			Pattern tableNamePattern = Pattern.compile("^[ A-Za-z0-9_-]{1," + maxLength + "}$");
			if (tableNamePattern.matcher(name).find()) {
				return true;
			} else {
				return false;
			}
		}
    }
}
