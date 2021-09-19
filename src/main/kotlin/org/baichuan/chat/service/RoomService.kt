package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen
import org.baichuan.chat.commons.constants.CacheSizeConstants
import org.baichuan.chat.commons.extend.badRequest
import java.util.ArrayList

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
@WebApiServiceGen
interface RoomService {
    fun createRoom(
        body: JsonObject, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun enterRoom(
        roomid: String, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun getRoom(
        roomid: String, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun getUserListFromRoom(
        roomid: String, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun leaveRoom(
        context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun listRooms(
        body: JsonObject,
        context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )
}

object RoomServiceLocalCache {
    /**
     * key roomId
     * value roomName
     */
    val roomCache = HashMap<String, String>(CacheSizeConstants.size_2097152)

    /**
     * room name cache
     */
    val roomNameCache = ArrayList<String>(CacheSizeConstants.size_2097152)

    /**
     * key username
     * value roomId
     */
    val usernameRoomIdCache = HashMap<String, String>(CacheSizeConstants.size_65536)

    /**
     * 保存一个房间里的所有用户信息
     * key roomId
     * value username
     */
    val roomIdUsernameCache = HashMap<String, MutableSet<String>>(CacheSizeConstants.size_65536)

    fun getRoomIdByUsername(username: String, resultHandler: Handler<AsyncResult<ServiceResponse>>): String {
        if (!containsKey(username)) {
            resultHandler.badRequest()
            throw RuntimeException()
        }
        return usernameRoomIdCache[username]!!
    }

    fun containsKey(username: String): Boolean {
        return usernameRoomIdCache.containsKey(username)
    }

    fun put(username: String, roomId: String) {
        usernameRoomIdCache[username] = roomId
    }

    fun remove(username: String) {
        usernameRoomIdCache.remove(username)
    }
}