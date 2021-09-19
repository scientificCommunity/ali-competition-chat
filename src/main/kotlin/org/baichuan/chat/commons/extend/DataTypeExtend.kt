package org.baichuan.chat.commons.extend

import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
internal fun Long.toBytes(): ByteArray {
    val buffer: ByteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.putLong(this)
    return buffer.array()
}

internal fun ByteArray.toLong(): Long {
    val buffer: ByteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.put(this)
    buffer.flip() //need flip
    return buffer.long
}


fun LocalDateTime.toLong(): Long = this.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli() ?: 0 // 使用了默认时区

fun Any?.toJsonSupportValue() =
    if (this is Number || this is CharSequence || this is Boolean || this == null || this is Iterable<*>) this
    else if (this is LocalDateTime) this.toLong()
    else this.toString()