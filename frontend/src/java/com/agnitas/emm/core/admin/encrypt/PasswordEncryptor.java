/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.encrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.Sha1Encoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;

/**
 * Utility class to encrypt passwords.
 */
public class PasswordEncryptor {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(PasswordEncryptor.class);
	
	/** Fixed charset used to read salt from file. */
	private static final String SALT_CHARSET = "ISO8859_1";
	
	/** Hex encoded salt. */
	private final String hexSalt;
	
	/** Encoder for SHA-1 hash. */
	private final Sha1Encoder sha1Encoder;
	
	/** Encoder for SHA-512 hash. */
	private final Sha512Encoder sha512Encoder;
	
	/** UTF-8 encoding. */
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	/**
	 * Creates a new password encryptor instance.
	 * 
	 * @param file File containing the salt
	 * 
	 * @throws IOException on reading salt
	 */
	public PasswordEncryptor(final File file) throws IOException {
		this.sha1Encoder = new Sha1Encoder();
		this.sha512Encoder = new Sha512Encoder();

		try {
			this.hexSalt = toPasswordHexString( readSaltFromFile( file));
		} catch( IOException e) {
			logger.fatal( "unable to read salt from file: " + file.getAbsolutePath());
			
			throw e;
		}
	}
	
	/**
	 * Read the salt String from file.
	 * 
	 * @param file the file containing the salt
	 * 
	 * @return salt read from file
	 * 
	 * @throws IOException on errors accessing salt file
	 */
	private static final String readSaltFromFile(final File file) throws IOException {
		try(final FileInputStream fis = new FileInputStream( file)) {
			try(final InputStreamReader isr = new InputStreamReader( fis, SALT_CHARSET)) {
				try(final BufferedReader br = new BufferedReader( isr)) {
					return br.readLine();
				}
			}
		}
	}
	
	/**
	 * Encrypts a password using salt and obfuscation value. The obfuscation value is 
	 * a known value. The value should be different for different users.
	 * 
	 * @param password the password to encrypt
	 * @param obfuscatingValue a value used for obfuscation
	 * @param encoding character encoding of password
	 * @param encoder encoder used for hashing
	 * 
	 * @return encrypted password
	 * @throws Exception 
	 */
	public final String encrypt(final String password, final int obfuscatingValue, final String encoding, final ByteArrayEncoder encoder) throws Exception {
		try {
			byte[] passwordBytes;
			if (encoding != null) {
				passwordBytes = password.getBytes(encoding);
			} else {
				passwordBytes = password.getBytes();
			}
			return toPasswordHexString(encoder.encode(plus(passwordBytes, encryptSalt(obfuscatingValue, encoding))));
		} catch (UnsupportedEncodingException e) {
			logger.error("Cannot encrypt user password: " + e.getMessage(), e);
			throw new Exception("Cannot encrypt user password");
		}
	}
	
	/**
	 * Encrypts the salt using the obfuscation value.
	 * 
	 * @param obfuscatingValue obfuscation value
	 * 
	 * @return encrypted salt
	 * @throws UnsupportedEncodingException 
	 */
	private final byte[] encryptSalt(final int obfuscatingValue, final String encoding) throws Exception {
		byte[] hexSaltBytes;
		if (encoding != null) {
			hexSaltBytes = hexSalt.getBytes(encoding);
		} else {
			hexSaltBytes = hexSalt.getBytes();
		}
		return sha1Encoder.encode(plus(hexSaltBytes, Integer.toString(obfuscatingValue, 16).getBytes("UTF-8")));
	}
	
	/**
	 * Concatenates two arrays of bytes.
	 * 
	 * @param b0 first array of bytes
	 * @param b1 second array of bytes
	 * 
	 * @return combined array of bytes
	 */
	private static final byte[] plus(final byte[] b0, final byte[] b1) {
		final byte[] b = new byte[b0.length + b1.length];
		
		for(int i = 0; i < b0.length; i++)
			b[i] = b0[i];
		for(int i = 0; i < b1.length; i++)
			b[b0.length + i] = b1[i];
		
		return b;
	}
	
