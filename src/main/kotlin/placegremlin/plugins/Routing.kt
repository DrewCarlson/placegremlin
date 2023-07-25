package placegremlin.plugins

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.plugins.autohead.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Method.QUALITY
import org.imgscalr.Scalr.Mode.FIT_EXACT
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

fun Application.configureRouting() {
    install(AutoHeadResponse)
    val imagePath = System.getenv("IMAGE_PATH")
    val imagesUri = if (imagePath.isNullOrBlank()) {
        checkNotNull(this::class.java.classLoader.getResource("gremlins")?.toURI())
    } else {
        URI.create(imagePath)
    }

    routing {
        get("/") {
            call.respondText("Load images with https://placegreml.in/{width}/{height}/image.jpg\nExample: https://placegreml.in/200/200/image.jpg")
        }
        route("/{width}/{height}/image.jpg") {
            handle {
                val width = call.parameters["width"]?.toIntOrNull()
                val height = call.parameters["height"]?.toIntOrNull()

                if (width == null || height == null) {
                    call.respond(BadRequest, "Both width and height must be provided and must be integers")
                    return@handle
                }

                val files = File(imagesUri).listFiles()
                if (files.isNullOrEmpty()) {
                    call.respond(InternalServerError, "No images found")
                    return@handle
                }

                val randomFile = files.random()
                call.application.log.debug("Serving image: ${randomFile.absolutePath}")
                var image: BufferedImage? = ImageIO.read(randomFile)

                checkNotNull(image) {
                    "Failed to load image: ${randomFile.absolutePath}"
                }

                image = Scalr.resize(image, QUALITY, FIT_EXACT, width, height)

                call.respondOutputStream(ContentType.Image.JPEG) {
                    ImageIO.write(image, "jpg", this)
                }
            }
        }
    }
}
