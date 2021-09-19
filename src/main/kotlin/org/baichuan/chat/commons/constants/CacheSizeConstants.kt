package org.baichuan.chat.commons.constants

import org.baichuan.chat.commons.constants.CacheSizeConstants.size_2097152
import org.baichuan.chat.commons.constants.CacheSizeConstants.size_524288


object CacheSizeConstants {
    const val size_65536 = 2 shl 15
    const val size_524288 = 2 shl 18
    const val size_2097152 = 2 shl 20

    const val size_64 = 2 shl 5
}

fun main() {
    println(size_2097152)
    println(size_524288 * 3600L/1000000000)
}