/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
import org.springframework.beans.factory.annotation.Required;

/**
 * Class to encrypt a webservice user password and get the password back from en encrypted base64 string.
 * Webservice user passwords are currently needed in clear to verify the WSSE security nonce.
 * Therefore we cannot just hash the password and check its correctness, but need the clients password stored on serverside.
 * This WebservicePasswordEncryptor helps to not store the clear password in servers database.
 */
public final class WebservicePasswordEncryptor {

	/** Name of algorithm used for password encryption. */
	private static final String ALGORITHM_VERSION = "PBEWithHmacSHA256AndAES_256";
	
	/** Password for encryption. */
	private char[] encryptorPasswordChars;
	
	/** Service handling configuration. */
	private ConfigService configService;
	
	private String saltFilePathOverride = null;
	
	@Required
	public void setConfigService(final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "configService is null");
	}
	
	public void setSaltFilePathOverride(String saltFilePathOverride) {
		this.saltFilePathOverride = saltFilePathOverride;
	}
	
	/**
	 * Only the files first line of text is used as salt
	 * 
	 * @throws IOException on errors reading salt file
	 * 
	 * @return salt
	 */
	private char[] getEncryptorSaltChars() throws IOException {
		if (encryptorPasswordChars == null) {
			String saltFilePath;
			if (saltFilePathOverride != null) {
				saltFilePath = saltFilePathOverride;
			} else {
				saltFilePath = configService.getValue(ConfigValue.SystemSaltFile);
			}
			final List<String> lines = FileUtils.readLines(new File(saltFilePath), StandardCharsets.UTF_8);
			final String encryptorPasswordString = lines.get(0).replace("“", "\""); // Replace some not allowed non-ascii password chars
			encryptorPasswordChars = encryptorPasswordString.toCharArray();
		}
		return encryptorPasswordChars;
	}
	
	public final String encrypt(final String username, final String password) throws Exception {
		// When migrating to a newer algorithm, implement fallback here
		return encryptVersion2(username, password);
		
	}
	
	private final String encryptVersion2(final String username, final String password) throws Exception {
		final byte[] saltBytes = create8ByteSalt(username);
		
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_VERSION);
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getEncryptorSaltChars()));
        final Cipher pbeCipher = Cipher.getInstance(ALGORITHM_VERSION);
        
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        
        return AgnUtils.encodeBase64(pbeCipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
    }

	public final String decrypt(final String username, final String encryptedPasswordBase64) throws Exception {
		// When migrating to a newer algorithm, implement fallback here
		return decryptVersion2(username, encryptedPasswordBase64);
    }
	
	private final String decryptVersion2(final String username, final String encryptedPasswordBase64) throws Exception {
		final byte[] saltBytes = create8ByteSalt(username);
		
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_VERSION);
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getEncryptorSaltChars()));
        final Cipher pbeCipher = Cipher.getInstance(ALGORITHM_VERSION);
        
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        
        return new String(pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedPasswordBase64)), StandardCharsets.UTF_8);
	}
	
	private static final byte[] create8ByteSalt(final String username) {
		if (username.length() < 8) {
			return (username + AgnUtils.repeatString("=", 8 - username.length())).getBytes(StandardCharsets.UTF_8);
		} else {
			return username.substring(0, 8).getBytes(StandardCharsets.UTF_8);
		}
	}
}
