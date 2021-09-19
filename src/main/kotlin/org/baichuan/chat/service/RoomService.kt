package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.bean.dto.CreateRoomDTO
import org.baichuan.chat.bean.dto.ListRoomsDTO
import org.baichuan.chat.commons.constants.CacheSizeConstants
import org.baichuan.chat.commons.extend.badRequest
import org.baichuan.chat.db.DbHolder
import org.baichuan.chat.jooq.Tables
import java.util.ArrayList

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
@WebApiServiceGen
interface RoomService {
    fun createRoom(
        body: CreateRoomDTO, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
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
        body: ListRoomsDTO,
        context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun initRoomCache(
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
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
    val usernameRoomIdCache = HashMap<String, Long>(CacheSizeConstants.size_65536)

    /**
     * key roomId
     * value username
     */
    val roomIdUsernameCache = HashMap<Long, MutableSet<String>>(CacheSizeConstants.size_65536)

    fun getRoomIdByUsername(username: String, resultHandler: Handler<AsyncResult<ServiceResponse>>): Long {
        if (!containsKey(username)) {
            resultHandler.badRequest()
            throw RuntimeException()
        }
        return usernameRoomIdCache[username]!!
    }

    fun containsKey(username: String): Boolean {
        return usernameRoomIdCache.containsKey(username)
    }

    fun put(username: String, roomId: Long) {
        usernameRoomIdCache[username] = roomId
    }

    fun remove(username: String) {
        usernameRoomIdCache.remove(username)
    }

    fun initRoomCache() {
        DbHolder.sqlCreator!!
            .select(
                Tables.G_ROOM.ID,
                Tables.G_ROOM.NAME
            )
            .from(Tables.G_ROOM)
            .offset(0)
            .limit(CacheSizeConstants.size_524288)
            .apply {
                DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues)).onSuccess {
                    it.map { row ->
                        val roomName = row.getString(Tables.G_ROOM.NAME.name)
                        roomCache[row.getLong(Tables.G_ROOM.ID.name).toString()] = roomName

                        roomNameCache.add(roomName)
                    }
                    println("初始化room缓存完毕")
                }.onFailure {
                    println("初始化room cache失败，cause:$it")
                }
            }
    }
}