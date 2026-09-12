package com.d4rksoul.base.ecommerce.spring.react.security;

import com.d4rksoul.base.ecommerce.spring.react.exception.KeyInitializationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyProvider {

    private final Path keysPath;
    private final Path publicKeyPath;
    private final Path privateKeyPath;

    public JwtKeyProvider() {
        this.keysPath = Paths.get("src/main/resources/keys");
        this.publicKeyPath = keysPath.resolve("public.pem");
        this.privateKeyPath = keysPath.resolve("private.pem");
    }

    public KeyPair getOrInitializeKeyPair() {
        try {
            if (Files.exists(publicKeyPath) && Files.exists(privateKeyPath)) {
                return loadKeysFromDisk();
            }

            return generateAndSaveKeysToDisk();
        } catch (GeneralSecurityException | IOException e) {
            throw new KeyInitializationException("Could not configure cryptographic keys due to a security error", e);
        }
    }

    private KeyPair loadKeysFromDisk() throws GeneralSecurityException, IOException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        String publicPem = Files.readString(publicKeyPath)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicBytes = Base64.getDecoder().decode(publicPem);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicSpec);

        String privatePem = Files.readString(privateKeyPath)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateBytes = Base64.getDecoder().decode(privatePem);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateSpec);

        return new KeyPair(publicKey, privateKey);
    }

    private KeyPair generateAndSaveKeysToDisk() throws GeneralSecurityException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Files.createDirectories(keysPath);

        Base64.Encoder encoder = Base64.getMimeEncoder(64, new byte[]{'\n'});

        String encodedPublic = encoder.encodeToString(keyPair.getPublic().getEncoded());
        String publicPem = "-----BEGIN PUBLIC KEY-----\n" + encodedPublic + "\n-----END PUBLIC KEY-----\n";
        Files.writeString(publicKeyPath, publicPem);

        String encodedPrivate = encoder.encodeToString(keyPair.getPrivate().getEncoded());
        String privatePem = "-----BEGIN PRIVATE KEY-----\n" + encodedPrivate + "\n-----END PRIVATE KEY-----\n";
        Files.writeString(privateKeyPath, privatePem);

        return keyPair;
    }
}
