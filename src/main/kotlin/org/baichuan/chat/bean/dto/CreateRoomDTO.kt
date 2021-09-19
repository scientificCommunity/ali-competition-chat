package org.baichuan.chat.bean.dto

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import org.baichuan.chat.jooq.tables.pojos.GRoom

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
@DataObject(generateConverter = true, publicConverter = false)
class CreateRoomDTO() {
    lateinit var name: String

    constructor(json: JsonObject) : this() {
        CreateRoomDTOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { CreateRoomDTOConverter.toJson(this, it) }

    fun genGRoom(): GRoom {
        val gRoom = GRoom()
        gRoom.name = name
        return gRoom
    }
}