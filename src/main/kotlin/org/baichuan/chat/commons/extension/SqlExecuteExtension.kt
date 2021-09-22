package org.baichuan.chat.commons.extension

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.Tuple
import org.baichuan.chat.db.DbHolder
import org.jooq.*
import java.util.function.Consumer

fun <T, P> PreparedQuery<P>.executeTemplate(params: Tuple, resultHandler: Handler<AsyncResult<T>>): Future<P> {
    return this.execute(params).onFailure {
        resultHandler.handle(Future.failedFuture(it))
    }
}

fun <T : Record> InsertOnDuplicateStep<T>.executeWithEmptyText(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    consumer: Consumer<Unit>
) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            resultHandler.badRequest()
        }.onSuccess {
            resultHandler.plainTextSucceed()
            consumer.accept(Unit)
        }
}

fun <T : Record> SelectConditionStep<T>.executeWithPlainText(resultHandler: Handler<AsyncResult<ServiceResponse>>) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            resultHandler.badRequest()
        }
        .map { rowSet ->
            val firstOrNull = rowSet.firstOrNull()
            if (firstOrNull == null) {
                resultHandler.badRequest()
                return@map
            } else {
                resultHandler.plainTextSucceed(firstOrNull.toJsonObject())
            }
        }
}

fun <T : Record> SelectConditionStep<T>.executeWithJson(resultHandler: Handler<AsyncResult<ServiceResponse>>) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            resultHandler.badRequest()
        }
        .map { rowSet ->
            val firstOrNull = rowSet.firstOrNull()
            if (firstOrNull == null) {
                resultHandler.badRequest()
                return@map
            } else {
                resultHandler.jsonSucceed(firstOrNull.toJsonObject())
            }
        }
}

fun <T : Record> SelectLimitPercentAfterOffsetStep<T>.executeWithJsonList(resultHandler: Handler<AsyncResult<ServiceResponse>>) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            resultHandler.badRequest()
        }
        .map { rowSet ->
            resultHandler.jsonListSucceed(rowSet.toJsonObjects())
        }
}

fun <T : Record> SelectLimitPercentAfterOffsetStep<T>.executeWithJsonList(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    consumer: Consumer<List<JsonObject>>
) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            resultHandler.badRequest()
        }
        .map { rowSet ->
            val result = rowSet.toJsonObjects()
            resultHandler.jsonListSucceed(result)
            consumer.accept(result)
        }
}

fun <T : Record> SelectConditionStep<T>.executeMustHaveOneResult(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    consumer: Consumer<JsonObject>
) {
    doExecute(sql, bindValues, resultHandler, consumer)
}

fun <T : Record> InsertResultStep<T>.executeMustHaveOneResult(
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    consumer: Consumer<JsonObject>
) {
    doExecute(sql, bindValues, resultHandler, consumer)
}

private inline fun doExecute(
    sql: String,
    bindValues: List<Any>,
    resultHandler: Handler<AsyncResult<ServiceResponse>>,
    consumer: Consumer<JsonObject>
) {
    DbHolder.sqlClient!!.delegate.preparedQuery(sql).execute(Tuple.tuple(bindValues))
        .onFailure {
            println("error when execute sql,sql is$sql,cause is $it")
            resultHandler.badRequest()
        }
        .map { rowSet ->
            val firstOrNull = rowSet.firstOrNull()
            if (firstOrNull == null) {
                println("unExpect result when execute sql,sql is$sql")
                resultHandler.badRequest()
                return@map
            } else {
                consumer.accept(firstOrNull.toJsonObject())
            }
        }
}
