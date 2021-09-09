package com.core.library.crypto;

public class KeyInitialization {

	private String keyPairAlias;
	private String secretKeyAlias;

	private String keyStorePassword;
	private String trustStorePassword;
	private String secretPassword;

	private String storeType;

	private String keyStoreLocation;
	private String trustStoreLocation;
	private String secretLocation;

	public String getKeyPairAlias() {
		return keyPairAlias;
	}

	public void setKeyPairAlias(String keyPairAlias) {
		this.keyPairAlias = keyPairAlias;
	}

	public String getSecretKeyAlias() {
		return secretKeyAlias;
	}

	public void setSecretKeyAlias(String secretKeyAlias) {
		this.secretKeyAlias = secretKeyAlias;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public String getSecretPassword() {
		return secretPassword;
	}

	public void setSecretPassword(String secretPassword) {
		this.secretPassword = secretPassword;
	}

	public String getStoreType() {
		return storeType;
	}

	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	public String getKeyStoreLocation() {
		return keyStoreLocation;
	}

	public void setKeyStoreLocation(String keyStoreLocation) {
		this.keyStoreLocation = keyStoreLocation;
	}

	public String getTrustStoreLocation() {
		return trustStoreLocation;
	}

	public void setTrustStoreLocation(String trustStoreLocation) {
		this.trustStoreLocation = trustStoreLocation;
	}

	public String getSecretLocation() {
		return secretLocation;
	}

	public void setSecretLocation(String secretLocation) {
		this.secretLocation = secretLocation;
	}
}
