package com.clds.bottletalk;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class JasyptEncryptionTest {

    private PooledPBEStringEncryptor encryptor;
    private final String plainText = "null";
    private final String encryptionKey = "null";

    @BeforeEach
    void setUp() {
        encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(encryptionKey);
        config.setAlgorithm("PBEWITHHMACSHA1ANDAES_128");
        config.setPoolSize("1");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        encryptor.setConfig(config);
    }



    @Test
    void testEncryptionAndDecryption() {
        String encryptedText = encryptor.encrypt(plainText);
        System.out.println("암호문 : "+encryptedText);
        assertNotNull(encryptedText);
        assertNotEquals(plainText, encryptedText);
        String decryptedText = encryptor.decrypt(encryptedText);
        System.out.println("평문 : "+decryptedText);
        assertEquals(plainText, decryptedText);
    }


}
