package me.tripp.application

import FeedEntry
import GenerateReq
import GenerateResponse
import PatchReq
import PatchResp
import Piece
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.BodyProgress.Plugin.install
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/fullstack-dalle-frontend.js") {}
//        script(src = "/static/main.js") {}

//        div {
//            id="feed"
//        }
    }
}
suspend fun handle(req: FeedEntry) {
    logger.info("handle: $req")
    val piece = Piece(req.prompt!!)
    val images = try { work(piece) } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
    logger.info("got ${images.size} images for $req")
    req.images = images
    val response = Json.encodeToString(req)
    feed.add(req)
    persistState()
    feedSessions.forEach { session ->
        session.outgoing.trySend(Frame.Text(response))
    }
}
fun persistState() {
    val state = Json.encodeToString(feed)
    logger.info("persistState()")
    persistenceFile.writeText(state)
}
fun restorePersistedState() {
    if(persistenceFile.length() == 0L) {
        logger.info("state is empty")
        return
    }
    val state = Json.decodeFromString<List<FeedEntry>>(persistenceFile.readText())
    state.filter{!it.images.isNullOrEmpty()}.forEach {
        feed.add(it)
    }
}
suspend fun subscribeToFeed(session: DefaultWebSocketServerSession) {
    logger.info("subscribeToFeed(), {}", session)
    feed
        .filter{!it.images.isNullOrEmpty()}
        .forEach{ feedEntry ->
            val response = Json.encodeToString(feedEntry)
            logger.info("subscribeToFeed() sending feed entry from {}", feedEntry.ts)
            session.outgoing.send(Frame.Text(response))
        }
    feedSessions.add(session)
    try {
        for(frame in session.incoming) {
            logger.info("received message from feed, ignoring")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }finally {
        logger.info("Closing session")
    }
}
suspend fun subscribeToPatch(session: DefaultWebSocketSession) {
    patchSessions.add(session)
    try {
        for(frame in session.incoming) {
            when(frame) {
                is Frame.Text -> handlePatch(frame.readText())
                else -> {}
            }

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
suspend fun handlePatch(frameText: String) {
    //apply patch and push to all patch sessions
    val req = try { Json.decodeFromString<PatchReq>(frameText) } catch (e :Exception) {e.printStackTrace(); throw e}//kabooom


    val feedEntry = feed.first { it.ts == req.itemTs } //kabooom
    when(req.patchAction) {
        PatchAction.LIKE -> {
            feedEntry.likes = 1 + (feedEntry.likes ?: 0)
        }
        else -> {}
    }
    persistState()
    val patchResponse = Json.encodeToString(PatchResp(req.itemTs, feedEntry))
    patchSessions.forEach { sess ->
        sess.outgoing.send(Frame.Text(patchResponse))
    }

}
fun main() {
    restorePersistedState()

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            webSocket("/prompt") {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val request: FeedEntry = Json.decodeFromString(text)
                            handle(request)
                        }
                        else -> {
                            println("Frame: " + frame.frameType)
                        }
                    }
                }
            }
            webSocket("/feed") {
                subscribeToFeed(this)
            }
            webSocket("/patch") {
                subscribeToPatch(this)

            }
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}
private val persistenceFile = File("state.json")
private val logger = LoggerFactory.getLogger("server")
private val feedSessions = Collections.synchronizedList<DefaultWebSocketSession>(mutableListOf())
private val patchSessions = Collections.synchronizedList<DefaultWebSocketSession>(mutableListOf())
private val feed = mutableListOf<FeedEntry>()

private val client = HttpClient() {
    install(HttpTimeout) {
        requestTimeoutMillis = 180_000
    }
}
suspend fun work(piece: Piece): List<String> {
    val response: String = client.post("https://backend.craiyon.com/generate") {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(GenerateReq(piece.text)))
    }.bodyAsText()
    val r: GenerateResponse = Json.decodeFromString(response)
    return r.images
}
