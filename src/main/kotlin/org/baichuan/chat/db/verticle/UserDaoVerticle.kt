package org.baichuan.chat.db.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.serviceproxy.ServiceBinder
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.baichuan.chat.db.dao.UserDao
import org.baichuan.chat.db.dao.impl.UserDaoImpl
import org.jooq.DSLContext

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
class UserDaoVerticle(private val sqlClient: PgPool, private val sqlCreator: DSLContext) : AbstractVerticle() {
    override fun start() {
        val serviceBinder = ServiceBinder(vertx)
        serviceBinder.setAddress(ServiceAddressConstants.USER_DAO_ADDRESS)
            .register(UserDao::class.java, UserDaoImpl(sqlClient, sqlCreator))
    }
}