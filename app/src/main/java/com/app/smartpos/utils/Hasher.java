package com.app.smartpos.utils;

import android.util.Log;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;

public class Hasher {
    public boolean hashPassword(String password , String hashedDBPassword) {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        HashFormat hashFormat = new Shiro1CryptFormat();
        DefaultHashService defaultHashService = new DefaultHashService();
        defaultHashService.setHashIterations(500000);
        defaultHashService.setHashAlgorithmName("SHA-512");
        passwordService.setHashService(defaultHashService);
        passwordService.setHashFormat(hashFormat);
//        String hash = passwordService.encryptPassword(password);

        return passwordService.passwordsMatch(password, hashedDBPassword);
    }


}