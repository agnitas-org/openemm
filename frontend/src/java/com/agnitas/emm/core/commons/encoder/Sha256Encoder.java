/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA256 encoding.
 */
public final class Sha256Encoder implements ByteArrayEncoder {
	/**
	 * The message digest instance.
	 */
	private final MessageDigest algorithm;

	/**
	 * Creates a new encoder instance.
	 * 
	 * @throws RuntimeException on errors creating instance
	 */
	public Sha256Encoder() {
		try {
			this.algorithm = MessageDigest.getInstance("SHA-256");
		} catch( NoSuchAlgorithmException e) {
			throw new RuntimeException( "No SHA256 algorithm", e);
		}
	}
	
	/**
	 * Encodes array of bytes.
	 * 
	 * @param data array of bytes
	 * 
	 * @return Encoded byte array
	 */
	@Override
	public final byte[] encode(final byte[] data) {
		algorithm.reset();	// Make sure, we do include previously hashed data.
		
		return algorithm.digest( data);
	}
	
	/**
	 * Computes SHA512 hash of given String and returns String of hex digits.
	 * For input string UTF-8 is assumed.
	 * 
	 * @param str UTF-8 encoded String to hash
	 * 
	 * @return String of hex digits representing SHA512 hash
	 * 
	 * @throws UnsupportedEncodingException when UTF-8 is not supported
	 */
	public final String encodeToHex(final String str) throws UnsupportedEncodingException {
		return encodeToHex(str, "UTF-8");
	}
	
	/**
	 * Computes SHA512 hash of given String and returns String of hex digits.
	 * For input string given charset is used.
	 * 
	 * @param str String to hash
	 * @param charset charset for String
	 * 
	 * @return String of hex digits representing SHA512 hash
	 * 
	 * @throws UnsupportedEncodingException when given charset is not supported
	 */
	public final String encodeToHex(final String str, final String charset) throws UnsupportedEncodingException {
		final byte[] data = str.getBytes(charset);
		
		return encodeToHex(data);
	}
	/**
	 * Encodes given array of bytes by SHA512 hash and returns a sequence of hex digits as String.
	 * 
	 * @param data data to encode
	 * 
	 * @return String of hex digits
	 */
	public final String encodeToHex(final byte[] data) {
		final byte[] result = encode(data);

		return HexEncoder.toHexString(result);
	}

}
