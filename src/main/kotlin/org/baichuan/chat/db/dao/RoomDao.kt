package org.baichuan.chat.db.dao

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.baichuan.chat.commons.constants.ServiceAddressConstants

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/11
 */
@ProxyGen
@VertxGen
interface RoomDao {
    @Fluent
    fun listRooms(pageIndex: Int, pageSize: Int, resultHandler: Handler<AsyncResult<List<JsonObject>>>): RoomDao

    @Fluent
    fun insert(
        roomName: String,
        resultHandler: Handler<AsyncResult<Long>>
    ): RoomDao

    @Fluent
    fun fetchOneById(
        roomId: Long,
        resultHandler: Handler<AsyncResult<JsonObject>>
    ): RoomDao
}

object RoomDaoFactory {
    private var proxy: org.baichuan.chat.reactivex.db.dao.RoomDao? = null

    fun getProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.RoomDao {
        if (proxy == null) {
            synchronized(RoomDaoFactory::class.java) {
                if (proxy == null) {
                    proxy = createProxy(vertx)
                }
            }
        }
        return proxy!!
    }

    private fun createProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.RoomDao {
        return org.baichuan.chat.reactivex.db.dao.RoomDao(
            RoomDaoVertxEBProxy(
                vertx,
                ServiceAddressConstants.ROOM_DAO_ADDRESS
            )
        )
    }
}