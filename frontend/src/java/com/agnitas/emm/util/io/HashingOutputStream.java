/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

/**
 * Sub-class of FilterOutputStream with computing message digests.
 * 
 * The message digest is valid only after closing the stream.
 */
public class HashingOutputStream extends FilterOutputStream {

	private final MessageDigest messageDigest;
	private byte[] digest;
	
	public HashingOutputStream(final OutputStream out, final String algorithm) throws NoSuchAlgorithmException {
		super(out);
		
		this.messageDigest = MessageDigest.getInstance(algorithm);
	}
	
	public HashingOutputStream(final OutputStream out, final String algorithm, final String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
		super(out);
		
		this.messageDigest = MessageDigest.getInstance(algorithm, provider);
	}
	
	public HashingOutputStream(final OutputStream out, final MessageDigest messageDigest) {
		super(out);
		
		this.messageDigest = messageDigest;
	}

	/**
	 * Returns (a copy of) the message digest of the written data. 
	 * Returns <code>null</code> if stream isn't closed.
	 * 
	 * @return message digest of written data of <code>null</code>.
	 */
	public final byte[] getDigest() {
		if(digest == null) {
			return digest;
		}
		
		// Return copy to protect computed digest;
		return Arrays.copyOf(this.digest, this.digest.length); 
	}

	@Override
	public final void write(final int b) throws IOException {
		super.write(b);
		this.messageDigest.update((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		this.messageDigest.update(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		this.messageDigest.update(b, off, len);
	}

	/**
	 * Closes stream and finalized the message digest.
	 * 
	 * @throws IOException on errors closing stream
	 */
	@Override
	public void close() throws IOException {
		super.close();
		
		this.digest = this.messageDigest.digest();
	}
	
}
