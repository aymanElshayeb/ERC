package com.app.smartpos.utils;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Hasher {
    public static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec("MyEncryptionKey1".getBytes(), "AES");
    }

    public static byte[] encryptMsg(String message) {
        /* Encrypt the message. */
        Cipher cipher = null;
        byte[] cipherText = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey());
            cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText) {
        Cipher cipher = null;
        String decryptString = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, generateKey());
            decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptString;
    }

    public boolean hashPassword(String password, String hashedDBPassword) {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        HashFormat hashFormat = new Shiro1CryptFormat();
        DefaultHashService defaultHashService = new DefaultHashService();
        defaultHashService.setHashIterations(500000);
        defaultHashService.setHashAlgorithmName("SHA-512");
        passwordService.setHashService(defaultHashService);
        passwordService.setHashFormat(hashFormat);
        //String hash = passwordService.encryptPassword(password);
        //Utils.addLog("datadata",hash);
        return passwordService.passwordsMatch(password, hashedDBPassword);
    }


}