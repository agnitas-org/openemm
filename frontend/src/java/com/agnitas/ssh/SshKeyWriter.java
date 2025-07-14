/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ssh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.jce.ECNamedCurveTable;

import com.agnitas.ssh.Asn1Codec.DerTag;

/**
 * Writer for SSH public and private keys with optional password protection<br />
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
public class SshKeyWriter {
	/**
	 * Converts this keypair into protected OpenSSH format<br />
	 * This format includes private and public key data and is accepted by PuTTY's key import<br />
	 * <br />
	 * passwordEncoding is used for encoding of password:<br />
	 *	- default is "UTF-8"<br />
	 *	- other value may be "ISO-8859-1"<br />
	 * <br />
	 * Watchout for PuTTY's key import can only use special characters in passwords, if the ISO-8859-1 encoding is used for passwordAndCommentEncoding.<br />
	 * But OpenSSH's default encoding is UTF-8<br />
	 */
	public static void writeOpenSshv1Key(final OutputStream outputStream, final SshKey sshKey, final char[] passwordChars, Charset passwordAndCommentEncoding) throws Exception {
		if (passwordAndCommentEncoding == null) {
			passwordAndCommentEncoding = StandardCharsets.UTF_8;
		}

		final byte[] publicKeyData = KeyPairUtilities.getPublicKeyBytes(sshKey.getKeyPair().getPublic());
		final byte[] privateKeyData = getOpenSshv1PrivateKeyBytes(sshKey, passwordAndCommentEncoding);

		final BlockDataWriter keyDataBuffer = new BlockDataWriter();
		// Storage format name
		keyDataBuffer.writeZeroLimitedData("openssh-key-v1".getBytes(StandardCharsets.UTF_8));

		byte[] kdfInitialVectorBytes = null;
		int kdfRounds = 0;
		if (passwordChars == null) {
			// EncryptionCipherName
			keyDataBuffer.writeData("none".getBytes(StandardCharsets.UTF_8));
			// kdf: key derivation function
			keyDataBuffer.writeData("none".getBytes(StandardCharsets.UTF_8));
			// kdf info
			keyDataBuffer.writeSimpleInt(0);
		} else {
			// EncryptionCipherName
			keyDataBuffer.writeData("aes256-ctr".getBytes(StandardCharsets.UTF_8));
			// kdf: key derivation function
			keyDataBuffer.writeData("bcrypt".getBytes(StandardCharsets.UTF_8));
			// kdf info
			kdfInitialVectorBytes = new byte[16];
			new SecureRandom().nextBytes(kdfInitialVectorBytes);
			kdfRounds = 16;
			final BlockDataWriter kdfInfoWriter = new BlockDataWriter();
			kdfInfoWriter.writeData(kdfInitialVectorBytes);
			kdfInfoWriter.writeSimpleInt(kdfRounds);
			keyDataBuffer.writeData(kdfInfoWriter.toByteArray());
		}

		// Amount of stored keys
		keyDataBuffer.writeSimpleInt(1);

		// Public key
		keyDataBuffer.writeData(publicKeyData);

		// Private key
		try (final Password password = passwordChars == null ? null : new Password(passwordChars.clone())) {
			if (password != null) {
				// Encrypt private key data by bcrypt pbkdf
				// Putty uses "ISO-8859-1" for password encoding, even for those keys stored in OpenSSHv1 and OpenSSL format
				// "ssh-keygen" on Linx uses UTF-8 for password encoding
				final byte[] privateKeyDataBytesEncrypted;
				final byte[] passwordBytes = StandardCharsets.UTF_8.equals(passwordAndCommentEncoding) ? password.getPasswordBytesUtfEncoded() : password.getPasswordBytesIsoEncoded();
				byte[] derivedKeyBytes = null;
				try {
					derivedKeyBytes = new byte[48];
					new BCryptPBKDF().derivePassword(passwordBytes, kdfInitialVectorBytes, kdfRounds, derivedKeyBytes);
					final SecretKey secretKey = new SecretKeySpec(derivedKeyBytes, 0, 32, "AES");
					final AlgorithmParameterSpec iv = new IvParameterSpec(derivedKeyBytes, 32, 16);

					final Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
					cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

					privateKeyDataBytesEncrypted = cipher.doFinal(privateKeyData);
				} finally {
					clear(derivedKeyBytes);
				}

				keyDataBuffer.writeData(privateKeyDataBytesEncrypted);
			} else {
				keyDataBuffer.writeData(privateKeyData);
			}
		}

		outputStream.write(("-----BEGIN OPENSSH PRIVATE KEY-----\n").getBytes(StandardCharsets.UTF_8));
		outputStream.write(toWrappedBase64(keyDataBuffer.toByteArray(), 64, "\n").getBytes(StandardCharsets.UTF_8));
		outputStream.write(("\n-----END OPENSSH PRIVATE KEY-----\n").getBytes(StandardCharsets.UTF_8));
	}

	private static byte[] getOpenSshv1PrivateKeyBytes(final SshKey sshKey, final Charset commentCharset) throws Exception {
		if (sshKey.getKeyPair().getPrivate() == null) {
			throw new Exception("Invalid empty privateKey parameter");
		} else {
			final BlockDataWriter privateKeyWriter = new BlockDataWriter();
			final int checkInt = new SecureRandom().nextInt();
			privateKeyWriter.writeSimpleInt(checkInt);
			privateKeyWriter.writeSimpleInt(checkInt);

			if (sshKey.getKeyPair().getPrivate() instanceof RSAPrivateCrtKey) {
				privateKeyWriter.writeData("ssh-rsa".getBytes(StandardCharsets.UTF_8));
				final RSAPrivateCrtKey privateKeyRSA = (RSAPrivateCrtKey) sshKey.getKeyPair().getPrivate();
				privateKeyWriter.writeBigInt(privateKeyRSA.getModulus());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPublicExponent());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrivateExponent());
				privateKeyWriter.writeBigInt(privateKeyRSA.getCrtCoefficient());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeP());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeQ());
			} else if (sshKey.getKeyPair().getPrivate() instanceof DSAPrivateKey) {
				privateKeyWriter.writeData("ssh-dss".getBytes(StandardCharsets.UTF_8));
				final DSAPrivateKey privateKeyDSA = (DSAPrivateKey) sshKey.getKeyPair().getPrivate();
				final DSAPublicKey publicKeyDSA = (DSAPublicKey) sshKey.getKeyPair().getPublic();
				privateKeyWriter.writeBigInt(privateKeyDSA.getParams().getP());
				privateKeyWriter.writeBigInt(privateKeyDSA.getParams().getQ());
				privateKeyWriter.writeBigInt(privateKeyDSA.getParams().getG());
				privateKeyWriter.writeBigInt(publicKeyDSA.getY());
				privateKeyWriter.writeBigInt(privateKeyDSA.getX());
			} else if (sshKey.getKeyPair().getPrivate() instanceof ECPrivateKey) {
				final ECPrivateKey privateKeyEC = (ECPrivateKey) sshKey.getKeyPair().getPrivate();
				final ECPublicKey publicKeyEC = (ECPublicKey) sshKey.getKeyPair().getPublic();
				final String ecCurveName = KeyPairUtilities.getEcDsaEllipticCurveName(publicKeyEC);
				privateKeyWriter.writeData(("ecdsa-sha2-" + ecCurveName).getBytes(StandardCharsets.UTF_8));
				privateKeyWriter.writeData((ecCurveName).getBytes(StandardCharsets.UTF_8));
				final org.bouncycastle.jce.spec.ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecCurveName.replace("nist", "sec") + "r1");
				final org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().createPoint(publicKeyEC.getW().getAffineX(), publicKeyEC.getW().getAffineY());
				final byte[] eccKeyBlobBytes = point.getEncoded(false);
				privateKeyWriter.writeData(eccKeyBlobBytes);
				privateKeyWriter.writeBigInt(privateKeyEC.getS());
			} else {
				throw new IllegalArgumentException("Unsupported SSH cipher algorithm");
			}

			privateKeyWriter.writeData(sshKey.getComment() == null ? new byte[0] : sshKey.getComment().getBytes(commentCharset));

			// Padding
			final int paddingSize = 16 - (privateKeyWriter.toByteArray().length % 16);
			if (paddingSize < 16) {
				for (int i = 0; i < paddingSize; i++) {
					privateKeyWriter.writePaddingByte((byte) (i + 1));
				}
			}

			return privateKeyWriter.toByteArray();
		}
	}

	private static class BlockDataWriter {
		private final ByteArrayOutputStream outputStream;
		private final DataOutput keyDataOutput;

		private BlockDataWriter() {
			outputStream = new ByteArrayOutputStream();
			keyDataOutput = new DataOutputStream(outputStream);
		}

		private byte[] toByteArray() {
			return outputStream.toByteArray();
		}

		private void writeSimpleInt(final int value) throws Exception {
			keyDataOutput.writeInt(value);
		}

		private void writeBigInt(final BigInteger value) throws Exception {
			writeData(value.toByteArray());
		}

		private void writeData(final byte[] value) throws Exception {
			keyDataOutput.writeInt(value.length);
			if (value.length > 0) {
				keyDataOutput.write(value);
			}
		}

		private void writeZeroLimitedData(final byte[] value) throws IOException, Exception {
			keyDataOutput.write(value);
			keyDataOutput.write(new byte[] { 0 });
		}

		private void writePaddingByte(final byte value) throws Exception {
			keyDataOutput.write(value);
		}
	}

	/**
	 * Converts this public key into unprotected PEM format (PKCS#1) for OpenSSH keys<br />
	 * This format includes public key data only and is NOT accepted by PuTTY's key import<br />
	 */
	public static void writePKCS1Format(final OutputStream outputStream, final PublicKey publicKey) throws Exception {
		final byte[] publicKeyBytes = KeyPairUtilities.getPublicKeyBytes(publicKey);

		final String publicKeyBase64 = toWrappedBase64(publicKeyBytes, 64, "\r\n");

		final StringBuilder content = new StringBuilder();
		content.append("---- BEGIN SSH2 PUBLIC KEY ----").append("\r\n");
		content.append(publicKeyBase64).append("\r\n");
		content.append("---- END SSH2 PUBLIC KEY ----").append("\r\n");

		outputStream.write(content.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Converts this key into PEM format (PKCS#8) for OpenSSH keys<br />
	 * Using default encryption method "AES-128-CBC", when the optional password is set.<br />
	 * This format includes private and public key data and is accepted by PuTTY's key import.<br />
	 * <br />
	 * Watchout for PuTTY's key import can only use special characters in passwords, if the ISO-8859-1 encoding is used for passwordEncoding.<br />
	 * But OpenSSH's default encoding is UTF-8<br />
	 */
	public static void writePKCS8Format(final OutputStream outputStream, final KeyPair keyPair, final char[] passwordChars, final Charset passwordEncoding) throws Exception {
		writePKCS8Format(outputStream, keyPair, null, passwordChars, passwordEncoding);
	}

	/**
	 * Converts this keypair into protected PEM format (PKCS#8) for OpenSSL keys<br />
	 * This format includes private and public key data and is accepted by PuTTY's key import<br />
	 * <br />
	 * keyEncryptionCipherName:<br />
	 *	- default is "AES-128-CBC"<br />
	 *	- other value may be "DES-EDE3-CBC"<br />
	 * <br />
	 * passwordEncoding is used for encoding of password:<br />
	 *	- default is "UTF-8"<br />
	 *	- other value may be "ISO-8859-1"<br />
	 * <br />
	 * Watchout for PuTTY's key import can only use special characters in passwords, if the ISO-8859-1 encoding is used for passwordEncoding.<br />
	 * But OpenSSH's default encoding is UTF-8<br />
	 */
	public static void writePKCS8Format(final OutputStream outputStream, final KeyPair keyPair, String keyEncryptionCipherName, final char[] passwordChars, final Charset passwordEncoding) throws Exception {
		String keyTypeName;
		byte[] keyData;
		final String algorithmName = KeyPairUtilities.getAlgorithm(keyPair);
		if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "RSA PRIVATE KEY";
			keyData = createRsaBinaryKey(keyPair);
		} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "DSA PRIVATE KEY";
			keyData = createDsaBinaryKey(keyPair);
		} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "EC PRIVATE KEY";
			keyData = createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP256_ARRAY);
		} else if ("ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "EC PRIVATE KEY";
			keyData = createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP384_ARRAY);
		} else if ("ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "EC PRIVATE KEY";
			keyData = createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP521_ARRAY);
		} else if ("ssh-ed25519".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "PRIVATE KEY";
			keyData = keyPair.getPrivate().getEncoded();
		} else if ("ssh-ed448".equalsIgnoreCase(algorithmName)) {
			keyTypeName = "PRIVATE KEY";
			keyData = keyPair.getPrivate().getEncoded();
		} else {
			throw new IllegalArgumentException("Unsupported cipher: " + algorithmName);
		}

		final Map<String, String> headers = new LinkedHashMap<>();

		if (passwordChars != null && passwordChars.length > 0) {
			try (Password password = new Password(passwordChars.clone())) {
				final byte[] passwordBytes;
				if (passwordEncoding == null) {
					passwordBytes = password.getPasswordBytesUtfEncoded();
				} else if (passwordEncoding == StandardCharsets.UTF_8) {
					passwordBytes = password.getPasswordBytesUtfEncoded();
				} else if (passwordEncoding == StandardCharsets.ISO_8859_1) {
					passwordBytes = password.getPasswordBytesIsoEncoded();
				} else {
					throw new Exception("Unsupported passwordEncoding: " + passwordEncoding.name());
				}

				if (keyEncryptionCipherName == null || "".equals(keyEncryptionCipherName.trim())) {
					keyEncryptionCipherName = "AES-128-CBC";
				}

				if (!"AES-128-CBC".equalsIgnoreCase(keyEncryptionCipherName) && !"DES-EDE3-CBC".equalsIgnoreCase(keyEncryptionCipherName)) {
					throw new Exception("Unknown key encryption cipher: " + keyEncryptionCipherName);
				}

				final SecureRandom rnd = new SecureRandom();
				final Cipher cipher;
				final String ivString;
				if ("DES-EDE3-CBC".equalsIgnoreCase(keyEncryptionCipherName)) {
					final byte[] iv = new byte[8];
					rnd.nextBytes(iv);
					ivString = toHexString(iv);
					cipher = Cipher.getInstance("DESede/CBC/NoPadding");
					cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(stretchPasswordForOpenSsl(passwordBytes, iv, 8, 24), "DESede"), new IvParameterSpec(iv));
					keyData = addLengthCodedPadding(keyData, 8);
				} else if ("AES-128-CBC".equalsIgnoreCase(keyEncryptionCipherName)) {
					final byte[] iv = new byte[16];
					rnd.nextBytes(iv);
					ivString = toHexString(iv);
					cipher = Cipher.getInstance("AES/CBC/NoPadding");
					cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(stretchPasswordForOpenSsl(passwordBytes, iv, 8, 16), "AES"), new IvParameterSpec(iv));
					keyData = addLengthCodedPadding(keyData, 16);
				} else {
					throw new Exception("Unknown key encryption cipher: " + keyEncryptionCipherName);
				}
				headers.put("Proc-Type", "4,ENCRYPTED");
				headers.put("DEK-Info", keyEncryptionCipherName.toUpperCase() + "," + ivString);

				keyData = cipher.doFinal(keyData);
			}
		}

		outputStream.write(("-----BEGIN " + keyTypeName + "-----\n").getBytes(StandardCharsets.UTF_8));
		outputStream.write(getPemHeaderLines(headers, 64).getBytes(StandardCharsets.UTF_8));
		outputStream.write(toWrappedBase64(keyData, 64, "\n").getBytes(StandardCharsets.UTF_8));
		outputStream.write(("\n-----END " + keyTypeName + "-----\n").getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Converts this key into unprotected DER format (binary data) for OpenSSH keys<br />
	 * <br />
	 * <b>Use with caution, because this key format is not protected by any password</>
	 */
	public static void writeDerFormat(final OutputStream outputStream, final KeyPair keyPair) throws Exception {
		final String algorithmName = KeyPairUtilities.getAlgorithm(keyPair);
		if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
			outputStream.write(createRsaBinaryKey(keyPair));
		} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
			outputStream.write(createDsaBinaryKey(keyPair));
		} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)) {
			outputStream.write(createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP256_ARRAY));
		} else if ("ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)) {
			outputStream.write(createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP384_ARRAY));
		} else if ("ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
			outputStream.write(createEcdsaBinaryKey(keyPair, OID.ECDSA_CURVE_NISTP521_ARRAY));
		} else {
			throw new IllegalArgumentException("Unsupported cipher: " + algorithmName);
		}
	}

	private static String getPemHeaderLines(final Map<String, String> headers, final int maxLineLimit) {
		final StringBuilder headerBuilder = new StringBuilder();
		if (headers != null && !headers.isEmpty()) {
			for (final Entry<String, String> entry : headers.entrySet()) {
				headerBuilder.append(entry.getKey() + ": ");
				if ((entry.getKey().length() + entry.getValue().length() + 2) > maxLineLimit) {
					int offset = Math.max(maxLineLimit - entry.getKey().length() - 2, 0);
					headerBuilder.append(entry.getValue().substring(0, offset) + "\\" + "\n");
					for (; offset < entry.getValue().length(); offset += maxLineLimit) {
						if ((offset + maxLineLimit) >= entry.getValue().length()) {
							headerBuilder.append(entry.getValue().substring(offset) + "\n");
						} else {
							headerBuilder.append(entry.getValue().substring(offset, offset + maxLineLimit) + "\\" + "\n");
						}
					}
				} else {
					headerBuilder.append(entry.getValue() + "\n");
				}
			}

			headerBuilder.append("\n");
		}
		return headerBuilder.toString();
	}

	private static byte[] stretchPasswordForOpenSsl(final byte[] passwordBytes, final byte[] iv, final int usingIvSize, final int keySize) throws Exception {
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

	private static byte[] createRsaBinaryKey(final KeyPair keyPair) throws Exception {
		final RSAPrivateCrtKey privateKey = ((RSAPrivateCrtKey) keyPair.getPrivate());

		return Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_SEQUENCE,
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, BigInteger.ZERO.toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getModulus().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPublicExponent().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPrivateExponent().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPrimeP().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPrimeQ().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPrimeExponentP().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getPrimeExponentQ().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getCrtCoefficient().toByteArray())
				);
	}

	private static byte[] createDsaBinaryKey(final KeyPair keyPair) throws Exception {
		final DSAPublicKey publicKey = ((DSAPublicKey) keyPair.getPublic());
		final DSAPrivateKey privateKey = ((DSAPrivateKey) keyPair.getPrivate());

		return Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_SEQUENCE,
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, BigInteger.ZERO.toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getParams().getP().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getParams().getQ().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getParams().getG().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, publicKey.getY().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, privateKey.getX().toByteArray())
				);
	}

	private static byte[] createEcdsaBinaryKey(final KeyPair keyPair, final byte[] oidKey) throws Exception {
		final ECPrivateKey privateKey = ((ECPrivateKey) keyPair.getPrivate());
		final ECPublicKey publicKey = ((ECPublicKey) keyPair.getPublic());

		byte[] qBytes;
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(publicKey.getEncoded());
		if (Asn1Codec.DER_TAG_SEQUENCE != enclosingDerTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> derDataTags = Asn1Codec.readDerTags(enclosingDerTag.getData());
		if (Asn1Codec.DER_TAG_SEQUENCE != derDataTags.get(0).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> sshAlgorithmDerTags = Asn1Codec.readDerTags(derDataTags.get(0).getData());
		final OID ecDsaPublicKeyOid = new OID(sshAlgorithmDerTags.get(0).getData());
		if (Arrays.equals(OID.ECDSA_PUBLICKEY_ARRAY, ecDsaPublicKeyOid.getByteArrayEncoding())) {
			final OID ecDsaCurveOid = new OID(sshAlgorithmDerTags.get(1).getData());
			if (Arrays.equals(OID.ECDSA_CURVE_NISTP256_ARRAY, ecDsaCurveOid.getByteArrayEncoding())
					|| Arrays.equals(OID.ECDSA_CURVE_NISTP384_ARRAY, ecDsaCurveOid.getByteArrayEncoding())
					|| Arrays.equals(OID.ECDSA_CURVE_NISTP521_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				if (Asn1Codec.DER_TAG_BIT_STRING != derDataTags.get(1).getTagId()) {
					throw new Exception("Invalid key data found");
				} else {
					qBytes = derDataTags.get(1).getData();
				}
			} else {
				throw new Exception("Unknown SSH EcDSA curve OID: " + ecDsaCurveOid.getStringEncoding());
			}
		} else {
			throw new Exception("Unknown SSH EcDSA public key OID: " + ecDsaPublicKeyOid.getStringEncoding());
		}

		return Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_SEQUENCE,
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_INTEGER, BigInteger.ONE.toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_OCTET_STRING, privateKey.getS().toByteArray()),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_CONTEXT_SPECIFIC_0, Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_OBJECT, oidKey)),
				Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_CONTEXT_SPECIFIC_1, Asn1Codec.createDerTagData(Asn1Codec.DER_TAG_BIT_STRING, qBytes))
				);
	}

	private static byte[] addLengthCodedPadding(final byte[] data, final int paddingSize) {
		final byte[] dataPadded;
		if (data.length % paddingSize != 0) {
			dataPadded = new byte[((data.length / paddingSize) + 1) * paddingSize];
		} else {
			dataPadded = new byte[data.length + paddingSize];
		}
		for (int i = 0; i < data.length; i++) {
			dataPadded[i] = data[i];
		}
		final byte padValue = (byte) (dataPadded.length - data.length);
		for (int i = data.length; i < dataPadded.length; i++) {
			dataPadded[i] = padValue;
		}
		return dataPadded;
	}

	public static void writePuttyVersion2Key(final OutputStream outputStream, final SshKey sshKey, final char[] passwordChars) throws Exception {
		try (Password password = passwordChars == null ? null : new Password(passwordChars.clone())) {
			final String algorithmName = sshKey.getAlgorithm();

			final byte[] publicKeyBytes = KeyPairUtilities.getPublicKeyBytes(sshKey.getKeyPair().getPublic());
			byte[] privateKeyBytes = getPuttyVersion2PrivateKeyBytes(sshKey.getKeyPair().getPrivate());

			// padding up to multiple of 16 bytes for AES/CBC/NoPadding encryption
			privateKeyBytes = addRandomPadding(privateKeyBytes, 16);

			final String macHash = calculatePuttyMacChecksumVersion2(password, algorithmName, sshKey.getComment(), publicKeyBytes, privateKeyBytes);

			if (password != null) {
				final byte[] puttyKeyEncryptionKey = getPuttyPrivateKeyEncryptionKeyVersion2(password.getPasswordBytesIsoEncoded());

				final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
				cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(puttyKeyEncryptionKey, 0, 32, "AES"), new IvParameterSpec(new byte[16])); // initial vector=0

				privateKeyBytes = cipher.doFinal(privateKeyBytes);
			}

			final String publicKeyBase64 = toWrappedBase64(publicKeyBytes, 64, "\r\n");
			final String privateKeyBase64 = toWrappedBase64(privateKeyBytes, 64, "\r\n");

			final StringBuilder content = new StringBuilder();
			content.append("PuTTY-User-Key-File-2: ").append(algorithmName).append("\r\n");
			content.append("Encryption: ").append(password == null ? "none" : "aes256-cbc").append("\r\n");
			content.append("Comment: ").append(sshKey.getComment()).append("\r\n");
			content.append("Public-Lines: ").append(getLineCount(publicKeyBase64)).append("\r\n");
			content.append(publicKeyBase64).append("\r\n");
			content.append("Private-Lines: ").append(getLineCount(privateKeyBase64)).append("\r\n");
			content.append(privateKeyBase64).append("\r\n");
			content.append("Private-MAC: ").append(macHash);

			outputStream.write(content.toString().getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	public static byte[] getPuttyVersion2PrivateKeyBytes(final PrivateKey privateKey) throws Exception {
		if (privateKey == null) {
			throw new Exception("Invalid empty privateKey parameter");
		} else {
			final BlockDataWriter privateKeyWriter = new BlockDataWriter();
			if (privateKey instanceof RSAPrivateCrtKey) {
				final RSAPrivateCrtKey privateKeyRSA = (RSAPrivateCrtKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrivateExponent());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeP());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeQ());
				privateKeyWriter.writeBigInt(privateKeyRSA.getCrtCoefficient());
			} else if (privateKey instanceof DSAPrivateKey) {
				final DSAPrivateKey privateKeyDSA = (DSAPrivateKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyDSA.getX());
			} else if (privateKey instanceof ECPrivateKey) {
				final ECPrivateKey privateKeyEC = (ECPrivateKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyEC.getS());
			} else {
				throw new IllegalArgumentException("Unsupported SSH cipher algorithm");
			}
			return privateKeyWriter.toByteArray();
		}
	}

	public static void writePuttyVersion3Key(final OutputStream outputStream, final SshKey sshKey, final char[] passwordChars) throws Exception {
		try (final Password password = passwordChars == null ? null : new Password(passwordChars.clone())) {
			final String algorithmName = sshKey.getAlgorithm();

			final byte[] publicKeyBytes = KeyPairUtilities.getPublicKeyBytes(sshKey.getKeyPair().getPublic());
			byte[] privateKeyBytes = getPuttyVersion3PrivateKeyBytes(sshKey.getKeyPair().getPrivate());

			// padding up to multiple of 16 bytes for AES/CBC/NoPadding encryption
			privateKeyBytes = addRandomPadding(privateKeyBytes, 16);

			final String publicKeyBase64 = toWrappedBase64(publicKeyBytes, 64, "\r\n");

			final StringBuilder content = new StringBuilder();
			content.append("PuTTY-User-Key-File-3: ").append(algorithmName).append("\r\n");
			content.append("Encryption: ").append(password == null ? "none" : "aes256-cbc").append("\r\n");
			content.append("Comment: ").append(sshKey.getComment()).append("\r\n");
			content.append("Public-Lines: ").append(getLineCount(publicKeyBase64)).append("\r\n");
			content.append(publicKeyBase64).append("\r\n");

			final String macHash;

			if (password != null) {
				final String keyDerivation = "Argon2id";
				content.append("Key-Derivation: ").append(keyDerivation).append("\r\n");
				final int argon2Memory = 8192;
				content.append("Argon2-Memory: ").append(argon2Memory).append("\r\n");
				final int argon2Passes = 21;
				content.append("Argon2-Passes: ").append(argon2Passes).append("\r\n");
				final int argon2Parallelism = 1;
				content.append("Argon2-Parallelism: ").append(argon2Parallelism).append("\r\n");
				final byte[] argon2Salt = new byte[16];
				new SecureRandom().nextBytes(argon2Salt);
				content.append("Argon2-Salt: ").append(toHexString(argon2Salt)).append("\r\n");

				byte[] puttyKeyEncryptionKey = null;
				try {
					puttyKeyEncryptionKey = getPuttyPrivateKeyEncryptionKeyVersion3Argon2(password.getPasswordBytesIsoEncoded(), keyDerivation, argon2Memory, argon2Passes, argon2Parallelism, argon2Salt);
					macHash = calculatePuttyMacChecksumVersion3Argon2(algorithmName, "aes256-cbc", sshKey.getComment(), publicKeyBytes, privateKeyBytes, puttyKeyEncryptionKey);

					final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
					cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(puttyKeyEncryptionKey, 0, 32, "AES"), new IvParameterSpec(puttyKeyEncryptionKey, 32, 16));

					privateKeyBytes = cipher.doFinal(privateKeyBytes);
				} finally {
					clear(puttyKeyEncryptionKey);
				}
			} else {
				macHash = calculatePuttyMacChecksumVersion3Argon2(algorithmName, "none", sshKey.getComment(), publicKeyBytes, privateKeyBytes, null);
			}

			final String privateKeyBase64 = toWrappedBase64(privateKeyBytes, 64, "\r\n");

			content.append("Private-Lines: ").append(getLineCount(privateKeyBase64)).append("\r\n");
			content.append(privateKeyBase64).append("\r\n");
			content.append("Private-MAC: ").append(macHash);

			outputStream.write(content.toString().getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	public static byte[] getPuttyVersion3PrivateKeyBytes(final PrivateKey privateKey) throws Exception {
		if (privateKey == null) {
			throw new Exception("Invalid empty privateKey parameter");
		} else {
			final BlockDataWriter privateKeyWriter = new BlockDataWriter();
			if (privateKey instanceof RSAPrivateCrtKey) {
				final RSAPrivateCrtKey privateKeyRSA = (RSAPrivateCrtKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrivateExponent());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeP());
				privateKeyWriter.writeBigInt(privateKeyRSA.getPrimeQ());
				privateKeyWriter.writeBigInt(privateKeyRSA.getCrtCoefficient());
			} else if (privateKey instanceof DSAPrivateKey) {
				final DSAPrivateKey privateKeyDSA = (DSAPrivateKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyDSA.getX());
			} else if (privateKey instanceof ECPrivateKey) {
				final ECPrivateKey privateKeyEC = (ECPrivateKey) privateKey;
				privateKeyWriter.writeBigInt(privateKeyEC.getS());
			} else {
				throw new IllegalArgumentException("Unsupported SSH cipher algorithm");
			}
			return privateKeyWriter.toByteArray();
		}
	}

	private static byte[] addRandomPadding(final byte[] data, final int paddingSize) {
		if (data.length % paddingSize != 0) {
			final byte[] dataPadded;
			dataPadded = new byte[((data.length / paddingSize) + 1) * paddingSize];
			for (int i = 0; i < data.length; i++) {
				dataPadded[i] = data[i];
			}
			final byte[] randomPadding = new byte[dataPadded.length - data.length];
			new SecureRandom().nextBytes(randomPadding);
			for (int i = 0; i < randomPadding.length; i++) {
				dataPadded[data.length + i] = randomPadding[i];
			}
			return dataPadded;
		} else {
			return data;
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

	private static String calculatePuttyMacChecksumVersion2(final Password password, final String keyType, final String comment, final byte[] publicKey, final byte[] privateKey) throws Exception {
		final String encryptionType = password == null ? "none" : "aes256-cbc";
		final MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.update("putty-private-key-file-mac-key".getBytes(StandardCharsets.UTF_8));
		if (password != null) {
			digest.update(password.getPasswordBytesIsoEncoded());
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

		final byte[] commentBytes = comment.getBytes(StandardCharsets.ISO_8859_1);
		data.writeInt(commentBytes.length);
		data.write(commentBytes);

		data.writeInt(publicKey.length);
		data.write(publicKey);

		data.writeInt(privateKey.length);
		data.write(privateKey);

		return toHexString(mac.doFinal(out.toByteArray())).toLowerCase();
	}

	/**
	 * Converts byte array to base64 with linebreaks
	 */
	private static String toWrappedBase64(final byte[] byteArray, final int maxLineLength, final String lineBreak) {
		return Base64.getMimeEncoder(maxLineLength, lineBreak.getBytes(StandardCharsets.ISO_8859_1)).encodeToString(byteArray);
	}

	private static String toHexString(final byte[] data) {
		final StringBuilder returnString = new StringBuilder();
		for (final byte dataByte : data) {
			returnString.append(String.format("%02X", dataByte));
		}
		return returnString.toString();
	}

	private static int getLineCount(final String dataString) throws IOException {
		if (dataString == null) {
			return 0;
		} else if ("".equals(dataString)) {
			return 1;
		} else {
			try (LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(dataString))) {
				while (lineNumberReader.readLine() != null) {
					// do nothing
				}
				return lineNumberReader.getLineNumber();
			}
		}
	}

	public static void clear(final byte[] array) {
		if (array != null) {
			Arrays.fill(array, (byte) 0);
		}
	}
}
