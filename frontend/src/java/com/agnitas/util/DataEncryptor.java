/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

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
import com.agnitas.exception.DecryptionException;
import com.agnitas.exception.EncryptionException;
import com.agnitas.exception.SaltFileException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Class to encrypt data and get the data back from encrypted base64 string.
 */
public class DataEncryptor {

	private static final Logger logger = LogManager.getLogger(DataEncryptor.class);

	private char[] encryptorPassword;
	private byte[] saltBytes;
	private String saltFilePathOverride = null;
	
	private ConfigService configService;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setSaltFilePathOverride(String saltFilePathOverride) {
		this.saltFilePathOverride = saltFilePathOverride;
	}
	
	/**
	 * Only the files first line of text is used as password
	 */
	private void init() {
		try {
			String saltFilePath;
			if (saltFilePathOverride != null) {
				saltFilePath = saltFilePathOverride;
			} else {
				saltFilePath = configService.getValue(ConfigValue.SystemSaltFile);
			}
			List<String> lines = FileUtils.readLines(new File(saltFilePath), StandardCharsets.UTF_8);
			String encryptorPasswordString = lines.get(0).replace("“", "\""); // Replace some not allowed non-ascii password chars
			encryptorPassword = encryptorPasswordString.toCharArray();
			if (encryptorPasswordString.length() < 8) {
				saltBytes = (encryptorPasswordString + AgnUtils.repeatString("=", 8 - encryptorPasswordString.length())).getBytes(StandardCharsets.UTF_8);
			} else {
				saltBytes = encryptorPasswordString.substring(0, 8).getBytes(StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			logger.error("Error occurred while reading salt file!", e);
			throw new SaltFileException("Error reading salt file", e);
		}
	}
	
	public String encrypt(String dataToEncrypt) {
		return encryptByLiveMode(dataToEncrypt);
    }
	
	public String encrypt(byte[] dataToEncrypt) {
		return encryptByLiveMode(dataToEncrypt);
	}

	public String decrypt(String encryptedDataBase64) {
		try {
			return decryptByLiveMode(encryptedDataBase64);
		} catch (DecryptionException e) {
			// Cannot decrypt with stronger cipher, falling back to old one
			return decryptByCompatibilityMode(encryptedDataBase64);
		}
    }
	
	public byte[] decryptToByteArray(String encrypted) {
		try {
			return decryptToByteArrayByLiveMode(encrypted);
		} catch (DecryptionException e) {
			// Cannot decrypt with stronger cipher, falling back to old one
			return decryptToByteArrayByCompatibilityMode(encrypted);
		}
	}

	private String encryptByLiveMode(String dataToEncrypt) {
		return encryptByLiveMode(dataToEncrypt.getBytes(StandardCharsets.UTF_8));
    }
	
	private String encryptByLiveMode(byte[] dataToEncrypt) {
		if (saltBytes == null) {
			init();
		}

        try {
			final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
			final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
			final Cipher pbeCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_256");
			final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
			return AgnUtils.encodeBase64(pbeCipher.doFinal(dataToEncrypt));
        } catch (GeneralSecurityException e) {
			throw new EncryptionException("Encryption failed", e);
        }
    }
	
	public byte[] decryptToByteArrayByCompatibilityMode(String encryptedDataBase64) {
		if (saltBytes == null) {
			init();
		}

		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20));
			return pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedDataBase64));
		} catch (GeneralSecurityException e) {
			logger.error("Error occurred while decrypt '%s' by compatibility mode!".formatted(encryptedDataBase64), e);
			throw new DecryptionException("Decryption failed", e);
		}
	}

	private String decryptByCompatibilityMode(String encryptedDataBase64) {
		return new String(decryptToByteArrayByCompatibilityMode(encryptedDataBase64), StandardCharsets.UTF_8);
    }
	
	private byte[] decryptToByteArrayByLiveMode(String encryptedDataBase64) {
		if (saltBytes == null) {
			init();
		}
		try {
			final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_256");
			final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptorPassword));
			final Cipher pbeCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_256");
			final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(saltBytes, 16));
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(saltBytes, 20, ivParamSpec));
			return pbeCipher.doFinal(AgnUtils.decodeBase64(encryptedDataBase64));
		} catch (GeneralSecurityException e) {
			throw new DecryptionException("Decryption failed", e);
		}
    }

	private String decryptByLiveMode(String encryptedDataBase64) {
		return new String(decryptToByteArrayByLiveMode(encryptedDataBase64), StandardCharsets.UTF_8);
    }
}
