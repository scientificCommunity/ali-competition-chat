package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 * redis版
 */
@WebApiServiceGen
interface UserService {
    fun createUser(
        body: JsonObject, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun loginUser(
        username: String,
        password: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun getUserByName(
        username: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    )
}