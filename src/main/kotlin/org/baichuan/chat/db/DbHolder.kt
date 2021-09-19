package org.baichuan.chat.db

import io.vertx.reactivex.sqlclient.Pool
import org.jooq.DSLContext

object DbHolder {
    var sqlClient: Pool? = null
    var sqlCreator: DSLContext? = null

    fun init(pool: Pool, dslContext: DSLContext) {
        sqlClient = pool
        sqlCreator = dslContext
    }
}