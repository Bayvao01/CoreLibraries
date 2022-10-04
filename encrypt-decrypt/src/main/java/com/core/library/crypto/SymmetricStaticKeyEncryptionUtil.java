package com.core.library.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SymmetricStaticKeyEncryptionUtil {

	private static final Logger logger = LoggerFactory.getLogger(SymmetricStaticKeyEncryptionUtil.class);

	public String encrypt(String payLoad, String passKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {

		byte[] salt = CryptoUtils.getRandomNonce(EncryptDecryptConstants.SALT_LENGTH_BYTE);

		byte[] iv = CryptoUtils.getRandomNonce(EncryptDecryptConstants.IV_SIZE);

		SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(passKey.toCharArray(), salt);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(EncryptDecryptConstants.TAG_BIT_LENGTH, iv);

		byte[] encryptedText = aesEncrypt(payLoad, secretKey, gcmParamSpec, iv, salt);

		logger.debug("Inside [encryptSymmData] completed");
		return Base64.getEncoder().encodeToString(encryptedText);
	}

	public byte[] aesEncrypt(String payLoad, Key secretKey, GCMParameterSpec gcmParamSpec, byte[] iv, byte[] salt)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [aesEncrypt] started");

		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec);

		byte[] cipherText = c.doFinal(payLoad.getBytes());
		logger.debug("Inside [aesEncrypt] completed");

		return ByteBuffer.allocate(iv.length + salt.length + cipherText.length).put(iv).put(salt).put(cipherText)
				.array();

	}

	public String decryptStaticData(String passKey, String encodedMsg) throws NoSuchAlgorithmException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {

		logger.debug("Inside [decryptStatic] started");
		byte[] encryptedMsg = Base64.getDecoder().decode(encodedMsg);

		ByteBuffer bb = ByteBuffer.wrap(encryptedMsg);

		byte[] iv = new byte[EncryptDecryptConstants.IV_SIZE];
		bb.get(iv);

		byte[] salt = new byte[EncryptDecryptConstants.SALT_LENGTH_BYTE];
		bb.get(salt);

		byte[] cipherText = new byte[bb.remaining()];

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(EncryptDecryptConstants.TAG_BIT_LENGTH, iv);

		logger.debug("Inside [decryptStatic] completed");
		byte[] decryptedText = aesDecryptStatic(passKey, gcmParamSpec, salt, cipherText);
		logger.debug("Inside [decryptSymmData] completed");
		return new String(decryptedText);

	}

	private byte[] aesDecryptStatic(String passKey, GCMParameterSpec gcmParamSpec, byte[] salt, byte[] cipherText)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {

		logger.debug("Inside [aesDecrypt] started");
		
		SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(passKey.toCharArray(), salt);
		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec);
		logger.debug("Inside [aesDecrypt] completed");
		return c.doFinal(cipherText);

	}
}
