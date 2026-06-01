package moe.nea.frobchat.views.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import moe.nea.frobchat.build.BuildConfig
import moe.nea.frobchat.layouts.CenterColumn
import moe.nea.frobchat.util.FrobRoute
import moe.nea.frobchat.util.findGlobalNavController

@Serializable
object WelcomePage : FrobRoute {
	@Composable
	override fun Content() {
		val nav = findGlobalNavController()
		CenterColumn {
			Text("Welcome to ${BuildConfig.BRAND}", fontSize = 20.sp)
			Text("Test Page, Please add more content")
			Button(onClick = {
				println("Button that does nothing!")
			}) {
				Text("Continue")
			}
		}
	}
}
