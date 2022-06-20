/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class SigningOutputStream extends OutputStream {

	private final Signature sig;
	private final int maxDataLength = 128;
	
	private byte[] signature;

	
	public SigningOutputStream(final PrivateKey privateKey, final String algorithm, final String provider) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
		sig = Signature.getInstance(algorithm, provider);
		sig.initSign(privateKey);
	}

	public SigningOutputStream(final PrivateKey privateKey, final String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
		sig = Signature.getInstance(algorithm);
		sig.initSign(privateKey);
	}
	
	@Override
	public final void write(final int b) throws IOException {
		try {
			sig.update((byte) b);
		} catch (final SignatureException e) {
			throw new IOException("Signature error", e);
		}
	}

	@Override
	public final void write(final byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	@Override
	public final void write(final byte[] b, final int off, final int len) throws IOException {
		try {
			final int blockLength = Math.min(len, maxDataLength);
			
			for(int i = off; i < len; i+= maxDataLength) {
				sig.update(b, off , blockLength);
			}
		} catch (final SignatureException e) {
			throw new IOException("Signature error", e);
		}
	}

	@Override
	public final void close() throws IOException {
		try {
			this.signature = this.sig.sign();
		} catch (SignatureException e) {
			throw new IOException("Signature error", e);
		}
	}

	public final byte[] getSignature() {
		if(signature == null) {
			return null;
		} else {
			return Arrays.copyOf(this.signature, this.signature.length);
		}
	}

}
