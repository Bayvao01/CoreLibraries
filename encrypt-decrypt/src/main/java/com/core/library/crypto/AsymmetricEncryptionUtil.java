package com.core.library.crypto;

import java.io.IOException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AsymmetricEncryptionUtil {

	private static final Logger logger = LoggerFactory.getLogger(AsymmetricEncryptionUtil.class);

	private KeyProvider keyProvider;

	public AsymmetricEncryptionUtil(ResourceLoader resourceLoader) {
		this.keyProvider = new KeyProvider(resourceLoader);
	}

	public String encryptData(String encryptedEncodedStr, KeyInitialization keyInit)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, KeyStoreException, CertificateException, IOException {

		logger.debug("Inside [encryptData] started");
		keyProvider.initTrustStore(keyInit);

		String alias = keyInit.getKeyPairAlias();
		Key key = keyProvider.getPublicKey(alias);

		String encryptedString = encrypt(encryptedEncodedStr, key);
		logger.debug("Inside [encryptData] completed");
		return encryptedString;
	}

	public String ecryptData(String encryptedEncodedStr, Key key) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [encryptData key] started");

		String encryptedString = encrypt(encryptedEncodedStr, key);

		logger.debug("Inside [encryptData key] completed");
		return encryptedString;
	}

	public String encrypt(String encryptedEncodedStr, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [encrypt] started");
		Cipher cipher = Cipher.getInstance(EncryptDecryptConstants.ALGORITHMNAME + "/"
				+ EncryptDecryptConstants.MODEOFOPERATION + "/" + EncryptDecryptConstants.PADDINGSCHEME);

		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] encryptedBytes = cipher.doFinal(encryptedEncodedStr.getBytes());
		byte[] encryptedEncodedStrKey = Base64.getUrlEncoder().encode(encryptedBytes);

		logger.debug("Inside [encrypt] completed");
		return new String(encryptedEncodedStrKey);

	}

	public String decryptData(String encryptedEncodedMsg, KeyInitialization keyInit) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		logger.debug("Inside [decryptData] started");

		keyProvider.initKeyStore(keyInit);
		byte[] encryptedString = Base64.getUrlDecoder().decode(encryptedEncodedMsg);
		String alias = keyInit.getKeyPairAlias();
		Key key = keyProvider.getKey(alias, keyInit.getKeyStorePassword());
		Cipher cipher;

		cipher = Cipher.getInstance(EncryptDecryptConstants.ALGORITHMNAME + "/"
				+ EncryptDecryptConstants.MODEOFOPERATION + "/" + EncryptDecryptConstants.PADDINGSCHEME);
		cipher.init(Cipher.DECRYPT_MODE, key);

		byte[] plainText = cipher.doFinal(encryptedString);
		logger.debug("Inside [decryptData] completed");
		return new String(plainText);

	}

	public String getPrivateKey(KeyInitialization keyInit) throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException {

		logger.debug("Inside [getPrivateKey] started");
		keyProvider.initKeyStore(keyInit);
		String alias = keyInit.getKeyPairAlias();
		Key key = keyProvider.getKey(alias, keyInit.getKeyStorePassword());

		logger.debug("Inside [getPrivateKey] completed");
		return new String(key.getEncoded());
	}
}
