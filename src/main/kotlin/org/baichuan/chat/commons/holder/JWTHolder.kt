package org.baichuan.chat.commons.holder

import io.vertx.core.Vertx
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions

object JWTHolder {
    private var jwtProvider: JWTAuth? = null
    private var jwtOptions: JWTOptions? = null
    var isInit = false
    fun init(vertx: Vertx) {
        isInit = true
        jwtProvider = JWTAuth.create(
            vertx, JWTAuthOptions()
                .addPubSecKey(
                    PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer("keyboard cat")
                )
        )

        jwtOptions = JWTOptions()
            .setExpiresInMinutes(60 * 24 * 7)
            .setAlgorithm("HS256")
    }

    fun getProvider(): JWTAuth {
        return jwtProvider!!
    }

    fun jwtOptions(): JWTOptions {
        return jwtOptions!!
    }
}