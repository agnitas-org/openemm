/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

/**
 * Encoder for ROT47 obfuscation.
 */
public class Rot47Encoder {

	/**
	 * Encode given String. Moves all printable ASCII characters
	 * (33 to 126 incl.) 47 positions ahead and corrects resulting 
	 * character if it is not in valid range.
	 * All unconvertible characters remain unchanged.
	 * 
	 * @param string String to encode
	 * 
	 * @return ROT47 transformed String
	 */
	public String encode( String string) {
		StringBuffer buffer = new StringBuffer();
		char c;
		
		for( int i = 0; i < string.length(); i++) {
			c = string.charAt( i);
			
			// Is c convertible (in valid range)?
			if( c >= 33 && c <= 126) {
				// Move 47 position ahead
				c += 47;
				
				// Is c outside valid range? Then corrent it
				if( c > 126)
					c -= 94;	// 94: 2*47 = the range of convertible characters
			}
			
			buffer.append( c);
		}
		
		return buffer.toString();
	}

}
