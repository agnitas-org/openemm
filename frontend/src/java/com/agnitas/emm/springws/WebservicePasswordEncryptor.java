/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Class to encrypt a webservice user password and get the password back from en encrypted base64 string.
 * Webservice user passwords are currently needed in clear to verify the WSSE security nonce.
 * Therefore we cannot just hash the password and check its correctness, but need the clients password stored on serverside.
 * This WebservicePasswordEncryptor helps to not store the clear password in servers database.
 */
public final class WebservicePasswordEncryptor {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(WebservicePasswordEncryptor.class);
	
	private static final String ALGORITHM_VERSION_1 = "PBEWithMD5AndDES";
	private static final String ALGORITHM_VERSION_2 = "PBEWithHmacSHA256AndAES_256";
	
	private char[] encryptorPasswordChars;
	
	private ConfigService configService;
	
	private String saltFilePathOverride = null;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setSaltFilePathOverride(String saltFilePathOverride) {
		this.saltFilePathOverride = saltFilePathOverride;
	}
	
	/**
	 * Only the files first line of text is used as password
	 * 
	 * @param passwordFile
	 * @throws IOException
	 */
	private char[] getEncryptorPasswordChars() throws IOException {
		if (encryptorPasswordChars == null) {
			String saltFilePath;
			if (saltFilePathOverride != null) {
				saltFilePath = saltFilePathOverride;
			} else {
				saltFilePath = configService.getValue(ConfigValue.SystemSaltFile);
			}
			@SuppressWarnings("unchecked")
			final List<String> lines = FileUtils.readLines(new File(saltFilePath), "UTF-8");
			final String encryptorPasswordString = lines.get(0).replace("“", "\""); // Replace some not allowed non-ascii password chars
			encryptorPasswordChars = encryptorPasswordString.toCharArray();
		}
		return encryptorPasswordChars;
	}
	
	public final String encrypt(final String username, final String password) throws Exception {
		return encryptVersion2(username, password);
	}
	
	private final String encryptVersion2(final String username, final String password) throws Exception {
		final byte[] saltBytes = create8ByteSalt(username);
		
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_VERSION_2);
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getEncryptorPasswordChars()));
        final Cipher pbeCipher = Cipher.getInstance(ALGORITHM_VERSION_2);
        
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        
        return AgnUtils.encodeBase64(pbeCipher.doFinal(password.getBytes("UTF-8")));
    }

	public final String decrypt(final String username, final String encryptedPasswordBase64) throws Exception {
		try {
			return decryptVersion2(username, encryptedPasswordBase64);
		} catch(final GeneralSecurityException e) {
			// Cannot decrypt with stronger cipher, falling back to old one
			return decryptVersion1(username, encryptedPasswordBase64);
		}
    }
	
	private final String decryptVersion2(final String username, final String encryptedPasswordBase64) throws Exception {
		final byte[] saltBytes = create8ByteSalt(username);
		
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_VERSION_2);
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getEncryptorPasswordChars()));
        final Cipher pbeCipher = Cipher.getInstance(ALGORITHM_VERSION_2);
        
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        
        return new String(pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedPasswordBase64)), "UTF-8");
	}
	
	private final String decryptVersion1(final String username, final String encryptedPasswordBase64) throws Exception {
		final byte[] saltBytes = create8ByteSalt(username);
		
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_VERSION_1);
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getEncryptorPasswordChars()));
        final Cipher pbeCipher = Cipher.getInstance(ALGORITHM_VERSION_1);
        
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20));
        
        return new String(pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedPasswordBase64)), "UTF-8");
	}
	
	private static final byte[] create8ByteSalt(final String username) throws UnsupportedEncodingException {
		if (username.length() < 8) {
			return (username + AgnUtils.repeatString("=", 8 - username.length())).getBytes("UTF-8");
		} else {
			return username.substring(0, 8).getBytes("UTF-8");
		}
	}
}
