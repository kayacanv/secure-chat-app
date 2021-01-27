package com.example.messengerapp

import android.os.Build
import android.support.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


object AES {
    private val RSA = "RSA"

    fun generateRSAKkeyPair():KeyPair {
        val secureRandom = SecureRandom()
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA)
        keyPairGenerator.initialize(2048, secureRandom)
        return keyPairGenerator.generateKeyPair()
    }

    fun do_RSAEncryption(plainText: String, publicKey: PublicKey) : String {
        val encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.getEncoder().encodeToString(encrypt.doFinal(plainText.toByteArray(StandardCharsets.UTF_8)))
    }


    fun do_RSADecryption(cipherText: String, privateKey: PrivateKey) : String {
        val decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        decrypt.init(Cipher.DECRYPT_MODE, privateKey)
        return String(decrypt.doFinal(  Base64.getDecoder().decode( cipherText)  ), StandardCharsets.UTF_8)
    }

    fun publicKeyToString(publicKey: PublicKey) : String {
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    fun stringToPublicKey(encodedString: String) : PublicKey {
        val publicBytes: ByteArray = Base64.getDecoder().decode(encodedString)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeyToString(privateKey: PrivateKey) : String {
        return Base64.getEncoder().encodeToString(privateKey.encoded)
    }

    fun stringToPrivateKey(encodedString: String) : PrivateKey {
        val privateBytes: ByteArray = Base64.getDecoder().decode(encodedString)
        val keySpec = PKCS8EncodedKeySpec(privateBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
}