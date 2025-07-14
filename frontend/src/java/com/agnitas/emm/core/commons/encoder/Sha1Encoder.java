/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA1 encoding.
 */
public class Sha1Encoder implements ByteArrayEncoder {
	/**
	 * The message digest instance.
	 */
	private final MessageDigest sha1;
	
	/**
	 * Creates a new Sha1Encoder instance.
	 * 
	 * @throws RuntimeException on errors creating instance
	 */
	public Sha1Encoder() {
		try {
			this.sha1 = MessageDigest.getInstance("SHA1");
		} catch( NoSuchAlgorithmException e) {
			throw new RuntimeException( "No SHA1 algorithm", e);
		}
	}
	
	/**
	 * Computes SHA1 hash of array of bytes.
	 * 
	 * @param data array of bytes
	 * 
	 * @return SHA1 hash of byte array
	 */
	@Override
	public byte[] encode( byte[] data) {
		return sha1.digest( data);
	}
}
