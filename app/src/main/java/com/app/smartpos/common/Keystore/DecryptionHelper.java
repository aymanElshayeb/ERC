package com.app.smartpos.common.Keystore;

import static com.app.smartpos.common.Keystore.KeyStoreHelper.ENCRYPTION_ALGORITHM;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DecryptionHelper {

    public String decrypt(byte[] encryptedData, byte[] iv, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, "UTF-8");
    }
}


