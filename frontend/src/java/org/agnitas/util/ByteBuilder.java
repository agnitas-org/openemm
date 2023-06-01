/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import	java.util.Arrays;
import	java.nio.charset.StandardCharsets;

/**
 * A simular implmention of StringBuilder for byte arrays in a minimalistic version
 */
public class ByteBuilder {
	private static int	DEFAULT_CAPACITY = 4096;
	private byte[]		buffer;
	private int		capacity;
	private int		position;

	/**
	 * Create instance with initial capacity
	 * 
	 * @param initialCapacity the preallocated space for the internal used byte array
	 */
	public ByteBuilder (int initialCapacity) {
		capacity = initialCapacity > 0 ? initialCapacity : DEFAULT_CAPACITY;
		buffer = new byte[capacity];
		position = 0;
	}
	
	/**
	 * Create instance with default initial capactiy
	 */
	public ByteBuilder () {
		this (0);
	}

	/**
	 * Create instance from existing byte array
	 * 
	 * @param content initialize the internal array with this bytes array
	 */
	public ByteBuilder (final byte[] content) {
		this (content.length);
		append (content);
	}
	
	private static final char[]		printable = {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~'};
	@Override
	public String toString () {
		StringBuilder	out = new StringBuilder ();

		out.append ("b'");
		for (int n = 0; n < position; ++n) {
			int	b = buffer[n] & 0xff;

			if (b > 0x20 && b < 0x7f) {
				if (b == 0x5c || b == 0x27) {
					out.append ("\\");
				}
				out.append (printable[b - 0x21]);
			} else {
				out.append (String.format ("\\x%02x", b));
			}
		}
		out.append ("'");
		return out.toString ();
	}

	/**
	 * Clear the current content, do NOT free any allocated space
	 */
	public void clear () {
		position = 0;
	}
	
	/**
	 * Append one byte the buffer
	 *
	 * @param b the byte to append
	 */
	public void append (final byte b) {
		expand (1);
		buffer[position++] = b;
	}
	
	/**
	 * Append an array of bytes to the buffer
	 * 
	 * @param b the byte array to append
	 */
	public void append (final byte[] b) {
		expand (b.length);
		for (int n = 0; n < b.length; ++n) {
			buffer[position++] = b[n];
		}
	}
	
	/**
	 * Append a string, encoded in UTF-8, to the buffer
	 * 
	 * @param s the string to append
	 */
	public void append (final String s) {
		append (s.getBytes (StandardCharsets.UTF_8));
	}
	
	/**
	 * Append a character, encoded in UTF-8, to the buffer
	 * 
	 * @param ch the character to append
	 */
	public void append (final char ch) {
		append (Character.toString (ch));
	}
	
	/**
	 * Append a string represantion of an integer to the buffer
	 * 
	 * @param n the input value
	 */
	public void append (final int n) {
		append (Integer.toString (n));
	}

	/**
	 * Return a copy of the currently filled buffer
	 * 
	 * @return the copy as byte array
	 */
	public final byte[] value () {
		return Arrays.copyOf (buffer, position);
	}
	
	private void expand (int amount) {
		if (position + amount > capacity) {
			capacity += capacity + amount;
			buffer = Arrays.copyOf (buffer, capacity);
		}
	}
}
