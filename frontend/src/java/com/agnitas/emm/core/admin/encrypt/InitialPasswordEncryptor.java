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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.commons.encoder.Sha1Encoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;

/**
 * Utility class to encrypt initial passwords.
 */
public class InitialPasswordEncryptor {
	private static SecureRandom secureRandom = new SecureRandom();
	
	public static void main(String[] args) throws Exception {
		if (args == null || (args.length != 2 && args.length != 3)) {
			System.err.println("Invalid parameters.\nUsage:\n"
				+ "\tInitialPasswordEncryptor <SaltFilePath> <SqlOutFilePath>\n"
				+ "or\n"
				+ "\tInitialPasswordEncryptor <SaltFilePath> -p <password>");
			System.exit(1);
		} else if (args.length == 2) {
			if (!new File(replaceHomeVariables(args[0])).exists()) {
				System.err.println("Invalid parameters.\nSaltFilePath '" + args[0] + "' does not exist.");
				System.exit(1);
			} else if (!new File(replaceHomeVariables(args[0])).isFile()) {
				System.err.println("Invalid parameters.\nSaltFilePath '" + args[0] + "' is not a file.");
				System.exit(1);
			} else if (new File(replaceHomeVariables(args[1])).exists()) {
				System.err.println("Invalid parameters.\nSqlOutFilePath '" + args[1] + "' already exists.");
				System.exit(1);
			} else {
				String generatedPassword = new String(generatePassword(10));
				
				int adminID = 1;
				File saltFile = new File(replaceHomeVariables(args[0]));
				File sqlFile = new File(replaceHomeVariables(args[1]));
		
				InitialPasswordEncryptor passwordEncryptor = new InitialPasswordEncryptor(saltFile);
				String emmPasswordHash = passwordEncryptor.encrypt(generatedPassword, adminID, "UTF-8");
				
				try (FileOutputStream outputStream = new FileOutputStream(sqlFile)) {
					outputStream.write(("UPDATE admin_tbl SET secure_password_hash = '" + emmPasswordHash + "', pwdchange_date = CURRENT_TIMESTAMP, is_one_time_pass = 1 WHERE admin_id = " + adminID + ";").getBytes("UTF-8"));
				}
				
				System.out.println("Generated EMM password: " + generatedPassword);
				System.out.println("Hashed EMM password: " + emmPasswordHash);
				System.out.println("Created SQL file: '" + sqlFile.getAbsolutePath() + "'");
			}
		} else if (args.length == 3) {
			if (!new File(replaceHomeVariables(args[0])).exists()) {
				System.err.println("Invalid parameters.\nSaltFilePath '" + args[0] + "' does not exist.");
				System.exit(1);
			} else if (!new File(replaceHomeVariables(args[0])).isFile()) {
				System.err.println("Invalid parameters.\nSaltFilePath '" + args[0] + "' is not a file.");
				System.exit(1);
			} else if (!"-p".equals(args[1])) {
				System.err.println("Invalid parameters.\nSecond of three must be '-p'.");
				System.exit(1);
			} else {
				String password = args[2];
				
				int adminID = 1;
				File saltFile = new File(replaceHomeVariables(args[0]));
		
				InitialPasswordEncryptor passwordEncryptor = new InitialPasswordEncryptor(saltFile);
				String emmPasswordHash = passwordEncryptor.encrypt(password, adminID, "UTF-8");

				System.out.println(emmPasswordHash);
			}
		}
	}
	
	/** Fixed charset used to read salt from file. */
	private static final String SALT_CHARSET = "ISO8859_1";
	
	/**
	 * Hex encoded salt.
	 */
	private final String hexSalt;
	
	/** Encoder for SHA-1 hash. */
	private final Sha1Encoder sha1Encoder;

	/** Encoder for SHA-512 hash. */
	private final Sha512Encoder sha512Encoder;
	
	/** UTF-8 encoding. */
	private final Charset utf8;
	
