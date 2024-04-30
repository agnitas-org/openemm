/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.pemfile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.agnitas.emm.util.io.DerInputStream;

public class PemUtil {
	
	public static final PrivateKey readRSAPrivateKeyFromDerEncodedBinaryData(final byte[] keyData) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		try(final ByteArrayInputStream bais = new ByteArrayInputStream(keyData)) {
			try(final DerInputStream dis = new DerInputStream(bais)) {
				dis.read();										// Skip header of DER structure
				dis.readLength();								// Skip size of DER structure
				
				/*final BigInteger version = */dis.readInteger();	// Skip version indicator
				
				final BigInteger modulus = dis.readInteger();
				final BigInteger publicExponent = dis.readInteger();
				final BigInteger privateExponent = dis.readInteger();
				final BigInteger prime1 = dis.readInteger();
				final BigInteger prime2 = dis.readInteger();
				final BigInteger exponent1 = dis.readInteger();
				final BigInteger exponent2 = dis.readInteger();
				final BigInteger coefficient = dis.readInteger();

				final KeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, prime1, prime2, exponent1, exponent2, coefficient);
						final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
						final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
						
				return privateKey;
			}
		}
	}
	
	public static final PrivateKey readRSAPrivateKeyFromBase64String(final String base64Encoded) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		final String strippedBase64Encoded = base64Encoded.replaceAll("-----BEGIN [^-]*-----", "").replaceAll("-----END [^-]*-----", "");

		final byte[] keyData = Base64.decodeBase64(strippedBase64Encoded.getBytes(StandardCharsets.US_ASCII));

		return readRSAPrivateKeyFromDerEncodedBinaryData(keyData);
	}
	
	public static final PrivateKey readRSAPrivateKeyFromPemFile(final String filename) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		final String base64Encoded = readFile(filename);

		return readRSAPrivateKeyFromBase64String(base64Encoded);
	}
	
	private static final String readFile(final String filename) throws FileNotFoundException, IOException {
		try(final FileReader fr = new FileReader(filename)) {
			try(final BufferedReader in = new BufferedReader(fr)) {
				final StringBuffer buffer = new StringBuffer();
				String line;
				
				while((line = in.readLine()) != null)
					buffer.append(line).append("\n");
				
				return buffer.toString();
			}
		}
	}
}
