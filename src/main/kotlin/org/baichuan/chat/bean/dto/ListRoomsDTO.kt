package org.baichuan.chat.bean.dto

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/9
 */
@DataObject(generateConverter = true, publicConverter = false)
class ListRoomsDTO() : PageDTO() {
    constructor(json: JsonObject) : this() {
        ListRoomsDTOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { ListRoomsDTOConverter.toJson(this, it) }
}