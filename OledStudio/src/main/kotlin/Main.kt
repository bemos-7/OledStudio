import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import api.ktor_server.startKtorServer
import storage.DisplayStorage
import ui.App
import ui.tool_bar.CustomWindowToolBar
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.RoundRectangle2D
import kotlin.system.exitProcess

fun main() = application {
    var isWindowVisible by remember {
        mutableStateOf(true)
    }
    DisplayStorage.loadLocally()
    val trayIcon = painterResource("trayIcon.png")
    Tray(
        icon = trayIcon,
        tooltip = "OledStudio",
        menu = {
            Item(text = "Open", onClick = { isWindowVisible = true })
            Item(text = "Exit", onClick = { exitProcess(0) })
        }
    )
    val windowState = rememberWindowState(
        width = 800.dp,
        height = 1135.dp
    )
    remember { startKtorServer() }
    if (isWindowVisible) {
        Window(
            onCloseRequest = { isWindowVisible = false },
            title = "OLED Studio",
            icon = painterResource("trayIcon.png"),
            state = windowState,
            undecorated = true,
            resizable = false
        ) {
            LaunchedEffect(Unit) {
                window.addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent?) {
                        val cornerRadius = 24.0

                        window.shape = RoundRectangle2D.Double(
                            0.0,
                            0.0,
                            window.width.toDouble(),
                            window.height.toDouble(),
                            cornerRadius,
                            cornerRadius
                        )
                    }
                })
            }

            CustomWindowToolBar(
                title = "OLED Studio",
                windowState = windowState,
                onCloseClick = { isWindowVisible = false }
            ) {
                App()
            }
        }
    }
}
