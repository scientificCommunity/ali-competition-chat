package org.baichuan.chat.service.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/20
 */
class ServiceVerticle : AbstractVerticle() {
    override fun start() {
        //因为user跟room用了本地缓存，如果多实例的话需要考虑线程同步
        vertx.deployVerticle(UserServiceVerticle::class.java, DeploymentOptions().setInstances(1))
        vertx.deployVerticle(RoomServiceVerticle::class.java, DeploymentOptions().setInstances(16))

        vertx.deployVerticle(MessageServiceVerticle::class.java, DeploymentOptions().setInstances(32))
    }

    override fun stop() {

    }
}