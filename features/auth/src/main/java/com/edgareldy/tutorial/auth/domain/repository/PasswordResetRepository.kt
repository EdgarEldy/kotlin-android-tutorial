package com.edgareldy.tutorial.auth.domain.repository

/**
 * Contract for the 3-step mobile password reset flow.
 * Step 1: sendOtp - requests an OTP sent to the user's email.
 * Step 2: verifyOtp - validates the OTP without consuming it.
 * Step 3: confirmReset - applies the new password and consumes the OTP.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
interface PasswordResetRepository {

    /** Sends a 6-digit OTP to [email]. Returns success even if the address is unknown (security). */
    suspend fun sendOtp(email: String): Result<Unit>

    /** Verifies that [otp] is valid for [email] without consuming it. */
    suspend fun verifyOtp(email: String, otp: String): Result<Unit>

    /** Resets the password using the OTP previously verified by [verifyOtp]. */
    suspend fun confirmReset(otp: String, newPassword: String, confirmPassword: String): Result<Unit>
}
