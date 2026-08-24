package ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OledPreview(
    bytes: List<Int>,
    modifier: Modifier = Modifier
) {

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(250)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101419),
                        Color(0xFF080A0D)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF2A3038),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(
                    1.dp,
                    Color(0xFF343A42),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            ) {

                val pixelWidth =
                    size.width / 128f

                val pixelHeight =
                    size.height / 64f

                for (y in 0 until 64) {

                    for (x in 0 until 128) {

                        val byteIndex =
                            y * 16 + x / 8

                        if (byteIndex >= bytes.size) {
                            continue
                        }

                        val bit =
                            7 - (x % 8)

                        val isOn =
                            (bytes[byteIndex] and (1 shl bit)) != 0

                        if (isOn) {

                            /*
                             * Лёгкое свечение пикселя.
                             */
                            drawRect(
                                color = Color(
                                    red = 0.78f,
                                    green = 0.95f,
                                    blue = 0.86f,
                                    alpha = alpha
                                ),
                                topLeft = Offset(
                                    x * pixelWidth,
                                    y * pixelHeight
                                ),
                                size = Size(
                                    pixelWidth + 0.4f,
                                    pixelHeight + 0.4f
                                )
                            )
                        }
                    }
                }
            }

            Text(
                text = "128 × 64",
                color = Color(0xFF69727D),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            )
        }
    }
}