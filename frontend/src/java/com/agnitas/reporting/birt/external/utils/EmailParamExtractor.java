/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailParamExtractor {

	
	public static String getOnepixelParam(String emailParam) {
		Pattern pattern  = Pattern.compile("onepixlog=\"([A-Za-z_]+)\"");		
		Matcher matcher = pattern.matcher(emailParam);
		if( matcher.find()) {
			return matcher.group(1);
		}		
		return null;		
	}
	
	public static String getMailformat(String emailParam) {
		Pattern pattern  = Pattern.compile("mailformat=\"([0-9]+)\"");		
		Matcher matcher = pattern.matcher(emailParam);
		if( matcher.find()) {
			return matcher.group(1);
		}		
		return null;
	}
}
