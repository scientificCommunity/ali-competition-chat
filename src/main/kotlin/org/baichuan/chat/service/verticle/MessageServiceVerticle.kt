package org.baichuan.chat.service.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.serviceproxy.ServiceBinder
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.baichuan.chat.service.MessageService
import org.baichuan.chat.service.RoomService
import org.baichuan.chat.service.UserService
import org.baichuan.chat.service.impl.MessageServiceImpl
import org.baichuan.chat.service.impl.MessageServiceImplDisk
import org.baichuan.chat.service.impl.RoomServiceImpl
import org.baichuan.chat.service.impl.UserServiceImpl

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/20
 */
class MessageServiceVerticle : AbstractVerticle() {
    override fun start() {
        val serviceBinder = ServiceBinder(vertx)
        serviceBinder.setAddress(ServiceAddressConstants.MESSAGE_SERVICE_ADDRESS)
            .register(MessageService::class.java, MessageServiceImplDisk(vertx))
    }
}