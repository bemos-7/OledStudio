package ui.tool_bar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

val MacClose = Color(0xFFFF5F56)
val MacMinimize = Color(0xFFFFBD2E)
val MacMaximize = Color(0xFF27C93F)
val ToolBar = Color(0xFF11161B)
val Border = Color(0xFF303030)

@Composable
fun WindowScope.CustomWindowToolBar(
    title: String,
    windowState: WindowState,
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentWindow = this.window
    val windowShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(windowShape)
            .border(width = 1.dp, color = Border, shape = windowShape)
    ) {
        WindowDraggableArea {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(ToolBar),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp).weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WindowControlButton(color = MacClose, onClick = onCloseClick)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
    }
}

@Composable
private fun WindowControlButton(
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()

    val buttonColor by animateColorAsState(
        targetValue = if (isHovered.value) color.copy(alpha = 0.8f) else color,
        label = ""
    )

    Box(
        modifier = Modifier
            .size(15.dp)
            .clip(CircleShape)
            .background(buttonColor)
            .hoverable(interactionSource)
            .clickable (
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    )
}