/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

public class UIDBase64 {
	private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	
	public String encodeLong( long l) {
		return encodeLong( l, true);
	}
	
	public String encodeLong( long l, boolean skipLeadingZero) {
		String encoding = "";
		
		int value = 0;
		
		for( int i = 0; i < 11; i++) {
			
			/*
			 * Due to the length of 64 bit for type long, 2 bits are missing. So, we have to shift by 4 bit
			 * first to get the trailing 6-Bit-blocks correctly aligned.
			 */
			if( i == 0) {
				value = (int)((l & 0xF000000000000000L) >>> 60);
				l <<= 4;
			} else {
				value = (int)((l & 0xFC00000000000000L) >>> 58);
				l <<= 6;
			}
			
			if( value < 0)
				value += 64;

			if( value != 0)
				skipLeadingZero = false;
			
			if( value != 0 || !skipLeadingZero)
				encoding += SYMBOLS.charAt( value);
		}
		
		if( encoding.equals(""))
			encoding = SYMBOLS.substring(0, 1);
		
		return encoding;
	}
	
	public long decodeLong( String base64) {
		long l = 0;
		int index;
		
		for( int i = 0; i < base64.length(); i++) {
			index = SYMBOLS.indexOf( base64.charAt( i));
			
			if( index == -1)
				throw new IllegalArgumentException();
			
			l = l * 64 + index;
		}
		
		return l;
	}

	public String encodeString( String str) throws Exception {
		return encodeBytes(str.getBytes("UTF-8"));
	}
	
	public String encodeBytes( byte[] bytes) {
		int remainingBits = 0;
		int byteIndex = 0;
		int value = 0;
		int tmpValue;
		int maskShift;
		
		String base64 = "";
		
		while( byteIndex < bytes.length || remainingBits > 0) {
			if( remainingBits < 6) {
				if( byteIndex < bytes.length) {
					tmpValue = bytes[byteIndex];
					byteIndex++;
				
					if( tmpValue < 0)
						tmpValue += 256;

					value = value * 256 + tmpValue;
					remainingBits += 8;
					
				} else {
					value <<= (6 - remainingBits);
					remainingBits = 6;
				}
			}
			maskShift = remainingBits - 6;
			
			tmpValue = (value & (63 << maskShift)) >>> maskShift;
			value = value & ~(63 << maskShift);
			remainingBits -= 6;
			base64 += SYMBOLS.charAt(tmpValue);
		}
		
		if( base64.equals( ""))
			base64 = SYMBOLS.substring( 0, 1);

		return base64;
	}
}
