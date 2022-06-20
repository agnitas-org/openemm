/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the IntelliAd tracking string.
 */
public class IntelliAdTrackingStringParser {

	/** Regular expression pattern used for splitting tracking string. */
	private static final Pattern pattern = Pattern.compile( "^(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)$");
	
	/**
	 * Parses the IntelliAd tracking string.
	 * 
	 * @param trackingString tracking string to parse
	 * @return parsed tracking data
	 * 
	 * @throws IntelliAdTrackingStringParserException on errors during parsing
	 */
	public static IntelliAdTrackingData parse( String trackingString) throws IntelliAdTrackingStringParserException {
		Matcher matcher = pattern.matcher( trackingString);
		
		if( !matcher.matches())
			throw new IntelliAdTrackingStringParserException( "Invalid tracking string: " + trackingString);
		
		String customerId = matcher.group( 1);
		String marketId = matcher.group( 2);
		String channelId = matcher.group( 3);
		String campaignId = matcher.group( 4);
		String adgroupId = matcher.group( 5);
		String criterionId = matcher.group( 6);
		
		return new IntelliAdTrackingData(customerId, marketId, channelId, campaignId, adgroupId, criterionId);
	}

}
