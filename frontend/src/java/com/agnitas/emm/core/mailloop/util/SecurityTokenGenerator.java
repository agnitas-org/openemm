/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.util;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generator for Mailloop security tokens.
 */
public class SecurityTokenGenerator {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(SecurityTokenGenerator.class);
	
	/** RNG for generating security tokens. */
	private static final transient Random random = new SecureRandom();
	
	/** Alphabet used for generation of security tokens. */
	private static final transient char[] SECURITY_TOKEN_SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray();

	/** Default length of token. */
	private static final transient int DEFAULT_TOKEN_LENGTH = 32;
	
	/**
	 * Creates a new security token with default length.
	 * 	
	 * @return random security token
	 */
	public static String generateSecurityToken() {
		return generateSecurityToken(DEFAULT_TOKEN_LENGTH);
	}
	
	/**
	 * Creates a security token with given length.
	 * 
	 * @param length length of security token
	 * 
	 * @return random security token
	 */
	public static String generateSecurityToken(final int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("length must be > 0");
		}

		if (logger.isInfoEnabled()) {
			logger.info("Creating Mailloop security token of length " + length);
		}

		final StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			builder.append(SECURITY_TOKEN_SYMBOLS[random.nextInt(SECURITY_TOKEN_SYMBOLS.length)]);
		}

		return builder.toString();
	}
}
