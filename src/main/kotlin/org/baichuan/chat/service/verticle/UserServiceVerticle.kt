package org.baichuan.chat.service.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.serviceproxy.ServiceBinder
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.baichuan.chat.service.UserService
import org.baichuan.chat.service.impl.UserServiceImpl

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/20
 */
class UserServiceVerticle : AbstractVerticle() {
    override fun start() {
        val serviceBinder = ServiceBinder(vertx)
        serviceBinder.setAddress(ServiceAddressConstants.USER_SERVICE_ADDRESS)
            .register(UserService::class.java, UserServiceImpl(vertx))
    }
}