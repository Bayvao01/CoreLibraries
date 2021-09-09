package com.core.library.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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
public class PublicKeyEncryptDecryptUtil {

	private static final Logger logger = LoggerFactory.getLogger(PublicKeyEncryptDecryptUtil.class);

	public String getEncryptedPublicKey(byte[] publicKey) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException {

		return encrypt(publicKey, EncryptDecryptConstants.PUB_ENC_DEC);
	}

	private String encrypt(byte[] payload, String passKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException {

		byte[] salt = CryptoUtils.getRandomNonce(EncryptDecryptConstants.SALT_LENGTH_BYTE);

		byte[] iv = CryptoUtils.getRandomNonce(EncryptDecryptConstants.IV_SIZE);

		SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(passKey.toCharArray(), salt);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(EncryptDecryptConstants.TAG_BIT_LENGTH, iv);

		byte[] encryptedText = aesEncrypt(payload, secretKey, gcmParamSpec, iv, salt);

		logger.debug("Inside [encryptSymmData] completed");
		return Base64.getEncoder().encodeToString(encryptedText);
	}

	public byte[] aesEncrypt(byte[] payload, Key secretKey, GCMParameterSpec gcmParamSpec, byte[] iv, byte[] salt)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [aesEncrypt] started");

		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec);

		logger.debug("Inside [aesEncrypt] completed");
		byte[] cipherText = c.doFinal(payload);

		return ByteBuffer.allocate(iv.length + salt.length + cipherText.length).put(iv).put(salt).put(cipherText)
				.array();

	}

	public Key getDecryptedPublicKey(String encodedMsg) throws NoSuchAlgorithmException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException {

		logger.debug("Inside [getDecryptedPublicKey] started");

		byte[] decryptedData = decryptStatic(EncryptDecryptConstants.PUB_ENC_DEC, encodedMsg);

		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decryptedData);
		KeyFactory keyFactory = KeyFactory.getInstance(EncryptDecryptConstants.RSA);
		Key publicKey = keyFactory.generatePublic(publicKeySpec);
		logger.debug("Inside [getDecryptedPublicKey] completed");
		return publicKey;

	}

	private byte[] decryptStatic(String passKey, String encodedMsg)
			throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

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
		return aesDecrypt(passKey, gcmParamSpec, salt, cipherText);
	}

	private byte[] aesDecrypt(String passKey, GCMParameterSpec gcmParamSpec, byte[] salt, byte[] cipherText)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		logger.debug("Inside [aesDecrypt] started");

		SecretKey secretKey = CryptoUtils.getAESKeyFromPassword(passKey.toCharArray(), salt);

		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec);

		logger.debug("Inside [aesDecrypt] completed");
		return c.doFinal(cipherText);
	}

}
