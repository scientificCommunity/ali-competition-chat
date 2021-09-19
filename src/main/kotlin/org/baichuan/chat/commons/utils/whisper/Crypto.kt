package org.baichuan.chat.commons.utils.whisper

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


abstract class Crypto private constructor(internal val key: SecretKey) {
  internal val random: SecureRandom = SecureRandom()
  internal abstract val algorithm: String
  internal abstract val mode: String
  internal abstract val keySize: Int

  abstract fun encrypt(input: ByteArray): ByteArray
  abstract fun decrypt(input: ByteArray): ByteArray

  companion object {
    private val base62 = Base62.createInstance()
    private val base64Decoder = Base64.getDecoder()
    private val base64Encoder = Base64.getEncoder()

    @JvmStatic
    fun generateKey(algorithm: String, keySize: Int, random: SecureRandom = SecureRandom()): SecretKey {
      val keyGenerator = KeyGenerator.getInstance(algorithm)
      keyGenerator.init(keySize, random)
      return keyGenerator.generateKey()
    }

    @JvmStatic
    fun getAes128GCMInstance(keySource: String): Crypto = Aes128GCM(SecretKeySpec(base64Decoder.decode(keySource), Aes128GCM.ALGORITHM))

    @JvmStatic
    fun getAes128ECBInstance(keySource: String): Crypto = Aes128ECB(SecretKeySpec(base64Decoder.decode(keySource), Aes128ECB.ALGORITHM))

    @JvmStatic
    fun getAes128ECBHmacSHA1Instance(keySource: String): Crypto = Aes128ECBHmacSHA1(SecretKeySpec(base64Decoder.decode(keySource), Aes128ECBHmacSHA1.ALGORITHM))

    private class Aes128GCM(key: SecretKey, private val nonceLength: Int = 3, private val tagLength: Int = 96) : Crypto(key) {
      companion object {
        const val ALGORITHM: String = "AES"
        const val MODE: String = "AES/GCM/PKCS5Padding"
        const val KEY_SIZE: Int = 128
      }

      override fun encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(mode)
        val nonce = ByteArray(nonceLength)
        random.nextBytes(nonce)
        val spec = GCMParameterSpec(tagLength, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        return nonce + cipher.doFinal(input)
      }

      override fun decrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(mode)
        val spec = GCMParameterSpec(tagLength, input.sliceArray(0 until nonceLength))
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(input.sliceArray(nonceLength until input.size))
      }

      override val algorithm get() = ALGORITHM
      override val mode get() = MODE
      override val keySize get() = KEY_SIZE
    }

    private class Aes128ECB(key: SecretKey) : Crypto(key) {
      companion object {
        const val ALGORITHM: String = "AES"
        const val MODE: String = "AES/ECB/PKCS5Padding"
        const val KEY_SIZE: Int = 128
      }

      override fun encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(input)
      }

      override fun decrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(input)
      }

      override val algorithm get() = ALGORITHM
      override val mode get() = MODE
      override val keySize get() = KEY_SIZE
    }

    private class Aes128ECBHmacSHA1(key: SecretKey) : Crypto(key) {
      companion object {
        const val ALGORITHM: String = "AES"
        const val MAC_ALGORITHM: String = "HmacSHA1"
        const val MODE: String = "AES/ECB/PKCS5Padding"
        const val KEY_SIZE: Int = 128
        const val BLOCK_SIZE: Int = 16 // in bytes
      }

      override fun encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val mac = Mac.getInstance(MAC_ALGORITHM)
        mac.init(key)
        val payload = cipher.doFinal(input)
        return payload + mac.doFinal(payload)
      }

      override fun decrypt(input: ByteArray): ByteArray {
        val mac = Mac.getInstance(MAC_ALGORITHM)
        mac.init(key)
        val payload = input.sliceArray(0 until BLOCK_SIZE)
        val tag = input.sliceArray(BLOCK_SIZE until input.size)
        val digest = mac.doFinal(payload)
        if (!digest!!.contentEquals(tag))
          throw javax.crypto.AEADBadTagException()
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(payload)
      }

      override val algorithm get() = ALGORITHM
      override val mode get() = MODE
      override val keySize get() = KEY_SIZE
    }
  }
}

