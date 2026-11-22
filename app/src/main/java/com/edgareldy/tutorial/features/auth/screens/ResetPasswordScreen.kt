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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgareldy.tutorial.features.auth.components.AuthScaffold
import com.edgareldy.tutorial.features.auth.components.AuthTextField
import com.edgareldy.tutorial.features.auth.components.PasswordResetStepper
import com.edgareldy.tutorial.features.auth.viewmodel.ResetPasswordUiState
import com.edgareldy.tutorial.features.auth.viewmodel.ResetPasswordViewModel

/**
 * Reset-password screen (step 3 of the password reset flow).
 * The OTP is received as a navigation argument and never displayed or logged.
 * On success, navigates to SignIn so the user can log in with the new password.
 * Restyled with PasswordResetStepper (step 3) and a password strength indicator.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun ResetPasswordScreen(
    otp: String,
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ResetPasswordUiState.Success -> {
                viewModel.resetState()
                onResetSuccess()
            }
            is ResetPasswordUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    val strength = passwordStrength(newPassword)
    val strengthLabel = when {
        newPassword.isEmpty() -> ""
        strength <= 0.25f -> "Weak"
        strength <= 0.5f -> "Fair"
        strength <= 0.75f -> "Good"
        else -> "Strong"
    }
    val strengthColor = if (strength > 0.6f) Color(0xFF16A34A) else Color(0xFFF59E0B)

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
                text = "New Password",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PasswordResetStepper(currentStep = 3)
        Spacer(modifier = Modifier.height(28.dp))

        AuthTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "New password",
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                                      else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (newPassword.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { strength },
                modifier = Modifier.fillMaxWidth(),
                color = strengthColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.bodySmall,
                color = strengthColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.resetPassword(otp, newPassword, confirmPassword) },
            enabled = uiState !is ResetPasswordUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState is ResetPasswordUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Reset password")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Calculates a password strength score between 0.0 and 1.0.
 * Increments of 0.25 are added for length >= 8, uppercase, digit,
 * and special character presence.
 */
private fun passwordStrength(password: String): Float {
    var score = 0f
    if (password.length >= 8) score += 0.25f
    if (password.any { it.isUpperCase() }) score += 0.25f
    if (password.any { it.isDigit() }) score += 0.25f
    if (password.any { !it.isLetterOrDigit() }) score += 0.25f
    return score
}
