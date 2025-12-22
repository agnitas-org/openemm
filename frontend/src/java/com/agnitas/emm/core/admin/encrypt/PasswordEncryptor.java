/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.Sha1Encoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.exception.SaltFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to encrypt passwords.
 */
public class PasswordEncryptor {
	
	private static final Logger LOGGER = LogManager.getLogger(PasswordEncryptor.class);
	
	/** Salt bytes. */
	private byte[] hexSaltBytes;
	
	/** Encoder for SHA-1 hash. */
	private final Sha1Encoder sha1Encoder = new Sha1Encoder();
	
	/** Encoder for SHA-512 hash. */
	private final Sha512Encoder sha512Encoder = new Sha512Encoder();
	
	/** UTF-8 encoding. */
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	private ConfigService configService;
	
	private String saltFilePathOverride = null;
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setSaltFilePathOverride(String saltFilePathOverride) {
		this.saltFilePathOverride = saltFilePathOverride;
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
	private static String readSaltFromFile(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream( file)) {
			try (InputStreamReader isr = new InputStreamReader( fis, StandardCharsets.ISO_8859_1)) {
				try (BufferedReader br = new BufferedReader( isr)) {
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
	 * @param charset character encoding of password
	 * @param encoder encoder used for hashing
	 * 
	 * @return encrypted password
	 */
	public String encrypt(String password, int obfuscatingValue, Charset charset, ByteArrayEncoder encoder) {
		byte[] passwordBytes;
		if (charset != null) {
			passwordBytes = password.getBytes(charset);
		} else {
			passwordBytes = password.getBytes();
		}

		return toPasswordHexString(encoder.encode(plus(passwordBytes, encryptSalt(obfuscatingValue, charset))));
	}
	
	/**
	 * Encrypts the salt using the obfuscation value.
	 * 
	 * @param obfuscatingValue obfuscation value
	 * 
	 * @return encrypted salt
	 */
	private byte[] encryptSalt(int obfuscatingValue, Charset charset) {
		if (hexSaltBytes == null) {
			String saltFilePath;
			if (saltFilePathOverride != null) {
				saltFilePath = saltFilePathOverride;
			} else {
				saltFilePath = configService.getValue(ConfigValue.SystemSaltFile);
			}
			try {
				String hexSalt = toPasswordHexString(readSaltFromFile(new File(saltFilePath)));
				if (charset != null) {
					hexSaltBytes = hexSalt.getBytes(charset);
				} else {
					hexSaltBytes = hexSalt.getBytes();
				}
			} catch (IOException e) {
				LOGGER.fatal( "unable to read salt from file: {}", saltFilePath);
				throw new SaltFileException("unable to read salt from file: " + saltFilePath, e);
			}
		}
		
		return sha1Encoder.encode(plus(hexSaltBytes, Integer.toString(obfuscatingValue, 16).getBytes(StandardCharsets.UTF_8)));
	}
	
	/**
	 * Concatenates two arrays of bytes.
	 * 
	 * @param b0 first array of bytes
	 * @param b1 second array of bytes
	 * 
	 * @return combined array of bytes
	 */
	private static byte[] plus(byte[] b0, byte[] b1) {
		final byte[] b = new byte[b0.length + b1.length];
		
		for(int i = 0; i < b0.length; i++) {
			b[i] = b0[i];
		}
		for(int i = 0; i < b1.length; i++) {
			b[b0.length + i] = b1[i];
		}
		
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
	private static String toPasswordHexString(String str) {
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
	private static String toPasswordHexString(byte[] bytes) {
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
	
	public final boolean isAdminPassword(String password, Admin admin) {
		return isAdminPassword(password, admin, this.sha512Encoder);
	}
	
	public boolean isAdminPassword(String password, Admin admin, ByteArrayEncoder encoder) {
		// TODO: remove this default system encoding and iso encoding in future where all passwords are UTF-8 encoded
		final String encryptedPasswordToCheck_SystemEncoding = encrypt(password, admin.getAdminID(), null, encoder);
		final String encryptedPasswordToCheck_UtfEncoding = encrypt(password, admin.getAdminID(), StandardCharsets.UTF_8, encoder);
		final String encryptedPasswordToCheck_IsoEncoding = encrypt(password, admin.getAdminID(), Charset.forName("ISO-8859-15"), encoder);

		LOGGER.debug(
				"Hash for (admin) password typed by user: {}/{}/{}",
				encryptedPasswordToCheck_SystemEncoding,
				encryptedPasswordToCheck_UtfEncoding,
				encryptedPasswordToCheck_IsoEncoding
		);

		return encryptedPasswordToCheck_SystemEncoding.equalsIgnoreCase(admin.getSecurePasswordHash())
				|| encryptedPasswordToCheck_UtfEncoding.equalsIgnoreCase(admin.getSecurePasswordHash())
				|| encryptedPasswordToCheck_IsoEncoding.equalsIgnoreCase(admin.getSecurePasswordHash());
	
	}
	
	public String computeAdminPasswordHash(String password, int adminID) {
		return encrypt(password, adminID, StandardCharsets.UTF_8, sha512Encoder);
	}

	public boolean isSupervisorPassword(String password, int supervisorID, String expectedHash) {
		return isSupervisorPassword(password, supervisorID, expectedHash, this.sha512Encoder);
	}
	
	private boolean isSupervisorPassword(String password, int supervisorID, String expectedPasswordHash, ByteArrayEncoder encoder) {
		// TODO: remove this default system encoding and iso encoding in future where all passwords are UTF-8 encoded
		final String providedPasswordHash_SystemEncoding = encrypt(password, supervisorID, null, encoder);
		final String providedPasswordHash_UtfEncoding = encrypt(password, supervisorID, StandardCharsets.UTF_8, encoder);
		final String providedPasswordHash_IsoEncoding = encrypt(password, supervisorID, Charset.forName("ISO-8859-15"), encoder);

		return providedPasswordHash_SystemEncoding.equals(expectedPasswordHash)
			|| providedPasswordHash_UtfEncoding.equals(expectedPasswordHash)
			|| providedPasswordHash_IsoEncoding.equals(expectedPasswordHash);
	}
	
	public String computeSupervisorPasswordHash(String password, int supervisorID) {
		return encrypt(password, supervisorID, StandardCharsets.UTF_8, sha512Encoder);
	}

}
