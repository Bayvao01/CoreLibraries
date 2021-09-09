package com.core.library.crypto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class SymmetricKeyEncryptionUtil {

	private static final Logger logger = LoggerFactory.getLogger(SymmetricKeyEncryptionUtil.class);

	private KeyProvider keyProvider;

	public SymmetricKeyEncryptionUtil(ResourceLoader resourceLoader) {
		this.keyProvider = new KeyProvider(resourceLoader);
	}

	public String encryptSymmData(String plainText, KeyInitialization keyInit) throws UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [encryptSymmData] started");
		keyProvider.initSecretStore(keyInit);
		Key secretKey = keyProvider.getSecretKey(keyInit.getSecretKeyAlias(), keyInit.getSecretPassword());

		return encrypt(plainText, secretKey);

	}

	private String encrypt(String plainText, Key secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		byte[] iv = CryptoUtils.getRandomNonce(EncryptDecryptConstants.IV_SIZE);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(EncryptDecryptConstants.TAG_BIT_LENGTH, iv);

		byte[] encryptedText = aesEncrypt(plainText, secretKey, gcmParamSpec, iv);
		logger.debug("Inside [encryptSymmData] completed");
		return Base64.getEncoder().encodeToString(encryptedText);
	}

	public byte[] aesEncrypt(String plainText, Key secretKey, GCMParameterSpec gcmParamSpec, byte[] iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [aesEncrypt] started");

		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec);
		logger.debug("Inside [aesEncrypt] completed");
		byte[] cipherText = c.doFinal(plainText.getBytes());

		return ByteBuffer.allocate(iv.length + cipherText.length).put(iv).put(cipherText).array();

	}

	public String decryptSymmData(String encodedMsg, KeyInitialization keyInit) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [decryptSymmData] started");

		byte[] encryptedMsg = Base64.getDecoder().decode(encodedMsg);
		keyProvider.initSecretStore(keyInit);
		Key secretKey = keyProvider.getSecretKey(keyInit.getSecretKeyAlias(), keyInit.getSecretPassword());

		ByteBuffer bb = ByteBuffer.wrap(encryptedMsg);

		byte[] iv = new byte[EncryptDecryptConstants.IV_SIZE];
		bb.get(iv);

		byte[] cipherText = new byte[bb.remaining()];

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(EncryptDecryptConstants.TAG_BIT_LENGTH, iv);
		byte[] plainText = aesDecrypt(cipherText, secretKey, gcmParamSpec);
		logger.debug("Inside [decryptSymmData] completed");
		return new String(plainText);

	}

	private byte[] aesDecrypt(byte[] encryptedMsg, Key secretKey, GCMParameterSpec gcmParamSpec)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [aesDecrypt] started");
		
		Cipher c = Cipher.getInstance(EncryptDecryptConstants.ALGO_TRANSFORMATION_STRING);
		c.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec);
		
		logger.debug("Inside [aesDecrypt] completed");
		return c.doFinal(encryptedMsg);

	}
}
