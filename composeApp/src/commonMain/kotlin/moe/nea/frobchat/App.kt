package moe.nea.frobchat

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import moe.nea.frobchat.config.InjectPreferenceProvider
import moe.nea.frobchat.config.findPreference
import moe.nea.frobchat.util.NavigationContext
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {

    InjectPreferenceProvider {
        val (darkMode) = findPreference { colorTheme }
        MaterialTheme(colorScheme = darkMode.resolveToColors()) {
            NavigationContext()
        }
    }
}