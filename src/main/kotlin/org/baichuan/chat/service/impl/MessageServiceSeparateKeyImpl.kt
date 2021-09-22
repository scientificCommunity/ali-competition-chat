package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import org.baichuan.chat.commons.extension.badRequest
import org.baichuan.chat.commons.extension.jsonArraySucceed
import org.baichuan.chat.commons.extension.plainTextSucceed
import org.baichuan.chat.commons.extension.subscribeTemplate
import org.baichuan.chat.service.MessageService
import org.baichuan.chat.service.RedisFactory
import org.baichuan.chat.service.RoomServiceLocalCache

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/15
 * 根据序号将消息分散到不同的key中
 */
class MessageServiceSeparateKeyImpl(val vertx: Vertx) : MessageService {
    private val zSetMessageKey = "messages"

    private val redis = RedisFactory.getClient()

    override fun sendMessage(
        body: JsonObject,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)

        val currentTime = System.currentTimeMillis()
        body.put("timestamp", currentTime)

        redis.rxZadd(listOf(roomId + zSetMessageKey, "NX", currentTime.toString(), body.toString()))
            .subscribeTemplate(resultHandler, {
                resultHandler.plainTextSucceed()
            }, {
                resultHandler.plainTextSucceed()
            })
    }

    override fun retrieve(
        body: JsonObject,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)

        val pageIndex = body.getInteger("pageIndex")
        val pageSize = body.getInteger("pageSize")
        if (pageIndex == 0 || pageSize <= 0) {
            resultHandler.badRequest()
        }

        if (pageIndex < 0) {
            val startIndex = (pageIndex * -1 - 1) * pageSize
            val endIndex = startIndex + pageSize - 1
            redis.rxZrevrange(listOf(roomId + zSetMessageKey, startIndex.toString(), endIndex.toString()))
                .subscribeTemplate(resultHandler, {
                    resultHandler.jsonArraySucceed(it.toString())
                }) {
                    resultHandler.jsonArraySucceed(JsonArray())
                }
        } else {
            val startIndex = (pageIndex - 1) * pageSize
            val endIndex = startIndex + pageSize - 1
            redis.rxZrange(listOf(roomId + zSetMessageKey, startIndex.toString(), endIndex.toString()))
                .subscribeTemplate(resultHandler, {
                    resultHandler.jsonArraySucceed(it.toString())
                }) {
                    resultHandler.jsonArraySucceed(JsonArray())
                }
        }
    }
}