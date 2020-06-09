/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encrypt;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Required;

/**
 * Encryption class for profile field content.
 */
public class ProfileFieldEncryptor {

	/** Index of matching group for encryption identifier. */
	@SuppressWarnings("unused")
	private static final int A9N_ENCRYPTION_IDENTIFIER_GROUP_INDEX = 1;

	/** Index of matching group for company ID. */
	private static final int A9N_COMPANY_ID_GROUP_INDEX = 2;

	/** Index of matching group for customer ID. */
	@SuppressWarnings("unused")
	private static final int A9N_CUSTOMER_ID_GROUP_INDEX = 3;

	/** Index of matching group for data. */
	private static final int A9N_DATA_GROUP_INDEX = 4;
	
	/** 
	 * Identifier for used encryption method and format (named &quot;A9N&quot;). 
	 *
	 * Data format for encryption is
	 * encryption-identifier &quot;:&quot; company ID &quot;:&quot; customer ID &quot;:&quot; data 
	 * 
	 */
	private static final String ENCRYPTION_IDENTIFIER0 = "a9n";
	
	/** Separator between parts of encrypted data. */
	private static final char SEPARATOR = ':';
	
	/** UTF-8 character set. */
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	/** Pattern for parsing A9N dataformat */
	private static final Pattern A9N_FORMAT = Pattern.compile("^(" + Pattern.quote(ENCRYPTION_IDENTIFIER0) + ")\\:([^:]+)\\:([^:]+)\\:(.*)$");
	
	/** Provider for encryption key. */
	private KeyProvider keyProvider;
	
	/**
	 * Encrypts given data combined with given IDs. IDs are additional security properties to avoid cross-company access.
	 *  
	 * @param data data to encrypt
	 * @param companyId company ID
	 * @param customerId customer ID (for informational purpose only)
	 * 
	 * @return encrypted data
	 * @throws Exception 
	 */
	public String encryptToBase64(final String data, final int companyId, final int customerId) throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append(ENCRYPTION_IDENTIFIER0);
		buffer.append(SEPARATOR);
		buffer.append(companyId);
		buffer.append(SEPARATOR);
		buffer.append(customerId);
		buffer.append(SEPARATOR);
		buffer.append(data);
		
		try {
			Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
			byte[] dataBytes = buffer.toString().getBytes(UTF_8);
			byte[] encryptedBytes = cipher.doFinal(dataBytes);
			
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (InvalidKeyException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid key", e);
		} catch (NoSuchAlgorithmException e) {
			throw new ProfileFieldEncryptorConfigurationException("Unknown crypto algorithm", e);
		} catch (NoSuchPaddingException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid padding", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid crypto algorithm", e);
		} catch (IllegalBlockSizeException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid block size", e);
		} catch (BadPaddingException e) {
			throw new ProfileFieldEncryptorConfigurationException("Bad padding", e);
		}
		
	}
	
	/**
	 * Decrypt given data combined with given IDs. IDs are additional security properties to avoid cross-company access.
	 *  
	 * @param data data to decrypt
	 * @param companyId company ID
	 * 
	 * @return decrypted data (no additional data like IDs)
	 * @throws Exception 
	 */
	public String decryptFromBase64(final String data, final int companyId) throws Exception {
		byte[] encryptedBytes;
		try {
			encryptedBytes = Base64.getDecoder().decode(data);
		} catch (Exception e1) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid base64 data", null);
		}

		try {
			Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			String decryptedData = new String(decryptedBytes, UTF_8);
			
			Matcher m = A9N_FORMAT.matcher(decryptedData);
			if(m.matches()) {
				try {
					int actualCompanyId = Integer.parseInt(m.group(A9N_COMPANY_ID_GROUP_INDEX));
					
					if(actualCompanyId != companyId) {
						throw new ProfileFieldDecryptVerificationException("Mismatching company ID (found " + actualCompanyId + ", expected " + companyId + ")");
					}
					
					return m.group(A9N_DATA_GROUP_INDEX);
				} catch(NumberFormatException e) {
					throw new ProfileFieldDecryptVerificationException("Invalid data format (number excpected)");
				}
			} else {
				throw new ProfileFieldDecryptVerificationException("Invalid data format");
			}
		} catch (InvalidKeyException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid key", e);
		} catch (NoSuchAlgorithmException e) {
			throw new ProfileFieldEncryptorConfigurationException("Unknown crypto algorithm", e);
		} catch (NoSuchPaddingException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid padding", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid crypto algorithm", e);
		} catch (IllegalBlockSizeException e) {
			throw new ProfileFieldEncryptorConfigurationException("Invalid block size", e);
		} catch (BadPaddingException e) {
			throw new ProfileFieldEncryptorConfigurationException("Bad padding", e);
		}
	}
	
	/**
	 * Creates a cipher according to given arguments and configuration.
	 * 
	 * @param mode encryption or decryption mode
	 * 
	 * @return initialized {@link Cipher}
	 * @throws Exception 
	 */
	private Cipher createCipher(int mode) throws Exception {
		SecretKey key = new SecretKeySpec(this.keyProvider.getEncryptionKey(), "DES");
		
		IvParameterSpec ivectorSpecv = new IvParameterSpec(keyProvider.getEncryptionKey());
		
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(mode, key, ivectorSpecv);
		
		return cipher;
	}
	
	/**
	 * Sets the {@link KeyProvider} to be used by the encryptor.
	 * 
	 * @param provider {@link KeyProvider}
	 */
	@Required
	public void setKeyProvider(final KeyProvider provider) {
		this.keyProvider = provider;
	}
}
