package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import converter.Background
import converter.Dithering
import converter.Image2CppConverter
import converter.Image2CppSettings
import converter.Rotation
import converter.Scaling
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import storage.DisplayStorage
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private val BackgroundColor = Color(0xFF080A0D)
private val CardColor = Color(0xFF101419)
private val CardColor2 = Color(0xFF14191F)
private val BorderColor = Color(0xFF252B33)
private val PrimaryColor = Color(0xFF8BE9B2)
private val TextPrimary = Color(0xFFE7EBEF)
private val TextSecondary = Color(0xFF858E99)

@Composable
fun App() {

    val scope = rememberCoroutineScope()

    var sourceImage by remember {
        mutableStateOf(
            DisplayStorage.currentSourceImage
        )
    }

    var bitmapBytes by remember {
        mutableStateOf(
            DisplayStorage.currentBytes
        )
    }

    var settings by remember {
        mutableStateOf(
            Image2CppSettings()
        )
    }

    var status by remember {
        mutableStateOf("Ready")
    }
    /*
     * LIVE PROCESSING
     */
    LaunchedEffect(
        sourceImage,
        settings
    ) {

        val image = sourceImage ?: return@LaunchedEffect

        bitmapBytes =
            Image2CppConverter.convert(
                source = image,
                settings = settings
            )

        status = "Preview updated"
    }

    MaterialTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 28.dp,
                        vertical = 22.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                Header(
                    hasImage = sourceImage != null,
                    status = status
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(18.dp)
                ) {

                    /*
                     * LEFT
                     */
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(18.dp)
                    ) {

                        SectionTitle(
                            title = "Source",
                            subtitle =
                                "Original image"
                        )

                        SourceCard(
                            image = sourceImage,
                            onOpen = {
                                scope.launch(Dispatchers.IO) {

                                    val file =
                                        FileKit.pickFile(
                                            type = PickerType.Image
                                        )

                                    if (file == null) {
                                        return@launch
                                    }

                                    try {
                                        val bytes =
                                            file.readBytes()

                                        val image =
                                            ImageIO.read(
                                                bytes.inputStream()
                                            )

                                        if (image != null) {
                                            sourceImage = image
                                            DisplayStorage.saveSourceImage(image)
                                            status = "Image loaded"
                                        }

                                    } catch (
                                        e: Exception
                                    ) {

                                        e.printStackTrace()

                                        status =
                                            "Failed to open image"
                                    }
                                }
                            }
                        )
                    }

                    /*
                     * RIGHT
                     */
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(18.dp)
                    ) {

                        SectionTitle(
                            title = "OLED Preview",
                            subtitle =
                                "What will be sent to ESP"
                        )

                        OledPreview(
                            bytes = bitmapBytes
                        )
                    }
                }

                /*
                 * PROCESSING
                 */

                SectionTitle(
                    title = "Processing",
                    subtitle =
                        "Image2cpp-style conversion"
                )

                ProcessingCard(
                    settings = settings,
                    onSettingsChange = {
                        settings = it
                    }
                )

                /*
                 * BOTTOM BAR
                 */

                BottomBar(
                    hasImage = sourceImage != null,
                    status = status,
                    onSend = {
                        val image = sourceImage ?: return@BottomBar

                        DisplayStorage.save(
                            bytes = bitmapBytes,
                            sourceImage = image
                        )
                        status = "Image saved & sent to ESP"
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun Header(
    hasImage: Boolean,
    status: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "OLED Studio",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text =
                    "Image converter for ESP8266 • SSD1306",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        StatusIndicator(
            active = hasImage,
            text = status
        )
    }
}

@Composable
private fun StatusIndicator(
    active: Boolean,
    text: String
) {

    val color by animateColorAsState(
        if (active)
            PrimaryColor
        else
            Color(0xFF59616B),
        animationSpec = tween(250)
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Color(0xFF11161B)
            )
            .border(
                1.dp,
                BorderColor,
                RoundedCornerShape(50)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(color)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = text,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {

    Column {

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SourceCard(
    image: BufferedImage?,
    onOpen: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(CardColor)
            .border(
                1.dp,
                BorderColor,
                RoundedCornerShape(18.dp)
            )
            .clickable(
                onClick = onOpen
            )
            .padding(20.dp),
        contentAlignment =
            Alignment.Center
    ) {

        if (image == null) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "＋",
                    color = PrimaryColor,
                    fontSize = 36.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Drop an image here",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "PNG • JPG • BMP • WEBP",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

        } else {

            SourceImagePreview(
                image = image
            )
        }
    }
}

@Composable
private fun SourceImagePreview(
    image: BufferedImage
) {

    /*
     * Пока здесь используем простой
     * текстовый индикатор.
     *
     * Следующим шагом можно сделать
     * полноценный Compose Image.
     */

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "IMAGE LOADED",
            color = PrimaryColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "${image.width} × ${image.height}",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Click to replace",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProcessingCard(
    settings: Image2CppSettings,
    onSettingsChange: (Image2CppSettings) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(CardColor)
            .border(
                1.dp,
                BorderColor,
                RoundedCornerShape(18.dp)
            )
            .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {

        /*
         * THRESHOLD
         */

        Column {

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Threshold",
                    color = TextPrimary,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = settings.threshold.toString(),
                    color = PrimaryColor,
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Slider(
                value =
                    settings.threshold.toFloat(),

                onValueChange = {

                    onSettingsChange(
                        settings.copy(
                            threshold =
                                it.toInt()
                        )
                    )
                },

                valueRange = 0f..255f,

                colors =
                    SliderDefaults.colors(
                        thumbColor =
                            PrimaryColor,

                        activeTrackColor =
                            PrimaryColor,

                        inactiveTrackColor =
                            Color(0xFF303740)
                    )
            )
        }

        /*
         * ROW 1
         */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            SettingDropdown(
                modifier =
                    Modifier.weight(1f),

                title = "Background",

                value =
                    when (settings.background) {

                        Background.WHITE ->
                            "White"

                        Background.BLACK ->
                            "Black"
                    },

                options = listOf(
                    "White",
                    "Black"
                ),

                onSelect = { value ->

                    onSettingsChange(
                        settings.copy(
                            background =
                                if (
                                    value == "White"
                                ) {
                                    Background.WHITE
                                } else {
                                    Background.BLACK
                                }
                        )
                    )
                }
            )

            SettingDropdown(
                modifier =
                    Modifier.weight(1f),

                title = "Scaling",

                value =
                    when (settings.scaling) {

                        Scaling.ORIGINAL ->
                            "Original"

                        Scaling.KEEP_RATIO ->
                            "Keep ratio"

                        Scaling.STRETCH ->
                            "Stretch"

                        Scaling.STRETCH_HORIZONTAL ->
                            "Stretch X"

                        Scaling.STRETCH_VERTICAL ->
                            "Stretch Y"
                    },

                options = listOf(
                    "Original",
                    "Keep ratio",
                    "Stretch",
                    "Stretch X",
                    "Stretch Y"
                ),

                onSelect = { value ->

                    val scaling =
                        when (value) {

                            "Original" ->
                                Scaling.ORIGINAL

                            "Keep ratio" ->
                                Scaling.KEEP_RATIO

                            "Stretch" ->
                                Scaling.STRETCH

                            "Stretch X" ->
                                Scaling.STRETCH_HORIZONTAL

                            else ->
                                Scaling.STRETCH_VERTICAL
                        }

                    onSettingsChange(
                        settings.copy(
                            scaling = scaling
                        )
                    )
                }
            )
        }

        /*
         * CHECKBOXES
         */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            ToggleSetting(
                title = "Center X",
                checked =
                    settings.centerHorizontal,

                onCheckedChange = {

                    onSettingsChange(
                        settings.copy(
                            centerHorizontal =
                                it
                        )
                    )
                }
            )

            ToggleSetting(
                title = "Center Y",
                checked =
                    settings.centerVertical,

                onCheckedChange = {

                    onSettingsChange(
                        settings.copy(
                            centerVertical =
                                it
                        )
                    )
                }
            )

            ToggleSetting(
                title = "Invert",
                checked =
                    settings.invert,

                onCheckedChange = {

                    onSettingsChange(
                        settings.copy(
                            invert = it
                        )
                    )
                }
            )
        }

        /*
         * ROW 2
         */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            SettingDropdown(
                modifier =
                    Modifier.weight(1f),

                title = "Rotation",

                value =
                    when (settings.rotation) {

                        Rotation.NONE ->
                            "0°"

                        Rotation.DEG_90 ->
                            "90°"

                        Rotation.DEG_180 ->
                            "180°"

                        Rotation.DEG_270 ->
                            "270°"
                    },

                options = listOf(
                    "0°",
                    "90°",
                    "180°",
                    "270°"
                ),

                onSelect = { value ->

                    val rotation =
                        when (value) {

                            "90°" ->
                                Rotation.DEG_90

                            "180°" ->
                                Rotation.DEG_180

                            "270°" ->
                                Rotation.DEG_270

                            else ->
                                Rotation.NONE
                        }

                    onSettingsChange(
                        settings.copy(
                            rotation = rotation
                        )
                    )
                }
            )

            SettingDropdown(
                modifier =
                    Modifier.weight(1f),

                title = "Dithering",

                value =
                    when (settings.dithering) {

                        Dithering.NONE ->
                            "None"

                        Dithering.BINARY ->
                            "Binary"

                        Dithering.BAYER ->
                            "Bayer"

                        Dithering.FLOYD_STEINBERG ->
                            "Floyd-Steinberg"

                        Dithering.ATKINSON ->
                            "Atkinson"
                    },

                options = listOf(
                    "None",
                    "Binary",
                    "Bayer",
                    "Floyd-Steinberg",
                    "Atkinson"
                ),

                onSelect = { value ->

                    val dithering =
                        when (value) {

                            "Binary" ->
                                Dithering.BINARY

                            "Bayer" ->
                                Dithering.BAYER

                            "Floyd-Steinberg" ->
                                Dithering.FLOYD_STEINBERG

                            "Atkinson" ->
                                Dithering.ATKINSON

                            else ->
                                Dithering.NONE
                        }

                    onSettingsChange(
                        settings.copy(
                            dithering =
                                dithering
                        )
                    )
                }
            )
        }

        /*
         * FLIPS
         */

        Row {

            ToggleSetting(
                title = "Flip X",
                checked =
                    settings.flipHorizontal,

                onCheckedChange = {

                    onSettingsChange(
                        settings.copy(
                            flipHorizontal = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            ToggleSetting(
                title = "Flip Y",
                checked =
                    settings.flipVertical,

                onCheckedChange = {

                    onSettingsChange(
                        settings.copy(
                            flipVertical = it
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange =
                onCheckedChange,

            colors =
                androidx.compose.material.CheckboxDefaults.colors(
                    checkedColor =
                        PrimaryColor,

                    uncheckedColor =
                        Color(0xFF4B535D),

                    checkmarkColor =
                        Color(0xFF0A0D10)
                )
        )

        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingDropdown(
    modifier: Modifier,
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
    ) {

        Text(
            text = title,
            color = TextSecondary,
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Box {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .background(CardColor2)
                    .border(
                        1.dp,
                        BorderColor,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        expanded = true
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "⌄",
                    color = TextSecondary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                options.forEach { option ->

                    DropdownMenuItem(
                        onClick = {

                            onSelect(option)

                            expanded = false
                        }
                    ) {

                        Text(
                            text = option
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    hasImage: Boolean,
    status: String,
    onSend: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(CardColor)
            .border(
                1.dp,
                BorderColor,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    if (hasImage)
                        PrimaryColor
                    else
                        Color(0xFF525A65)
                )
        )

        Spacer(
            modifier = Modifier.width(9.dp)
        )

        Text(
            text = status,
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            enabled = hasImage,

            onClick = onSend,

            shape =
                RoundedCornerShape(10.dp),

            colors =
                ButtonDefaults.buttonColors(
                    backgroundColor =
                        PrimaryColor,

                    contentColor =
                        Color(0xFF08100C),

                    disabledBackgroundColor =
                        Color(0xFF252B31),

                    disabledContentColor =
                        Color(0xFF656D77)
                )
        ) {

            Text(
                text = "Send to ESP  →",
                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}