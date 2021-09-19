package org.baichuan.chat.db.dao

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.baichuan.chat.bean.dto.CreateUserDTO
import org.baichuan.chat.commons.constants.ServiceAddressConstants

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/11
 */
@ProxyGen
@VertxGen
interface UserDao {
    @Fluent
    fun insert(
        data: CreateUserDTO,
        resultHandler: Handler<AsyncResult<Long>>
    ): UserDao

    @Fluent
    fun fetchOneByUsername(
        username: String,
        resultHandler: Handler<AsyncResult<JsonObject>>
    ): UserDao

    @Fluent
    fun listUsers(
        offset: Int,
        limit: Int,
        resultHandler: Handler<AsyncResult<List<JsonObject>>>
    ): UserDao
}

object UserDaoFactory {
    private var proxy: org.baichuan.chat.reactivex.db.dao.UserDao? = null

    fun getProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.UserDao {
        if (proxy == null) {
            synchronized(UserDaoFactory::class.java) {
                if (proxy == null) {
                    proxy = createProxy(vertx)
                }
            }
        }
        return proxy!!
    }

    private fun createProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.UserDao {
        return org.baichuan.chat.reactivex.db.dao.UserDao(
            UserDaoVertxEBProxy(
                vertx,
                ServiceAddressConstants.USER_DAO_ADDRESS
            )
        )
    }
}