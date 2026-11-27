package com.d4rksoul.base.ecommerce.spring.react.security;

import com.d4rksoul.base.ecommerce.spring.react.config.JwtProperties;
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

import static org.springframework.util.StringUtils.hasText;

@Component
public class JwtKeyProvider {

    private static final String PUBLIC_KEY_LABEL = "PUBLIC KEY";
    private static final String PRIVATE_KEY_LABEL = "PRIVATE KEY";

    private final JwtProperties properties;
    private final Path keysPath;
    private final Path publicKeyPath;
    private final Path privateKeyPath;

    public JwtKeyProvider(JwtProperties properties) {
        this.properties = properties;
        this.keysPath = Paths.get("keys/");
        this.publicKeyPath = keysPath.resolve("public.pem");
        this.privateKeyPath = keysPath.resolve("private.pem");
    }

    public KeyPair getOrInitializeKeyPair() {
        try {
            if (hasText(properties.privateKey()) && hasText(properties.publicKey())) {
                return toKeyPair(
                    decodePem(properties.publicKey(), PUBLIC_KEY_LABEL),
                    decodePem(properties.privateKey(), PRIVATE_KEY_LABEL)
                );
            }

            if (Files.exists(publicKeyPath) && Files.exists(privateKeyPath)) {
                return toKeyPair(
                    decodePem(Files.readString(publicKeyPath), PUBLIC_KEY_LABEL),
                    decodePem(Files.readString(privateKeyPath), PRIVATE_KEY_LABEL)
                );
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Files.createDirectories(keysPath);

            writePem(publicKeyPath, PUBLIC_KEY_LABEL, keyPair.getPublic().getEncoded());
            writePem(privateKeyPath, PRIVATE_KEY_LABEL, keyPair.getPrivate().getEncoded());

            return keyPair;
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new KeyInitializationException(
                "Could not configure cryptographic keys due to a security error. ", e
            );
        }
    }

    private KeyPair toKeyPair(
        byte[] publicKeyBytes, byte[] privateKeyBytes
    ) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        return new KeyPair(publicKey, privateKey);
    }

    private byte[] decodePem(String pem, String label) {
        String base64 = pem
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
