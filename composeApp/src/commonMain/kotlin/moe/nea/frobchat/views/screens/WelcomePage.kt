package moe.nea.frobchat.views.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PublicOff
import androidx.compose.material.icons.outlined.ReplayCircleFilled
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.classic
import de.connect2x.trixnity.clientserverapi.client.classicLoginWithPassword
import de.connect2x.trixnity.clientserverapi.client.classicLoginWithToken
import de.connect2x.trixnity.clientserverapi.client.unauthenticated
import de.connect2x.trixnity.clientserverapi.model.authentication.IdentifierType
import de.connect2x.trixnity.clientserverapi.model.authentication.LoginType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.Url
import moe.nea.frobchat.build.BuildConfig
import moe.nea.frobchat.config.findPreference
import moe.nea.frobchat.layouts.CenterColumn
import moe.nea.frobchat.util.FrobRoute
import moe.nea.frobchat.util.findGlobalNavController
import moe.nea.frobchat.util.getHostname
import moe.nea.frobchat.util.matrix.createMatrixThinClient
import moe.nea.frobchat.views.components.Throbber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.math.acos
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

@Serializable
object WelcomePage : FrobRoute {
	sealed interface HomeServerState {
		object DEBOUNCED : HomeServerState
		object QUERYING : HomeServerState
		data class SUCCESS(val baseUrl: String) : HomeServerState
		object FAILURE : HomeServerState
	}

	@Composable
	override fun Content() {
		val nav = findGlobalNavController()
		val (server, setServer) = remember { mutableStateOf(TextFieldValue("matrix.org")) }
		val (homeServerState, setHomeServerState) = remember { mutableStateOf<HomeServerState>(HomeServerState.DEBOUNCED) }
		LaunchedEffect(server.text) {
			setHomeServerState(HomeServerState.DEBOUNCED)
			delay(0.6.seconds)
			setHomeServerState(HomeServerState.QUERYING)
			val client = createMatrixThinClient(
				MatrixClientAuthProviderData.unauthenticated(
					Url("https://${server.text}/") // TODO: parse potential existing https url
				)
			)
			val wellKnown = client.discovery.getWellKnown()
			val homeServerBaseUrl = wellKnown.getOrNull()?.homeserver?.baseUrl
			if (homeServerBaseUrl != null) {
				setHomeServerState(HomeServerState.SUCCESS(homeServerBaseUrl))
			} else {
				logger.error(wellKnown.exceptionOrNull()) { "Failed to connect to homeserver at ${server.text}" }
				setHomeServerState(HomeServerState.FAILURE)
			}
		}
		CenterColumn {
			Text("Welcome to ${BuildConfig.BRAND}", fontSize = 20.sp)
			OutlinedTextField(
				server, setServer,
				Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
				singleLine = true,
				leadingIcon = { Icon(Icons.Outlined.Public, "Homeserver") }
			)

			val (icon, description, enabled) = when (homeServerState) {
				HomeServerState.DEBOUNCED -> Triple(Icons.Outlined.HourglassBottom, "Waiting", null)
				HomeServerState.FAILURE -> Triple(Icons.Outlined.PublicOff, "Failed to connect", null)
				HomeServerState.QUERYING -> Triple(Icons.Outlined.ReplayCircleFilled, "Connecting", null)
				is HomeServerState.SUCCESS -> Triple(
					Icons.Outlined.ArrowCircleRight, "Success", {
						nav.navigate(
							LoginPage(
								homeServerState.baseUrl // TODO: this is technically not spec url handling (with the whole dns SRV bullshit and ports)
							)
						)
					})
			}
			Button(
				onClick = {
					if (enabled != null) enabled()
				},
				enabled = enabled != null,
				modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.End)
			) {
				Icon(imageVector = icon, contentDescription = description)
				Text("Log In!")
			}
		}
	}
}

@Serializable
data class LoginPage(val homeserver: String) : FrobRoute {
	enum class LoginState {
		NEUTRAL,
		LOGGING_IN,
		FAILED,
	}

	@Composable
	override fun Content() {
		val nav = findGlobalNavController()
		val client = createMatrixThinClient(
			MatrixClientAuthProviderData.unauthenticated(Url(homeserver))
		)
		val loginTypes = useRequest(homeserver) {
			logger.info { "Requesting login types from $homeserver" }
			client.authentication.getLoginTypes()
				.getOrNull()
				?.filter {
					when (it) {
						is LoginType.Password -> true
						is LoginType.SSO -> it.identityProviders.isNotEmpty()
						else -> false
					}
				}
		}
		val coroutineScope = rememberCoroutineScope()
		val (username, setUsername) = remember { mutableStateOf(TextFieldValue("")) }
		val (password, setPassword) = remember { mutableStateOf(TextFieldValue("")) }
		val (deviceId) = findPreference { deviceId }
		val (loginState, setLoginState) = remember { mutableStateOf(LoginState.NEUTRAL) }
		val interactable = loginState != LoginState.LOGGING_IN
		CenterColumn {
			Text("Logging into $homeserver", style = MaterialTheme.typography.titleMedium)
			OutlinedTextField(
				username, setUsername,
				Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
				singleLine = true,
				leadingIcon = { Icon(Icons.Outlined.AlternateEmail, "Username") }
			)
			OutlinedTextField(
				password, setPassword,
				Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
				singleLine = true,
				leadingIcon = { Icon(Icons.Outlined.Password, "Password") }
			)
			Button(
				onClick = {
					setLoginState(LoginState.NEUTRAL)
					coroutineScope.launch {
						logger.info { "Trying to log in." }
						client.authentication.login(
							identifier = IdentifierType.User(username.text),
							password = password.text,
							deviceId = deviceId,
							initialDeviceDisplayName = "FrobChat ${getHostname()}",
						).fold({ login ->
							logger.info {
								MatrixClientAuthProviderData.classic(
									baseUrl = Url(homeserver),
									accessToken = login.accessToken,
									accessTokenExpiresInMs = login.accessTokenExpiresInMs,
									refreshToken = login.refreshToken
								)
							}
						}, { exc ->
							logger.info(exc) { "Failed to login" }
							setLoginState(LoginState.FAILED)
						})
					}
				},
				enabled = interactable && password.text.isNotEmpty() && username.text.isNotEmpty(),
				modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.End)
			) {
				if (interactable)
					Icon(imageVector = Icons.Filled.ArrowCircleRight, contentDescription = "Log In")
				else
					Icon(imageVector = Icons.Filled.HourglassBottom, contentDescription = "Logging in")
				Text("Log In!")
			}
		}
	}
}

@Composable
fun <T> useRequest(
	key: Any?,
	makeRequest: @DisallowComposableCalls suspend CoroutineScope.() -> T
): T? {
	val (result, setResult) = remember { mutableStateOf<T?>(null) }
	LaunchedEffect(key) {
		setResult(null)
		setResult(makeRequest())
	}
	return result
}
