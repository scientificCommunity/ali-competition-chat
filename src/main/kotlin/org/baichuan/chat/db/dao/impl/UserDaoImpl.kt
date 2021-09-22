package org.baichuan.chat.db.dao.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.sqlclient.Pool
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.bean.dto.CreateUserDTO
import org.baichuan.chat.db.dao.UserDao
import org.baichuan.chat.jooq.Tables.G_USER
import org.jooq.DSLContext

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/18
 */
class UserDaoImpl(private val sqlClient: Pool, private val sqlCreator: DSLContext) : UserDao {
    override fun insert(data: CreateUserDTO, resultHandler: Handler<AsyncResult<Long>>): UserDao {
        sqlCreator
            .insertInto(
                G_USER,
                G_USER.USERNAME,
                G_USER.PASSWORD,
                G_USER.EMAIL,
                G_USER.FIRSTNAME,
                G_USER.LASTNAME,
                G_USER.PHONE
            )
            .values(data.username, data.password, data.email, data.firstName, data.lastName, data.phone)
            .returning(G_USER.ID)
            .apply {
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .onSuccess { rowSet ->
                        rowSet.map {
                            resultHandler.handle(Future.succeededFuture(it.getLong(G_USER.ID.name)))
                        }
                    }
            }
        return this
    }

    override fun fetchOneByUsername(username: String, resultHandler: Handler<AsyncResult<JsonObject>>): UserDao {
        sqlCreator
            .select(G_USER.fields().asList())
            .from(G_USER)
            .where(G_USER.USERNAME.eq(username))
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

    override fun listUsers(offset: Int, limit: Int, resultHandler: Handler<AsyncResult<List<JsonObject>>>): UserDao {
        sqlCreator
            .select(G_USER.USERNAME, G_USER.PASSWORD, G_USER.ID)
            .from(G_USER)
            .offset(offset)
            .limit(limit)
            .apply {
                sqlClient.delegate.preparedQuery(sql).executeTemplate(Tuple.tuple(bindValues), resultHandler)
                    .onSuccess {
                        resultHandler.handle(Future.succeededFuture(it.toJsonObjects()))
                    }
            }

        return this
    }
}