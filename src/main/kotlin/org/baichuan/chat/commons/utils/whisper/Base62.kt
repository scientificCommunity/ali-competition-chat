package org.baichuan.chat.commons.utils.whisper

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.math.ceil
import kotlin.math.ln

class Base62 private constructor(private val alphabet: CharArray) {
  private var lookup: ByteArray = ByteArray(256)

  /**
   * Creates the lookup table from character to index of character in character set.
   */
  init {
    alphabet.forEachIndexed { index: Int, c: Char ->
      lookup[c.toInt()] = (index and 0xFF).toByte()
    }
  }

  /**
   * Encodes a sequence of bytes in Base62 encoding.
   *
   * @param message a byte sequence.
   * @return a sequence of Base62-encoded string.
   */
  fun encode(message: ByteArray): String {
    val indices = convert(message, STANDARD_BASE, TARGET_BASE)
    val translation = CharArray(indices.size)
    for (i in indices.indices) {
      translation[i] = alphabet[indices[i].toInt()]
    }
    return String(translation)
  }

  /**
   * Decodes a sequence of Base62-encoded bytes.
   *
   * @param encoded a sequence of Base62-encoded bytes.
   * @return a byte sequence.
   * @throws IllegalArgumentException when `encoded` is not encoded over the Base62 alphabet.
   */
  fun decode(encoded: String): ByteArray {
    val content = encoded.toByteArray(StandardCharsets.US_ASCII)
    require(isBase62Encoding(content)) { "Input is not encoded correctly" }

    val prepared = ByteArray(content.size)
    for (i in content.indices) {
      prepared[i] = lookup[content[i].toInt()]
    }
    return convert(prepared, TARGET_BASE, STANDARD_BASE)
  }

  /**
   * Checks whether a sequence of bytes is encoded over a Base62 alphabet.
   *
   * @param bytes a sequence of bytes.
   * @return `true` when the bytes are encoded over a Base62 alphabet, `false` otherwise.
   */
  private fun isBase62Encoding(bytes: ByteArray?): Boolean {
    if (bytes == null) {
      return false
    }
    for (e in bytes) {
      if ('0'.toByte() > e || '9'.toByte() < e) {
        if ('a'.toByte() > e || 'z'.toByte() < e) {
          if ('A'.toByte() > e || 'Z'.toByte() < e) {
            return false
          }
        }
      }
    }
    return true
  }

  /**
   * Converts a byte array from a source base to a target base using the alphabet.
   */
  private fun convert(message: ByteArray, sourceBase: Int, targetBase: Int): ByteArray {
    /**
     * This algorithm is inspired by: http://codegolf.stackexchange.com/a/21672
     */
    val estimatedLength = estimateOutputLength(message.size, sourceBase, targetBase)
    val out = ByteArrayOutputStream(estimatedLength)
    var source = message
    while (source.size > 0) {
      val quotient = ByteArrayOutputStream(source.size)
      var remainder = 0
      for (i in source.indices) {
        val accumulator: Int = (source[i].toInt() and 0xFF) + remainder * sourceBase
        val digit = (accumulator - accumulator % targetBase) / targetBase
        remainder = accumulator % targetBase
        if (quotient.size() > 0 || digit > 0) {
          quotient.write(digit)
        }
      }
      out.write(remainder)
      source = quotient.toByteArray()
    }

    // pad output with zeroes corresponding to the number of leading zeroes in the message
    var i = 0
    while (i < message.size - 1 && message[i].toInt() == 0) {
      out.write(0)
      i++
    }
    return reverse(out.toByteArray())
  }

  /**
   * Estimates the length of the output in bytes.
   */
  private fun estimateOutputLength(inputLength: Int, sourceBase: Int, targetBase: Int): Int {
    return ceil(ln(sourceBase.toDouble()) / ln(targetBase.toDouble()) * inputLength).toInt()
  }

  /**
   * Reverses a byte array.
   */
  private fun reverse(arr: ByteArray): ByteArray {
    val length = arr.size
    val reversed = ByteArray(length)
    for (i in 0 until length) {
      reversed[length - i - 1] = arr[i]
    }
    return reversed
  }

  companion object {
    private const val STANDARD_BASE = 256
    private const val TARGET_BASE = 62
    private val GMP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private val INVERTED = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    @JvmStatic
    fun createInstance() = createInstanceWithGmpCharacterSet()

    @JvmStatic
    fun createInstanceWithGmpCharacterSet() = Base62(GMP)

    @JvmStatic
    fun createInstanceWithInvertedCharacterSet() = Base62(INVERTED)
  }
}
