package org.baichuan.chat.commons.utils

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.NonReadableChannelException
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object FileUtils {
    private const val MAX = 628
    private const val WHITE_SPACE: Byte = 32

    //逗号
    private const val COMMA: Byte = 44
    private val fcOutHolder = HashMap<Long, FileChannel>()
    private val fcInHolder = HashMap<Long, FileChannel>()
    private val countFcOutHolder = HashMap<Long, FileChannel>()
    private val countFcInHolder = HashMap<Long, FileChannel>()
    private val singlePool = ThreadPoolExecutor(1, 1, 1000, TimeUnit.DAYS, LinkedBlockingDeque())
    private var total = 0

    private val roomIdMsgCache = HashMap<Long, List<String>>()

    fun writeTask(msg: String, roomId: Long) {
        singlePool.execute {
            val readTotalCount = readTotalCount(getCountFcIn(roomId))
            writeCount(readTotalCount + 1, getCountFcOut(roomId))
            doWrite(msg, roomId)
        }
    }

    private fun getFcOut(roomId: Long): FileChannel {
        val fc = if (fcOutHolder.containsKey(roomId)) {
            fcOutHolder[roomId]
        } else {
            val fout = FileOutputStream("$roomId.data", true)
            fcOutHolder[roomId] = fout.channel
            fcOutHolder[roomId]
        }
        return fc!!
    }

    private fun getCountFcOut(roomId: Long): FileChannel {
        val fc = if (countFcOutHolder.containsKey(roomId)) {
            countFcOutHolder[roomId]
        } else {
            // append模式下跟write+position在linux下貌似有bug
            val fout = FileOutputStream("$roomId.data.count")
            countFcOutHolder[roomId] = fout.channel
            countFcOutHolder[roomId]
        }
        return fc!!
    }

    private fun getCountFcIn(roomId: Long): FileChannel {
        val fc = if (countFcInHolder.containsKey(roomId)) {
            countFcInHolder[roomId]
        } else {
            val fout = try {
                FileInputStream("$roomId.data.count")
            } catch (e: FileNotFoundException) {
                writeCount(0, getCountFcOut(roomId))
                FileInputStream("$roomId.data.count")
            }

            countFcInHolder[roomId] = fout.channel
            countFcInHolder[roomId]
        }
        return fc!!
    }

    private fun getFcIn(roomId: Long): FileChannel {
        val fc = if (fcInHolder.containsKey(roomId)) {
            fcInHolder[roomId]
        } else {
            val fout = try {
                FileInputStream("$roomId.data")
            } catch (e: FileNotFoundException) {
                writeCount(0, getFcOut(roomId))
                FileInputStream("$roomId.data")
            }

            fcInHolder[roomId] = fout.channel
            fcInHolder[roomId]
        }
        return fc!!
    }

    fun doWrite(msg: String, roomId: Long) {
        val start = System.currentTimeMillis()

        val fc = getFcOut(roomId)

        val buffer = ByteBuffer.allocateDirect(MAX + 1)
        val message = msg.toByteArray()

        //插入逗号
        buffer.put(COMMA)
        //插入数据
        for (b in message) {
            buffer.put(b)
        }
        //插入补齐数据
        for (j in 0 until MAX - message.size) {
            buffer.put(WHITE_SPACE)
        }

        buffer.flip()

        fc.write(buffer)
        //fc.force(true)
        //println("cost : " + (System.currentTimeMillis() - start))
    }

    fun readMsg(roomId: Long, pageIndex: Int, pageSize: Int): String {
        val fc = getFcIn(roomId)
        val countFc = getCountFcIn(roomId)

        //将要读取的buffer大小
        var bufferSize = pageSize * (MAX + 1) - 1
        val totalCount = readTotalCount(countFc)
        println("当前读取到的totalCount为：$totalCount")
//        val totalCount = total
        var offset = if (pageIndex < 0) {
            //请求的页数超过了已有的页数
            val remainCount = totalCount - (pageIndex + 1) * -1 * pageSize
            if (remainCount < 0) {
                return ""
            }

            //如果剩下的不够一页，则有多少取多少
            if (remainCount < pageSize) {
                bufferSize = remainCount.toInt() * (MAX + 1) - 1
                0
            } else {
                (totalCount + pageIndex * pageSize) * (MAX + 1)
            }
        } else {
            if ((pageIndex - 1) * pageSize > totalCount) {
                return ""
            }
            ((pageIndex - 1) * pageSize * (MAX + 1)).toLong()
        }

        if (offset.toLong() < 0) {
            offset = 0
        }

        val buffer = ByteBuffer.allocate(bufferSize)
        //linux下write指定position写数据貌似有bug，所以count数换到里一个单独的文件保存，所以就不用跳过9个字节读了
        //fc.read(buffer, offset.toLong() + 9)
        fc.read(buffer, offset + 1)

        return String(buffer.array())
    }

    private fun readTotalCount(fc: FileChannel): Long {
        val buffer = ByteBuffer.allocate(8)
        try {
            fc.read(buffer, 0)
        } catch (e: NonReadableChannelException) {
            return 0
        }
        buffer.flip()
        return buffer.long
    }

    fun writeCount(msg: Long, fc: FileChannel) {
        val buffer = ByteBuffer.allocateDirect(8)
        buffer.putLong(msg)
        buffer.flip()
        fc.write(buffer, 0)
    }
}

fun main() {
    //FileUtils.doWrite("asdfasf", 1)
}