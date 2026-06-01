package moe.nea.frobchat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import moe.nea.frobchat.build.BuildConfig

fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = BuildConfig.BRAND,
        state = windowState,
    ) {
        App()
    }
}