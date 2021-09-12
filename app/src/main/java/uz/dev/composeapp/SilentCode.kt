package uz.dev.composeapp

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.RelocationRequester
import androidx.compose.ui.layout.relocationRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
private fun Modifier.relocate(bounds: Rect? = null): Modifier =
    composed {
        val ime = LocalWindowInsets.current.ime
        val scope = rememberCoroutineScope()
        val relocationRequester = remember { RelocationRequester() }
        this
            .relocationRequester(relocationRequester)
            .onFocusEvent { state ->
                if (state.isFocused) {
                    scope.launch {
                        delay(150)
                        while (ime.animationInProgress) {
                            delay(30)
                        }
                        Log.d("Relocation", "relocating $bounds")
                        relocationRequester.bringIntoView(rect = bounds)
                    }
                }
            }
    }

data class AuthState(
    val phoneNumber: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthenticationContent() {
    var state by remember { mutableStateOf(AuthState()) }
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.label_auth_title)) }
            )
        },
        content = { paddings ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
                    .padding( horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    text = stringResource(id = R.string.label_greeting),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold)
                )

                Text(
                    modifier = Modifier
                        .height(height = 48.dp)
                        .padding(top = 4.dp),
                    text = stringResource(id = R.string.label_enter_to_continue),
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(40.dp))

//                var visibleArea by remember { mutableStateOf<Rect?>(null) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusable()
                        .relocate()
                       /* .onGloballyPositioned {
                            visibleArea = it.size
                                .toSize()
                                .toRect()
                        }*/,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val clearPhoneButton: @Composable (() -> Unit)? =
                        if (state.phoneNumber.isEmpty()) null
                        else {
                            {
                                IconButton(
                                    onClick = { state = state.copy(phoneNumber = "") },
                                    content = { Icon(Icons.Default.Clear, null) }
                                )
                            }
                        }

                    TextField(
                        modifier = Modifier
//                            .relocate(/*visibleArea*/)
                            .padding(top = 32.dp),
                        value = state.phoneNumber,
                        onValueChange = { phoneNumber ->
                            state = state.copy(phoneNumber = phoneNumber)
                        },
                        label = { Text(stringResource(id = R.string.label_phone_number)) },
                        placeholder = { Text(stringResource(id = R.string.label_phone_number_placeholder)) },
                        trailingIcon = clearPhoneButton,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )

                    Spacer(modifier = Modifier.height(100.dp))

                    TextField(
                        modifier = Modifier
//                            .relocate(/*visibleArea*/)
                            .padding(top = 16.dp),
                        value = state.password,
                        onValueChange = { password ->
                            state = state.copy(password = password)
                        },
                        label = { Text(stringResource(id = R.string.label_password)) },
                        placeholder = { Text(stringResource(id = R.string.label_password_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = when (state.showPassword) {
                            true -> VisualTransformation.None
                            false -> PasswordVisualTransformation()
                        }
                    )

                    Button(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .fillMaxWidth(),
                        content = { Text(stringResource(id = R.string.label_login)) },
                        onClick = {
                            // some logic to sign in
                        }
                    )

                    OutlinedButton(
                        modifier = Modifier
//								.navigationBarsWithImePadding()
                            .padding(top = 16.dp),
                        content = { Text(stringResource(id = R.string.label_login_via_sms)) },
                        onClick = {
                            // some login to sign in with sms
                        }
                    )
                    val annotatedRegistrationText = buildAnnotatedString {
                        append(text = stringResource(id = R.string.label_registration_part_1)).also {
                            append(text = " ")
                        }
                        pushStringAnnotation(
                            tag = "register_link",
                            annotation = stringResource(
                                id = R.string.label_registration_part_tag
                            )
                        )
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.secondary)) {
                            append(text = stringResource(id = R.string.label_registration_part_2))
                        }
                        pop()
                    }
                    ClickableText(
                        modifier = Modifier
                            .fillMaxWidth()
//								.navigationBarsWithImePadding()
                            .padding(top = 16.dp),
                        text = annotatedRegistrationText,
                        style = MaterialTheme.typography.body2
                            .copy(textAlign = TextAlign.Center),
                        onClick = {
                            // todo
                        }
                    )
                }
            }
        },
        bottomBar = {
            Spacer(modifier = Modifier.navigationBarsWithImePadding())
        }
    )
}
