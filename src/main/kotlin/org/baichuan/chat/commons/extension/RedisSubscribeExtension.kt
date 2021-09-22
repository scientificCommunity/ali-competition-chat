package org.baichuan.chat.commons.extension

import io.reactivex.Maybe
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.reactivex.redis.client.Response

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/27
 */
class RedisSubscribeExtend {

}

fun <T : Response> Maybe<T>.subscribeTemplate(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    onSuccess: Consumer<in T>
) {
    this.subscribe({
        onSuccess.accept(it)
    }, {
        resultHandler.badRequest()
    }, {
        resultHandler.badRequest()
    })
}

fun <T : Response> Maybe<T>.subscribeTemplate(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    onSuccess: Consumer<in T>,
    onComplete: Action
) {
    this.subscribe({
        onSuccess.accept(it)
    }, {
        resultHandler.badRequest()
    }, {
        onComplete.run()
    })
}

fun <T : Response> Maybe<T>.subscribeTemplate(
) {
    this.subscribe({
    }, {
        println("do nothing executed failed, cause:$it")
    }, {
    })
}