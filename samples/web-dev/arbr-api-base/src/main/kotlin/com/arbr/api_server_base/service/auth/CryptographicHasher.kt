package com.arbr.api_server_base.service.auth

import jakarta.annotation.PostConstruct
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


@Component
class CryptographicHasher(
    @Value("\${arbr.user_auth.private_key}")
    private val userAuthPrivateKey: String,
    @Value("\${topdown.github_auth.private_key}")
    private val githubAuthPrivateKey: String,
) {

    @PostConstruct
    fun init() {
        // Add BouncyCastle as a Security Provider
        Security.addProvider(BouncyCastleProvider())
    }

    private fun privateKey(kind: PrivateKeyKind): String {
        return when (kind) {
            PrivateKeyKind.USER_TOKEN -> userAuthPrivateKey
            PrivateKeyKind.GITHUB_ACCESS_TOKEN -> githubAuthPrivateKey
        }
    }

    private fun makeCipher(kind: PrivateKeyKind, decrypt: Boolean): Cipher {
        val key = privateKey(kind)
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC")
        val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
        val mode = if (decrypt) Cipher.DECRYPT_MODE else Cipher.ENCRYPT_MODE
        cipher.init(mode, secretKey)
        return cipher
    }

    fun encrypt(kind: PrivateKeyKind, string: String): String {
        val cipher = makeCipher(kind, false)
        val encryptedData = cipher.doFinal(string.toByteArray())

        // Write the encoded data with Base64
        return Base64.getEncoder().encodeToString(encryptedData)
    }

    fun decrypt(kind: PrivateKeyKind, encodedString: String): String {
        val cipher = makeCipher(kind, true)
        val decodedValue = Base64.getDecoder().decode(encodedString)
        return String(cipher.doFinal(decodedValue))
    }
}
