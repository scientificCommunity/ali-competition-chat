package org.baichuan.chat.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.api.service.WebApiServiceGen
import io.vertx.ext.web.impl.LRUCache
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.bean.dto.CreateUserDTO
import org.baichuan.chat.commons.constants.CacheSizeConstants
import org.baichuan.chat.commons.extension.badRequest
import org.baichuan.chat.db.DbHolder
import org.baichuan.chat.db.dao.RoomDaoFactory
import org.baichuan.chat.jooq.Tables
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/8
 */
@WebApiServiceGen
interface UserService {
    fun createUser(
        body: CreateUserDTO, context: ServiceRequest, resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun loginUser(
        username: String,
        password: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    fun getUserByName(
        username: String,
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    )

    /**
     * 初始化用户信息缓存
     */
    fun initUserCache(
        context: ServiceRequest,
        resultHandler: Handler<AsyncResult<ServiceResponse>>
    )
}

object UserLocalCache {
    private val log: Logger = LoggerFactory.getLogger(UserLocalCache::class.java)

    /**
     * key username
     * value userDto
     */
    val usernameUserCache = LRUCache<String, JsonObject>(CacheSizeConstants.size_524288)

    /**
     * key username
     * value userId
     */
    val usernameUserIdCache = HashMap<String, Long>(2 shl 15)

    fun initCache(vertx: Vertx, resultHandler: Handler<AsyncResult<ServiceResponse>>) {
        initUserCache()
        initRoomCache(vertx, resultHandler)
    }

    fun initUserCache() {
        DbHolder.sqlCreator!!
            .select(
                Tables.G_USER.USERNAME,
                Tables.G_USER.PASSWORD,
                Tables.G_USER.FIRSTNAME.`as`("firstName"),
                Tables.G_USER.LASTNAME.`as`("lastName"),
                Tables.G_USER.EMAIL,
                Tables.G_USER.PHONE
            )
            .from(Tables.G_USER)
            .offset(0)
            .limit(CacheSizeConstants.size_524288)
            .apply {
                DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues)).onSuccess {
                    it.map { row ->
                        usernameUserCache[row.getString(Tables.G_USER.USERNAME.name)] = row.toJsonObject()
                    }
                    println("初始化用户缓存完毕")
                }.onFailure {
                    println("初始化用户缓存失败，cause:$it")
                }
            }
    }

    fun initRoomCache(vertx: Vertx, resultHandler: Handler<AsyncResult<ServiceResponse>>) {
        RoomDaoFactory.getProxy(vertx).rxListRooms(1, CacheSizeConstants.size_65536).subscribe({
            it.forEach { json ->
                RoomServiceLocalCache.roomCache[json.getString("id")] = json.getString("name")
            }
        }, {
            log.error("init room cache failed.cause:", it)
            resultHandler.badRequest()
        })
    }
}