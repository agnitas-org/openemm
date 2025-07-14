/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ssh;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.agnitas.ssh.Asn1Codec.DerTag;
import com.agnitas.ssh.SshKey.SshKeyFormat;

/**
 * Reader for SSH public and private keys with optional password protection<br />
 * <br />
 * Supported key formats:<br />
 * - OpenSSHv1 (proprietary format of OpenSSH, "-----BEGIN OPENSSH PRIVATE KEY-----")<br />
 * - OpenSSL, PKCS#8 ("-----BEGIN RSA/DSA/EC PRIVATE KEY-----", doesn't support EdDSA)<br />
 * - PuTTY key version 2 ("PuTTY-User-Key-File-2: ...")<br />
 * - PuTTY key version 3 ("PuTTY-User-Key-File-3: ...")<br />
 * - PKCS#1 ("---- BEGIN SSH2 PRIVATE KEY ----", no password encryption)<br />
 * <br />
 * Supported cipher algorithms:<br />
 * - RSA<br />
 * - DSA<br />
 * - EC / ECDSA (nistp256, nistp384, nistp521)<br />
 * - EdDSA (Ed25519, Ed448)<br />
 * <br />
 */
public class SshKeyReader {
	/**
	 * Read public key data only and ignore the private key parts.<br />
	 * This may be useful if you only need the public key and don't know the password of the encrypted private key.<br />
	 * This reads multiple stored public keys, like in authorized keys files.<br />
	 *
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static List<SshKey> readAllPublicKeys(final InputStream inputStream) throws Exception {
		try (final BufferedReader dataReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
			final List<SshKey> keyList = new ArrayList<>();
			SshKey nextKey;
			while ((nextKey = readKey(dataReader, null, true)) != null) {
				keyList.add(nextKey);
			}
			return keyList;
		}
	}

	/**
	 * Read public and private key data, as far as they are included.<br />
	 * Definition of a password is optional. Use NULL for unencrypted private keys.<br />
	 * On multiple stored public keys (like authorized keys files) only the first key is read.<br />
	 *
	 * @param inputStream
	 * @param passwordChars
	 * @return
	 * @throws Exception
	 */
	public static SshKey readKey(final InputStream inputStream, final char[] passwordChars) throws Exception {
		try (final BufferedReader dataReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
			return readKey(dataReader, passwordChars, false);
		}
	}

	private static SshKey readKey(final BufferedReader dataReader, final char[] passwordChars, final boolean skipPrivateKey) throws Exception {
		try (final Password password = passwordChars == null ? null : new Password(passwordChars.clone())) {
			// Read the stream internally with ISO-8859-1 charset, because PuTTY keys use it and their comments are part of the MAC checksum, which would be wrong otherwise.
			// OpenSSH keys and OpenSSL keys are more robust in that matters and their comments encoding will be fixed if needed
			String nextLine;

			// Skip empty lines and comment lines (#), especially for public key files
			while ((nextLine = dataReader.readLine()) != null) {
				if (isNotBlank(nextLine) && !nextLine.startsWith("#")) {
					break;
				}
			}

			if (nextLine == null) {
				// This was the last key or there is none at all
				return null;
			} else if (nextLine.startsWith("-----BEGIN ") && nextLine.endsWith("-----")) {
				final String currentKeyName = nextLine.substring(11, nextLine.length() - 5);
				final Map<String, String> keyProperties = readUpToLine(dataReader, nextLine.replace("BEGIN ", "END "));
				Charset passwordCharset = StandardCharsets.UTF_8;
				while (true) {
					try {
						byte[] keyData = Base64.getDecoder().decode(keyProperties.get(null));
						if (currentKeyName.toLowerCase().contains("private")) {
							if (currentKeyName.startsWith("RSA")) {
								if (skipPrivateKey) {
									return null;
								} else {
									if ("4,ENCRYPTED".equals(keyProperties.get("Proc-Type")) && !skipPrivateKey) {
										keyData = decryptOpenSslKeyData(keyData, keyProperties.get("DEK-Info"), password, passwordCharset);
									}
									return new SshKey(SshKeyFormat.OpenSSL, null, readPKCS8RsaPrivateKey(keyData));
								}
							} else if (currentKeyName.startsWith("DSA")) {
								if (skipPrivateKey) {
									return null;
								} else {
									if ("4,ENCRYPTED".equals(keyProperties.get("Proc-Type")) && !skipPrivateKey) {
										keyData = decryptOpenSslKeyData(keyData, keyProperties.get("DEK-Info"), password, passwordCharset);
									}
									return new SshKey(SshKeyFormat.OpenSSL, null, readPKCS8DsaPrivateKey(keyData));
								}
							} else if (currentKeyName.startsWith("EC")) {
								if (skipPrivateKey) {
									return null;
								} else {
									if ("4,ENCRYPTED".equals(keyProperties.get("Proc-Type")) && !skipPrivateKey) {
										keyData = decryptOpenSslKeyData(keyData, keyProperties.get("DEK-Info"), password, passwordCharset);
									}
									return new SshKey(SshKeyFormat.OpenSSL, null, readPKCS8EcdsaPrivateKey(keyData));
								}
							} else if (currentKeyName.startsWith("OPENSSH")) {
								return readOpenSshv1Key(keyData, password, skipPrivateKey);
							} else {
								throw new Exception("Unknown key identifier found: " + nextLine);
							}
						} else if (currentKeyName.toLowerCase().contains("public")) {
							return new SshKey(SshKeyFormat.OpenSSL, null, new KeyPair(parsePublicKeyBytes(keyData), null));
						} else {
							throw new Exception("Unknown key identifier found: " + nextLine);
						}
					} catch (final Exception e) {
						// Putty uses "ISO-8859-1" for password encoding, even for those keys stored in OpenSSHv1 and OpenSSL format
						// "ssh-keygen" on Linux uses UTF-8 for password encoding
						if (password != null && StandardCharsets.UTF_8.equals(passwordCharset)) {
							if (!Arrays.equals(password.getPasswordBytesIsoEncoded(), password.getPasswordBytesUtfEncoded())) {
								passwordCharset = StandardCharsets.ISO_8859_1;
							} else {
								throw e;
							}
						} else {
							throw e;
						}
					}
				}
			} else if (nextLine.startsWith("---- BEGIN SSH2 ") && nextLine.endsWith(" KEY ----")) {
				if (nextLine.toLowerCase().contains("private")) {
					// "---- BEGIN SSH2 PRIVATE KEY ----"
					final Map<String, String> keyProperties = readUpToLine(dataReader, nextLine.replace("BEGIN ", "END "));
					Charset passwordCharset = StandardCharsets.UTF_8;
					while (true) {
						try {
							byte[] keyData = Base64.getDecoder().decode(keyProperties.get(null));
							if ("4,ENCRYPTED".equals(keyProperties.get("Proc-Type"))) {
								keyData = decryptOpenSslKeyData(keyData, keyProperties.get("DEK-Info"), password, passwordCharset);
							}
							return new SshKey(SshKeyFormat.OpenSSL, null, readPKCS8RsaPrivateKey(keyData));
						} catch (final Exception e) {
							// Putty uses "ISO-8859-1" for password encoding, even for those keys stored in OpenSSHv1 and OpenSSL format
							// "ssh-keygen" on Linux uses UTF-8 for password encoding
							if (password != null && StandardCharsets.UTF_8.equals(passwordCharset)) {
								if (!Arrays.equals(password.getPasswordBytesIsoEncoded(), password.getPasswordBytesUtfEncoded())) {
									passwordCharset = StandardCharsets.ISO_8859_1;
								} else {
									throw e;
								}
							} else {
								throw e;
							}
						}
					}
				} else if (nextLine.toLowerCase().contains("public")) {
					// "---- BEGIN SSH2 PUBLIC KEY ----"
					final Map<String, String> keyProperties = readUpToLine(dataReader, nextLine.replace("BEGIN ", "END "));
					final byte[] keyData = Base64.getDecoder().decode(keyProperties.get(null));
					return new SshKey(SshKeyFormat.OpenSSL, null, new KeyPair(parsePublicKeyBytes(keyData), null));
				} else {
					throw new Exception("Unknown key identifier found: " + nextLine);
				}
			} else if (nextLine.startsWith("PuTTY-User-Key-File-2")) {
				final Map<String, String> keyProperties = readPuttyKeyProperties(dataReader);
				keyProperties.put("PuTTY-User-Key-File-2", nextLine.split(" ", 2)[1]);
				return readPuttyVersion2Key(keyProperties, password, skipPrivateKey);
			} else if (nextLine.startsWith("PuTTY-User-Key-File-3")) {
				final Map<String, String> keyProperties = readPuttyKeyProperties(dataReader);
				keyProperties.put("PuTTY-User-Key-File-3", nextLine.split(" ", 2)[1]);
				return readPuttyVersion3Key(keyProperties, password, skipPrivateKey);
			} else if (nextLine.startsWith("ssh-rsa ")
					|| nextLine.startsWith("ssh-dss ")
					|| nextLine.startsWith("ecdsa-sha2-nistp256 ")
					|| nextLine.startsWith("ecdsa-sha2-nistp384 ")
					|| nextLine.startsWith("ecdsa-sha2-nistp521 ")
					|| nextLine.startsWith("ssh-ed25519 ")
					|| nextLine.startsWith("ssh-ed448 ")) {
				final String[] keyParts = nextLine.split(" ", 3);
				final byte[] keyData = Base64.getDecoder().decode(keyParts[1]);
				String keyComment = keyParts[2];
				keyComment = fixCommentEncodingIfNeeded(keyComment);
				return new SshKey(SshKeyFormat.OpenSSL, keyComment, new KeyPair(parsePublicKeyBytes(keyData), null));
			} else {
				throw new Exception("No keydata found");
			}
		}
	}

