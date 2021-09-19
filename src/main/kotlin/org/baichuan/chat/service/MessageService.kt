package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen
import io.vertx.ext.web.impl.ConcurrentLRUCache
import org.baichuan.chat.bean.dto.MessageRetrieveDTO
import org.baichuan.chat.bean.dto.MessageSendDTO
import org.baichuan.chat.commons.constants.CacheSizeConstants.size_64
import org.baichuan.chat.commons.constants.CacheSizeConstants.size_65536
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/15
 */
@WebApiServiceGen
interface MessageService {
    fun sendMessage(
        body: MessageSendDTO, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun retrieve(
        body: MessageRetrieveDTO, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )
}

object MessageCache {
    val newlyMessageCache = ConcurrentLRUCache<Long, ArrayDeque<JsonObject>>(size_64)

    /**
     * newlyMessageCache对每个房间已缓存消息数
     */
    val roomIdMessageCacheCount = ConcurrentHashMap<Long, Int>()

    /**
     * 每个房间总的消息数，用于正向缓存
     */
    val roomIdMessageTotalCount = ConcurrentHashMap<Long, Int>()

    const val singleRoomMessageCacheLimit = size_65536
    const val pageLimit = size_65536 / 20
    const val pageLimitNag = size_65536 / 20 * -1
}