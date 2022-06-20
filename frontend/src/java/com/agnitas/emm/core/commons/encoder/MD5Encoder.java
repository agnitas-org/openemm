/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for MD5 hashing.
 * 
 * Do not use this class for new code.
 * 
 * @see Sha1Encoder
 * @see Sha512Encoder
 */
@Deprecated // TODO Used in existing code due to customer specification. Do not use for new code.
public class MD5Encoder implements ByteArrayEncoder {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger( MD5Encoder.class);

	public static final int DIGEST_HEX_LENGTH = 32;

	/**
	 * Size (in bytes) of the buffer for input stream hashing.
	 */
	private static final int BUFFER_SIZE = 8192;

	/**
	 * The message digest instance.
	 */
	private final MessageDigest digest;
	
	/**
	 * Creates a new MD5Encoder instance.
	 * 
	 * @throws RuntimeException on errors creating instance
	 */
	public MD5Encoder() {
		try {
			this.digest = MessageDigest.getInstance("MD5");
		} catch( NoSuchAlgorithmException e) {
			logger.fatal( "No MD5 algorithm???", e);
			
			throw new RuntimeException( "No MD5 algorithm", e);
		}
	}

	/**
	 * Compute hash of the data read from the input stream.
	 *
	 * @param stream a stream to read.
	 * @return a hash.
	 * @throws IOException when stream reading invocation throws one.
	 */
	public byte[] encode(InputStream stream) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytes;

		while ((bytes = stream.read(buffer)) > 0) {
			digest.update(buffer, 0, bytes);
		}

		return digest.digest();
	}

	/**
	 * Compute hash of the data read from the input stream.
	 *
	 * @param stream a stream to read.
	 * @return a hash represented as a hex-string.
	 * @throws IOException when stream reading invocation throws one.
	 */
	public String encodeToHex(InputStream stream) throws IOException {
		return HexEncoder.toHexString(encode(stream));
	}

	/**
	 * Computes hash of array of bytes.
	 * 
	 * @param data array of bytes
	 * 
	 * @return hash of byte array
	 */
	@Override
	public byte[] encode( byte[] data) {
		return digest.digest( data);
	}
	
	/**
	 * Encodes given array of bytes by MD5 hash and returns a sequence of hex digits as String.
	 * 
	 * @param data data to encode
	 * 
	 * @return String of hex digits
	 */
	public String encodeToHex( byte[] data) {
		byte[] result = encode( data);

		return HexEncoder.toHexString( result);
	}
	
	/**
	 * Computes MD5 hash of given String and returns String of hex digits.
	 * For input string UTF-8 is assumed.
	 * 
	 * @param str UTF-8 encoded String to hash
	 * 
	 * @return String of hex digits representing MD5 hash
	 * 
	 * @throws UnsupportedEncodingException when UTF-8 is not supported
	 */
	public String encodeToHex( String str) throws UnsupportedEncodingException {
		return encodeToHex( str, "UTF-8");
	}
	
	/**
	 * Computes MD5 hash of given String and returns String of hex digits.
	 * For input string given charset is used.
	 * 
	 * @param str String to hash
	 * @param charset charset for String
	 * 
	 * @return String of hex digits representing MD5 hash
	 * 
	 * @throws UnsupportedEncodingException when given charset is not supported
	 */
	public String encodeToHex( String str, String charset) throws UnsupportedEncodingException {
		byte[] data = str.getBytes( charset);
		
		return encodeToHex( data);
	}
}
