package com.app.smartpos.common.Keystore;

import static com.app.smartpos.common.Keystore.KeyStoreHelper.ENCRYPTION_ALGORITHM;

import android.util.Pair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class EncryptionHelper {

    public Pair<byte[], byte[]> encrypt(String apiKey, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV(); // Initialization Vector

        byte[] encryption = cipher.doFinal(apiKey.getBytes("UTF-8"));
        return new Pair<>(iv, encryption);
    }
}

