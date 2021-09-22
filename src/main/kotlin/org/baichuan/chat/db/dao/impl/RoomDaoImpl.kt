package org.baichuan.chat.db.dao.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.sqlclient.Pool
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.db.dao.RoomDao
import org.baichuan.chat.jooq.Tables.G_ROOM
import org.jooq.DSLContext

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/11
 */
class RoomDaoImpl(private val sqlClient: Pool, private val sqlCreator: DSLContext) : RoomDao {
    override fun listRooms(
        pageIndex: Int,
        pageSize: Int,
        resultHandler: Handler<AsyncResult<List<JsonObject>>>
    ): RoomDao {
        sqlCreator.select(G_ROOM.fields().asList())
            .from(G_ROOM)
            .offset((pageIndex - 1) * pageSize)
            .limit(pageSize)
            .apply {
                //追求更好的性能可以用Tuple.wrap()，但是要注意传入的必须是一个可以修改的list
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .onSuccess {
                        resultHandler.handle(Future.succeededFuture(it.toJsonObjects()))
                    }
            }

        return this
    }

    override fun insert(roomName: String, resultHandler: Handler<AsyncResult<Long>>): RoomDao {
        sqlCreator
            .insertInto(G_ROOM, G_ROOM.NAME)
            .values(roomName)
            .returning(G_ROOM.ID)
            .apply {
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .onSuccess { rowSet ->
                        rowSet.map {
                            resultHandler.handle(Future.succeededFuture(it.getLong(G_ROOM.ID.name)))
                        }
                    }
            }
        return this
    }

    override fun fetchOneById(roomId: Long, resultHandler: Handler<AsyncResult<JsonObject>>): RoomDao {
        sqlCreator
            .select(G_ROOM.fields().asList())
            .from(G_ROOM)
            .where(G_ROOM.ID.eq(roomId))
            .apply {
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .map { rowSet ->
                        val firstOrNull = rowSet.firstOrNull()
                        if (firstOrNull == null) {
                            resultHandler.handle(Future.failedFuture("none found"))
                            return@map
                        } else {
                            resultHandler.handle(Future.succeededFuture(firstOrNull.toJsonObject()))
                        }
                    }
            }
        return this
    }

}