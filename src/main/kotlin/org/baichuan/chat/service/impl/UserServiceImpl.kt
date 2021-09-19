package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import org.apache.commons.lang3.StringUtils
import org.baichuan.chat.commons.extend.badRequest
import org.baichuan.chat.commons.extend.jsonSucceed
import org.baichuan.chat.commons.extend.plainTextSucceed
import org.baichuan.chat.commons.extend.subscribeTemplate
import org.baichuan.chat.commons.utils.JWTUtils
import org.baichuan.chat.service.RedisFactory
import org.baichuan.chat.service.UserService

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
class UserServiceImpl(private val vertx: Vertx) : UserService {

    override fun createUser(
        body: JsonObject, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val username = body.getString("username")
        RedisFactory.getClient().rxSetnx(username, body.toString()).subscribe({
            resultHandler.plainTextSucceed()
        }, {
            it.printStackTrace()
            resultHandler.badRequest()
        })
    }

    override fun loginUser(
        username: String,
        password: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        if (username.isBlank() || password.isBlank()) {
            resultHandler.badRequest()
        }
        RedisFactory.getClient().rxGet(username).subscribeTemplate(resultHandler) {
            val user = JsonObject(it.toString())
            if (password == user.getString("password")) {
                resultHandler.plainTextSucceed(JWTUtils.genToken(username))
            } else {
                resultHandler.badRequest()
            }
        }
    }

    override fun getUserByName(
        username: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        RedisFactory.getClient().rxGet(username).subscribeTemplate(resultHandler) {
            val user = JsonObject(it.toString())
            user.remove("username")
            user.remove("password")
            resultHandler.jsonSucceed(user)
        }
    }
}