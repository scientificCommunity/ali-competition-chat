package org.baichuan.chat.commons.utils.whisper

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
object ObfuscatorHolder {

    private var userObfuscator: Obfuscator? = null
    private var roomObfuscator: Obfuscator? = null
    private var messageObfuscator: Obfuscator? = null

    fun init() {
        userObfuscator =
            Obfuscator.createInstance(Crypto.getAes128ECBHmacSHA1Instance("2a2p9ff/SkdfVdsaffgSRA=="))
        roomObfuscator =
            Obfuscator.createInstance(Crypto.getAes128ECBHmacSHA1Instance("2a2p9ff/aSDFasdfq13Adf=="))
        messageObfuscator =
            Obfuscator.createInstance(Crypto.getAes128ECBHmacSHA1Instance("2a2p9ff/A2D1afsaffgSRA=="))
    }

    fun obfuscate(id: Long, type: WhisperType): String {
        return when (type) {
            WhisperType.USER -> userObfuscator!!.obfuscate(id)
            WhisperType.ROOM -> roomObfuscator!!.obfuscate(id)
            else -> messageObfuscator!!.obfuscate(id)
        }
    }

    fun restore(id: String, type: WhisperType): Long {
        return when (type) {
            WhisperType.USER -> userObfuscator!!.restore(id)
            WhisperType.ROOM -> roomObfuscator!!.restore(id)
            else -> messageObfuscator!!.restore(id)
        }
    }


    enum class WhisperType {
        USER, ROOM, MESSAGE
    }
}

fun main() {
    ObfuscatorHolder.init()
    println(ObfuscatorHolder.obfuscate(1,ObfuscatorHolder.WhisperType.ROOM))
}