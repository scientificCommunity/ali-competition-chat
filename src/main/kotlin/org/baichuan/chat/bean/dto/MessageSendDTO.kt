package org.baichuan.chat.bean.dto

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/15
 */
@DataObject(generateConverter = true, publicConverter = false)
class MessageSendDTO() {
    /**
     * room id
     */
    lateinit var id: String
    lateinit var text: String

    constructor(json: JsonObject) : this() {
        MessageSendDTOConverter.fromJson(json, this)
    }

    fun toJson(): JsonObject = JsonObject().also { MessageSendDTOConverter.toJson(this, it) }
}