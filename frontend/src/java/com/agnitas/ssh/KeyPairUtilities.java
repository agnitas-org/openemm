/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ssh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.agnitas.ssh.Asn1Codec.DerTag;

public class KeyPairUtilities {
	/**
	 * Create a RSA keypair of given strength
	 */
	public static KeyPair createRsaKeyPair(final int keyStrength) throws Exception {
		if (keyStrength < 512) {
			throw new Exception("Invalid RSA key strength: " + keyStrength);
		}
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(keyStrength);
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Create a DSA keypair of 1024 bit strength<br/>
	 * Watchout: OpenSSH only supports 1024 bit DSA key strength<br/>
	 */
	public static KeyPair createDsaKeyPair() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Create a DSA keypair of given strength<br/>
	 * Watchout: OpenSSH only supports 1024 bit DSA key strength<br/>
	 */
	public static KeyPair createDsaKeyPair(final int keyStrength) throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
		keyPairGenerator.initialize(keyStrength);
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Create a ECDSA keypair of eliptic curve name.
	 * Supported eliptic curve names are
	 *   nistp256 or secp256
	 *   nistp384 or secp384
	 *   nistp521 or secp521
	 */
	public static KeyPair createEllipticCurveKeyPair(final String ecdsaCurveName) throws Exception {
		if (ecdsaCurveName == null || "".equals(ecdsaCurveName.trim())) {
			throw new Exception("Missing ECDSA curve name parameter");
		}
		final String curveName = ecdsaCurveName.toLowerCase().trim().replace("nist", "sec");
		if (!"secp256".equals(curveName) && !"secp384".equals(curveName) && !"secp521".equals(curveName)) {
			throw new Exception("Unknown ECDSA curve name: " + ecdsaCurveName);
		}
		final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(curveName + "r1"));
		final ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
		keyPairGenerator.initialize(ecParameterSpec, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	public static KeyPair createEllipticCurveKeyPair(final int curveId) throws Exception {
		if (256 != curveId && 384 != curveId && 521 != curveId) {
			throw new Exception("Invalid ECDSA curve id parameter");
		}
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
		keyPairGenerator.initialize(curveId);
		return keyPairGenerator.generateKeyPair();
	}

	public static KeyPair createEd25519CurveKeyPair() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
		return keyPairGenerator.generateKeyPair();
	}

	public static KeyPair createEd448CurveKeyPair() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed448");
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Key type<br />
	 * "ssh-rsa" for RSA key<br />
	 * "ssh-dss" for DSA key<br />
	 * "ecdsa-sha2-nistp256" or "ecdsa-sha2-nistp384" or "ecdsa-sha2-nistp521" for ECDSA key<br />
	 */
	public static String getAlgorithm(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else if (keyPair.getPrivate() != null) {
			return getAlgorithm(keyPair.getPrivate());
		} else if (keyPair.getPublic() != null) {
			return getAlgorithm(keyPair.getPublic());
		} else {
			throw new Exception("KeyPair data is empty");
		}
	}

	/**
	 * Key type<br />
	 * "ssh-rsa" for RSA key<br />
	 * "ssh-dss" for DSA key<br />
	 * "ecdsa-sha2-nistp256" or "ecdsa-sha2-nistp384" or "ecdsa-sha2-nistp521" for ECDSA key<br />
	 */
	public static String getAlgorithm(final PublicKey publicKey) throws Exception {
		if (publicKey == null){
			throw new Exception("Invalid empty publicKey parameter");
		} else if ("RSA".equals(publicKey.getAlgorithm())) {
			return "ssh-rsa";
		} else if ("DSA".equals(publicKey.getAlgorithm())) {
			return "ssh-dss";
		} else if ("EC".equals(publicKey.getAlgorithm()) || "ECDSA".equals(publicKey.getAlgorithm())) {
			return "ecdsa-sha2-" + getEcDsaEllipticCurveName((ECPublicKey) publicKey);
		} else {
			throw new Exception("Unsupported ssh algorithm name: " + publicKey.getAlgorithm());
		}
	}

	/**
	 * Key type<br />
	 * "ssh-rsa" for RSA key<br />
	 * "ssh-dss" for DSA key<br />
	 * "ecdsa-sha2-nistp256" or "ecdsa-sha2-nistp384" or "ecdsa-sha2-nistp521" for ECDSA key<br />
	 */
	public static String getAlgorithm(final PrivateKey privateKey) throws Exception {
		if (privateKey == null){
			throw new Exception("Invalid empty privateKey parameter");
		} else if (privateKey instanceof RSAPrivateCrtKey) {
			return "ssh-rsa";
		} else if (privateKey instanceof DSAPrivateKey) {
			return "ssh-dss";
		} else if (privateKey instanceof ECPrivateKey) {
			return "ecdsa-sha2-" + getEcDsaEllipticCurveName((ECPrivateKey) privateKey);
		} else{
			throw new IllegalArgumentException("Unknown private key cipher");
		}
	}

	public static int getKeyStrength(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getKeyStrength(keyPair.getPublic());
		}
	}

