/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for stream handling and processing.
 */
public class StreamHelper {
	
	/**
	 * Reads all data from given InputStream and returns a byte array containing that data.
	 * 
	 * @param stream InputStream to read
	 *
	 * @return byte array with all data from stream
	 * 
	 * @throws IOException on errors converting stream to byte array
	 */
	public static byte[] streamToByteArray( InputStream stream) throws IOException {
		int read;
		byte[] buffer = new byte[4096];
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			while( (read = stream.read( buffer)) != -1) {
				baos.write( buffer, 0, read);
			}
			
			return baos.toByteArray();
		}
	}
	
	/**
	 * Reads all data from given InputStream and returns a byte array containing that data.
	 * 
	 * @param stream InputStream to read
	 * @param length number of bytes to read
	 *
	 * @return byte array with all data from stream
	 * 
	 * @throws IOException on errors converting stream to byte array
	 */
	public static byte[] streamToByteArray( InputStream stream, long length) throws IOException {
		int read;
		byte[] buffer = new byte[4096];
		long totalRead = 0;
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			while( (read = stream.read( buffer)) != -1 && totalRead < length) {
				baos.write( buffer, 0, read);
				totalRead += read;
			}
			
			return baos.toByteArray();
		}
	}
}
