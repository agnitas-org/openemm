/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.tokengen;

import java.security.SecureRandom;
import java.util.Objects;

public final class RandomTokenGenerator implements TokenGenerator {

	/** Alphabet to choose symbols from. */
	private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/** Random number generator. */
	private final SecureRandom random;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param random random number genertor
	 */
	public RandomTokenGenerator(final SecureRandom random) {
		this.random = Objects.requireNonNull(random);
	}
	
	@Override
	public final String generateToken(final int tokenLength) {
		final StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < tokenLength; i++)
			sb.append(randomSymbol());

		return sb.toString();
	}
	
	/**
	 * Returns a random symbol from alphabet.
	 * 
	 * @return random symbol
	 */
	private final char randomSymbol() {
		return ALPHABET.charAt(this.random.nextInt(ALPHABET.length()));
	}

}
