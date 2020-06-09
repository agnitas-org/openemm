/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * See Wiki for DeepTracking documentation:
 * http://wiki.agnitas.local/doku.php?id=support:howtos:shopmessung
 */
public class DeepTrackingToken {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(DeepTrackingToken.class);
	
	/**
	 * Secret key for hash generation and data validation
	 */
	private static long XOR_KEY = 568565;
	
	private static long HEX_KEY = 0x5CF16053L;

	/**
	 * Company ID
	 */
	private int companyID;

	/**
	 * Mailing ID
	 */
	private int mailingID;

	/**
	 * Customer ID
	 */
	private int customerID;

	/**
	 * Link ID
	 */
	private int linkID = 0;

	public DeepTrackingToken(int companyID, int mailingID, int customerID, int linkID) {
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.customerID = customerID;
		this.linkID = linkID;
	}
	
	public static DeepTrackingToken parseTokenString(String tokenString) throws Exception {
		if (tokenString.endsWith("gc")) {
			// Old hashed Token UID
			// Cut trailing mandatory "gc"
			tokenString = tokenString.substring(0, tokenString.length() - 2);
			
			int companyID = Integer.parseInt(tokenString.substring(0, 8), 16);
			int mailingID = decryptWithHexKey(tokenString.substring(8, 16));
			int customerID = decryptWithHexKey(tokenString.substring(16, 24));
			int linkID = decryptWithHexKey(tokenString.substring(24, 32));
			String tokenHashValue = tokenString.substring(32);
			
			DeepTrackingToken deepTrackingToken = new DeepTrackingToken(companyID, mailingID, customerID, linkID);
			String validHashValueMD5 = deepTrackingToken.createDataHashWithXorKeyMD5();
			String validHashValueSHA512 = deepTrackingToken.createDataHashWithXorKeySHA512();
			
			if (tokenHashValue.equals(validHashValueMD5) || tokenHashValue.equals(validHashValueSHA512)) {
				return deepTrackingToken;
			} else {
				throw new Exception("Invalid DeepTrackingToken verification: " + tokenString);
			}
		} else if (tokenString.endsWith("g")) {
			// New hashed Token UID
			// Cut trailing mandatory "g"
			tokenString = tokenString.substring(0, tokenString.length() - 1);

			// Watch out: mailingID is first value within the token
			int mailingID = Integer.parseInt(tokenString.substring(0, 8), 16);
			int companyID = decryptWithHexKey(tokenString.substring(8, 16));
			int customerID = decryptWithHexKey(tokenString.substring(16, 24));
			int linkID = decryptWithHexKey(tokenString.substring(24, 32));
			
			String tokenHashValue = tokenString.substring(32);
			
			DeepTrackingToken deepTrackingToken = new DeepTrackingToken(companyID, mailingID, customerID, linkID);
			String validHashValueMD5 = deepTrackingToken.createNewDataHashWithXorKeyMD5();
			String validHashValueSHA512 = deepTrackingToken.createNewDataHashWithXorKeySHA512();

			if (tokenHashValue.equals(validHashValueMD5) || tokenHashValue.equals(validHashValueSHA512)) {
				return deepTrackingToken;
			} else {
				throw new Exception("Invalid DeepTrackingToken verification: " + tokenString);
			}
		} else {
			throw new Exception("Invalid DeepTrackingToken: " + tokenString);
		}
	}

	/**
	 * convert int to hex with special HexKey
	 */
	private static String encryptWithHexKey(int number, int length) throws Exception {
		String text = Long.toHexString(number ^ HEX_KEY);

		// add zeros until long enough
		return leftPadWithZeros(text, length);
	}

	/**
	 * convert hex to int with special HexKey
	 */
	private static int decryptWithHexKey(String encryptedText) throws Exception {
		try {
			return (int) (Integer.parseInt(encryptedText, 16) ^ HEX_KEY);
		} catch (Exception e) {
			throw new Exception("Invalid DeepTrackingToken value: " + encryptedText);
		}
	}

	/**
	 * Add leading zeros to string to match given length
	 */
	private static String leftPadWithZeros(String text, int length) throws Exception {
		int textLngth = text.length();
		
		if (textLngth == length) {
			return text;
		} else if (textLngth < length) {
			return StringUtils.leftPad(text, length, '0');
		} else {
			throw new Exception("Padding error: text length of '" + text + "' exceeds the maximum: " + length);
		}
	}

	public int getCompanyID() {
		return companyID;
	}

	public int getMailingID() {
		return mailingID;
	}

	public int getCustomerID() {
		return customerID;
	}

	public int getLinkID() {
		return linkID;
	}

	/**
	 * Create old hashed Token UID
	 */
	public String createTokenString() throws Exception {
		StringBuilder tokenBuilder = new StringBuilder()
			.append(leftPadWithZeros(Long.toHexString(companyID), 8))
			.append(encryptWithHexKey(mailingID, 8))
			.append(encryptWithHexKey(customerID, 8))
			.append(encryptWithHexKey(linkID, 8))
			.append(createDataHashWithXorKeySHA512())
			.append("gc");
		return tokenBuilder.toString();
	}
	
