package moe.nea.frobchat.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import moe.nea.frobchat.util.compose.upgrade
import kotlin.coroutines.coroutineContext

val globalPreferencesProvider =
	staticCompositionLocalOf<Preferences> {
		error("Global Navigation Scope not provided")
	}

@Composable
@ReadOnlyComposable
fun findGlobalPreferences(): Preferences {
	return globalPreferencesProvider.current
}

@Composable
fun <T> findPreference(selectPreference: Preferences.() -> DataValue<T>): MutableState<T> {
	val prefs = findGlobalPreferences()
	val pref = selectPreference(prefs)
	return pref.asState().upgrade(pref::set)
}


@Composable
fun InjectPreferenceProvider(content: @Composable () -> Unit) {
	CompositionLocalProvider(globalPreferencesProvider provides Preferences(DataStore)) {
		content()
	}
}
