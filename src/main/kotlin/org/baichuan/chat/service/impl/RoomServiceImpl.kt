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
import org.baichuan.chat.service.RedisFactory
import org.baichuan.chat.service.RoomService
import org.baichuan.chat.service.RoomServiceLocalCache
import java.util.*

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
class RoomServiceImpl(private val vertx: Vertx) : RoomService {
    val redis = RedisFactory.getClient()

    private val zSetRoomKey = "rooms"
    private val zSetRoomValueScore = "1"

    override fun createRoom(
        body: JsonObject,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val roomName = body.getString("name")
        val roomId = UUID.randomUUID().toString()

        redis.rxSetnx(roomId, roomName).subscribeTemplate(resultHandler) {
            redis.rxZadd(listOf(zSetRoomKey, "NX", zSetRoomValueScore, roomName)).subscribeTemplate(resultHandler, {
                resultHandler.plainTextSucceed(roomId)
            }, {
                resultHandler.plainTextSucceed(roomId)
            })
        }
    }

    override fun enterRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        redis.rxGet(roomid).subscribeTemplate(resultHandler, {
            val username = context.user.getString("username")

            RoomServiceLocalCache.usernameRoomIdCache[username] = roomid
            RoomServiceLocalCache.put(username, roomid)

            if (RoomServiceLocalCache.roomIdUsernameCache.containsKey(roomid)) {
                RoomServiceLocalCache.roomIdUsernameCache[roomid]!!.add(username)
            } else {
                RoomServiceLocalCache.roomIdUsernameCache[roomid] = mutableSetOf(username)
            }

            resultHandler.plainTextSucceed()
        })

        /*redis.rxGet(roomid).subscribeTemplate(resultHandler) {
            val username = context.user.getString("username")
            redis.rxSet(listOf(username, roomid))
            redis.rxZadd(listOf(roomid, "NX", zSetRoomValueScore, username))

            resultHandler.plainTextSucceed()
        }*/
    }

    override fun leaveRoom(
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.usernameRoomIdCache.remove(context.user.getString("username"))
        RoomServiceLocalCache.remove(context.user.getString("username"))

        if (roomId != null) {
            RoomServiceLocalCache.roomIdUsernameCache[roomId]!!.remove(username)
        }
        resultHandler.plainTextSucceed()
    }

    override fun getRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        redis.rxGet(roomid).subscribeTemplate(resultHandler) {
            resultHandler.plainTextSucceed(it.toString())
        }
    }

    override fun getUserListFromRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        if (!RoomServiceLocalCache.roomIdUsernameCache.containsKey(roomid)) {
            resultHandler.badRequest()
        }
        redis.rxGet(roomid).subscribeTemplate(resultHandler, {
            val result = JsonArray()
            RoomServiceLocalCache.roomIdUsernameCache[roomid]!!.forEach { username ->
                result.add(username)
            }
            resultHandler.jsonArraySucceed(result)
        })
    }

    override fun listRooms(
        body: JsonObject,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val pageIndex = body.getInteger("pageIndex")
        val pageSize = body.getInteger("pageSize")
        val startIndex = (pageIndex - 1) * pageSize
        val endIndex = startIndex + pageSize - 1

        redis.rxZrange(listOf(zSetRoomKey, startIndex.toString(), endIndex.toString()))
            .subscribeTemplate(resultHandler, {
                val result = JsonArray()
                it.forEach { single ->
                    result.add(single.toString())
                }
                resultHandler.jsonArraySucceed(result)
            }) {
                resultHandler.jsonArraySucceed(JsonArray())
            }
    }
}