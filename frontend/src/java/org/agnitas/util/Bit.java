/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

/**
 * Class to provide several static methods for handling
 * bit coded values
 */
public class Bit {
	/**
	 * Creates a bit mask from provided array of bits.
	 * 
	 * These are the bits in the range of 0 .. maxBits-1
	 * (where maxBits may be implemention dependend and is
	 * not validated). The bitmask is created by setting
	 * the bit of each provided position in the result.
	 * 
	 * @param bits the array of bits to create the bitmask from
	 * @return     the created bitmask as a long value
	 */
	static public long createBitmask (int[] bits) {
		long	rc = 0;
		
		for (int bit : bits) {
			rc |= 1 << bit;
		}
		return rc;
	}
	
	/**
	 * Creates a bitmask using method createBitmask but
	 * accepting a variable number of arguments
	 * 
	 * @param bits the bits to create a bitmask from as variable argument
	 * @return     the created bitmask as a long value
	 */
	static public long bitmask (int ... bits) {
		return createBitmask (bits);
	}
	
	/**
	 * Checks if at least one of the provided bits are set
	 * 
	 * @param value the value to check against
	 * @param bits  a list of bits as variable argument to check
	 * @return      true, if at least one provided bit is set, false otherwise or if no bits had been provided
	 */
	static public boolean isset (long value, int ... bits) {
		return 0 != (value & createBitmask (bits));
	}
	static public boolean isset (int value, int ... bits) {
		return 0 != (value & createBitmask (bits));
	}

	/**
	 * Set all provied bits for value
	 * 
	 * @param value the value as a base to add all provided bits
	 * @param bits  a list of bits as variable argument to set
	 * @return      the updated value with all provided bits set
	 */
	static public long set (long value, int ... bits) {
		return value | createBitmask (bits);
	}
	static public int set (int value, int ... bits) {
		return value | (int) createBitmask (bits);
	}

	/**
	 * Clears all provided bits from value
	 * 
	 * @param value the value as a base to remove all provided bits
	 * @param bits  a list of bits as variable argument to be removed
	 * @return      the updated value with all provided bits removed
	 */
	static public long clr (long value, int ... bits) {
		return value & (~ createBitmask (bits));
	}
	static public int clr (int value, int ... bits) {
		return value & (~ ((int) createBitmask (bits)));
	}
}
