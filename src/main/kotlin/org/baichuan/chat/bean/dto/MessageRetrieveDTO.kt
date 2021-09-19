package org.baichuan.chat.bean.dto

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/15
 */
@DataObject(generateConverter = true, publicConverter = false)
class MessageRetrieveDTO() : PageDTO() {
    constructor(json: JsonObject) : this() {
        MessageRetrieveDTOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { MessageRetrieveDTOConverter.toJson(this, it) }
}