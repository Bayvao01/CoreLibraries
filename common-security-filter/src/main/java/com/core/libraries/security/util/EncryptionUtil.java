package com.core.libraries.security.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import static com.core.libraries.security.constants.SecurityConstants.*;

public class EncryptionUtil {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    @Value("${cipher.secret}")
    private static final String SECRET_KEY = "hsl43=m/mdYo87%fesSYm2";
    @Value("${cipher.salt}")
    private static final String SALT = "foodswipe";
    private static final byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static String encrypt(String plainText) {
        try {
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(),
                    PBKDF2_ENCRYPTION_ITERATION_COUNT, PBKDF2_ENCRYPTION_KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ENCRYPTION_ALGORITHM_AES);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logger.error("Error while encrypting: {}", e.getMessage());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt) {
        try {
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(),
                    PBKDF2_ENCRYPTION_ITERATION_COUNT, PBKDF2_ENCRYPTION_KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ENCRYPTION_ALGORITHM_AES);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            logger.error("Error while decrypting: {}", e.getMessage());
        }
        return null;
    }

}