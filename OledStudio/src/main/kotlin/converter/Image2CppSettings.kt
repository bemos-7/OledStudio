package converter

enum class Background {
    WHITE,
    BLACK
}

enum class Scaling {
    ORIGINAL,
    KEEP_RATIO,
    STRETCH,
    STRETCH_HORIZONTAL,
    STRETCH_VERTICAL
}

enum class Rotation {
    NONE,
    DEG_90,
    DEG_180,
    DEG_270
}

enum class Dithering {
    NONE,
    BINARY,
    BAYER,
    FLOYD_STEINBERG,
    ATKINSON
}

data class Image2CppSettings(
    val width: Int = 128,
    val height: Int = 64,

    val background: Background = Background.WHITE,

    val threshold: Int = 128,

    val invert: Boolean = false,

    val scaling: Scaling = Scaling.KEEP_RATIO,

    val centerHorizontal: Boolean = true,
    val centerVertical: Boolean = true,

    val rotation: Rotation = Rotation.NONE,

    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,

    val dithering: Dithering = Dithering.NONE
)