	/**
	 * Creates a new password encryptor instance.
	 * 
	 * @param file File containing the salt
	 * 
	 * @throws IOException on reading salt
	 */
	public InitialPasswordEncryptor( File file) throws IOException {
		this.utf8 = Charset.forName( "UTF-8");
		this.sha1Encoder = new Sha1Encoder();
		this.sha512Encoder = new Sha512Encoder();

		try {
			this.hexSalt = toPasswordHexString( readSaltFromFile( file));
		} catch( IOException e) {
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
	private String readSaltFromFile( File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), SALT_CHARSET))) {
			String salt = br.readLine();
			return salt;
		}
	}
	
	/**
	 * Encrypts a password using salt and obfuscation value. The obfuscation value is
	 * a known value. The value should be different for different users.
	 * 
	 * @param password the password to encrypt
	 * @param obfuscatingValue a value used for obfuscation
	 * 
	 * @return encrypted password
	 * @throws Exception
	 */
	public String encrypt(String password, int obfuscatingValue, String encoding) throws Exception {
		try {
			byte[] passwordBytes;
			if (encoding != null) {
				passwordBytes = password.getBytes(encoding);
			} else {
				passwordBytes = password.getBytes();
			}
			return toPasswordHexString(sha512Encoder.encode(plus(passwordBytes, encryptSalt(obfuscatingValue, encoding))));
		} catch (UnsupportedEncodingException e) {
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
	private byte[] encryptSalt(int obfuscatingValue, String encoding) throws Exception {
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
	private byte[] plus( byte[] b0, byte[] b1) {
		byte[] b = new byte[b0.length + b1.length];
		
		for( int i = 0; i < b0.length; i++) {
			b[i] = b0[i];
		}
		for( int i = 0; i < b1.length; i++) {
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
	private String toPasswordHexString( String str) {
		byte[] bytes = str.getBytes( utf8);

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
	private String toPasswordHexString(byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		
		for( int i = 0; i < bytes.length; i++) {
			int k = bytes[i];
			if (k < 0) {
				k += 128;
			}
			
			String s = ("00" + Integer.toString(k, 16));

			String nextByteString = s.substring( s.length() - 2);
			
			buffer.append(nextByteString);
		}
		
		return buffer.toString();
	}

	public static String replaceHomeVariables(String value) {
		if (value != null && value.trim().length() > 0) {
			String homeDir = System.getProperty("user.home");
			if (homeDir != null && homeDir.endsWith(File.separator)) {
				homeDir = homeDir.substring(0, homeDir.length() - 1);
			}
			
			return value.replace("~", homeDir).replace("$HOME", homeDir).replace("${HOME}", homeDir);
		} else {
			return value;
		}
	}

	public static int getRandomNumber(int excludedMaximum) {
		return secureRandom.nextInt(excludedMaximum);
	}

	public static char[] generatePassword(int passwordLength) throws Exception {
		List<Character> passwordLetters = new ArrayList<>();

		char[] upperCase = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		char[] lowerCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		char[] numbers = "0123456789".toCharArray();
		char[] special = "!#$%&?@".toCharArray();

		passwordLetters.add(upperCase[getRandomNumber(upperCase.length)]);
		passwordLetters.add(lowerCase[getRandomNumber(lowerCase.length)]);
		passwordLetters.add(numbers[getRandomNumber(numbers.length)]);
		passwordLetters.add(special[getRandomNumber(special.length)]);
		
		while (passwordLetters.size() < passwordLength) {
			int nextType = getRandomNumber(3);
			if (nextType == 0) {
				passwordLetters.add(upperCase[getRandomNumber(upperCase.length)]);
			} else if (nextType == 1) {
				passwordLetters.add(lowerCase[getRandomNumber(lowerCase.length)]);
			} else if (nextType == 2) {
				passwordLetters.add(numbers[getRandomNumber(numbers.length)]);
			} else if (nextType == 3) {
				passwordLetters.add(special[getRandomNumber(special.length)]);
			}
		}
		
		char[] password = new char[passwordLength];
		for (int i = 0; i < passwordLength; i++) {
			Character nextCharacter = passwordLetters.get(getRandomNumber(passwordLetters.size()));
			password[i] = nextCharacter;
			passwordLetters.remove(nextCharacter);
		}
		
		return password;
	}
}
