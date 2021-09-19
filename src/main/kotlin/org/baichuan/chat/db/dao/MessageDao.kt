package org.baichuan.chat.db.dao

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.pgclient.PgPool
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.jooq.DSLContext

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/11
 */
@ProxyGen
@VertxGen
interface ManuallyMessageDao {
    @Fluent
    fun listMessages(
            pageIndex: Int,
            pageSize: Int,
            roomId: Long,
            resultHandler: Handler<AsyncResult<List<JsonObject>>>
    ): ManuallyMessageDao

    @Fluent
    fun insert(
            id: String,
            text: String,
            roomId: Long,
            resultHandler: Handler<AsyncResult<Long>>
    ): ManuallyMessageDao
}

object ManuallyMessageDaoFactory {
    private var proxy: org.baichuan.chat.reactivex.db.dao.ManuallyMessageDao? = null

    fun getProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.ManuallyMessageDao {
        if (proxy == null) {
            synchronized(ManuallyMessageDaoFactory::class.java) {
                if (proxy == null) {
                    proxy = createProxy(vertx)
                }
            }
        }
        return proxy!!
    }

    private fun createProxy(vertx: Vertx): org.baichuan.chat.reactivex.db.dao.ManuallyMessageDao {
        return org.baichuan.chat.reactivex.db.dao.ManuallyMessageDao(
                ManuallyMessageDaoVertxEBProxy(
                        vertx,
                        ServiceAddressConstants.MESSAGE_DAO_ADDRESS
                )
        )
    }
}