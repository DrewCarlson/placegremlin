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
import javax.imageio.ImageIO

private val IMAGE_SIZE_RANGE = 1..5000

fun Application.configureRouting() {
    install(AutoHeadResponse)
    val imagesUri = System.getenv("IMAGE_PATH").orEmpty().ifBlank {
        checkNotNull(this::class.java.classLoader.getResource("gremlins")?.file)
    }
    val imageFiles = File(imagesUri).listFiles()

    routing {
        get("/") {
            call.respondText("Load images with https://placegreml.in/{width}/{height}/image.jpg\nExample: https://placegreml.in/200/200/image.jpg")
        }
        get("/{width}/{height}/image.jpg") {
            val width = call.parameters["width"]?.toIntOrNull()
            val height = call.parameters["height"]?.toIntOrNull()

            if ((width == null || height == null) || (width !in IMAGE_SIZE_RANGE || height !in IMAGE_SIZE_RANGE)) {
                call.respond(BadRequest, "Both width and height must be provided and must be integers within $IMAGE_SIZE_RANGE")
                return@get
            }

            if (imageFiles.isNullOrEmpty()) {
                call.respond(InternalServerError, "No images found")
                return@get
            }

            val randomFile = imageFiles.random()
            call.application.log.debug("Serving image: ${randomFile.absolutePath}")

            var image: BufferedImage? = try {
                checkNotNull(ImageIO.read(randomFile))
            } catch (e: Throwable) {
                call.application.log.error("Failed to load image file: ${randomFile.absolutePath}", e)
                call.respond(InternalServerError, "Failed to load image file")
                return@get
            }

            image = try {
                checkNotNull(Scalr.resize(image, QUALITY, FIT_EXACT, width, height))
            } catch (e: Throwable) {
                call.application.log.error("Error while processing image", e)
                call.respond(InternalServerError, "Failed to process and resize image")
                return@get
            }

            call.respondOutputStream(ContentType.Image.JPEG) {
                ImageIO.write(image, "jpg", this)
            }
        }
    }
}
