package org.baichuan.chat.bean.dto

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import org.baichuan.chat.jooq.tables.pojos.GUser

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
@DataObject(generateConverter = true, publicConverter = false)
class CreateUserDTO() {
    lateinit var username: String
    lateinit var password: String
    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var email: String
    lateinit var phone: String

    constructor(json: JsonObject) : this() {
        CreateUserDTOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { CreateUserDTOConverter.toJson(this, it) }

    fun genGUser(): GUser {
        val gUser = GUser()
        gUser.username = username
        gUser.password = password
        gUser.firstname = firstName
        gUser.lastname = lastName
        gUser.email = email
        gUser.phone = phone
        return gUser
    }
}