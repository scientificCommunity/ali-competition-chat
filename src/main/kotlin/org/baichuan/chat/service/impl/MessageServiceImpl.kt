package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import org.apache.commons.io.FileUtils
import org.baichuan.chat.bean.dto.MessageRetrieveDTO
import org.baichuan.chat.bean.dto.MessageSendDTO
import org.baichuan.chat.commons.extend.executeWithEmptyText
import org.baichuan.chat.commons.extend.executeWithJsonList
import org.baichuan.chat.commons.extend.jsonArraySucceed
import org.baichuan.chat.commons.extend.plainTextSucceed
import org.baichuan.chat.db.DbHolder.sqlCreator
import org.baichuan.chat.jooq.Tables
import org.baichuan.chat.service.MessageCache
import org.baichuan.chat.service.MessageCache.newlyMessageCache
import org.baichuan.chat.service.MessageCache.pageLimit
import org.baichuan.chat.service.MessageCache.pageLimitNag
import org.baichuan.chat.service.MessageCache.roomIdMessageCacheCount
import org.baichuan.chat.service.MessageCache.singleRoomMessageCacheLimit
import org.baichuan.chat.service.MessageService
import org.baichuan.chat.service.RoomServiceLocalCache

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/15
 */
class MessageServiceImpl(val vertx: Vertx) : MessageService {

    override fun sendMessage(
        body: MessageSendDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val startTime = System.currentTimeMillis()

        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)

        sqlCreator!!.insertInto(
            Tables.G_MESSAGE,
            Tables.G_MESSAGE.ID,
            Tables.G_MESSAGE.ROOM_ID,
            Tables.G_MESSAGE.TEXT
        ).values(body.id, roomId, body.text)
            .executeWithEmptyText(resultHandler) {
                println("send msg cost is ${System.currentTimeMillis() - startTime}")

                //不浪费主线程资源
                vertx.executeBlocking<Unit> {
                    val message = JsonObject()
                    message.put("id", body.id)
                    message.put("text", body.text)
                    message.put("timestamp", System.currentTimeMillis())

                    synchronized(MessageCache::class.java) {
                        if (roomIdMessageCacheCount.containsKey(roomId)) {
                            //println("添加缓存，当前count:${roomIdMessageCacheCount[roomId]},size:${newlyMessageCache[roomId]!!.size},roomId:${roomId}")
                            newlyMessageCache[roomId]!!.add(message)
                            roomIdMessageCacheCount[roomId] = roomIdMessageCacheCount[roomId]!! + 1
                            if (roomIdMessageCacheCount[roomId]!! >= singleRoomMessageCacheLimit) {
                                //移除头节点
                                //println("移除头节点，当前count:${roomIdMessageCacheCount[roomId]}")
                                newlyMessageCache[roomId]!!.removeFirst()
                            }
                        } else {
                            //println("初始化，消息id:${body.id}")
                            roomIdMessageCacheCount[roomId] = 1
                            val arrayDeque = ArrayDeque<JsonObject>()
                            arrayDeque.add(message)
                            newlyMessageCache[roomId] = arrayDeque
                        }
                    }
                }
            }
    }

    private fun listBatchFetch(list: ArrayDeque<JsonObject>, offset: Int, limit: Int): JsonArray {
        val size = list.size

        //数据索引
        var index = (offset - 1) * limit
        var cursor = 1
        if (offset < 0) {
            index = size - 1 + (offset + 1) * limit
            cursor = -1
        }

        //循环批次
        var i = 0

        //定义返回结果
        val result = JsonArray()
        while (i < limit) {
            if (index == size || index < 0) {
                return result
            }

            result.add(list[index])
            index += cursor
            i++
        }
        return result
    }

    override fun retrieve(
        body: MessageRetrieveDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val startTime = System.currentTimeMillis()

        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)

        if (newlyMessageCache.containsKey(roomId)
            && (body.pageIndex!! < pageLimit)
            && body.pageIndex!! > pageLimitNag
        ) {
            val list = newlyMessageCache[roomId]
            //如果查找最新消息或者缓存池没满
            if (list!!.size < singleRoomMessageCacheLimit || body.pageIndex!! < 0) {
                println("send msg cost is ${System.currentTimeMillis() - startTime}")

                resultHandler.jsonArraySucceed(listBatchFetch(list!!, body.pageIndex!!, body.pageSize!!))
                return
            }

            //正向已丢弃的页数
            val discardPages = (roomIdMessageCacheCount[roomId]!! - singleRoomMessageCacheLimit) / body.pageSize!!

            //如果缓存里有正向的历史消息
            val newPageIndex = body.pageIndex!! - discardPages
            if (newPageIndex > 0) {
                println("send msg cost is ${System.currentTimeMillis() - startTime}")

                resultHandler.jsonArraySucceed(listBatchFetch(list, newPageIndex, body.pageSize!!))
                return
            }
        } else {
            resultHandler.jsonArraySucceed(JsonArray())
        }

        val sql = sqlCreator!!.select(Tables.G_MESSAGE.ID, Tables.G_MESSAGE.TEXT, Tables.G_MESSAGE.TIMESTAMP)
            .from(Tables.G_MESSAGE)
            .where(Tables.G_MESSAGE.ROOM_ID.eq(roomId))

        val sqlResult = if (body.pageIndex!! > 0) {
            sql.orderBy(Tables.G_MESSAGE.TIMESTAMP.asc())
                .offset((body.pageIndex!! - 1) * body.pageSize!!)
                .limit(body.pageSize!!)
        } else {
            val newPageIndex = body.pageIndex!! * -1
            sql.orderBy(Tables.G_MESSAGE.TIMESTAMP.desc())
                .offset((newPageIndex - 1) * body.pageSize!!)
                .limit(body.pageSize!!)
        }

        sqlResult.executeWithJsonList(resultHandler) {
            println("send msg cost is ${System.currentTimeMillis() - startTime}")
        }
    }
}