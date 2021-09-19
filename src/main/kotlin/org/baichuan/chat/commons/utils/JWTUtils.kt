package org.baichuan.chat.commons.utils

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.baichuan.chat.commons.holder.JWTHolder
import org.baichuan.chat.jooq.tables.pojos.GUser

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
object JWTUtils {

    /**
     * 根据用户信息生成token
     * @param user 用户信息
     */
    fun genToken(user: JsonObject): String {
        val jwtClaims = JsonObject()
        jwtClaims.put("jti", "game-chat")
        //jwtClaims.put("id", user.getLong("id"))
        jwtClaims.put("username", user.getString("username"))
        return JWTHolder.getProvider().generateToken(jwtClaims, JWTHolder.jwtOptions())
    }

    /**
     * 根据用户信息生成token
     * @param username 用户名
     */
    fun genToken(username: String): String {
        val jwtClaims = JsonObject()
        jwtClaims.put("jti", "game-chat")
        //jwtClaims.put("id", userId)
        jwtClaims.put("username", username)
        return JWTHolder.getProvider().generateToken(jwtClaims, JWTHolder.jwtOptions())
    }
}

fun main() {
    JWTHolder.init(Vertx.vertx())
    val jwtClaims = JsonObject()
    jwtClaims.put("jti", "game-chat")
    jwtClaims.put("id", 2)
    jwtClaims.put("username", "test")
    val genToken = JWTHolder.getProvider().generateToken(jwtClaims, JWTHolder.jwtOptions())
    println(genToken)
}