	private static Map<String, String> readUpToLine(final BufferedReader dataReader, final String endLine) throws Exception {
		try {
			final Map<String, String> keyProperties = new HashMap<>();
			String nextLine;
			while ((nextLine = dataReader.readLine()) != null) {
				if (endLine.equals(nextLine)) {
					return keyProperties;
				} else {
					final int indexOfHeaderSeparator = nextLine.indexOf(": ");
					if (indexOfHeaderSeparator > 0) {
						// Headers must be at start of key data
						if (keyProperties.get(null) != null && keyProperties.get(null).length() > 0) {
							throw new Exception("Corrupt key data found: Headers found after keydata start");
						} else {
							final String headerName = nextLine.substring(0, indexOfHeaderSeparator).trim();
							keyProperties.put(headerName, nextLine.substring(indexOfHeaderSeparator + 2));
						}
					} else {
						if (keyProperties.get(null) == null) {
							keyProperties.put(null, "");
						}
						keyProperties.put(null, keyProperties.get(null) + nextLine);
					}
				}
			}
			throw new Exception("Corrupt key data found: End line is missing: '" + endLine + "'");
		} catch (final IOException e) {
			throw new Exception("Corrupt key data found", e);
		}
	}

	private static Map<String, String> readPuttyKeyProperties(final BufferedReader dataReader) throws Exception {
		try {
			final Map<String, String> keyProperties = new HashMap<>();
			String nextLine;
			while ((nextLine = dataReader.readLine()) != null) {
				final int indexOfHeaderSeparator = nextLine.indexOf(": ");
				if (indexOfHeaderSeparator > 0) {
					final String headerName = nextLine.substring(0, indexOfHeaderSeparator).trim();
					if ("Public-Lines".equals(headerName) || "Private-Lines".equals(headerName)) {
						final int numberOfLines = Integer.parseInt(nextLine.substring(indexOfHeaderSeparator + 2));
						final StringBuilder value = new StringBuilder();
						for (int i = 0; i < numberOfLines; i++) {
							if ((nextLine = dataReader.readLine()) != null) {
								value.append(nextLine);
							} else {
								throw new Exception("Corrupt key data found: Missing some lines for '" + headerName + "'");
							}
						}
						keyProperties.put(headerName, value.toString());
					} else {
						// Watchout for Comment values correct encoding, because it is part of the MAC checksum
						keyProperties.put(headerName, nextLine.substring(indexOfHeaderSeparator + 2));
					}
				} else if (isNotBlank(nextLine)) {
					throw new Exception("Corrupt key data found: Unexpected line: '" + nextLine + "'");
				}
			}
			return keyProperties;
		} catch (final IOException e) {
			throw new Exception("Corrupt key data found", e);
		}
	}

