package moe.nea.frobchat.config


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class Preferences(val _store: DataStore) {
	val colorTheme =
		_store.createEnumValue(
			"ui.colorTheme", SelectedColorTheme.SYSTEM,
		)

	val _deviceId =
		_store.createStringValue("meta.deviceId")

	val deviceId = object : DataValue<String> {
		fun ensureInit() {
			if (_deviceId.getCurrent() == "")
				_deviceId.set(UUID.randomUUID().toString())
		}

		override fun getCurrent(): String {
			ensureInit()
			return _deviceId.getCurrent()
		}

		override fun get(): Flow<String> {
			ensureInit()
			return _deviceId.get()
		}

		override fun set(value: String) {
			_deviceId.set(value)
		}
	}
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
