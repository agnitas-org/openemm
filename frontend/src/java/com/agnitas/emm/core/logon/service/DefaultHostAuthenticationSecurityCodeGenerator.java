/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link HostAuthenticationSecurityCodeGenerator}.
 */
public class DefaultHostAuthenticationSecurityCodeGenerator implements HostAuthenticationSecurityCodeGenerator {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( DefaultHostAuthenticationSecurityCodeGenerator.class);
	
	/** Set of allowed characters. */
	private String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-+";
	
	@Override
	public String createSecurityCode() {
		if( logger.isInfoEnabled()) {
			logger.info( "Creating new security code of length " + codeLength);
		}
		
		StringBuffer buffer = new StringBuffer();
		
		for( int i = 0; i < this.codeLength; i++) {
			buffer.append( alphabet.charAt( random.nextInt( alphabet.length())));
		}
		
		return buffer.toString();
	}

	
	// --------------------------------------------------------------- Dependency Injection
	/** Random number generator. */
	private Random random;
	
	/** Length of code. */
	private int codeLength;
	
	/**
	 * Set random number generator.
	 * 
	 * @param random random number generator
	 */
	@Required
	public void setRandomNumberGenerator( Random random) {
		this.random = random;
	}
	
	/**
	 * Set length of code to be generated.
	 * 
	 * @param length length of code.
	 */
	@Required
	public void setLengthOfCode( int length) {
		this.codeLength = length;
	}
	
	/**
	 * Set alphabet of allowed characters for security code.
	 * 
	 * Ensure that only US-ASCII characters are used. Security code is case-sensitive!
	 * 
	 * @param alphabet alphabet of allowed characters.
	 */
	public void setAlphabet( String alphabet) {
		this.alphabet = alphabet;
	}
}