	/**
	 * Convert String to sequence of hex digits.
	 * String is assumed to be utf-8-encoded.
	 * 
	 * @param str String to convert
	 * 
	 * @return sequence of hex digits
	 */
	private static final String toPasswordHexString(final String str) {
		final byte[] bytes = str.getBytes( UTF_8);

		return toPasswordHexString( bytes);
	}
	
	/**
	 * Converts an array of bytes to a hex-encoded String for agnitas secure password storage.
	 * This must not be used for other data encoding.
	 * 
	 * @param bytes array of bytes
	 * 
	 * @return hex-encoded String
	 */
	private static final String toPasswordHexString(final byte[] bytes) {
		final StringBuffer buffer = new StringBuffer();
		
		for(int i = 0; i < bytes.length; i++) {
			int k = bytes[i];
			if(k < 0) {
				k += 128;
			}
			
			final String s = ("00" + Integer.toString(k, 16));

			final String nextByteString = s.substring(s.length() - 2);
			
			buffer.append(nextByteString);
		}
		
		return buffer.toString();
	}
	
	public final boolean isAdminPassword(final String password, final ComAdmin admin) throws Exception {
		return isAdminPassword(password, admin, this.sha512Encoder);
	}
	
	public final boolean isAdminPassword(final String password, final ComAdmin admin, final ByteArrayEncoder encoder) throws Exception {
		// TODO: remove this default system encoding and iso encoding in future where all passwords are UTF-8 encoded
		final String encryptedPasswordToCheck_SystemEncoding = encrypt(password, admin.getAdminID(), null, encoder);
		final String encryptedPasswordToCheck_UtfEncoding = encrypt(password, admin.getAdminID(), "UTF-8", encoder);
		final String encryptedPasswordToCheck_IsoEncoding = encrypt(password, admin.getAdminID(), "ISO-8859-15", encoder);

		if(logger.isDebugEnabled()) {
			logger.debug("Hash for (admin) password typed by user: " + encryptedPasswordToCheck_SystemEncoding + "/" + encryptedPasswordToCheck_UtfEncoding + "/" + encryptedPasswordToCheck_IsoEncoding);
		}

		return encryptedPasswordToCheck_SystemEncoding.equalsIgnoreCase(admin.getSecurePasswordHash())
				|| encryptedPasswordToCheck_UtfEncoding.equalsIgnoreCase(admin.getSecurePasswordHash())
				|| encryptedPasswordToCheck_IsoEncoding.equalsIgnoreCase(admin.getSecurePasswordHash());
	
	}
	
	public final String computeAdminPasswordHash(final String password, final int adminID, final int companyID) throws Exception {
		return computeAdminPasswordHash(password, adminID, this.sha512Encoder);
	}
	
	private final String computeAdminPasswordHash(final String password, final int adminID, final ByteArrayEncoder encoder) throws Exception {
		return encrypt(password, adminID, "UTF-8", encoder);
	}
	
	public final boolean isSupervisorPassword(final String password, final int supervisorID, final String expectedHash) throws Exception {
		return isSupervisorPassword(password, supervisorID, expectedHash, this.sha512Encoder);
	}
	
	private final boolean isSupervisorPassword(final String password, final int supervisorID, final String expectedPasswordHash, final ByteArrayEncoder encoder) throws Exception {
		// TODO: remove this default system encoding and iso encoding in future where all passwords are UTF-8 encoded
		final String providedPasswordHash_SystemEncoding = encrypt(password, supervisorID, null, encoder);
		final String providedPasswordHash_UtfEncoding = encrypt(password, supervisorID, "UTF-8", encoder);
		final String providedPasswordHash_IsoEncoding = encrypt(password, supervisorID, "ISO-8859-15", encoder);

		return providedPasswordHash_SystemEncoding.equals(expectedPasswordHash)
			|| providedPasswordHash_UtfEncoding.equals(expectedPasswordHash)
			|| providedPasswordHash_IsoEncoding.equals(expectedPasswordHash);
	}
	
	public final String computeSupervisorPasswordHash(final String password, final int supervisorID) throws Exception {
		return computeSupervisorPasswordHash(password, supervisorID, this.sha512Encoder);
	}
	
	private final String computeSupervisorPasswordHash(final String password, final int supervisorID, final ByteArrayEncoder encoder) throws Exception {
		return encrypt(password, supervisorID, "UTF-8", encoder);
	}

}
