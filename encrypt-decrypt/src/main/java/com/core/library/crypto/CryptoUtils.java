package com.core.library.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Bayvao Verma
 *
 */
public class CryptoUtils {

	private static final String AES = "AES";
	private static final int KEY_LENGTH = 256;
	private static final int ITERATION_COUNT = 65536;
	private static final String PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";

	private CryptoUtils() {

	}

	/**
	 * This method produces 16 bytes IV(Initialization Vector)
	 * 
	 * @param numBytes
	 * @return nonce with specified number of bytes
	 */
	public static byte[] getRandomNonce(int numBytes) {
		byte[] nonce = new byte[numBytes];
		new SecureRandom().nextBytes(nonce);
		return nonce;
	}

	/**
	 * @param password
	 * @param salt
	 * @return password derived AES 256 bits secret key
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
			throws InvalidKeySpecException, NoSuchAlgorithmException {

		SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA256);

		//iterationCount - 65536
		//keyLength - 256
		KeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES);
	}
}
