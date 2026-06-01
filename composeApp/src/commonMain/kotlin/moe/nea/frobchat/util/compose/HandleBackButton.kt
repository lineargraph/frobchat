package moe.nea.frobchat.util.compose

import androidx.compose.ui.Modifier
import moe.nea.frobchat.util.TypedNavHostController


expect fun Modifier.handleBackButton(navController: TypedNavHostController<*>): Modifier
