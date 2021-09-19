package org.baichuan.chat.bean.vo

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/11
 */
@DataObject(generateConverter = true, publicConverter = false)
class ListRoomsVO() {
    constructor(json: JsonObject) : this() {
        ListRoomsVOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { ListRoomsVOConverter.toJson(this, it) }
}