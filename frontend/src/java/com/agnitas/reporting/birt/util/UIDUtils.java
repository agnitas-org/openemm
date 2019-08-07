/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.ComAdmin;

public class UIDUtils {
	public static String createUID(ComAdmin admin) {
        String adminIDStr = Integer.toString(admin.getAdminID());
    	String companyIDStr = Integer.toString(admin.getCompanyID());
        String hash = admin.getSecurePasswordHash() != null ? admin.getSecurePasswordHash() : "";
        
        /*
         * Have to crop password hash to 32 bytes, otherwise we could run into an exception,
         * that the data exceed 117 bytes.
         * 
         * The problem is, that there is non CBC-able crypto-algorithm, that can work with
         * X509 keys.
         */
        if(hash.length() > 32) {
        	hash = hash.substring(0, 32);
        }
        
		return "<uid adminID=\"" + adminIDStr + "\" companyID=\"" + companyIDStr + "\" password=\"" + hash + "\"/>";
	}
	
	
	/**
	 * 
	 * @param uid - <uid adminID="..." companyID="..." password="..." />
	 * @return [0] := adminID , [1] := companyID , [2] := password , null if the pattern doesn't match
	 */
	
	public static String[] extractSecurityTokensFromUID(String uid) {
				
		String regex = "<uid\\s+(adminID=\"(.*?)\")\\s+(companyID=\"(.*?)\")\\s+(password=\"(.*?)\")\\s*/>" ;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(uid);
			
		if( matcher.find()) {
			return new String[]{matcher.group(2),matcher.group(4),matcher.group(6)};			
		}		
		return null;
	}
	   
}
