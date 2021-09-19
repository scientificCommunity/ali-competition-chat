package org.baichuan.chat.db.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.serviceproxy.ServiceBinder
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.baichuan.chat.db.dao.ManuallyMessageDao
import org.baichuan.chat.db.dao.impl.MessageDaoImpl
import org.jooq.DSLContext

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
class MessageDaoVerticle(private val sqlClient: PgPool, private val sqlCreator: DSLContext) : AbstractVerticle() {
    override fun start() {
        val serviceBinder = ServiceBinder(vertx)
        serviceBinder.setAddress(ServiceAddressConstants.MESSAGE_DAO_ADDRESS)
            .register(ManuallyMessageDao::class.java, MessageDaoImpl(sqlClient, sqlCreator))
    }
}