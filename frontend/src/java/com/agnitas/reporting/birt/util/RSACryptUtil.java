/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.agnitas.util.CryptographicUtilities;

public class RSACryptUtil {

	protected static final String ALGORITHM = "RSA";

	/**
	 * 
	 * <p>
	 * Title: RSAEncryptUtil
	 * </p>
	 * <p>
	 * Description: Utility class that helps encrypt and decrypt strings using RSA algorithm
	 * </p>
	 * 
	 * @author Aviran Mordo http://aviran.mordos.com
	 * @version 1.0
	 */

	public static byte[] encrypt(byte[] text, String publicKey) throws Exception {
		byte[] cipherText = null;

		PublicKey key = getPublicKeyFromString(publicKey);

		try {
			//
			// get an RSA cipher object
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// encrypt the plaintext using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text);
		} catch (Exception e) {
			Logger.getLogger(RSACryptUtil.class).error("Error while encrypting: " + e.getMessage());
			throw e;
		}
		return cipherText;
	}

	/**
	 * Encrypt a text using public key. The result is enctypted BASE64 encoded text
	 * 
	 * @param text
	 *            The original unencrypted text
	 * @param publicKey
	 *            The public key
	 * @return Encrypted text encoded as BASE64
	 * @throws java.lang.Exception
	 */
	public static String encrypt(String text, String publicKey) throws Exception {
		String encryptedText;
		try {
			byte[] cipherText = encrypt(text.getBytes("UTF8"), publicKey);
			encryptedText = encodeBASE64(cipherText);
		} catch (Exception e) {
			Logger.getLogger(RSACryptUtil.class).error("Error while encrypting: " + e.getMessage());
			throw e;
		}
		return encryptedText;
	}

	/**
	 * Decrypt text using private key
	 * 
	 * @param text
	 *            The encrypted text
	 * @param privateKey
	 *            The private key
	 * @return The unencrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] decrypt(byte[] text, String privateKey) throws Exception {
		byte[] dectyptedText = null;
		PrivateKey key = getPrivateKeyFromString(privateKey);
		try {
			// decrypt the text using the private key
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(text);
		} catch (Exception e) {
			Logger.getLogger(RSACryptUtil.class).error("Error while decrypting: " + e.getMessage());
			throw e;
		}
		return dectyptedText;

	}

	/**
	 * Decrypt BASE64 encoded text using private key
	 * 
	 * @param text
	 *            The encrypted text, encoded as BASE64
	 * @param privateKey
	 *            The private key
	 * @return The unencrypted text encoded as UTF8
	 * @throws java.lang.Exception
	 */
	public static String decrypt(String text, String privateKey) throws Exception {
		String result;
		try {
			// decrypt the text using the private key
			byte[] dectyptedText = decrypt(decodeBASE64(text), privateKey);
			result = new String(dectyptedText, "UTF8");
		} catch (Exception e) {
			Logger.getLogger(RSACryptUtil.class).error("Error while decrypting: " + e.getMessage());
			throw e;
		}
		return result;

	}

	/**
	 * Convert a Key to string encoded as BASE64
	 * 
	 * @param key
	 *            The key (private or public)
	 * @return A string representation of the key
	 * @throws UnsupportedEncodingException
	 */
	public static String getKeyAsString(Key key) throws Exception {
		return new String(Base64.encodeBase64(key.getEncoded()), "UTF-8");
	}

	/**
	 * Generates Private Key from BASE64 encoded string
	 * 
	 * @param key
	 *            BASE64 encoded string which represents the key
	 * @return The PrivateKey
	 * @throws java.lang.Exception
	 */
	public static PrivateKey getPrivateKeyFromString(String key) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, CryptographicUtilities.BOUNCY_CASTLE_PROVIDER);
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key.getBytes("UTF-8")));
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
	}

	/**
	 * Generates Public Key from BASE64 encoded string
	 * 
	 * @param key
	 *            BASE64 encoded string which represents the key
	 * @return The PublicKey
	 * @throws java.lang.Exception
	 */
	public static PublicKey getPublicKeyFromString(String key) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, CryptographicUtilities.BOUNCY_CASTLE_PROVIDER);
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(key.getBytes("UTF-8")));
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}

	/**
	 * Encode bytes array to BASE64 string
	 * 
	 * @param bytes
	 * @return Encoded string
	 * @throws UnsupportedEncodingException
	 */
	private static String encodeBASE64(byte[] bytes) throws Exception {
		return new String(Base64.encodeBase64(bytes), "UTF-8");
	}

	/**
	 * Decode BASE64 encoded string to bytes array
	 * 
	 * @param text
	 *            The string
	 * @return Bytes array
	 * @throws IOException
	 */
	private static byte[] decodeBASE64(String text) throws IOException {
		return Base64.decodeBase64(text.getBytes("UTF-8"));
	}

	/**
	 * avoid to read the publickey-file each time an encryption is requested
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static String getPublicKey(String filename) throws IOException {
		return readKeyFromFile(filename, true);
	}

	/**
	 * avoid to read the private-key file each time a decryption is requested
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */

	public static String getPrivateKey(String filename) throws IOException {
		return readKeyFromFile(filename, false);
	}

	/**
	 * Read a keyfile generated with 'openssl genrsa...' ,format it
	 * 
	 * @param filename
	 * @param isPublic
	 *            is it the public/private key ?
	 * @return the key as a String
	 * @throws IOException
	 */
	private static String readKeyFromFile(String filename, boolean isPublic) throws IOException {
		StringBuffer keyBuffer = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				keyBuffer.append(line.trim());
			}
		}
		if (isPublic) {
			return keyBuffer.toString().replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
		} else {
			return keyBuffer.toString().replace("-----BEGIN RSA PRIVATE KEY-----", "").replace("-----END RSA PRIVATE KEY-----", "");
		}
	}
}
