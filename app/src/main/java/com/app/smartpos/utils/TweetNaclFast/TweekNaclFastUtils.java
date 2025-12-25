package com.app.smartpos.utils.TweetNaclFast;

import com.app.smartpos.common.Utils;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public class TweekNaclFastUtils {
    public static String encrypt(String message) {
        byte[] secretKey = TweetNaclFast.randombytes(TweetNaclFast.SecretBox.keyLength);
        TweetNaclFast.SecretBox box = new TweetNaclFast.SecretBox(secretKey);
        byte[] nonce = TweetNaclFast.makeSecretBoxNonce();
        Utils.addLog("datadata_encrypted",new String(nonce, StandardCharsets.UTF_8));
        byte[] encrypted = box.box(message.getBytes(), nonce);
        return new String(encrypted, StandardCharsets.UTF_8);

    }

    public static String decrypt(byte[] encryptedMessage) {
        byte[] secretKey = TweetNaclFast.randombytes(TweetNaclFast.SecretBox.keyLength);
        byte[] nonce = TweetNaclFast.makeSecretBoxNonce();
        TweetNaclFast.SecretBox box2 = new TweetNaclFast.SecretBox(secretKey);
        byte[] decrypted =  box2.open(encryptedMessage, nonce);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
