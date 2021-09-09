package com.core.library.crypto;

/**
 * @author Bayvao Verma
 *
 */
public class EncryptDecryptConstants {

	private EncryptDecryptConstants() {

	}

	/**
	 * Below constants are for Asymmetric Encryption
	 *
	 */
	public static final String ALGORITHMNAME = "RSA";
	public static final String PADDINGSCHEME = "OAEPWITHSHA-256ANDMGF1PADDING";
	public static final String MODEOFOPERATION = "ECB";

	/**
	 * Below constants are for Symmetric Encryption
	 *
	 */
	public static final String RSA = "RSA";
	public static final int AES_KEY_SIZE = 256;
	public static final int IV_SIZE = 96;
	public static final int SALT_LENGTH_BYTE = 16;
	public static final int TAG_BIT_LENGTH = 128;
	public static final String ALGO_TRANSFORMATION_STRING = "AES/GCM/NoPadding";
	
	/**
	 * static key to for public key Encryption
	 *
	 */
	public static final String PUB_ENC_DEC = "Ni46k7I3t@m76pU$h93sHeeT92Bayv@O";
}
