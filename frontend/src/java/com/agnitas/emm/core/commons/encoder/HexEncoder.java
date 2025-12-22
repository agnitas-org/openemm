/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Utility class to encode String or byte arrays 
 * as hex string.
 */
public class HexEncoder implements ByteArrayToStringEncoder {
	
	private static final Logger logger = LogManager.getLogger(HexEncoder.class);
	
	/**
	 * Converts a String to a hex String.
	 * 
	 * @param str unencoded String
	 * 
	 * @return hex-encoded String
	 */
	public static String toHexString(String str) {
		if (StringUtils.isEmpty(str))
			return "";

        return toHexString(str.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Converts an array of bytes to a hex-encoded String.
	 * 
	 * @param bytes array of bytes
	 * 
	 * @return hex-encoded String
	 */
	public static String toHexString(byte[] bytes) {
		return new String(Hex.encodeHex(bytes));
	}
	
	public static byte[] fromHexString(String hexString){
		if (StringUtils.isEmpty(hexString))
			return new byte[0];
			
		try {
			return Hex.decodeHex(hexString.toCharArray());
		} catch (DecoderException e) {
			logger.error("Error occured: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String encodeToString(byte[] data, Charset charset) {
		return new String(Hex.encodeHex(data));
	}
}
