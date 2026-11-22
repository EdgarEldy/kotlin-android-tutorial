package com.edgareldy.tutorial.features.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Row of 6 individual OTP digit boxes. Each box is a 48x56.dp filled cell with
 * a 12.dp rounded shape. Focus advances automatically when a digit is entered
 * and moves back on backspace. The [onOtpChange] callback always returns the
 * full current OTP string (up to 6 characters).
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun OtpInputRow(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fieldBg = MaterialTheme.colorScheme.surfaceVariant
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        for (index in 0 until 6) {
            val digit = otp.getOrNull(index)?.toString() ?: ""

            BasicTextField(
                value = digit,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(1)
                    val newOtp = otp.toMutableList().also { list ->
                        while (list.size <= index) list.add(' ')
                        if (filtered.isNotEmpty()) {
                            list[index] = filtered[0]
                        } else {
                            if (index < list.size) list[index] = ' '
                        }
                    }.joinToString("").trimEnd()

                    val cleanOtp = buildString {
                        for (i in 0 until 6) {
                            val ch = newOtp.getOrNull(i)
                            if (ch != null && ch.isDigit()) append(ch)
                        }
                    }

                    if (filtered.isNotEmpty()) {
                        val newFull = buildString {
                            for (i in 0 until 6) {
                                when {
                                    i < index -> append(otp.getOrElse(i) { ' ' })
                                    i == index -> append(filtered[0])
                                    else -> append(otp.getOrElse(i) { ' ' })
                                }
                            }
                        }.trimEnd().filter { it.isDigit() }
                        onOtpChange(newFull)
                        if (index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    } else {
                        val newFull = buildString {
                            for (i in 0 until 6) {
                                when {
                                    i < index -> append(otp.getOrElse(i) { ' ' })
                                    i == index -> { /* remove */ }
                                    else -> append(otp.getOrElse(i) { ' ' })
                                }
                            }
                        }.trimEnd().filter { it.isDigit() }
                        onOtpChange(newFull)
                        if (index > 0) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .width(48.dp)
                    .height(56.dp)
                    .focusRequester(focusRequesters[index]),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(56.dp)
                            .background(fieldBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (digit.isEmpty()) {
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
