package moe.nea.frobchat.config


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class Preferences(val _store: DataStore) {
	val colorTheme =
		_store.createEnumValue(
			"ui.colorTheme", SelectedColorTheme.SYSTEM,
		)


}

interface NamedEnum {
	val userFriendlyName: String
}

enum class SelectedColorTheme(override val userFriendlyName: String) : NamedEnum {
	LIGHT("Light Mode"), DARK("Dark Mode"), SYSTEM("Use System Color Theme"),
	;

	fun resolveToColors(): ColorScheme {
		return when (this) {
			SelectedColorTheme.LIGHT -> lightColorScheme()
			SelectedColorTheme.DARK -> darkColorScheme()
			SelectedColorTheme.SYSTEM -> lightColorScheme() // TODO: fetch system color theme somehow?
		}
	}
}
