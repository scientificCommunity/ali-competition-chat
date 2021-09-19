package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/15
 */
@WebApiServiceGen
interface MessageService {
    fun sendMessage(
        body: JsonObject, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun retrieve(
        body: JsonObject, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )
}