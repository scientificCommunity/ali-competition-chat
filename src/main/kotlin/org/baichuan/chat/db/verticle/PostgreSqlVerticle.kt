package org.baichuan.chat.db.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.pgclient.PgConnectOptions
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import org.baichuan.chat.db.DbHolder
import org.baichuan.chat.service.RoomServiceLocalCache
import org.baichuan.chat.service.UserLocalCache
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.ParamType
import org.jooq.conf.Settings
import org.jooq.impl.DSL

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
class PostgreSqlVerticle : AbstractVerticle() {
    override fun start() {
        val connectOptions = PgConnectOptions()
            .setPort(5432)
            .setHost("xxx")
            .setDatabase("game")
            .setUser("admin")
            .setPassword("xxx")
        val poolOptions = PoolOptions()
            .setMaxSize(32)

        val sqlClient = PgPool.pool(io.vertx.reactivex.core.Vertx(vertx), connectOptions, poolOptions)
        val sqlCreator: DSLContext = DSL.using(
            SQLDialect.POSTGRES,
            Settings().withRenderFormatted(true)
                .withParamType(ParamType.NAMED)
                .withRenderNamedParamPrefix("$")
        )

        DbHolder.init(sqlClient, sqlCreator)

        UserLocalCache.initUserCache()
        RoomServiceLocalCache.initRoomCache()
    }
}