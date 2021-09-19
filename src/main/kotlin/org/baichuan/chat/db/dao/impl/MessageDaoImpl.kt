package org.baichuan.chat.db.dao.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.sqlclient.Pool
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.commons.extend.executeTemplate
import org.baichuan.chat.commons.extend.toJsonObjects
import org.baichuan.chat.db.dao.ManuallyMessageDao
import org.baichuan.chat.jooq.Tables
import org.jooq.*
import java.time.LocalDateTime

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/15
 */
class MessageDaoImpl(private val sqlClient: Pool, private val sqlCreator: DSLContext) : ManuallyMessageDao {

    override fun listMessages(
        pageIndex: Int,
        pageSize: Int,
        roomId: Long,
        resultHandler: Handler<AsyncResult<List<JsonObject>>>
    ): ManuallyMessageDao {

        var sql = sqlCreator.select(Tables.G_MESSAGE.ID, Tables.G_MESSAGE.TEXT, Tables.G_MESSAGE.TIMESTAMP)
            .from(Tables.G_MESSAGE)
//            .offset((pageIndex - 1) * pageSize)
//            .limit(pageSize)

        var sqlResult: SelectLimitPercentAfterOffsetStep<Record3<String, String, LocalDateTime>>? = null
        sqlResult = if (pageIndex > 0) {
            sql.orderBy(Tables.G_MESSAGE.TIMESTAMP.asc())
                .offset((pageIndex - 1) * pageSize)
                .limit(pageSize)
        } else {
            val newPageIndex = pageIndex * -1
            sql.orderBy(Tables.G_MESSAGE.TIMESTAMP.desc())
                .offset((newPageIndex - 1) * pageSize)
                .limit(pageSize)
        }

        sqlResult.apply {
            //追求更好的性能可以用Tuple.wrap()，但是要注意传入的必须是一个可以修改的list
            sqlClient.delegate.preparedQuery(this.sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                .onSuccess {
                    resultHandler.handle(Future.succeededFuture(it.toJsonObjects()))
                }
        }

        return this
    }

    override fun insert(
        id: String,
        text: String,
        roomId: Long,
        resultHandler: Handler<AsyncResult<Long>>
    ): ManuallyMessageDao {
        sqlCreator.insertInto(
            Tables.G_MESSAGE,
            Tables.G_MESSAGE.ID,
            Tables.G_MESSAGE.ROOM_ID,
            Tables.G_MESSAGE.TEXT
        ).values(id, roomId, text)
            .apply {
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .onSuccess {
                        resultHandler.handle(Future.succeededFuture())
                    }
            }
        return this
    }
}