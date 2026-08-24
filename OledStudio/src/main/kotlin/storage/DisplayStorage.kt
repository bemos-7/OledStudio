package storage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object DisplayStorage {
    var currentBytes by mutableStateOf<List<Int>>(List(1024) { 0 })
    var currentSourceImage: BufferedImage? = null

    private const val APP_FOLDER_NAME = "OledStudio"
    private const val FILE_NAME = "saved_logo.dat"

    private const val BYTES_FILE_NAME =
        "saved_logo.dat"

    private const val IMAGE_FILE_NAME =
        "source_image.png"

    private val appDirectory: File
        get() {

            val os =
                System.getProperty("os.name")
                    .lowercase()

            val userHome =
                System.getProperty("user.home")

            val baseDir =
                when {

                    os.contains("win") -> {
                        System.getenv("APPDATA")
                            ?.let { File(it) }
                            ?: File(
                                userHome,
                                "AppData/Roaming"
                            )
                    }

                    os.contains("mac") -> {
                        File(
                            userHome,
                            "Library/Application Support"
                        )
                    }

                    else -> {
                        File(
                            userHome,
                            ".config"
                        )
                    }
                }

            return File(
                baseDir,
                APP_FOLDER_NAME
            ).also {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }

    private val bytesFile: File
        get() =
            File(
                appDirectory,
                BYTES_FILE_NAME
            )

    private val imageFile: File
        get() =
            File(
                appDirectory,
                IMAGE_FILE_NAME
            )

    fun saveLocally(
        bytes: List<Int>
    ) {
        try {
            bytesFile.writeBytes(
                bytes
                    .map { it.toByte() }
                    .toByteArray()
            )
            currentBytes = bytes
            println(
                "OLED bytes saved to: " +
                        bytesFile.absolutePath
            )
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    fun save(
        bytes: List<Int>,
        sourceImage: BufferedImage
    ) {
        saveLocally(bytes)
        saveSourceImage(sourceImage)
    }

    fun saveSourceImage(
        image: BufferedImage
    ) {
        try {
            ImageIO.write(
                image,
                "png",
                imageFile
            )
            currentSourceImage = image
            println(
                "Source image saved to: " +
                        imageFile.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadLocally() {
        try {
            if (bytesFile.exists()) {
                val bytes =
                    bytesFile
                        .readBytes()
                        .map {
                            it.toInt() and 0xFF
                        }
                if (bytes.size == 1024) {

                    currentBytes = bytes
                }
            }
            if (imageFile.exists()) {
                currentSourceImage =
                    ImageIO.read(imageFile)
            }
            println(
                "OLED Studio storage loaded"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
