package org.baichuan.chat.service.verticle

import io.vertx.reactivex.core.AbstractVerticle
import org.baichuan.chat.service.RedisFactory

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/27
 */
class RedisVerticle : AbstractVerticle() {
    override fun start() {
        RedisFactory.init(vertx)
    }
}