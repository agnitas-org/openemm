/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class DerInputStream extends FilterInputStream {
	
	public DerInputStream(InputStream in) {
		super(in);
	}
	
	public final BigInteger readInteger() throws IOException {
		int tag = read();
		if(tag != 2)
			throw new IOException(String.format("No INTEGER tag: %02X", tag));
		
		int length = readLength(); 
		
		byte[] data = readData(length);

		return new BigInteger(data);
	}
	
	public final int readLength() throws IOException {
		int data = read();
		
		if(data == -1) {
			throw new IOException("Premature end of file");
		} else if(data >= 0 && data < 128) {
			return data;
		} else {
			final int dataLength = data - 128;
			int length = 0;
			
			for(int i = 0; i < dataLength; i++) {
				data = read();
				if(data == -1)
					throw new IOException("Premature end of file");
				
				length = length * 256 + data;
			}
			
			return length;
		}
			
	}
	
	private final byte[] readData(final int length) throws IOException {
		final byte[] data = new byte[length];
		
		int read;
		
		for(int i = 0; i < length; i++) {
			read = read();
			
			if(read == -1)
				throw new IOException("Premature end of file");
			
			data[i] = (byte)read;
		}
		
		return data;
	}
}