	public static int getKeyStrength(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			final String algorithmName = getAlgorithm(publicKey);
			if ("ssh-rsa".equalsIgnoreCase(algorithmName)) {
				return ((RSAPublicKey) publicKey).getModulus().bitLength();
			} else if ("ssh-dss".equalsIgnoreCase(algorithmName)) {
				return ((DSAPublicKey) publicKey).getY().bitLength();
			} else if ("ecdsa-sha2-nistp256".equalsIgnoreCase(algorithmName)) {
				return 256;
			} else if ("ecdsa-sha2-nistp384".equalsIgnoreCase(algorithmName)) {
				return 384;
			} else if ("ecdsa-sha2-nistp521".equalsIgnoreCase(algorithmName)) {
				return 521;
			} else {
				throw new Exception("Unsupported ssh algorithm name: " + algorithmName);
			}
		}
	}

	public static byte[] getPublicKeyBytes(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			final BlockDataWriter publicKeyWriter = new BlockDataWriter();
			final String algorithmName = getAlgorithm(publicKey);
			if (publicKey instanceof RSAPublicKey) {
				final RSAPublicKey publicKeyRSA = (RSAPublicKey) publicKey;
				publicKeyWriter.writeString("ssh-rsa");
				publicKeyWriter.writeBigInt(publicKeyRSA.getPublicExponent());
				publicKeyWriter.writeBigInt(publicKeyRSA.getModulus());
				return publicKeyWriter.toByteArray();
			} else if (publicKey instanceof DSAPublicKey) {
				final DSAPublicKey publicKeyDSA = (DSAPublicKey) publicKey;
				publicKeyWriter.writeString("ssh-dss");
				publicKeyWriter.writeBigInt(publicKeyDSA.getParams().getP());
				publicKeyWriter.writeBigInt(publicKeyDSA.getParams().getQ());
				publicKeyWriter.writeBigInt(publicKeyDSA.getParams().getG());
				publicKeyWriter.writeBigInt(publicKeyDSA.getY());
			} else if (publicKey instanceof ECPublicKey) {
				final ECPublicKey publicKeyEC = (ECPublicKey) publicKey;
				final String ecCurveName = getEcDsaEllipticCurveName(publicKeyEC);
				publicKeyWriter.writeString(algorithmName);
				publicKeyWriter.writeString(ecCurveName);

				final DerTag enclosingDerTag = Asn1Codec.readDerTag(publicKeyEC.getEncoded());
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
							// remove first byte (odd sign)
							final byte[] dataWithLeadingZero = derDataTags.get(1).getData();
							final byte[] qBytes = Arrays.copyOfRange(dataWithLeadingZero, 1, dataWithLeadingZero.length);
							publicKeyWriter.writeData(qBytes);
						}
					} else {
						throw new Exception("Unknown SSH EcDSA curve OID: " + ecDsaCurveOid.getStringEncoding());
					}
				} else {
					throw new Exception("Unknown SSH EcDSA public key OID: " + ecDsaPublicKeyOid.getStringEncoding());
				}
			} else {
				throw new Exception("Unsupported  algorithm name: " + algorithmName);
			}
			return publicKeyWriter.toByteArray();
		}
	}

	public static String getMd5Fingerprint(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getMd5Fingerprint(keyPair.getPublic());
		}
	}

	public static String getMd5Fingerprint(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(getPublicKeyBytes(publicKey));
				return toHexString(md.digest(), ":");
			} catch (final Exception e) {
				throw new Exception("Cannot create MD5 fingerprint", e);
			}
		}
	}

	public static String getSha1Fingerprint(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha1Fingerprint(keyPair.getPublic());
		}
	}

	public static String getSha1Fingerprint(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(getPublicKeyBytes(publicKey));
				return toHexString(md.digest(), ":");
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA1 fingerprint", e);
			}
		}
	}

	public static String getSha1FingerprintBase64(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha1FingerprintBase64(keyPair.getPublic());
		}
	}

	public static String getSha1FingerprintBase64(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(getPublicKeyBytes(publicKey));
				return Base64.getEncoder().encodeToString(md.digest());
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA1 fingerprint", e);
			}
		}
	}

	public static String getSha256Fingerprint(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha256Fingerprint(keyPair.getPublic());
		}
	}

	public static String getSha256Fingerprint(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(getPublicKeyBytes(publicKey));
				return toHexString(md.digest(), ":");
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA256 fingerprint", e);
			}
		}
	}

	public static String getSha256FingerprintBase64(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha256FingerprintBase64(keyPair.getPublic());
		}
	}

	public static String getSha256FingerprintBase64(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(getPublicKeyBytes(publicKey));
				return Base64.getEncoder().encodeToString(md.digest());
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA256 fingerprint", e);
			}
		}
	}

	public static String getSha384Fingerprint(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha384Fingerprint(keyPair.getPublic());
		}
	}

	public static String getSha384Fingerprint(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-384");
				md.update(getPublicKeyBytes(publicKey));
				return toHexString(md.digest(), ":");
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA384 fingerprint", e);
			}
		}
	}

	public static String getSha384FingerprintBase64(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha384FingerprintBase64(keyPair.getPublic());
		}
	}

	public static String getSha384FingerprintBase64(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-384");
				md.update(getPublicKeyBytes(publicKey));
				return Base64.getEncoder().encodeToString(md.digest());
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA384 fingerprint", e);
			}
		}
	}

	public static String getSha512Fingerprint(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha512Fingerprint(keyPair.getPublic());
		}
	}

	public static String getSha512Fingerprint(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-512");
				md.update(getPublicKeyBytes(publicKey));
				return toHexString(md.digest(), ":");
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA512 fingerprint", e);
			}
		}
	}

	public static String getSha512FingerprintBase64(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else {
			return getSha512FingerprintBase64(keyPair.getPublic());
		}
	}

	public static String getSha512FingerprintBase64(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			try {
				final MessageDigest md = MessageDigest.getInstance("SHA-512");
				md.update(getPublicKeyBytes(publicKey));
				return Base64.getEncoder().encodeToString(md.digest());
			} catch (final Exception e) {
				throw new Exception("Cannot create SHA512 fingerprint", e);
			}
		}
	}

	public static String encodePublicKeyForAuthorizedKeys(final KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new Exception("Invalid empty keyPair parameter");
		} else if (keyPair.getPublic() == null) {
			throw new Exception("Missing public key for AuthorizedKey string generation");
		} else {
			return encodePublicKeyForAuthorizedKeys(keyPair.getPublic());
		}
	}

	public static String encodePublicKeyForAuthorizedKeys(final PublicKey publicKey) throws Exception {
		if (publicKey == null) {
			throw new Exception("Invalid empty publicKey parameter");
		} else {
			return getAlgorithm(publicKey) + " " + new String(Base64.getEncoder().encode(getPublicKeyBytes(publicKey)));
		}
	}

	private static class BlockDataWriter {
		private final ByteArrayOutputStream outputStream;
		private final DataOutput keyDataOutput;

		private BlockDataWriter() {
			outputStream = new ByteArrayOutputStream();
			keyDataOutput = new DataOutputStream(outputStream);
		}

		private void writeBigInt(final BigInteger bigIntegerData) throws Exception {
			writeData(bigIntegerData.toByteArray());
		}

		private void writeString(final String stringData) throws Exception {
			writeData(stringData.getBytes(StandardCharsets.ISO_8859_1));
		}

		private void writeData(final byte[] data) throws IOException, Exception {
			try {
				if (data.length <= 0) {
					throw new Exception("Key blocksize error");
				}

				keyDataOutput.writeInt(data.length);
				keyDataOutput.write(data);
			} catch (final IOException e) {
				throw new Exception("Key block write error", e);
			}
		}

		private byte[] toByteArray() {
			return outputStream.toByteArray();
		}
	}

	/**
	 * Uppercase hexadezimal display of ByteArray data with optional separator after each byte
	 */
	public static String toHexString(final byte[] data, final String separator) {
		final StringBuilder returnString = new StringBuilder();
		for (final byte dataByte : data) {
			if (returnString.length() > 0 && separator != null) {
				returnString.append(separator);
			}
			returnString.append(String.format("%02X", dataByte));
		}
		return returnString.toString().toLowerCase();
	}
	
	public static String getEcDsaEllipticCurveName(final ECPublicKey publicKeyEC) throws Exception {
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(publicKeyEC.getEncoded());
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
			if (Arrays.equals(OID.ECDSA_CURVE_NISTP256_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp256";
			} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP384_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp384";
			} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP521_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp521";
			} else {
				throw new Exception("Unknown SSH EcDSA curve OID: " + ecDsaCurveOid.getStringEncoding());
			}
		} else {
			throw new Exception("Unknown SSH EcDSA public key OID: " + ecDsaPublicKeyOid.getStringEncoding());
		}
	}
	
	public static String getEcDsaEllipticCurveName(final ECPrivateKey ecPrivateKey) throws Exception {
		final DerTag enclosingDerTag = Asn1Codec.readDerTag(ecPrivateKey.getEncoded());
		if (Asn1Codec.DER_TAG_SEQUENCE != enclosingDerTag.getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> derDataTags = Asn1Codec.readDerTags(enclosingDerTag.getData());

		final BigInteger keyEncodingVersion = new BigInteger(derDataTags.get(0).getData());
		if (!BigInteger.ZERO.equals(keyEncodingVersion)) {
			throw new Exception("Invalid key data version found");
		}

		if (Asn1Codec.DER_TAG_SEQUENCE != derDataTags.get(1).getTagId()) {
			throw new Exception("Invalid key data found");
		}
		final List<DerTag> sshAlgorithmDerTags = Asn1Codec.readDerTags(derDataTags.get(1).getData());
		final OID ecDsaPublicKeyOid = new OID(sshAlgorithmDerTags.get(0).getData());
		if (Arrays.equals(OID.ECDSA_PUBLICKEY_ARRAY, ecDsaPublicKeyOid.getByteArrayEncoding())) {
			final OID ecDsaCurveOid = new OID(sshAlgorithmDerTags.get(1).getData());
			if (Arrays.equals(OID.ECDSA_CURVE_NISTP256_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp256";
			} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP384_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp384";
			} else if (Arrays.equals(OID.ECDSA_CURVE_NISTP521_ARRAY, ecDsaCurveOid.getByteArrayEncoding())) {
				return "nistp521";
			} else {
				throw new Exception("Unknown SSH EcDSA curve OID: " + ecDsaCurveOid.getStringEncoding());
			}
		} else {
			throw new Exception("Unknown SSH EcDSA public key OID: " + ecDsaPublicKeyOid.getStringEncoding());
		}
	}
}
