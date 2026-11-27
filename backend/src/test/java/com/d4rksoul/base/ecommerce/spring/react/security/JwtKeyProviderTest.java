package com.d4rksoul.base.ecommerce.spring.react.security;

import com.d4rksoul.base.ecommerce.spring.react.config.JwtProperties;
import com.d4rksoul.base.ecommerce.spring.react.exception.KeyInitializationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtKeyProviderTest {

    private static KeyPair generated;

    @BeforeAll
    static void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        generated = generator.generateKeyPair();
    }

    @Test
    @DisplayName("Loads keys given as a bare base64 body")
    void loadsBareBase64Keys() {
        JwtProperties properties = new JwtProperties(
            Base64.getEncoder().encodeToString(generated.getPrivate().getEncoded()),
            Base64.getEncoder().encodeToString(generated.getPublic().getEncoded())
        );

        KeyPair loaded = new JwtKeyProvider(properties).getOrInitializeKeyPair();

        assertThat(loaded.getPublic()).isEqualTo(generated.getPublic());
        assertThat(loaded.getPrivate()).isEqualTo(generated.getPrivate());
    }

    @Test
    @DisplayName("Loads keys given as PEM with header, footer and line breaks")
    void loadsPemKeys() {
        JwtProperties properties = new JwtProperties(
            toPem("PRIVATE KEY", generated.getPrivate().getEncoded()),
            toPem("PUBLIC KEY", generated.getPublic().getEncoded())
        );

        KeyPair loaded = new JwtKeyProvider(properties).getOrInitializeKeyPair();

        assertThat(loaded.getPublic()).isEqualTo(generated.getPublic());
        assertThat(loaded.getPrivate()).isEqualTo(generated.getPrivate());
    }

    @Test
    @DisplayName("Fails with KeyInitializationException when the private key is invalid")
    void failsOnInvalidPrivateKey() {
        JwtProperties properties = new JwtProperties(
            "bm90LWEta2V5",
            Base64.getEncoder().encodeToString(generated.getPublic().getEncoded())
        );

        JwtKeyProvider provider = new JwtKeyProvider(properties);

        assertThatThrownBy(provider::getOrInitializeKeyPair)
            .isInstanceOf(KeyInitializationException.class);
    }

    private static String toPem(String label, byte[] encoded) {
        return "-----BEGIN " + label + "-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
            + "\n-----END " + label + "-----\n";
    }
}
