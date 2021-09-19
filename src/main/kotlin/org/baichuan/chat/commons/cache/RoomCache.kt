package org.baichuan.chat.commons.cache

object RoomCache {
    private val userRoomEnterCache = HashMap<Long, Long>()
    private val roomUsersEnterCache = HashMap<Long, MutableSet<String>>()


}