package com.example.messengerapp;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.Cipher;
// import javax.xml.bind.DatatypeConverter;

public class AES {

    private static final String RSA = "RSA";
    private static Scanner sc;

    // Generating public & private keys
    // using RSA algorithm.
    public static KeyPair generateRSAKkeyPair()
            throws Exception
    {
        SecureRandom secureRandom = new SecureRandom ();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);

        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    // Encryption function which converts
    // the plainText into a cipherText
    // using private Key.
    public static byte[] do_RSAEncryption(String plainText, PublicKey publicKey) throws Exception
    {
        Cipher encrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedMessage = encrypt.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return encryptedMessage;
    }

    // Decryption function which converts
    // the ciphertext back to the
    // orginal plaintext.
    public static String do_RSADecryption(byte[] cipherText, PrivateKey privateKey) throws Exception
    {
        Cipher decrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decrypt.init(Cipher.DECRYPT_MODE, privateKey);
        String decryptedMessage = new String(decrypt.doFinal(cipherText), StandardCharsets.UTF_8);
        return decryptedMessage;
    }
}