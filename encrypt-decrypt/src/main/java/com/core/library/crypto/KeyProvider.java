package com.core.library.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class KeyProvider {

	private ResourceLoader resourceLoader;
	private KeyStore keyStore;
	private KeyStore trustStore;
	private KeyStore secretStore;

	public KeyProvider(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void initKeyStore(KeyInitialization keyInit)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		String storeType = keyInit.getStoreType();
		String storeFile = keyInit.getKeyStoreLocation();
		InputStream inputStream = resourceLoader.getResource(storeFile).getInputStream();
		char[] password = keyInit.getKeyStorePassword().toCharArray();
		this.keyStore = KeyStore.getInstance(storeType);
		this.keyStore.load(inputStream, password);

	}

	public void initTrustStore(KeyInitialization keyInit)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		String storeType = keyInit.getStoreType();
		String storeFile = keyInit.getTrustStoreLocation();
		InputStream inputStream = resourceLoader.getResource(storeFile).getInputStream();
		char[] password = keyInit.getTrustStorePassword().toCharArray();
		this.trustStore = KeyStore.getInstance(storeType);
		this.trustStore.load(inputStream, password);

	}

	public void initSecretStore(KeyInitialization keyInit)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		String storeType = keyInit.getStoreType();
		String storeFile = keyInit.getSecretLocation();
		InputStream inputStream = resourceLoader.getResource(storeFile).getInputStream();
		char[] password = keyInit.getSecretPassword().toCharArray();
		this.secretStore = KeyStore.getInstance(storeType);
		this.secretStore.load(inputStream, password);

	}

	public Key getKey(String alias, String keyPassword)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		return keyStore.getKey(alias, keyPassword.toCharArray());

	}

	public PublicKey getPublicKey(String alias) throws KeyStoreException {
		
		return trustStore.getCertificate(alias).getPublicKey();
	}

	public Key getSecretKey(String alias, String keyPassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		
		return secretStore.getKey(alias, keyPassword.toCharArray());
	}

}
