/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

import java.nio.charset.Charset;

/**
 * Interface for encodings of byte-arrays to String.
 */
public interface ByteArrayToStringEncoder {

	/**
	 * Encodes a given byte array and returns a resulting String using given {@link Charset}.
	 * An encoder may not use the given {@link Charset}, if the resulting String contains
	 * ASCII characters only. 
	 * 
	 * @param data data to encode
	 * @param charset {@link Charset} to use
	 * 
	 * @return resulting String
	 * 
	 * @throws EncodingException on errors during encoding
	 */
	public String encodeToString(final byte[] data, final Charset charset) throws EncodingException;

}
