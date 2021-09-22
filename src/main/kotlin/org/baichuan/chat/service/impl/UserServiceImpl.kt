package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.bean.dto.CreateUserDTO
import org.baichuan.chat.commons.constants.CacheSizeConstants.size_524288
import org.baichuan.chat.commons.extension.*
import org.baichuan.chat.commons.utils.JWTUtils
import org.baichuan.chat.db.DbHolder.sqlClient
import org.baichuan.chat.db.DbHolder.sqlCreator
import org.baichuan.chat.jooq.Tables
import org.baichuan.chat.service.UserLocalCache
import org.baichuan.chat.service.UserService

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
class UserServiceImpl(private val vertx: Vertx) : UserService {

    override fun createUser(
        body: CreateUserDTO, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        sqlCreator!!
            .insertInto(
                Tables.G_USER,
                Tables.G_USER.USERNAME,
                Tables.G_USER.PASSWORD,
                Tables.G_USER.EMAIL,
                Tables.G_USER.FIRSTNAME,
                Tables.G_USER.LASTNAME,
                Tables.G_USER.PHONE
            )
            .values(body.username, body.password, body.email, body.firstName, body.lastName, body.phone)
            //.returning(Tables.G_USER.ID)
            .executeWithEmptyText(resultHandler) {
                resultHandler.plainTextSucceed()
                UserLocalCache.usernameUserCache[body.username] = body.toJson()
                //UserLocalCache.usernameUserIdCache[body.username] = it.getLong(Tables.G_USER.ID.name)
            }
    }

    override fun loginUser(
        username: String,
        password: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        //先从缓存中查找
        if (UserLocalCache.usernameUserCache.containsKey(username)) {
            if (UserLocalCache.usernameUserCache[username]!!.getString(Tables.G_USER.PASSWORD.name).equals(password)) {
                resultHandler.plainTextSucceed(
                    JWTUtils.genToken(
                        username
                    )
                )
            } else {
                resultHandler.badRequest()
            }
        } else {
            sqlCreator!!
                .select(
                    Tables.G_USER.PASSWORD
                )
                .from(Tables.G_USER)
                .where(Tables.G_USER.USERNAME.eq(username))
                .executeMustHaveOneResult(resultHandler) {
                    if (password != it.getString("password")) {
                        resultHandler.badRequest("Invalid username or password")
                    } else {
                        resultHandler.plainTextSucceed(JWTUtils.genToken(username))
                    }
                }
        }
    }

    override fun getUserByName(
        username: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        if (UserLocalCache.usernameUserCache.containsKey(username)) {
            val result = UserLocalCache.usernameUserCache[username]!!
            result.remove("password")
            result.remove("username")
            resultHandler.jsonSucceed(result)
            return
        }

        sqlCreator!!
            .select(
                Tables.G_USER.FIRSTNAME.`as`("firstName"),
                Tables.G_USER.LASTNAME.`as`("lastName"),
                Tables.G_USER.EMAIL,
                Tables.G_USER.PHONE
            )
            .from(Tables.G_USER)
            .where(Tables.G_USER.USERNAME.eq(username))
            .executeWithJson(resultHandler)
    }

    override fun initUserCache(context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>) {
        sqlCreator!!
            .select(
                Tables.G_USER.USERNAME,
                Tables.G_USER.FIRSTNAME.`as`("firstName"),
                Tables.G_USER.LASTNAME.`as`("lastName"),
                Tables.G_USER.EMAIL,
                Tables.G_USER.PHONE
            )
            .from(Tables.G_USER)
            .offset(0)
            .limit(size_524288)
            .apply {
                sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues)).onSuccess {
                    it.map { row ->
                        UserLocalCache.usernameUserCache[row.getString(Tables.G_USER.USERNAME.name)] =
                            row.toJsonObject()
                    }
                }.onFailure { resultHandler.badRequest() }
            }
    }
}