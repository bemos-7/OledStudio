package converter

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

object Image2CppConverter {

    fun convert(
        source: BufferedImage,
        settings: Image2CppSettings = Image2CppSettings()
    ): List<Int> {

        val prepared = prepareImage(source, settings)

        return when (settings.dithering) {
            Dithering.NONE ->
                toHorizontal1Bit(
                    prepared,
                    settings.threshold,
                    settings.invert
                )

            Dithering.BINARY ->
                toHorizontal1Bit(
                    prepared,
                    settings.threshold,
                    settings.invert
                )

            Dithering.BAYER ->
                toHorizontal1Bit(
                    prepared,
                    settings.threshold,
                    settings.invert
                )

            Dithering.FLOYD_STEINBERG ->
                toHorizontal1Bit(
                    prepared,
                    settings.threshold,
                    settings.invert
                )

            Dithering.ATKINSON ->
                toHorizontal1Bit(
                    prepared,
                    settings.threshold,
                    settings.invert
                )
        }
    }

    private fun prepareImage(
        source: BufferedImage,
        settings: Image2CppSettings
    ): BufferedImage {

        var image = source

        image = rotate(image, settings.rotation)

        if (settings.flipHorizontal) {
            image = flipHorizontal(image)
        }

        if (settings.flipVertical) {
            image = flipVertical(image)
        }

        val canvas = BufferedImage(
            settings.width,
            settings.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val graphics = canvas.createGraphics()

        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )

        graphics.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )

        when (settings.background) {

            Background.WHITE -> {
                graphics.color = Color.WHITE
                graphics.fillRect(
                    0,
                    0,
                    settings.width,
                    settings.height
                )
            }

            Background.BLACK -> {
                graphics.color = Color.BLACK
                graphics.fillRect(
                    0,
                    0,
                    settings.width,
                    settings.height
                )
            }
        }

        val size = calculateSize(
            image.width,
            image.height,
            settings.width,
            settings.height,
            settings.scaling
        )

        val drawX =
            if (settings.centerHorizontal) {
                (settings.width - size.first) / 2
            } else {
                0
            }

        val drawY =
            if (settings.centerVertical) {
                (settings.height - size.second) / 2
            } else {
                0
            }

        graphics.composite = AlphaComposite.SrcOver

        graphics.drawImage(
            image,
            drawX,
            drawY,
            size.first,
            size.second,
            null
        )

        graphics.dispose()

        return canvas
    }

    private fun calculateSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        scaling: Scaling
    ): Pair<Int, Int> {

        return when (scaling) {

            Scaling.ORIGINAL ->
                sourceWidth to sourceHeight

            Scaling.STRETCH ->
                targetWidth to targetHeight

            Scaling.STRETCH_HORIZONTAL ->
                targetWidth to sourceHeight

            Scaling.STRETCH_VERTICAL ->
                sourceWidth to targetHeight

            Scaling.KEEP_RATIO -> {

                val scaleX =
                    targetWidth.toDouble() / sourceWidth

                val scaleY =
                    targetHeight.toDouble() / sourceHeight

                val scale = minOf(scaleX, scaleY)

                (sourceWidth * scale).roundToInt() to (sourceHeight * scale).roundToInt()
            }
        }
    }

    /**
     * Формат:
     *
     * 128 пикселей по X
     * 8 пикселей = 1 байт
     *
     * 16 байт на строку
     * 64 строки
     *
     * 16 × 64 = 1024 байта
     */
    private fun toHorizontal1Bit(
        image: BufferedImage,
        threshold: Int,
        invert: Boolean
    ): List<Int> {

        val bytesPerRow =
            (image.width + 7) / 8

        val result =
            MutableList(bytesPerRow * image.height) { 0 }

        for (y in 0 until image.height) {

            for (x in 0 until image.width) {

                val rgb =
                    image.getRGB(x, y)

                val red =
                    (rgb shr 16) and 0xFF

                val green =
                    (rgb shr 8) and 0xFF

                val blue =
                    rgb and 0xFF

                val brightness =
                    (
                            red +
                                    green +
                                    blue
                            ) / 3

                var pixelOn =
                    brightness >= threshold

                if (invert) {
                    pixelOn = !pixelOn
                }

                if (pixelOn) {

                    val byteIndex =
                        y * bytesPerRow + x / 8

                    val bit =
                        7 - (x % 8)

                    result[byteIndex] =
                        result[byteIndex] or
                                (1 shl bit)
                }
            }
        }

        return result
    }

    private fun rotate(
        image: BufferedImage,
        rotation: Rotation
    ): BufferedImage {

        if (rotation == Rotation.NONE) {
            return image
        }

        val newWidth =
            if (
                rotation == Rotation.DEG_90 ||
                rotation == Rotation.DEG_270
            ) image.height
            else image.width

        val newHeight =
            if (
                rotation == Rotation.DEG_90 ||
                rotation == Rotation.DEG_270
            ) image.width
            else image.height

        val result = BufferedImage(
            newWidth,
            newHeight,
            BufferedImage.TYPE_INT_ARGB
        )

        val g = result.createGraphics()

        when (rotation) {

            Rotation.DEG_90 -> {
                g.translate(newWidth, 0)
                g.rotate(Math.PI / 2)
            }

            Rotation.DEG_180 -> {
                g.translate(newWidth, newHeight)
                g.rotate(Math.PI)
            }

            Rotation.DEG_270 -> {
                g.translate(0, newHeight)
                g.rotate(-Math.PI / 2)
            }

            Rotation.NONE -> Unit
        }

        g.drawImage(
            image,
            0,
            0,
            null
        )

        g.dispose()

        return result
    }

    private fun flipHorizontal(
        image: BufferedImage
    ): BufferedImage {

        val result = BufferedImage(
            image.width,
            image.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val g = result.createGraphics()

        g.drawImage(
            image,
            image.width,
            0,
            0,
            image.height,
            0,
            0,
            image.width,
            image.height,
            null
        )

        g.dispose()

        return result
    }

    private fun flipVertical(
        image: BufferedImage
    ): BufferedImage {

        val result = BufferedImage(
            image.width,
            image.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val g = result.createGraphics()

        g.drawImage(
            image,
            0,
            image.height,
            image.width,
            0,
            0,
            0,
            image.width,
            image.height,
            null
        )

        g.dispose()

        return result
    }
}