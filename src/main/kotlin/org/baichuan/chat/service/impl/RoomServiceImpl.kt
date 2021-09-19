package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.bean.dto.CreateRoomDTO
import org.baichuan.chat.bean.dto.ListRoomsDTO
import org.baichuan.chat.commons.constants.CacheSizeConstants
import org.baichuan.chat.commons.extend.*
import org.baichuan.chat.commons.utils.whisper.ObfuscatorHolder
import org.baichuan.chat.db.DbHolder
import org.baichuan.chat.db.DbHolder.sqlClient
import org.baichuan.chat.db.DbHolder.sqlCreator
import org.baichuan.chat.db.dao.RoomDaoFactory
import org.baichuan.chat.jooq.Tables.G_ROOM
import org.baichuan.chat.service.RoomService
import org.baichuan.chat.service.RoomServiceLocalCache
import org.baichuan.chat.service.RoomServiceLocalCache.roomNameCache

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
class RoomServiceImpl(private val vertx: Vertx) : RoomService {

    override fun createRoom(
        body: CreateRoomDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        sqlCreator!!
            .insertInto(G_ROOM, G_ROOM.NAME)
            .values(body.name)
            .returning(G_ROOM.ID)
            .executeMustHaveOneResult(resultHandler) {
                val id = it.getLong(G_ROOM.ID.name).toString()
                resultHandler.plainTextSucceed(id)
                RoomServiceLocalCache.roomCache[id] = body.name
            }
    }

    override fun enterRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val roomId = roomid.toLong()

        //先判断有没有这个room
        sqlCreator!!.select(G_ROOM.ID)
            .from(G_ROOM)
            .where(G_ROOM.ID.eq(roomId))
            .limit(1)
            .apply {
                sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
                    .onSuccess {
                        if (it.size() <= 0) {
                            resultHandler.badRequest()
                        } else {
                            val username = context.user.getString("username")
                            RoomServiceLocalCache.usernameRoomIdCache[username] = roomId
                            RoomServiceLocalCache.put(username, roomId)

                            if (RoomServiceLocalCache.roomIdUsernameCache.containsKey(roomId)) {
                                RoomServiceLocalCache.roomIdUsernameCache[roomId]!!.add(username)
                            } else {
                                RoomServiceLocalCache.roomIdUsernameCache[roomId] = mutableSetOf(username)
                            }
                            resultHandler.plainTextSucceed()
                        }
                    }
            }
    }

    override fun getRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        if (RoomServiceLocalCache.roomCache.containsKey(roomid)) {
            resultHandler.plainTextSucceed(RoomServiceLocalCache.roomCache[roomid])
        } else {
            sqlCreator!!
                .select(G_ROOM.NAME)
                .from(G_ROOM)
                .where(G_ROOM.ID.eq(roomid.toLong()))
                .executeMustHaveOneResult(resultHandler) {
                    resultHandler.plainTextSucceed(it.getString(G_ROOM.NAME.name))
                }
        }
    }

    override fun getUserListFromRoom(
        roomid: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val roomId: Long?
        try {
            roomId = roomid.toLong()
        } catch (e: Exception) {
            resultHandler.badRequest()
            return
        }

        if (!RoomServiceLocalCache.roomIdUsernameCache.containsKey(roomId)) {
            resultHandler.badRequest()
        }

        resultHandler.plainTextSucceed(
            RoomServiceLocalCache.roomIdUsernameCache[roomId]
        )
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

    override fun listRooms(
        body: ListRoomsDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val startIndex = (body.pageIndex!! - 1) * body.pageSize!!
        if (startIndex < roomNameCache.size) {
            val jsonArray = JsonArray()

            var i = startIndex
            while (i < startIndex + body.pageSize!! && i < roomNameCache.size) {
                jsonArray.add(roomNameCache[i])
                i++
            }

            resultHandler.jsonArraySucceed(jsonArray)
            return
        }

        sqlCreator!!.select(G_ROOM.NAME)
            .from(G_ROOM)
            .offset((body.pageIndex!! - 1) * body.pageSize!!)
            .limit(body.pageSize!!)
            .executeWithJsonList(resultHandler)
    }

    override fun initRoomCache(context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>) {
        sqlCreator!!
            .select(
                G_ROOM.ID,
                G_ROOM.NAME
            )
            .from(G_ROOM)
            .offset(0)
            .limit(CacheSizeConstants.size_524288)
            .apply {
                DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues)).onSuccess {
                    it.map { row ->
                        val roomName = row.getString(G_ROOM.NAME.name)
                        RoomServiceLocalCache.roomCache[row.getString(G_ROOM.ID.name)] = roomName

                        roomNameCache.add(roomName)
                    }
                }.onFailure { resultHandler.badRequest() }
            }
    }
}