package org.baichuan.chat.service.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import org.apache.commons.lang3.StringUtils
import org.baichuan.chat.bean.dto.MessageRetrieveDTO
import org.baichuan.chat.bean.dto.MessageSendDTO
import org.baichuan.chat.commons.extension.jsonArraySucceed
import org.baichuan.chat.commons.extension.plainTextSucceed
import org.baichuan.chat.commons.utils.FileUtils
import org.baichuan.chat.service.MessageService
import org.baichuan.chat.service.RoomServiceLocalCache

/**
 * @author: tk (rivers.boat.snow@gmail.com)
 * @date: 2021/7/15
 * 直接写磁盘
 */
class MessageServiceImpl(val vertx: Vertx) : MessageService {

    override fun sendMessage(
        body: MessageSendDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val startTime = System.currentTimeMillis()
        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)

        val toJson = body.toJson()
        toJson.put("timestamp", System.currentTimeMillis())

        vertx.executeBlocking<Unit> {
            //提交任务
            FileUtils.writeTask(toJson.toString(), roomId)
        }

        resultHandler.plainTextSucceed()
        println("send msg cost is ${System.currentTimeMillis() - startTime}")
    }

    private fun listBatchFetch(list: ArrayDeque<JsonObject>, offset: Int, limit: Int): JsonArray {
        val size = list.size

        //数据索引
        var index = (offset - 1) * limit
        var cursor = 1
        if (offset < 0) {
            index = size - 1 + (offset + 1) * limit
            cursor = -1
        }

        //循环批次
        var i = 0

        //定义返回结果
        val result = JsonArray()
        while (i < limit) {
            if (index == size || index < 0) {
                return result
            }

            result.add(list[index])
            index += cursor
            i++
        }
        return result
    }

    override fun retrieve(
        body: MessageRetrieveDTO,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    ) {
        val username = context.user.getString("username")
        val roomId = RoomServiceLocalCache.getRoomIdByUsername(username, resultHandler)
        if (body.pageIndex == 0) {
            resultHandler.jsonArraySucceed(JsonArray())
            return
        }

        vertx.executeBlocking<Unit> {
            val msg = FileUtils.readMsg(roomId, body.pageIndex!!, body.pageSize!!)

            resultHandler.jsonArraySucceed("[${StringUtils.trim(msg)}]")
        }
    }
}