	private static byte[] decryptOpenSslKeyData(byte[] keyData, final String dekInfo, final Password password, final Charset passwordCharset) throws Exception {
		if (password == null) {
			throw new WrongPasswordException();
		} else if (dekInfo == null) {
			throw new Exception("Missing key encryption info (DEK-Info)");
		} else {
			final String[] dekInfoParts = dekInfo.split(",");
			if (dekInfoParts.length < 2) {
				throw new Exception("Invalid key encryption info (DEK-Info)");
			} else {
				try {
					final String keyEncryptionCipherName = dekInfoParts[0].trim();
					final String ivString = dekInfoParts[1].trim();
					if ("DES-EDE3-CBC".equalsIgnoreCase(keyEncryptionCipherName)) {
						final byte[] iv = fromHexString(ivString);
						final Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
						cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(stretchPasswordForOpenSsl(password, iv, 8, 24, passwordCharset), "DESede"), new IvParameterSpec(iv));
						keyData = cipher.doFinal(keyData);
					} else if ("AES-128-CBC".equalsIgnoreCase(keyEncryptionCipherName)) {
						final byte[] iv = fromHexString(ivString);
						final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
						cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(stretchPasswordForOpenSsl(password, iv, 8, 16, passwordCharset), "AES"), new IvParameterSpec(iv));
						keyData = cipher.doFinal(keyData);
					} else {
						throw new Exception("Unknown key encryption cipher: " + keyEncryptionCipherName);
					}

					keyData = removeLengthCodedPadding(keyData);
					return keyData;
				} catch (final Exception e) {
					throw new Exception("Cannot decrypt key data", e);
				}
			}
		}
	}

	private static PublicKey parsePublicKeyBytes(final byte[] data) throws Exception {
		final BlockDataReader publicKeyReader = new BlockDataReader(data);
		final String algorithmName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);
		if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
			final BigInteger publicExponent = publicKeyReader.readBigInt();
			final BigInteger modulus = publicKeyReader.readBigInt();

			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
		} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
			final BigInteger p = publicKeyReader.readBigInt();
			final BigInteger q = publicKeyReader.readBigInt();
			final BigInteger g = publicKeyReader.readBigInt();

			final BigInteger y = publicKeyReader.readBigInt();

			final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			return keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
		} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)
				|| "ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)
				|| "ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
			final String ecdsaCurveName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);
			if (!"nistp256".equals(ecdsaCurveName)
					&& !"nistp384".equals(ecdsaCurveName)
					&& !"nistp521".equals(ecdsaCurveName)) {
				throw new Exception("Unsupported ECDSA curveName: " + ecdsaCurveName);
			} else {
				final byte[] eccKeyBlobBytes = publicKeyReader.readData();

				Security.addProvider(new BouncyCastleProvider());
				final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
				final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
				final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
				final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
				final PublicKey publicKey = keyFactory.generatePublic(pubSpec);
				return publicKey;
			}
		} else {
			throw new IllegalArgumentException("Invalid public key algorithm for PuTTY key (only supports RSA / DSA / ECDSA / EdDSA): " + algorithmName);
		}
	}

	private static class BlockDataReader {
		private final ByteArrayInputStream inputStream;
		private final DataInput keyDataInput;

		private BlockDataReader(final byte[] data) {
			inputStream = new ByteArrayInputStream(data);
			keyDataInput = new DataInputStream(inputStream);
		}

		private boolean isMoreDataAvailable() {
			return inputStream.available() > 0;
		}

		private int readSimpleInt() throws Exception {
			try {
				return keyDataInput.readInt();
			} catch (final IOException e) {
				throw new Exception("Key block read error", e);
			}
		}

		private BigInteger readBigInt() throws Exception {
			return new BigInteger(readData());
		}

		private byte[] readData() throws IOException, Exception {
			try {
				final int nextBlockSize = keyDataInput.readInt();
				if (nextBlockSize < 0) {
					throw new Exception("Key blocksize error. Maybe the key encryption password was wrong");
				} else if (nextBlockSize == 0) {
					return new byte[0];
				} else {
					final byte[] nextBlock = new byte[nextBlockSize];
					keyDataInput.readFully(nextBlock);
					return nextBlock;
				}
			} catch (final IOException e) {
				throw new Exception("Key block read error", e);
			}
		}

		private byte[] readZeroLimitedData() throws Exception {
			try {
				final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int nextByte;
				while ((nextByte = keyDataInput.readByte()) > 0) {
					if (nextByte == 0) {
						break;
					} else {
						buffer.write(nextByte);
					}
				}
				return buffer.toByteArray();
			} catch (final IOException e) {
				throw new Exception("Key block read error", e);
			}
		}

		private byte[] readLeftoverData() throws IOException, Exception {
			try {
				final byte[] nextBlock = new byte[inputStream.available()];
				keyDataInput.readFully(nextBlock);
				return nextBlock;
			} catch (final IOException e) {
				throw new Exception("Key block read error", e);
			}
		}
	}

	private static KeyPair readPKCS8RsaPrivateKey(final byte[] data) throws Exception {
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(data);
		if (Asn1Codec.DER_TAG_SEQUENCE != enclosingDerTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> derDataTags = Asn1Codec.readDerTags(enclosingDerTag.getData());

		final BigInteger keyEncodingVersion = new BigInteger(derDataTags.get(0).getData());
		if (!BigInteger.ZERO.equals(keyEncodingVersion)) {
			throw new Exception("Invalid key data version found");
		}

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(1).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger modulus = new BigInteger(derDataTags.get(1).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(2).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger publicExponent = new BigInteger(derDataTags.get(2).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(3).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger privateExponent = new BigInteger(derDataTags.get(3).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(4).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger primeP = new BigInteger(derDataTags.get(4).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(5).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger primeQ = new BigInteger(derDataTags.get(5).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(6).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger primeExponentP = new BigInteger(derDataTags.get(6).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(7).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger primeExponentQ = new BigInteger(derDataTags.get(7).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(8).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger crtCoefficient = new BigInteger(derDataTags.get(8).getData());

		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		final PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
		final PrivateKey privateKey = keyFactory.generatePrivate(new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient));
		return new KeyPair(publicKey, privateKey);
	}

	private static KeyPair readPKCS8DsaPrivateKey(final byte[] data) throws Exception {
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(data);
		if (Asn1Codec.DER_TAG_SEQUENCE != enclosingDerTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> derDataTags = Asn1Codec.readDerTags(enclosingDerTag.getData());

		final BigInteger keyEncodingVersion = new BigInteger(derDataTags.get(0).getData());
		if (!BigInteger.ZERO.equals(keyEncodingVersion)) {
			throw new Exception("Invalid key data version found");
		}

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(1).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger p = new BigInteger(derDataTags.get(1).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(2).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger q = new BigInteger(derDataTags.get(2).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(3).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger g = new BigInteger(derDataTags.get(3).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(4).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger y = new BigInteger(derDataTags.get(4).getData());

		if (Asn1Codec.DER_TAG_INTEGER != derDataTags.get(5).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final BigInteger x = new BigInteger(derDataTags.get(5).getData());

		final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
		final PublicKey publicKey = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
		final PrivateKey privateKey = keyFactory.generatePrivate(new DSAPrivateKeySpec(x, p, q, g));
		return new KeyPair(publicKey, privateKey);
	}

	private static KeyPair readPKCS8EcdsaPrivateKey(final byte[] data) throws Exception {
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(data);
		if (Asn1Codec.DER_TAG_SEQUENCE != enclosingDerTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> derDataTags = Asn1Codec.readDerTags(enclosingDerTag.getData());

		final BigInteger keyEncodingVersion = new BigInteger(derDataTags.get(0).getData());
		if (!BigInteger.ONE.equals(keyEncodingVersion)) {
			throw new Exception("Invalid key data version found");
		}

		if (Asn1Codec.DER_TAG_OCTET_STRING != derDataTags.get(1).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final byte[] sBytes = derDataTags.get(1).getData();

		if (Asn1Codec.DER_TAG_CONTEXT_SPECIFIC_0 != derDataTags.get(2).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final byte[] oidTagBytes = derDataTags.get(2).getData();
		final DerTag oidTag = Asn1Codec.readDerTag(oidTagBytes);
		if (Asn1Codec.DER_TAG_OBJECT != oidTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final byte[] oidBytes = oidTag.getData();
		String ecdsaCurveName;
		if (Arrays.equals(OID.ECDSA_CURVE_NISTP256_ARRAY, oidBytes)) {
			ecdsaCurveName = "nistp256";
		} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP384_ARRAY, oidBytes)) {
			ecdsaCurveName = "nistp384";
		} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP521_ARRAY, oidBytes)) {
			ecdsaCurveName = "nistp521";
		} else {
			try {
				throw new Exception("Unsupported ec curve oid found: " + new OID(oidBytes).getStringEncoding());
			} catch (final Exception e) {
				throw new Exception("Invalid ec curve oid found", e);
			}
		}

		if (Asn1Codec.DER_TAG_CONTEXT_SPECIFIC_1 != derDataTags.get(3).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final byte[] publicKeyTagBytes = derDataTags.get(3).getData();
		final DerTag publicKeyTag = Asn1Codec.readDerTag(publicKeyTagBytes);
		if (Asn1Codec.DER_TAG_BIT_STRING != publicKeyTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		byte[] eccKeyBlobBytes = publicKeyTag.getData();
		if (0 != eccKeyBlobBytes[0] || 4 != eccKeyBlobBytes[1]) {
			throw new Exception("Invalid key data found");
		}
		// Remove prefix "0"
		eccKeyBlobBytes = Arrays.copyOfRange(eccKeyBlobBytes, 1, eccKeyBlobBytes.length);

		Security.addProvider(new BouncyCastleProvider());
		final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
		final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1"));
		final ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
		final PrivateKey privateKey = keyFactory.generatePrivate(new ECPrivateKeySpec(new BigInteger(sBytes), ecParameterSpec));

		final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
		final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
		final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
		final PublicKey publicKey = keyFactory.generatePublic(pubSpec);

		return new KeyPair(publicKey, privateKey);
	}

	private static SshKey readOpenSshv1Key(final byte[] data, final Password password, final boolean skipPrivateKey) throws Exception {
		final BlockDataReader dataReader = new BlockDataReader(data);
		// Storage format name
		final String keyFormatString = new String(dataReader.readZeroLimitedData(), StandardCharsets.UTF_8);
		if (!"openssh-key-v1".equals(keyFormatString)) {
			throw new Exception("Invalid keyFormat name '" + keyFormatString + "' found. Expected 'openssh-key-v1'");
		}

		// EncryptionCipherName
		String encryptionCipherName = new String(dataReader.readData(), StandardCharsets.UTF_8);
		if ("none".equals(encryptionCipherName)) {
			encryptionCipherName = null;
		}

		// kdf: key derivation function
		String kdfName = new String(dataReader.readData(), StandardCharsets.UTF_8);
		if ("none".equals(kdfName)) {
			kdfName = null;
		}

		// kdf info
		byte[] kdfInitialVectorBytes = null;
		int kdfRounds = 0;
		final byte[] kdfInfoBytes = dataReader.readData();
		if (kdfInfoBytes.length > 0) {
			final BlockDataReader kdfInfoReader = new BlockDataReader(kdfInfoBytes);
			kdfInitialVectorBytes = kdfInfoReader.readData();
			kdfRounds = kdfInfoReader.readSimpleInt();
		}

		// Amount of stored keys
		final int amountOfKeys = dataReader.readSimpleInt();
		if (amountOfKeys != 1) {
			throw new Exception("Invalid amountOfKeys " + amountOfKeys + " found. Expected 1");
		}

		// Public key
		final byte[] publicKeyDataBytes = dataReader.readData();
		final PublicKey publicKey = readOpenSshv1PublicKey(publicKeyDataBytes);

		if (skipPrivateKey) {
			return new SshKey(SshKeyFormat.OpenSSHv1, null, new KeyPair(publicKey, null));
		}

		// Private key
		byte[] privateKeyDataBytes = dataReader.readData();

		// Decrypt private key data
		if ("aes256-ctr".equalsIgnoreCase(encryptionCipherName)) {
			if ("bcrypt".equalsIgnoreCase(kdfName)) {
				if (kdfInitialVectorBytes == null || kdfInitialVectorBytes.length == 0) {
					throw new Exception("Invalid key derivation function info 'kdfInitialVectorBytes' for key derivation function '" + kdfName + "'");
				} else if (kdfRounds <= 0) {
					throw new Exception("Invalid key derivation function info 'kdfRounds = " + kdfRounds + "' for key derivation function '" + kdfName + "'");
				} else {
					if (password == null) {
						throw new WrongPasswordException();
					} else {
						// Decrypt private key by bcrypt pbkdf
						// Putty uses "ISO-8859-1" for password encoding, even for those keys stored in OpenSSHv1 and OpenSSL format
						// "ssh-keygen" on Linux uses UTF-8 for password encoding
						for (final Charset charset : new Charset[] { StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1 }) {
							final byte[] passwordBytes = StandardCharsets.UTF_8.equals(charset) ? password.getPasswordBytesUtfEncoded() : password.getPasswordBytesIsoEncoded();
							final byte[] derivedKeyBytes = new byte[48];
							new BCryptPBKDF().derivePassword(passwordBytes, kdfInitialVectorBytes, kdfRounds, derivedKeyBytes);
							final SecretKey secretKey = new SecretKeySpec(derivedKeyBytes, 0, 32, "AES");
							final AlgorithmParameterSpec iv = new IvParameterSpec(derivedKeyBytes, 32, 16);

							final Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
							cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

							final byte[] privateKeyDataBytesDecrypted = cipher.doFinal(privateKeyDataBytes);

							if (privateKeyDataBytesDecrypted[0] == privateKeyDataBytesDecrypted[4]
									&& privateKeyDataBytesDecrypted[1] == privateKeyDataBytesDecrypted[5]
											&& privateKeyDataBytesDecrypted[2] == privateKeyDataBytesDecrypted[6]
													&& privateKeyDataBytesDecrypted[3] == privateKeyDataBytesDecrypted[7]) {
								privateKeyDataBytes = privateKeyDataBytesDecrypted;
								break;
							}
						}
						if (privateKeyDataBytes[0] != privateKeyDataBytes[4]
								|| privateKeyDataBytes[1] != privateKeyDataBytes[5]
										|| privateKeyDataBytes[2] != privateKeyDataBytes[6]
												|| privateKeyDataBytes[3] != privateKeyDataBytes[7]) {
							throw new WrongPasswordException();
						}
					}
				}
			} else if (kdfName != null) {
				throw new Exception("Invalid key derivation function method (Only 'bcrypt' allowed) '" + kdfName + "'");
			}
		} else if (encryptionCipherName != null) {
			throw new Exception("Invalid key encryption function method (Only 'aes-ctr' allowed) '" + encryptionCipherName + "'");
		}

		final SshKey sshKey;
		try {
			sshKey = readOpenSshv1PrivateKey(privateKeyDataBytes);
		} catch (final Exception e) {
			if (password != null) {
				throw new WrongPasswordException();
			} else {
				throw e;
			}
		}

		if (dataReader.isMoreDataAvailable()) {
			throw new Exception("Invalid key data: unexpected trailing data found");
		}

		return sshKey;
	}

	private static PublicKey readOpenSshv1PublicKey(final byte[] publicKeyData) throws Exception {
		final BlockDataReader publicKeyDataReader = new BlockDataReader(publicKeyData);
		final String algorithmType = new String(publicKeyDataReader.readData(), StandardCharsets.UTF_8);

		if ("ssh-rsa".equals(algorithmType)) {
			final BigInteger publicExponent = publicKeyDataReader.readBigInt();
			final BigInteger modulus = publicKeyDataReader.readBigInt();

			if (publicKeyDataReader.isMoreDataAvailable()) {
				throw new Exception("Invalid public key data: unexpected trailing data found");
			}

			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			final PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
			return publicKey;
		} else if ("ssh-dss".equals(algorithmType)) {
			final BigInteger p = publicKeyDataReader.readBigInt();
			final BigInteger q = publicKeyDataReader.readBigInt();
			final BigInteger g = publicKeyDataReader.readBigInt();
			final BigInteger y = publicKeyDataReader.readBigInt();

			if (publicKeyDataReader.isMoreDataAvailable()) {
				throw new Exception("Invalid public key data: unexpected trailing data found");
			}

			final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			final PublicKey publicKey = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
			return publicKey;
		} else if (algorithmType.startsWith("ecdsa-sha2-")) {
			final String ecdsaCurveName = new String(publicKeyDataReader.readData(), StandardCharsets.UTF_8);

			final byte[] eccKeyBlobBytes = publicKeyDataReader.readData();

			if (publicKeyDataReader.isMoreDataAvailable()) {
				throw new Exception("Invalid public key data: unexpected trailing data found");
			}

			Security.addProvider(new BouncyCastleProvider());
			final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
			final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
			final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
			final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
			final PublicKey publicKey = keyFactory.generatePublic(pubSpec);

			return publicKey;
		} else {
			throw new Exception("Unexpected key type '" + algorithmType + "'");
		}
	}

	private static SshKey readOpenSshv1PrivateKey(final byte[] privateKeyData) throws Exception {
		final BlockDataReader privateKeyDataReader = new BlockDataReader(privateKeyData);
		// Quick decryption validity check. Both checkInts must match.
		final int checkInt1 = privateKeyDataReader.readSimpleInt();
		final int checkInt2 = privateKeyDataReader.readSimpleInt();
		if (checkInt1 != checkInt2) {
			throw new WrongPasswordException();
		}

		final String algorithmName = new String(privateKeyDataReader.readData(), StandardCharsets.UTF_8);

		if ("ssh-rsa".equals(algorithmName)) {
			final BigInteger modulus = new BigInteger(privateKeyDataReader.readData());
			final BigInteger publicExponent = new BigInteger(privateKeyDataReader.readData());
			final BigInteger privateExponent = new BigInteger(privateKeyDataReader.readData());
			final BigInteger crtCoefficient = new BigInteger(privateKeyDataReader.readData());
			final BigInteger primeP = new BigInteger(privateKeyDataReader.readData());
			final BigInteger primeQ = new BigInteger(privateKeyDataReader.readData());

			String keyComment = new String(privateKeyDataReader.readData(), StandardCharsets.UTF_8);
			keyComment = fixCommentEncodingIfNeeded(keyComment);

			final byte[] paddingBytes = privateKeyDataReader.readLeftoverData();

			for (int i = 0; i < paddingBytes.length; i++) {
				if (paddingBytes[i] != i + 1) {
					throw new Exception("Invalid private key padding found");
				}
			}

			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			final PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));

			final BigInteger primeExponentP = privateExponent.mod(primeP.subtract(BigInteger.ONE)); // d mod (p-1) (= PrimeExponentP)
			final BigInteger primeExponentQ = privateExponent.mod(primeQ.subtract(BigInteger.ONE)); // d mod (q-1) (= PrimeExponentQ)

			final PrivateKey privateKey = keyFactory.generatePrivate(new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient));

			return new SshKey(SshKeyFormat.OpenSSHv1, keyComment, new KeyPair(publicKey, privateKey));
		} else if ("ssh-dss".equals(algorithmName)) {
			final BigInteger p = new BigInteger(privateKeyDataReader.readData());
			final BigInteger q = new BigInteger(privateKeyDataReader.readData());
			final BigInteger g = new BigInteger(privateKeyDataReader.readData());
			final BigInteger y = new BigInteger(privateKeyDataReader.readData());
			final BigInteger x = new BigInteger(privateKeyDataReader.readData());

			String keyComment = new String(privateKeyDataReader.readData(), StandardCharsets.UTF_8);
			keyComment = fixCommentEncodingIfNeeded(keyComment);

			final byte[] paddingBytes = privateKeyDataReader.readLeftoverData();

			for (int i = 0; i < paddingBytes.length; i++) {
				if (paddingBytes[i] != i + 1) {
					throw new Exception("Invalid private key padding found");
				}
			}

			final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			final PublicKey publicKey = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
			final PrivateKey privateKey = keyFactory.generatePrivate(new DSAPrivateKeySpec(x, p, q, g));

			return new SshKey(SshKeyFormat.OpenSSHv1, keyComment, new KeyPair(publicKey, privateKey));
		} else if (algorithmName.startsWith("ecdsa-sha2-")) {
			final String ecdsaCurveName = new String(privateKeyDataReader.readData(), StandardCharsets.UTF_8);

			final byte[] eccKeyBlobBytes = privateKeyDataReader.readData();
			final byte[] sBytes = privateKeyDataReader.readData();

			String keyComment = new String(privateKeyDataReader.readData(), StandardCharsets.UTF_8);
			keyComment = fixCommentEncodingIfNeeded(keyComment);

			final byte[] paddingBytes = privateKeyDataReader.readLeftoverData();

			for (int i = 0; i < paddingBytes.length; i++) {
				if (paddingBytes[i] != i + 1) {
					throw new Exception("Invalid private key padding found");
				}
			}

			Security.addProvider(new BouncyCastleProvider());
			final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
			final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
			parameters.init(new ECGenParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1"));
			final ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
			final PrivateKey privateKey = keyFactory.generatePrivate(new ECPrivateKeySpec(new BigInteger(sBytes), ecParameterSpec));

			final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
			final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
			final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
			final PublicKey publicKey = keyFactory.generatePublic(pubSpec);
			return new SshKey(SshKeyFormat.OpenSSHv1, keyComment, new KeyPair(publicKey, privateKey));
		} else {
			throw new Exception("Unexpected key type '" + algorithmName + "'");
		}
	}

	private static byte[] stretchPasswordForOpenSsl(final Password password, final byte[] iv, final int usingIvSize, final int keySize, final Charset passwordCharset) throws Exception {
		final byte[] passwordBytes = StandardCharsets.UTF_8.equals(passwordCharset) ? password.getPasswordBytesUtfEncoded() : password.getPasswordBytesIsoEncoded();
		final MessageDigest hash = MessageDigest.getInstance("MD5");
		final byte[] key = new byte[keySize];
		int hashesSize = keySize & 0XFFFFFFF0;

		if ((keySize & 0XF) != 0) {
			hashesSize += 0x10;
		}

		final byte[] hashes = new byte[hashesSize];
		byte[] previous;
		for (int index = 0; (index + 0x10) <= hashes.length; hash.update(previous, 0, previous.length)) {
			hash.update(passwordBytes, 0, passwordBytes.length);
			hash.update(iv, 0, usingIvSize);
			previous = hash.digest();
			System.arraycopy(previous, 0, hashes, index, previous.length);
			index += previous.length;
		}

		System.arraycopy(hashes, 0, key, 0, key.length);
		return key;
	}

	private static byte[] fromHexString(final String value) {
		if (value == null) {
			return null;
		} else {
			final int length = value.length();
			final byte[] data = new byte[length / 2];
			for (int i = 0; i < length; i += 2) {
				data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4) + Character.digit(value.charAt(i + 1), 16));
			}
			return data;
		}
	}

	private static String toHexString(final byte[] data) {
		final StringBuilder returnString = new StringBuilder();
		for (final byte dataByte : data) {
			returnString.append(String.format("%02X", dataByte));
		}
		return returnString.toString();
	}

	private static byte[] removeLengthCodedPadding(final byte[] data) {
		final int paddingSize = data[data.length - 1];
		final byte[] dataUnpadded = new byte[data.length - paddingSize];
		System.arraycopy(data, 0, dataUnpadded, 0, dataUnpadded.length);
		return dataUnpadded;
	}

	private static SshKey readPuttyVersion2Key(final Map<String, String> keyProperties, final Password password, final boolean skipPrivateKey) throws Exception {
		final String algorithmName = keyProperties.get("PuTTY-User-Key-File-2");
		if (!"ssh-rsa".equalsIgnoreCase(algorithmName)
				&& !"ssh-dss".equalsIgnoreCase(algorithmName)
				&& !"ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)
				&& !"ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)
				&& !"ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)
				&& !"ssh-ed25519".equalsIgnoreCase(algorithmName)
				&& !"ssh-ed448".equalsIgnoreCase(algorithmName)) {
			throw new Exception("Unsupported chipher: " + algorithmName);
		} else {
			final Decoder base64Decoder = Base64.getDecoder();

			final byte[] publicKeyData = base64Decoder.decode(keyProperties.get("Public-Lines"));

			if (skipPrivateKey) {
				return new SshKey(SshKeyFormat.Putty2, keyProperties.get("Comment"), new KeyPair(readPuttyPublicKeyData(publicKeyData), null));
			}

			final String encryptionMethod = keyProperties.get("Encryption");

			byte[] passwordByteArray = null;
			byte[] privateKeyData;
			if (encryptionMethod == null || "".equals(encryptionMethod) || "none".equalsIgnoreCase(encryptionMethod)) {
				privateKeyData = base64Decoder.decode(keyProperties.get("Private-Lines"));
			} else if ("aes256-cbc".equalsIgnoreCase(encryptionMethod)) {
				if (password == null || password.getPasswordChars().length == 0) {
					throw new WrongPasswordException();
				} else {
					passwordByteArray = password.getPasswordBytesIsoEncoded();
				}

				byte[] puttyKeyEncryptionKey = null;
				try {
					final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
					puttyKeyEncryptionKey = getPuttyPrivateKeyEncryptionKeyVersion2(passwordByteArray);
					cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(puttyKeyEncryptionKey, 0, 32, "AES"), new IvParameterSpec(new byte[16])); // initial vector=0

					privateKeyData = cipher.doFinal(base64Decoder.decode(keyProperties.get("Private-Lines")));
				} catch (final Exception e) {
					throw new Exception("Cannot decrypt PuTTY private key data", e);
				} finally {
					clear(puttyKeyEncryptionKey);
				}
			} else {
				throw new Exception("Unsupported key encryption method: " + encryptionMethod);
			}

			try {
				final String calculatedMacChecksum = calculatePuttyMacChecksumVersion2(passwordByteArray, algorithmName, keyProperties.get("Encryption"), keyProperties.get("Comment"), publicKeyData, privateKeyData);
				final String foundMacChecksum = keyProperties.get("Private-MAC");
				if (foundMacChecksum == null || !foundMacChecksum.equalsIgnoreCase(calculatedMacChecksum)) {
					throw new WrongPasswordException();
				}
			} catch (final WrongPasswordException e) {
				throw e;
			} catch (final Exception e) {
				throw new Exception("Invalid PuTTY key data: " + e.getMessage(), e);
			}

			final KeyPair keyPair = readPuttyKeyData(privateKeyData, publicKeyData);
			return new SshKey(SshKeyFormat.Putty2, keyProperties.get("Comment"), keyPair);
		}
	}

	private static byte[] getPuttyPrivateKeyEncryptionKeyVersion2(final byte[] passwordByteArray) throws NoSuchAlgorithmException {
		final byte[] puttyKeyEncryptionKey = new byte[32];
		final MessageDigest digest = MessageDigest.getInstance("SHA-1");

		digest.update(new byte[] { 0, 0, 0, 0 });
		digest.update(passwordByteArray);
		final byte[] key1 = digest.digest();

		digest.update(new byte[] { 0, 0, 0, 1 });
		digest.update(passwordByteArray);
		final byte[] key2 = digest.digest();

		System.arraycopy(key1, 0, puttyKeyEncryptionKey, 0, 20);
		System.arraycopy(key2, 0, puttyKeyEncryptionKey, 20, 12);
		return puttyKeyEncryptionKey;
	}

	private static String calculatePuttyMacChecksumVersion2(final byte[] passwordBytes, final String keyType, final String encryptionType, final String comment, final byte[] publicKey, final byte[] privateKey) throws Exception {
		final MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.update("putty-private-key-file-mac-key".getBytes(StandardCharsets.UTF_8));
		if (passwordBytes != null) {
			digest.update(passwordBytes);
		}
		final byte[] key = digest.digest();

		final Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(new SecretKeySpec(key, 0, 20, mac.getAlgorithm()));

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DataOutputStream data = new DataOutputStream(out);

		final byte[] keyTypeBytes = keyType.getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(keyTypeBytes.length);
		data.write(keyTypeBytes);

		final byte[] encryptionTypeBytes = encryptionType.getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(encryptionTypeBytes.length);
		data.write(encryptionTypeBytes);

		final byte[] commentBytes = (comment == null ? "" : comment).getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(commentBytes.length);
		data.write(commentBytes);

		data.writeInt(publicKey.length);
		data.write(publicKey);

		data.writeInt(privateKey.length);
		data.write(privateKey);

		return toHexString(mac.doFinal(out.toByteArray())).toLowerCase();
	}

	private static SshKey readPuttyVersion3Key(final Map<String, String> keyProperties, final Password password, final boolean skipPrivateKey) throws Exception {
		final String cipherName = keyProperties.get("PuTTY-User-Key-File-3");
		if (!"ssh-rsa".equalsIgnoreCase(cipherName)
				&& !"ssh-dss".equalsIgnoreCase(cipherName)
				&& !"ecdsa-sha2-nistp256".equalsIgnoreCase(cipherName)
				&& !"ecdsa-sha2-nistp384".equalsIgnoreCase(cipherName)
				&& !"ecdsa-sha2-nistp521".equalsIgnoreCase(cipherName)
				&& !"ssh-ed25519".equalsIgnoreCase(cipherName)
				&& !"ssh-ed448".equalsIgnoreCase(cipherName)) {
			throw new Exception("Unsupported chipher: " + cipherName);
		}

		final Decoder base64Decoder = Base64.getDecoder();

		final byte[] publicKeyData = base64Decoder.decode(keyProperties.get("Public-Lines"));

		if (skipPrivateKey) {
			return new SshKey(SshKeyFormat.Putty3, keyProperties.get("Comment"), new KeyPair(readPuttyPublicKeyData(publicKeyData), null));
		}

		final String encryptionMethod = keyProperties.get("Encryption");

		byte[] passwordByteArray = null;
		byte[] privateKeyData;
		byte[] puttyKeyEncryptionKey = null;
		try {
			if (encryptionMethod == null || "".equals(encryptionMethod) || "none".equalsIgnoreCase(encryptionMethod)) {
				privateKeyData = base64Decoder.decode(keyProperties.get("Private-Lines"));
			} else if ("aes256-cbc".equalsIgnoreCase(encryptionMethod)) {
				if (password == null || password.getPasswordChars().length == 0) {
					throw new WrongPasswordException();
				} else {
					passwordByteArray = password.getPasswordBytesIsoEncoded();
				}

				try {
					puttyKeyEncryptionKey = getPuttyPrivateKeyEncryptionKeyVersion3Argon2(passwordByteArray, keyProperties.get("Key-Derivation"), Integer.parseInt(keyProperties.get("Argon2-Memory")), Integer.parseInt(keyProperties.get("Argon2-Passes")), Integer.parseInt(keyProperties.get("Argon2-Parallelism")), fromHexString(keyProperties.get("Argon2-Salt")));
					final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
					cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(puttyKeyEncryptionKey, 0, 32, "AES"), new IvParameterSpec(puttyKeyEncryptionKey, 32, 16));

					privateKeyData = cipher.doFinal(base64Decoder.decode(keyProperties.get("Private-Lines")));
				} catch (final Exception e) {
					throw new Exception("Cannot decrypt PuTTY private key data", e);
				}
			} else {
				throw new Exception("Unsupported key encryption method: " + keyProperties.get("Encryption"));
			}

			try {
				final String calculatedMacChecksum;
				if ("none".equalsIgnoreCase(encryptionMethod)) {
					calculatedMacChecksum = calculatePuttyMacChecksumVersion3Argon2(cipherName, "none", keyProperties.get("Comment"), publicKeyData, privateKeyData, puttyKeyEncryptionKey);
				} else if ("aes256-cbc".equalsIgnoreCase(encryptionMethod)) {
					calculatedMacChecksum = calculatePuttyMacChecksumVersion3Argon2(cipherName, keyProperties.get("Encryption"), keyProperties.get("Comment"), publicKeyData, privateKeyData, puttyKeyEncryptionKey);
				} else {
					throw new Exception("Unsupported key encryption method: " + keyProperties.get("Encryption"));
				}
				final String foundMacChecksum = keyProperties.get("Private-MAC");
				if (foundMacChecksum == null || !foundMacChecksum.equalsIgnoreCase(calculatedMacChecksum)) {
					throw new WrongPasswordException();
				}
			} catch (final WrongPasswordException e) {
				throw e;
			} catch (final Exception e) {
				throw new Exception("Invalid PuTTY key data: " + e.getMessage(), e);
			}
		} finally {
			clear(puttyKeyEncryptionKey);
		}

		final KeyPair keyPair = readPuttyKeyData(privateKeyData, publicKeyData);
		return new SshKey(SshKeyFormat.Putty3, keyProperties.get("Comment"), keyPair);
	}

	private static byte[] getPuttyPrivateKeyEncryptionKeyVersion3Argon2(final byte[] passwordByteArray, final String argon2Type, final int argon2Memory, final int argon2Passes, final int argon2Parallelism, final byte[] argon2Salt) throws Exception {
		int argon2TypeInt;
		if ("Argon2i".equalsIgnoreCase(argon2Type)) {
			argon2TypeInt = Argon2Parameters.ARGON2_i;
		} else if ("Argon2d".equalsIgnoreCase(argon2Type)) {
			argon2TypeInt = Argon2Parameters.ARGON2_d;
		} else if ("Argon2id".equalsIgnoreCase(argon2Type)) {
			argon2TypeInt = Argon2Parameters.ARGON2_id;
		} else {
			throw new Exception("Unsupported Key-Derivation (Only \"Argon2i\", \"Argon2d\", \"Argon2id\" are supported): " + argon2Type);
		}
		final Argon2Parameters.Builder builder = new Argon2Parameters.Builder(argon2TypeInt)
				.withVersion(Argon2Parameters.ARGON2_VERSION_13)
				.withIterations(argon2Passes)
				.withMemoryAsKB(argon2Memory)
				.withParallelism(argon2Parallelism)
				.withSalt(argon2Salt);
		final Argon2BytesGenerator argon2BytesGenerator = new Argon2BytesGenerator();
		argon2BytesGenerator.init(builder.build());
		final byte[] puttyKeyEncryptionKey = new byte[80];
		argon2BytesGenerator.generateBytes(passwordByteArray, puttyKeyEncryptionKey);
		return puttyKeyEncryptionKey;
	}

	private static String calculatePuttyMacChecksumVersion3Argon2(final String keyType, final String encryptionType, final String comment, final byte[] publicKey, final byte[] privateKey, final byte[] puttyKeyEncryptionKey) throws Exception {
		final Mac mac = Mac.getInstance("HMACSHA256");
		if (puttyKeyEncryptionKey != null) {
			mac.init(new SecretKeySpec(puttyKeyEncryptionKey, 48, 32, mac.getAlgorithm()));
		} else {
			mac.init(new SecretKeySpec(new byte[32], 0, 32, mac.getAlgorithm()));
		}

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DataOutputStream data = new DataOutputStream(out);

		final byte[] keyTypeBytes = keyType.getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(keyTypeBytes.length);
		data.write(keyTypeBytes);

		final byte[] encryptionTypeBytes = encryptionType.getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(encryptionTypeBytes.length);
		data.write(encryptionTypeBytes);

		final byte[] commentBytes = (comment == null ? "" : comment).getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(commentBytes.length);
		data.write(commentBytes);

		data.writeInt(publicKey.length);
		data.write(publicKey);

		data.writeInt(privateKey.length);
		data.write(privateKey);

		return toHexString(mac.doFinal(out.toByteArray())).toLowerCase();
	}

	private static KeyPair readPuttyKeyData(final byte[] privateKeyData, final byte[] publicKeyData) throws Exception {
		try {
			final BlockDataReader publicKeyReader = new BlockDataReader(publicKeyData);
			final String algorithmName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);

			final BlockDataReader privateKeyReader = new BlockDataReader(privateKeyData);

			if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
				final BigInteger publicExponent = publicKeyReader.readBigInt();
				final BigInteger modulus = publicKeyReader.readBigInt();

				final BigInteger privateExponent = privateKeyReader.readBigInt();
				final BigInteger p = privateKeyReader.readBigInt(); // secret prime factor (= PrimeP)
				final BigInteger q = privateKeyReader.readBigInt(); // secret prime factor (= PrimeQ)
				final BigInteger iqmp = privateKeyReader.readBigInt(); // q^-1 mod p (= CrtCoefficient)

				final BigInteger dmp1 = privateExponent.mod(p.subtract(BigInteger.ONE)); // d mod (p-1) (= PrimeExponentP)
				final BigInteger dmq1 = privateExponent.mod(q.subtract(BigInteger.ONE)); // d mod (q-1) (= PrimeExponentQ)

				final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				final PublicKey publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
				final PrivateKey privateKey = keyFactory.generatePrivate(new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, p, q, dmp1, dmq1, iqmp));
				return new KeyPair(publicKey, privateKey);
			} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
				final BigInteger p = publicKeyReader.readBigInt();
				final BigInteger q = publicKeyReader.readBigInt();
				final BigInteger g = publicKeyReader.readBigInt();

				// Public key exponent
				final BigInteger y = publicKeyReader.readBigInt();

				// Private key exponent
				final BigInteger x = privateKeyReader.readBigInt();

				final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
				final PublicKey publicKey = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
				final PrivateKey privateKey = keyFactory.generatePrivate(new DSAPrivateKeySpec(x, p, q, g));
				return new KeyPair(publicKey, privateKey);
			} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)
					|| "ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)
					|| "ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
				final String ecdsaCurveName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);
				if (!"nistp256".equals(ecdsaCurveName)
						&& !"nistp384".equals(ecdsaCurveName)
						&& !"nistp521".equals(ecdsaCurveName)) {
					throw new Exception("Unsupported ECDSA curveName: " + ecdsaCurveName);
				} else {
					final byte[] eccKeyBlobBytes = publicKeyReader.readData();

					Security.addProvider(new BouncyCastleProvider());
					final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
					final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
					final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
					final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
					final PublicKey publicKey = keyFactory.generatePublic(pubSpec);

					final BigInteger s = privateKeyReader.readBigInt();

					final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
					parameters.init(new ECGenParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1"));
					final ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
					final PrivateKey privateKey = keyFactory.generatePrivate(new ECPrivateKeySpec(s, ecParameterSpec));

					return new KeyPair(publicKey, privateKey);
				}
			} else {
				throw new IllegalArgumentException("Invalid public key algorithm for PuTTY key (only supports RSA / DSA / ECDSA / EdDSA): " + algorithmName);
			}
		} catch (final Exception e) {
			throw new Exception("Cannot read key data", e);
		}
	}

	private static PublicKey readPuttyPublicKeyData(final byte[] publicKeyData) throws Exception {
		try {
			final BlockDataReader publicKeyReader = new BlockDataReader(publicKeyData);
			final String algorithmName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);

			if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
				final BigInteger publicExponent = publicKeyReader.readBigInt();
				final BigInteger modulus = publicKeyReader.readBigInt();

				final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
			} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
				final BigInteger p = publicKeyReader.readBigInt();
				final BigInteger q = publicKeyReader.readBigInt();
				final BigInteger g = publicKeyReader.readBigInt();

				// Public key exponent
				final BigInteger y = publicKeyReader.readBigInt();

				final KeyFactory keyFactory = KeyFactory.getInstance("DSA");
				return keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
			} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)
					|| "ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)
					|| "ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
				final String ecdsaCurveName = new String(publicKeyReader.readData(), StandardCharsets.UTF_8);
				if (!"nistp256".equals(ecdsaCurveName)
						&& !"nistp384".equals(ecdsaCurveName)
						&& !"nistp521".equals(ecdsaCurveName)) {
					throw new Exception("Unsupported ECDSA curveName: " + ecdsaCurveName);
				} else {
					final byte[] eccKeyBlobBytes = publicKeyReader.readData();

					Security.addProvider(new BouncyCastleProvider());
					final KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
					final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecdsaCurveName.replace("nist", "sec") + "r1");
					final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(eccKeyBlobBytes);
					final org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(point, ecSpec);
					return keyFactory.generatePublic(pubSpec);
				}
			} else {
				throw new IllegalArgumentException("Invalid public key algorithm for PuTTY key (only supports RSA / DSA / ECDSA / EdDSA): " + algorithmName);
			}
		} catch (final Exception e) {
			throw new Exception("Cannot read key data", e);
		}
	}

	/**
	 * Fix the encoding of a String if it was stored in UTF-8 encoding but decoded with ISO-8859-1 encoding
	 *
	 * Examples of byte data of wrongly encoded Umlauts and other special characters:
	 *	Ä: [-61, -124]
	 *	ä: [-61, -92]
	 *	ß: [-61, -97]
	 *	è: [-61, -88]
	 */
	private static String fixCommentEncodingIfNeeded(final String comment) {
		boolean wrongEncodingDetected = false;
		for (final byte nextByte : comment.getBytes(StandardCharsets.ISO_8859_1)) {
			if (nextByte == -61) {
				wrongEncodingDetected = true;
				break;
			}
		}
		if (wrongEncodingDetected) {
			return new String(comment.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		} else {
			return comment;
		}
	}

	public static boolean isBlank(final String value) {
		return value == null || value.length() == 0 || value.trim().length() == 0;
	}

	public static boolean isNotBlank(final String value) {
		return !isBlank(value);
	}

	public static void clear(final byte[] array) {
		if (array != null) {
			Arrays.fill(array, (byte) 0);
		}
	}
}
