/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Class to encrypt data and get the data back from encrypted base64 string.
 */
public class DataEncryptor {
	private static final Logger logger = LogManager.getLogger(DataEncryptor.class);
	
	private char[] encryptorPassword;
	private byte[] saltBytes;
	private String saltFilePathOverride = null;
	
	private ConfigService configService;
	
	public static final void main(final String...encryptorPassword) throws Exception {
		final DataEncryptor enc = new DataEncryptor();
		enc.setSaltFilePathOverride(AgnUtils.getUserHomeDir() + "/emm.salt");
		System.out.println(enc.encrypt("12345678901234567890"));
	}
	
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
	private void init() throws IOException {
		String saltFilePath;
		if (saltFilePathOverride != null) {
			saltFilePath = saltFilePathOverride;
		} else {
			saltFilePath = configService.getValue(ConfigValue.SystemSaltFile);
		}
		List<String> lines = FileUtils.readLines(new File(saltFilePath), "UTF-8");
		String encryptorPasswordString = lines.get(0).replace("“", "\""); // Replace some not allowed non-ascii password chars
		encryptorPassword = encryptorPasswordString.toCharArray();
		if (encryptorPasswordString.length() < 8) {
			saltBytes = (encryptorPasswordString + AgnUtils.repeatString("=", 8 - encryptorPasswordString.length())).getBytes("UTF-8");
		} else {
			saltBytes = encryptorPasswordString.substring(0, 8).getBytes("UTF-8");
		}
	}
	
	public String encrypt(String dataToEncrypt) throws Exception {
		return encryptByCompatibilityMode(dataToEncrypt);
    }
	
	public String encrypt(byte[] dataToEncrypt) throws Exception {
		return encryptByCompatibilityMode(dataToEncrypt);
	}

	public String decrypt(String encryptedDataBase64) throws Exception {
		try {
			return decryptByLiveMode(encryptedDataBase64);
		} catch(final GeneralSecurityException e) {
			try {
				// Cannot decrypt with stronger cipher, falling back to old one
				return decryptByCompatibilityMode(encryptedDataBase64);
			} catch (Exception e1) {
				logger.error("Compatibility mode decryption had error: " + e1.getMessage(), e1);
				e1.printStackTrace();
				
				// Throw the first exception, because most probably it contains the real problem
				throw e;
			}
		}
    }
	
	public byte[] decryptToByteArray(final String encrypted) throws Exception {
		try {
			return decryptToByteArrayByLiveMode(encrypted);
		} catch(final GeneralSecurityException e) {
			// Cannot decrypt with stronger cipher, falling back to old one
			return decryptToByteArrayByCompatibilityMode(encrypted);
		}
	}
	
	public String encryptByCompatibilityMode(String dataToEncrypt) throws Exception {
		return encryptByCompatibilityMode(dataToEncrypt.getBytes(StandardCharsets.UTF_8));
    }
	
	public String encryptByCompatibilityMode(byte[] dataToEncrypt) throws Exception {
		if (saltBytes == null) {
			init();
		}
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20));
        return AgnUtils.encodeBase64(pbeCipher.doFinal(dataToEncrypt));
    }

	public String encryptByLiveMode(String dataToEncrypt) throws Exception {
		return encryptByLiveMode(dataToEncrypt.getBytes(StandardCharsets.UTF_8));
    }
	
	public String encryptByLiveMode(byte[] dataToEncrypt) throws Exception {
		if (saltBytes == null) {
			init();
		}
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
        final Cipher pbeCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_256");
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        return AgnUtils.encodeBase64(pbeCipher.doFinal(dataToEncrypt));
    }
	
	public byte[] decryptToByteArrayByCompatibilityMode(String encryptedDataBase64) throws Exception {
		if (saltBytes == null) {
			init();
		}
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20));
        return pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedDataBase64));
		
	}

	public String decryptByCompatibilityMode(String encryptedDataBase64) throws Exception {
		return new String(decryptToByteArrayByCompatibilityMode(encryptedDataBase64), "UTF-8");
    }
	
	public byte[] decryptToByteArrayByLiveMode(String encryptedDataBase64) throws Exception {
		if (saltBytes == null) {
			init();
		}
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
        final Cipher pbeCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_256");
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
        return pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedDataBase64));
   	
    }

	public String decryptByLiveMode(String encryptedDataBase64) throws Exception {
		return new String(decryptToByteArrayByLiveMode(encryptedDataBase64), "UTF-8");
    }
}
