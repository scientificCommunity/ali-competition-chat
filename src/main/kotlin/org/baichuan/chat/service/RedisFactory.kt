package org.baichuan.chat.service

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.redis.client.Redis
import io.vertx.reactivex.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions

object RedisFactory {
    var redis: RedisAPI? = null

    fun init(vertx: Vertx) {
        val redisOptions = RedisOptions()
            .setConnectionString("redis://localhost:6379/0")
            .setMaxPoolSize(16)
            .setMaxWaitingHandlers(40960)
            .setMaxPoolWaiting(40960)

        val client = Redis.createClient(
            vertx,
            redisOptions
        )
            .connect()
        redis = RedisAPI.api(client)
    }

    fun getClient(): RedisAPI {
        return redis!!
    }

}

fun main() {
    RedisFactory.init(Vertx.vertx())
    RedisFactory.getClient().rxGet("aa1").subscribe({
        println(it.toString())
    }, {}, {
        println(123)
    })
}