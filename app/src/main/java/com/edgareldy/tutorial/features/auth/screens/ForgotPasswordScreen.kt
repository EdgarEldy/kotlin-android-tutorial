package com.edgareldy.tutorial.features.auth.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgareldy.tutorial.features.auth.components.AuthScaffold
import com.edgareldy.tutorial.features.auth.components.AuthTextField
import com.edgareldy.tutorial.features.auth.components.PasswordResetStepper
import com.edgareldy.tutorial.features.auth.viewmodel.ForgotPasswordUiState
import com.edgareldy.tutorial.features.auth.viewmodel.ForgotPasswordViewModel

/**
 * Forgot-password screen: collects the user's email to send a reset OTP.
 * On success, navigates to the VerifyOtp screen passing the email.
 * Restyled to match the mockup hero + card overlap layout with PasswordResetStepper.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onOtpSent: (email: String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ForgotPasswordUiState.Success -> {
                viewModel.resetState()
                onOtpSent(state.email)
            }
            is ForgotPasswordUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    AuthScaffold(
        heroHeight = 200.dp,
        snackbarHostState = snackbarHostState,
        heroContent = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp, start = 4.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PasswordResetStepper(currentStep = 1)
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Enter your email",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1C1B1F)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We will send a reset code to your email address.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF79747E)
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendOtp(email) },
            enabled = uiState !is ForgotPasswordUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState is ForgotPasswordUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send code")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
