/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * May need installed "US_export_policy.jar" and "local_policy.jar" for unlimited key strength Download: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
 */
public class CryptographicUtilities {
	public static Logger logger = LogManager.getLogger(CryptographicUtilities.class);
	
	public static String DEFAULT_SYMMETRIC_ENCRYPTION_METHOD = "AES/CBC/PKCS7Padding";
	
	public static String DEFAULT_SIGNATURE_METHOD = "SHA256WithRSA";
	
	public static String DEFAULT_ASYMMETRIC_ENCRYPTION_METHOD = "RSA/ECB/PKCS1Padding";
	
	/** BouncyCastle security provider. */
	public static final transient Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

	public static AsymmetricCipherKeyPair generateRsaKeyPair(int keyStrength) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
			generator.init(new RSAKeyGenerationParameters(RSAKeyGenParameterSpec.F4, SecureRandom.getInstance("SHA1PRNG"), keyStrength, 80));
			AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
			return keyPair;
		} catch (Exception e) {
			throw new Exception("Cannot create RSA keypair", e);
		}
	}

	public static void encryptAsymmetric(InputStream dataStream, OutputStream encryptedOutputStream, PublicKey publicKey, String encryptionMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			Cipher encryptCipher = Cipher.getInstance(encryptionMethod, "BC");
			encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			try (CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedOutputStream, encryptCipher)) {
				IOUtils.copy(dataStream, cipherOutputStream);
			}
		} catch (Exception e) {
			throw new Exception("Error while encrypting: " + e.getMessage(), e);
		}
	}

	public static void decryptAsymmetric(InputStream encryptedDataStream, OutputStream dataOutputStream, PrivateKey privateKey) throws Exception {
		decryptAsymmetric(encryptedDataStream, dataOutputStream, privateKey, DEFAULT_ASYMMETRIC_ENCRYPTION_METHOD);
	}

	public static void decryptAsymmetric(InputStream encryptedDataStream, OutputStream dataOutputStream, PrivateKey privateKey, String encryptionMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			Cipher decryptCipher = Cipher.getInstance(encryptionMethod, "BC");
			decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			try (InputStream cipherInputStream = new CipherInputStream(encryptedDataStream, decryptCipher)) {
				IOUtils.copy(cipherInputStream, dataOutputStream);
			}
		} catch (Exception e) {
			throw new Exception("Error while decrypting: " + e.getMessage(), e);
		}
	}

	/**
	 * Convert a Key to string encoded as BASE64
	 */
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static PublicKey getPublicKeyFromKeyPair(AsymmetricCipherKeyPair keyPair) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		RSAKeyParameters publicKey = (RSAKeyParameters)keyPair.getPublic();
		return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getExponent()));
	}

	public static PrivateKey getPrivateKeyFromKeyPair(AsymmetricCipherKeyPair keyPair) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		RSAPrivateCrtKeyParameters privateKey = (RSAPrivateCrtKeyParameters)keyPair.getPrivate();
		RSAKeyParameters publicKey = (RSAKeyParameters)keyPair.getPublic();
		return KeyFactory.getInstance("RSA").generatePrivate(
				new RSAPrivateCrtKeySpec(privateKey.getModulus(), publicKey.getExponent(), privateKey.getExponent(), privateKey.getP(), privateKey.getQ(), privateKey.getDP(), privateKey.getDQ(),
						privateKey.getQInv()));
	}

	/**
	 * Generates Public Key from BASE64 encoded string
	 */
	public static PublicKey getPublicKeyFromString(String key) throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
		    String publicKeyPEM = key;
		    publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
		    publicKeyPEM = publicKeyPEM.replace("\r", "");
		    publicKeyPEM = publicKeyPEM.replace("\n", "");
		    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		    byte[] publicKeyData = Base64.getDecoder().decode(publicKeyPEM);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(publicKeyData));
		} catch (Exception e) {
			throw new Exception("Cannot read public key", e);
		}
	}

	/**
	 * Generates Private Key from BASE64 encoded string
	 */
	public static PrivateKey getPrivateKeyFromString(String key) throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
		    String privateKeyPEM = key;
		    privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		    privateKeyPEM = privateKeyPEM.replace("\r", "");
		    privateKeyPEM = privateKeyPEM.replace("\n", "");
		    privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		    byte[] privateKeyData = Base64.getDecoder().decode(privateKeyPEM);
		    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyData);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new Exception("Cannot read private key", e);
		}
	}
	
	public static RSAPrivateKey getPrivateRsaKeyPairFromString(String key) throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
		    String privateKeyPEM = key.trim();
		    
		    final String pemBegin = "-----BEGIN PRIVATE KEY-----";
		    final String pemEnd = "-----END PRIVATE KEY-----";
		    
		    final String pemRsaBegin = "-----BEGIN RSA PRIVATE KEY-----";
		    final String pemRsaEnd = "-----END RSA PRIVATE KEY-----";
		    
		    if (privateKeyPEM.contains(pemRsaBegin) && privateKeyPEM.contains(pemRsaEnd) && privateKeyPEM.indexOf(pemRsaBegin) < privateKeyPEM.indexOf(pemRsaEnd)) {
		    	privateKeyPEM = privateKeyPEM.substring(privateKeyPEM.indexOf(pemRsaBegin) + pemRsaBegin.length(), privateKeyPEM.indexOf(pemRsaEnd)).trim();
		    } else if (privateKeyPEM.contains(pemBegin) && privateKeyPEM.contains(pemEnd) && privateKeyPEM.indexOf(pemBegin) < privateKeyPEM.indexOf(pemEnd)) {
		    	privateKeyPEM = privateKeyPEM.substring(privateKeyPEM.indexOf(pemBegin) + pemBegin.length(), privateKeyPEM.indexOf(pemEnd)).trim();
		    }
		    
		    privateKeyPEM = privateKeyPEM.replace("\r", "").replace("\n", "");
		    
		    byte[] privateKeyData = Base64.getDecoder().decode(privateKeyPEM);
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyData));
	        return (RSAPrivateKey) privateKey;
		} catch (Exception e) {
			throw new Exception("Cannot read private key", e);
		}
	}

	/**
	 * Read a keyfile generated with 'openssl genrsa...'
	 */
	public static PublicKey readRSAPublicKey(InputStream inputStream) throws Exception {
		return getPublicKeyFromString(IOUtils.toString(inputStream, "UTF-8"));
	}

	public static void encryptSymmetric(InputStream dataStream, OutputStream encryptedOutputStream, char[] password, byte[] salt, byte[] initializationVector, String encryptionMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			byte[] keyBytes = stretchPassword(password, 128, salt);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			Cipher encryptCipher = Cipher.getInstance(encryptionMethod, "BC");
			encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(initializationVector));
			try (CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedOutputStream, encryptCipher)) {
				IOUtils.copy(dataStream, cipherOutputStream);
			}
		} catch (Exception e) {
			throw new Exception("Error while encrypting: " + e.getMessage(), e);
		}
	}

	public static void decryptSymmetric(InputStream encryptedDataStream, OutputStream dataOutputStream, char[] password, byte[] salt, byte[] initializationVector, String encryptionMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			byte[] keyBytes = stretchPassword(password, 128, salt);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			Cipher decryptCipher = Cipher.getInstance(encryptionMethod, "BC");
			decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(initializationVector));
			try (CipherInputStream cipherInputStream = new CipherInputStream(encryptedDataStream, decryptCipher)) {
				IOUtils.copy(cipherInputStream, dataOutputStream);
			}
		} catch (Exception e) {
			throw new Exception("Error while decrypting: " + e.getMessage(), e);
		}
	}

	public static byte[] stretchPassword(char[] password, int keyLength, byte[] salt) {
		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
		generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, 1000);
		KeyParameter params = (KeyParameter)generator.generateDerivedParameters(keyLength);
		return params.getKey();
	}

	public static byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
		return signData(data, privateKey, DEFAULT_SIGNATURE_METHOD);
	}

	public static byte[] signData(byte[] data, PrivateKey privateKey, String signatureMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			Signature signature = Signature.getInstance(signatureMethod, "BC");
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new Exception("Cannot create signature", e);
		}
	}

	public static byte[] signStream(InputStream dataStream, PrivateKey privateKey) throws Exception {
		return signStream(dataStream, privateKey, DEFAULT_SIGNATURE_METHOD);
	}

	public static byte[] signStream(InputStream dataStream, PrivateKey privateKey, String signatureMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			Signature signature = Signature.getInstance(signatureMethod, "BC");
			signature.initSign(privateKey);
			byte[] buffer = new byte[4096];
			int bytesRead = dataStream.read(buffer);
			while (bytesRead >= 0) {
				signature.update(buffer, 0, bytesRead);
				bytesRead = dataStream.read(buffer);
			}
			return signature.sign();
		} catch (Exception e) {
			throw new Exception("Cannot create signature", e);
		}
	}

	public static boolean verifyData(byte[] data, PublicKey publicKey, byte[] signatureData) {
		return verifyData(data, publicKey, signatureData, DEFAULT_SIGNATURE_METHOD);
	}

	public static boolean verifyData(byte[] data, PublicKey publicKey, byte[] signatureData, String signatureMethod) {
		Security.addProvider(new BouncyCastleProvider());
		
		if (publicKey == null) {
			throw new IllegalArgumentException("Cannot verify signature. PublicKey is missing");
		}

		try {
			Signature signature = Signature.getInstance(signatureMethod, "BC");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(signatureData);
		} catch (Exception e) {
			throw new RuntimeException("Cannot verify signature", e);
		}
	}

	public static boolean verifyStream(InputStream dataStream, PublicKey publicKey, byte[] signatureData) throws Exception {
		return verifyStream(dataStream, publicKey, signatureData, DEFAULT_SIGNATURE_METHOD);
	}

	public static boolean verifyStream(InputStream dataStream, PublicKey publicKey, byte[] signatureData, String signatureMethod) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		try {
			Signature signature = Signature.getInstance(signatureMethod, "BC");
			signature.initVerify(publicKey);
			byte[] buffer = new byte[4096];
			int bytesRead = dataStream.read(buffer);
			while (bytesRead >= 0) {
				signature.update(buffer, 0, bytesRead);
				bytesRead = dataStream.read(buffer);
			}
			return signature.verify(signatureData);
		} catch (Exception e) {
			throw new Exception("Cannot verify signature", e);
		}
	}

	/**
	 * Check if "certificate" was certified by "trustedCertificates"
	 */
	public static boolean verifyChainOfTrust(final X509Certificate certificate, final Collection<? extends Certificate> trustedCertificates) throws Exception {
		final X509CertSelector targetConstraints = new X509CertSelector();
		targetConstraints.setCertificate(certificate);

		final Set<TrustAnchor> trustAnchors = new HashSet<>();
		for (final Certificate trustedRootCert : trustedCertificates) {
			trustAnchors.add(new TrustAnchor((X509Certificate) trustedRootCert, null));
		}

		final PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, targetConstraints);
		params.setRevocationEnabled(false);
		try {
			final PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("PKIX").build(params);
			return result != null;
		} catch (@SuppressWarnings("unused") final Exception cpbe) {
			return false;
		}
	}
}
