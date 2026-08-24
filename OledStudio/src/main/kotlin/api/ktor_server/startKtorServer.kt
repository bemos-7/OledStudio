package api.ktor_server
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import storage.DisplayStorage

@Serializable
data class ImageToEsp(
    val bytes: List<Int>
)

fun startKtorServer() {
    embeddedServer(Netty, port = 8006, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("api/esp/image") {
                val image = ImageToEsp(
                    bytes = DisplayStorage.currentBytes
                )
                call.respond(image)
            }
        }
    }.start(wait = false)
}