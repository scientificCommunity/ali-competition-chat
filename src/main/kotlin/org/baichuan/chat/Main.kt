package org.baichuan.chat

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.openapi.RouterBuilder
import org.baichuan.chat.commons.holder.JWTHolder
import org.baichuan.chat.commons.utils.whisper.ObfuscatorHolder
import org.baichuan.chat.db.verticle.PostgreSqlVerticle
import org.baichuan.chat.service.verticle.ServiceVerticle
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author: tk
 * @since: 2020/12/23
 */
class Main : AbstractVerticle() {
    private lateinit var server: HttpServer
    private val log = LoggerFactory.getLogger(Main::class.java)

    override fun start() {
        vertx.deployVerticle(
            PostgreSqlVerticle::class.java,
            DeploymentOptions().setConfig(config().getJsonObject("db"))
        )
        vertx.deployVerticle(ServiceVerticle())

        startHttpServer()
    }

    private fun startHttpServer() {
        // Generate the router
        val allowedHeaders = HashSet<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("X-UID")
        allowedHeaders.add("X-RequestId")
        allowedHeaders.add("X-Timestamp")
        allowedHeaders.add("X-Signature")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("token")

        val allowedMethods = HashSet<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)
        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)

        val globalHandler = CorsHandler.create(".*")
            .allowedHeaders(allowedHeaders)
            .allowedMethods(allowedMethods)
            .allowCredentials(true)

        val mainRouter = Router.router(vertx)
        mainRouter.route("/*")
            .handler(globalHandler)
            //.handler(TimeCostHandler())

        JWTHolder.init(vertx)
        ObfuscatorHolder.init()

        val httpServerOptions = HttpServerOptions()
//            .setSsl(true)
//            .setUseAlpn(true)
//            .setPemKeyCertOptions(
//                PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")
//            )

        RouterBuilder.create(vertx, "src/main/resources/openapi.yaml")
            .onSuccess { routerBuilder ->
                val provider = JWTAuth.create(
                    vertx, JWTAuthOptions()
                        .addPubSecKey(
                            PubSecKeyOptions()
                                .setAlgorithm("HS256")
                                .setBuffer("keyboard cat")
                        )
                )

                routerBuilder.mountServicesFromExtensions()
                routerBuilder.securityHandler("bearerAuth", JWTAuthHandler.create(provider))
                mainRouter.mountSubRouter("/", routerBuilder.createRouter())
                mainRouter.route("/*").handler(StaticHandler.create())
                val port = 8080
                vertx.createHttpServer(httpServerOptions).requestHandler(mainRouter).listen(port, "0.0.0.0")
                    .onComplete {
                        server = it.result()
                    }
                log.info("HTTP server started on port $port | application version is ${System.getProperty("version")}")
            }
            .onFailure { err ->
                println("load openapi failed. err:$err")
            }
    }
}

fun main() {
    val vertxOptions = VertxOptions()
    vertxOptions.eventLoopPoolSize = 64
    Vertx.vertx(vertxOptions).deployVerticle(Main())
}