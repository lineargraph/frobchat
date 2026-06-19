package moe.nea.frobchat.views.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import moe.nea.frobchat.build.BuildConfig
import moe.nea.frobchat.config.findPreference
import moe.nea.frobchat.layouts.CenterColumn
import moe.nea.frobchat.matrixapi.MatrixAuthentication
import moe.nea.frobchat.matrixapi.models.Login2
import moe.nea.frobchat.matrixapi.models.UserIdentifier
import moe.nea.frobchat.matrixapi.operations.Login
import moe.nea.frobchat.util.FrobRoute
import moe.nea.frobchat.util.findGlobalNavController
import moe.nea.frobchat.util.getHostname
import moe.nea.frobchat.util.matrix.awaitResult
import moe.nea.frobchat.util.matrix.createMatrixThinClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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
				MatrixAuthentication.anon(
					"https://${server.text}/" // TODO: parse potential existing https url
				)
			)
			val wellKnown = client.getWellknown().awaitResult()
			val homeServerBaseUrl = wellKnown.getOrNull()
			if (homeServerBaseUrl != null) {
				setHomeServerState(HomeServerState.SUCCESS(homeServerBaseUrl.mHomeserver().baseUrl()))
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
			MatrixAuthentication.anon(homeserver)
		)
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
						client.login(
							Login.Body(
								Login2(
									null,
									deviceId,
									run {
										UserIdentifier("m.id.user").asJson()
											.let {
												// TODO: Yeah i need ergonomics for this lol
												it as JsonObject
												it.addProperty("user", username.text)
												UserIdentifier.fromJson(it)
											}
									},
									"FrobChat ${getHostname()}",
									null,
									password.text,
									null,
									null,
									"m.login.password",
									null
								)
							)
						).awaitResult().fold({ login ->
							logger.info { "Successful login: $login!" }
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
