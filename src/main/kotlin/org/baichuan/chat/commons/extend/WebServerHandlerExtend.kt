package org.baichuan.chat.commons.extend

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceResponse

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
class WebServerHandlerExtend {

}

fun <T> Single<T>.subscribeTemplate(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    onSuccess: Consumer<in T>
) {
    this.subscribe({
        try {
            onSuccess.accept(it)
        } catch (e: Exception) {
            resultHandler.badRequest()
        }
    }, {
        resultHandler.badRequest()
    })
}


fun <T> Handler<AsyncResult<ServiceResponse>>.plainTextSucceed(result: T) {
    this.handle(
        Future.succeededFuture(
            succeedServiceResponse()
                .putHeader(
                    HttpHeaderNames.CONTENT_TYPE.toString(),
                    HttpHeaderValues.TEXT_PLAIN.toString()
                )
                .setPayload(Buffer.buffer(result.toString()))
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.jsonSucceed(result: JsonObject) {
    this.handle(
        Future.succeededFuture(
            succeedServiceResponse()
                .putHeader(
                    HttpHeaderNames.CONTENT_TYPE.toString(),
                    HttpHeaderValues.APPLICATION_JSON.toString()
                )
                .setPayload(result.toBuffer())
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.jsonArraySucceed(result: JsonArray) {
    this.handle(
        Future.succeededFuture(
            succeedServiceResponse()
                .putHeader(
                    HttpHeaderNames.CONTENT_TYPE.toString(),
                    HttpHeaderValues.APPLICATION_JSON.toString()
                )
                .setPayload(result.toBuffer())
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.jsonArraySucceed(jsonArrayString: String) {
    this.handle(
        Future.succeededFuture(
            succeedServiceResponse()
                .putHeader(
                    HttpHeaderNames.CONTENT_TYPE.toString(),
                    HttpHeaderValues.APPLICATION_JSON.toString()
                )
                .setPayload(Buffer.buffer(jsonArrayString))
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.jsonListSucceed(result: List<JsonObject>) {
    this.handle(
        Future.succeededFuture(
            succeedServiceResponse()
                .putHeader(
                    HttpHeaderNames.CONTENT_TYPE.toString(),
                    HttpHeaderValues.APPLICATION_JSON.toString()
                )
                .setPayload(JsonArray(result).toBuffer())
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.plainTextSucceed() {
    this.plainTextSucceed("")
}

fun Handler<AsyncResult<ServiceResponse>>.badRequest() {
    this.handle(
        Future.succeededFuture(
            badRequestServiceResponse()
        )
    )
}

fun Handler<AsyncResult<ServiceResponse>>.badRequest(msg: String) {
    this.handle(
        Future.succeededFuture(
            badRequestServiceResponse(msg)
        )
    )
}

fun succeedServiceResponse(): ServiceResponse {
    return buildServiceResponse(HttpResponseStatus.OK.code())
}

fun badRequestServiceResponse(): ServiceResponse {
    return buildServiceResponse(HttpResponseStatus.BAD_REQUEST.code())
}

fun badRequestServiceResponse(msg: String): ServiceResponse {
    return buildServiceResponse(HttpResponseStatus.BAD_REQUEST.code(), msg)
}

fun buildServiceResponse(statusCode: Int): ServiceResponse {
    return ServiceResponse().setStatusCode(statusCode)
}

fun buildServiceResponse(statusCode: Int, msg: String): ServiceResponse {
    return ServiceResponse().setStatusCode(statusCode).setPayload(Buffer.buffer(msg))
}