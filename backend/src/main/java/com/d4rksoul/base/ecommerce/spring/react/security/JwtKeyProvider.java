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
        this.keysPath = Paths.get("keys/");
        this.publicKeyPath = keysPath.resolve("public.pem");
        this.privateKeyPath = keysPath.resolve("private.pem");
    }

    public KeyPair getOrInitializeKeyPair() {
        try {
            if (Files.exists(publicKeyPath) && Files.exists(privateKeyPath)) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                PublicKey publicKey = keyFactory.generatePublic(
                    new X509EncodedKeySpec(readPem(publicKeyPath, "PUBLIC KEY"))
                );
                PrivateKey privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(readPem(privateKeyPath, "PRIVATE KEY"))
                );

                return new KeyPair(publicKey, privateKey);
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Files.createDirectories(keysPath);

            writePem(publicKeyPath, "PUBLIC KEY", keyPair.getPublic().getEncoded());
            writePem(privateKeyPath, "PRIVATE KEY", keyPair.getPrivate().getEncoded());

            return keyPair;
        } catch (GeneralSecurityException | IOException e) {
            throw new KeyInitializationException(
                "Could not configure cryptographic keys due to a security error. ", e
            );
        }
    }

    private byte[] readPem(Path path, String label) throws IOException {
        String base64 = Files.readString(path)
            .replace("-----BEGIN " + label + "-----", "")
            .replace("-----END " + label + "-----", "")
            .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }

    private void writePem(Path path, String label, byte[] encoded) throws IOException {
        String pem = "-----BEGIN " + label + "-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
            + "\n-----END " + label + "-----\n";

        Files.writeString(path, pem);
    }
}
