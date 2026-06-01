package moe.nea.frobchat.util


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.oshai.kotlinlogging.KotlinLogging
import moe.nea.frobchat.config.findPreference
import moe.nea.frobchat.util.compose.handleBackButton
import moe.nea.frobchat.views.screens.WelcomePage

// TODO: implement https://github.com/adrielcafe/voyager/issues/497
interface FrobRoute : Screen {
    fun ownsPage(page: FrobRoute): Boolean {
        return page == this
    }
}

val globalNavigationLocal =
    staticCompositionLocalOf<TypedNavHostController<FrobRoute>> { error("Global Navigation Scope not provided") }

class TypedNavHostController<T : Screen>(
    val navigator: Navigator,
) {
    fun navigate(page: T) {
        navigator.push(page)
    }

    fun goBack() {
        navigator.pop()
    }

    /**
     * **SAFETY**: this is safe assuming only navigate is ever called instead of navigator directly
     */
    @Suppress("UNCHECKED_CAST")
    val currentScreen get() = navigator.lastItem as T
}

@Composable
fun findGlobalNavController(): TypedNavHostController<FrobRoute> {
    return globalNavigationLocal.current
}

private val logger = KotlinLogging.logger { }

@Composable
fun NavigationContext() {
    Navigator(WelcomePage) { nav ->
        val globalNavController = TypedNavHostController<FrobRoute>(nav)
        Column(
            modifier = Modifier.fillMaxSize()
                .handleBackButton(globalNavController)
        ) { // TODO: is a column really the best container?
            CompositionLocalProvider(globalNavigationLocal provides globalNavController) {
                CurrentScreen()
            }
        }
    }
}