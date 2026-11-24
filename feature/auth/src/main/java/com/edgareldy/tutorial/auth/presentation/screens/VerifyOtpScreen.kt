package com.edgareldy.tutorial.auth.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgareldy.tutorial.auth.presentation.components.AuthScaffold
import com.edgareldy.tutorial.auth.presentation.components.OtpInputRow
import com.edgareldy.tutorial.auth.presentation.components.PasswordResetStepper
import com.edgareldy.tutorial.auth.presentation.viewmodel.VerifyOtpUiState
import com.edgareldy.tutorial.auth.presentation.viewmodel.VerifyOtpViewModel

/**
 * OTP verification screen (step 2 of the password reset flow).
 * The OTP entered here is never logged. On success, it is forwarded to
 * the ResetPassword screen via navigation arguments.
 * Restyled to match the mockup hero + card overlap layout with PasswordResetStepper.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun VerifyOtpScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onOtpVerified: (otp: String) -> Unit,
    viewModel: VerifyOtpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var otp by rememberSaveable { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(180) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VerifyOtpUiState.Success -> {
                viewModel.resetState()
                onOtpVerified(state.otp)
            }
            is VerifyOtpUiState.Error -> {
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
                text = "OTP Verification",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PasswordResetStepper(currentStep = 2)
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Enter the code",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OtpInputRow(
            otp = otp,
            onOtpChange = { otp = it }
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (secondsLeft > 0) {
            Text(
                text = "Expires in %d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Text(
                text = "OTP expired",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.verifyOtp(email, otp) },
            enabled = otp.length == 6 && uiState !is VerifyOtpUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState is VerifyOtpUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify OTP")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
