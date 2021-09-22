package org.baichuan.chat.commons.utils.whisper

import org.baichuan.chat.commons.extension.toBytes
import org.baichuan.chat.commons.extension.toLong


abstract class Obfuscator {
    private val base62 = Base62.createInstance()
    internal abstract val crypto: Crypto

    fun obfuscate(input: Long): String {
        return base62.encode(crypto.encrypt(input.toBytes()))
    }

    fun restore(input: String): Long {
        return crypto.decrypt(base62.decode(input)).toLong()
    }

    companion object {
        @JvmStatic
                /**
                 * [keySource] is a 128bit base62 encoded string
                 */
        fun createInstance(keySource: String): Obfuscator = createAes128GCMInstance(keySource)

        @JvmStatic
                /**
                 * [crypto] is a Crypto provider
                 */
        fun createInstance(crypto: Crypto): Obfuscator = CustomObfuscator(crypto)

        @JvmStatic
                /**
                 * [keySource] is a 128bit base62 encoded string
                 */
        fun createAes128GCMInstance(keySource: String): Obfuscator =
            CustomObfuscator(Crypto.getAes128GCMInstance(keySource))

        @JvmStatic
                /**
                 * [keySource] is a 128bit base62 encoded string
                 */
        fun createAes128ECBInstance(keySource: String): Obfuscator =
            CustomObfuscator(Crypto.getAes128ECBInstance(keySource))

        @JvmStatic
                /**
                 * [keySource] is a 128bit base62 encoded string
                 */
        fun createAes128ECBHmacSHA1Instance(keySource: String): Obfuscator =
            CustomObfuscator(Crypto.getAes128ECBHmacSHA1Instance(keySource))

        private class CustomObfuscator(override val crypto: Crypto) : Obfuscator()
    }
}
