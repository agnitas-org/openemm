/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ssh;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OID {
	/**
	 * OID for RSA algorithm
	 * OID 1.2.840.113549.1.1.1
	 */
	public static byte[] RSA_ALGORITHM_ARRAY = new byte[] { 42, -122, 72, -122, -9, 13, 1, 1, 1 };

	/**
	 * OID for DSA algorithm
	 * OID 1.2.840.10040.4.1
	 */
	public static byte[] DSA_ALGORITHM_ARRAY = new byte[] { 42, -122, 72, -50, 56, 4, 1 };

	/**
	 * OID for EcDSA Public Key
	 * OID 1.2.840.10045.2.1
	 */
	public static byte[] ECDSA_PUBLICKEY_ARRAY = new byte[] { 42, -122, 72, -50, 61, 2, 1 };

	/**
	 * OID for elliptic curve secp256r1 and nistp256
	 * OID 1.2.840.10045.3.1.7
	 */
	public static byte[] ECDSA_CURVE_NISTP256_ARRAY = new byte[] { 42, -122, 72, -50, 61, 3, 1, 7 };

	/**
	 * OID for elliptic curve secp384r1 and nistp384
	 * OID 1.3.132.0.34
	 */
	public static byte[] ECDSA_CURVE_NISTP384_ARRAY = new byte[] { 43, -127, 4, 0, 34 };

	/**
	 * OID for elliptic curve secp521r1 and nistp521
	 * OID 1.3.132.0.35
	 */
	public static byte[] ECDSA_CURVE_NISTP521_ARRAY = new byte[] { 43, -127, 4, 0, 35 };

	/**
	 * OID for EdDSA25519 algorithm
	 * OID 1.3.101.112
	 */
	public static byte[] EDDSA25519_ALGORITHM_ARRAY = new byte[] { 43, 101, 112 };

	/**
	 * OID for EdDSA448 algorithm
	 * OID 1.3.101.113
	 */
	public static byte[] EDDSA448_ALGORITHM_ARRAY = new byte[] { 43, 101, 113 };

	private int[] id;

	public OID(final String oidString) throws Exception {
		if (oidString == null || "".equals(oidString.trim())) {
			throw new Exception("Invalid OID empty data");
		} else {
			final String[] parts = oidString.split("\\.");
			id = new int[parts.length];
			for (int i = 0; i < parts.length; i++) {
				try {
					id[i] = Integer.parseInt(parts[i]);
				} catch (final Exception e) {
					throw new Exception("Invalid OID data: " + oidString, e);
				}
			}
		}
	}

	public OID(final byte[] oidArray) throws Exception {
		if (oidArray == null || oidArray.length == 0) {
			throw new Exception("Invalid OID empty data");
		} else {
			final List<Integer> idList = new ArrayList<>();
			if (oidArray.length > 0) {
				idList.add(oidArray[0] / 40);
				idList.add(oidArray[0] % 40);
			}
			for (int i = 1; i < oidArray.length; i++) {
				idList.add((int) decodeInteger(oidArray, i));
				while ((oidArray[i] & 0x80) != 0 && i + 1 < oidArray.length) {
					i++;
				}
			}
			id = idList.stream().mapToInt(Integer::intValue).toArray();
		}
	}

	public String getStringEncoding() {
		final StringBuilder returnValue = new StringBuilder();
		for (final int idPart : id) {
			if (returnValue.length() > 0) {
				returnValue.append(".");
			}
			returnValue.append(idPart);
		}
		return returnValue.toString();
	}

	public byte[] getByteArrayEncoding() throws Exception {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (id.length >= 1) {
			byte firstByte = (byte) (40 * id[0]);
			if (id.length >= 2) {
				firstByte = (byte) (firstByte + id[1]);
			}
			out.write(firstByte);
		}

		for (int i = 2; i < id.length; i++) {
			out.write(encodeInteger(id[i]));
		}

		return out.toByteArray();
	}

	public static byte[] encodeInteger(final long value) throws Exception {
		if (value < 0) {
			throw new Exception("Minimum encoded Integer underrun");
		} else if (value < 0x80) {
			return new byte[] { (byte) value };
		} else {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			long buffer = value;
			out.write((byte) (buffer & 0x7F));
			buffer = (buffer >> 7);
			while (buffer > 0) {
				out.write((byte) (0x80 | (buffer & 0x7F)));
				buffer = buffer >> 7;
			}
			final byte[] returnArray = out.toByteArray();
			for (int i = 0; i < returnArray.length / 2; i++) {
				final byte swap = returnArray[i];
				returnArray[i] = returnArray[returnArray.length - 1 - i];
				returnArray[returnArray.length - 1 - i] = swap;
			}
			return returnArray;
		}
	}

	public static long decodeInteger(final byte[] array, final int startIndex) throws Exception {
		if (array.length == 0 || startIndex >= array.length) {
			throw new Exception("Invalid encoded Integer data");
		} else {
			long returnValue = 0;
			for (int i = startIndex; i < array.length; i++) {
				final boolean isLastByteOfValue = (array[i] & 0x80) == 0;
				returnValue <<= 7;
				returnValue += array[i] & 0x7F;
				if (isLastByteOfValue) {
					return returnValue;
				}
			}
			throw new Exception("Invalid encoded Integer data: Final byte sign is missing");
		}
	}
}
