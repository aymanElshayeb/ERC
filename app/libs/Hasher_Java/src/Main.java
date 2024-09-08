import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;

public class Main {
    public static void main(String[] args) {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        HashFormat hashFormat = new Shiro1CryptFormat();
        DefaultHashService defaultHashService = new DefaultHashService();
        defaultHashService.setHashAlgorithmName("SHA-512");
        passwordService.setHashService(defaultHashService);
        passwordService.setHashFormat(hashFormat);
        final String password = "cY!g()vNfE#Mb(73";
        final String testHash = "$shiro1$SHA-512$500000$AzFJXciBK2A5Yl/GrZ558g==$cAvFhqWfj5J7au0iG0hmTG1pl4xqThHd7xrdY4HfLaL5Ea3GZXDx//gf2EczdKYxmgkOVC51loO2jgjfm8MEDg==";
        String hash = passwordService.encryptPassword(password);
        System.out.printf("%s%n%s%n%s%n",
                hash,
                passwordService.passwordsMatch(password, hash), // Validate plaintext against Argon2 hash,
                passwordService.passwordsMatch(password, testHash) // Validate plaintext against SHA-512 hash
        );
    }
}