	/**
	 * Create new hashed Token UID
	 */
	public String createNewTokenString() throws Exception {
		// Watch out: mailingID is first value within the token
		StringBuilder tokenBuilder = new StringBuilder()
			.append(leftPadWithZeros(Long.toHexString(mailingID), 8))
			.append(encryptWithHexKey(companyID, 8))
			.append(encryptWithHexKey(customerID, 8))
			.append(encryptWithHexKey(linkID, 8))
			.append(createNewDataHashWithXorKeySHA512())
			.append("g");
		return tokenBuilder.toString();
	}
	
	/**
	 * Create the verification hash key for this DeepTrackingToken
	 * 
	 * @deprecated To be removed after full introducion of SHA512
	 */
	@Deprecated
	private String createDataHashWithXorKeyMD5() throws Exception {
		try {
			StringBuilder hashBase = new StringBuilder("agn")
				.append(mailingID)
				.append("a")
				.append(linkID)
				.append("g")
				.append(customerID)
				.append("n")
				.append(XOR_KEY)
				.append("i")
				.append(companyID)
				.append("t");
			StringBuilder hashResult = new StringBuilder();
			
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.reset();
			md5Digest.update(hashBase.toString().getBytes("US-ASCII"));
			byte[] hashBytes = md5Digest.digest();
			
			for (int i = 0; i < hashBytes.length; i = i + 4) {
				Byte hashByte = hashBytes[i];
				hashResult.append(leftPadWithZeros(Integer.toHexString(hashByte.intValue() & 255), 2));
			}
			return hashResult.toString();
		} catch (Exception e) {
			throw new Exception("Couldn't make digest of partial content: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Create the verification hash key for this DeepTrackingToken
	 * This is the same algorithm as "createDataHashWithXorKey", but sum of customerID and linkID is used as customerID -value in hashBase
	 * This seems to be some legacy bug
	 * 
	 * @deprecated To be removed after full introducion of SHA512
	 */
	@Deprecated
	private String createNewDataHashWithXorKeyMD5() throws Exception {
		try {
			StringBuilder hashBase = new StringBuilder("agn")
				.append(mailingID)
				.append("a")
				.append(linkID)
				.append("g")
				.append(customerID + linkID)
				.append("n")
				.append(XOR_KEY)
				.append("i")
				.append(companyID)
				.append("t");
			StringBuilder hashResult = new StringBuilder();
			
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.reset();
			md5Digest.update(hashBase.toString().getBytes("US-ASCII"));
			byte[] hashBytes = md5Digest.digest();
			
			for (int i = 0; i < hashBytes.length; i = i + 4) {
				Byte hashByte = hashBytes[i];
				hashResult.append(leftPadWithZeros(Integer.toHexString(hashByte.intValue() & 255), 2));
			}
			return hashResult.toString();
		} catch (Exception e) {
			throw new Exception("Couldn't make digest of partial content: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Create the verification hash key for this DeepTrackingToken
	 */
	private String createDataHashWithXorKeySHA512() throws Exception {
		try {
			StringBuilder hashBase = new StringBuilder("agn")
				.append(mailingID)
				.append("a")
				.append(linkID)
				.append("g")
				.append(customerID)
				.append("n")
				.append(XOR_KEY)
				.append("i")
				.append(companyID)
				.append("t");
			StringBuilder hashResult = new StringBuilder();

			MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
			sha512Digest.reset();
			sha512Digest.update(hashBase.toString().getBytes("US-ASCII"));
			byte[] hashBytes = sha512Digest.digest();
			
			for (int i = 0; i < hashBytes.length; i = i + 4) {
				Byte hashByte = hashBytes[i];
				hashResult.append(leftPadWithZeros(Integer.toHexString(hashByte.intValue() & 255), 2));
			}
			return hashResult.toString();
		} catch (Exception e) {
			throw new Exception("Couldn't make digest of partial content: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Create the verification hash key for this DeepTrackingToken
	 * This is the same algorithm as "createDataHashWithXorKey", but sum of customerID and linkID is used as customerID -value in hashBase
	 * This seems to be some legacy bug
	 */
	private String createNewDataHashWithXorKeySHA512() throws Exception {
		try {
			StringBuilder hashBase = new StringBuilder("agn")
				.append(mailingID)
				.append("a")
				.append(linkID)
				.append("g")
				.append(customerID + linkID)
				.append("n")
				.append(XOR_KEY)
				.append("i")
				.append(companyID)
				.append("t");
			StringBuilder hashResult = new StringBuilder();
			
			MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
			sha512Digest.reset();
			sha512Digest.update(hashBase.toString().getBytes("US-ASCII"));
			byte[] hashBytes = sha512Digest.digest();
			
			for (int i = 0; i < hashBytes.length; i = i + 4) {
				Byte hashByte = hashBytes[i];
				hashResult.append(leftPadWithZeros(Integer.toHexString(hashByte.intValue() & 255), 2));
			}
			return hashResult.toString();
		} catch (Exception e) {
			throw new Exception("Couldn't make digest of partial content: " + e.getMessage(), e);
		}
	}
	
	@Override
	public String toString() {
		return "Company ID: " + companyID + "\n"
			+ "Mailing ID: " + mailingID + "\n"
			+ "Customer ID: " + customerID+ "\n"
			+ "Link ID: " + linkID;
	}
}
