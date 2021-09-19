package org.baichuan.chat.service.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.serviceproxy.ServiceBinder
import org.baichuan.chat.commons.constants.ServiceAddressConstants
import org.baichuan.chat.service.RoomService
import org.baichuan.chat.service.impl.RoomServiceImpl

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/20
 */
class RoomServiceVerticle : AbstractVerticle() {
    override fun start() {
        val serviceBinder = ServiceBinder(vertx)
        serviceBinder.setAddress(ServiceAddressConstants.ROOM_SERVICE_ADDRESS)
            .register(RoomService::class.java, RoomServiceImpl(vertx))
    }
}