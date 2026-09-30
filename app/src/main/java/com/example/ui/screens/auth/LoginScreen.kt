package com.example.ui.screens.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.WalletRepository
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingCardSurface
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingEmerald
import com.example.ui.theme.GamingGold
import com.example.ui.theme.GamingRed
import com.example.ui.theme.GamingRedDark
import com.example.ui.theme.GamingRedGradient
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository.getInstance() }
    val walletRepo = remember { WalletRepository.getInstance() }

    val authState by authRepo.authState.collectAsState()
    val errorMessage by authRepo.errorMessage.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var resendTimer by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Resend countdown timer
    LaunchedEffect(isTimerRunning, resendTimer) {
        if (isTimerRunning && resendTimer > 0) {
            delay(1000L)
            resendTimer -= 1
        } else if (resendTimer == 0) {
            isTimerRunning = false
        }
    }

    Surface(
        color = GamingDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GamingRedGradient)
                    .border(2.dp, GamingGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NG",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NIHARIKA GAMES",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = GamingRed,
                letterSpacing = 1.sp
            )

            Text(
                text = "India's Ultimate Gaming & Rewards Hub",
                fontSize = 13.sp,
                color = GamingTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Auth Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GamingCardSurface)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                if (authState == AuthState.CODE_SENT || authState == AuthState.VERIFYING) {
                    // OTP Verification Step
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    authRepo.logout()
                                    isTimerRunning = false
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = GamingTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verify Mobile OTP",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "A 6-digit verification code has been sent to:",
                            fontSize = 13.sp,
                            color = GamingTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+91 $phoneNumber",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        authRepo.logout()
                                        isTimerRunning = false
                                    }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Number",
                                    tint = GamingRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    color = GamingRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // OTP Input Field
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    otpCode = it
                                }
                            },
                            label = { Text("Enter 6-Digit OTP") },
                            placeholder = { Text("• • • • • •") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingRed,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary,
                                cursorColor = GamingRed
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GamingGold
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field")
                        )

                        // Error message
                        val activeError = localError ?: errorMessage
                        if (activeError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeError,
                                color = GamingRed,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Verify Button
                        Button(
                            onClick = {
                                if (otpCode.length < 6) {
                                    localError = "Please enter a valid 6-digit OTP"
                                    return@Button
                                }
                                localError = null
                                isSubmitting = true
                                authRepo.verifyOtp(
                                    otp = otpCode,
                                    onSuccess = { profile ->
                                        isSubmitting = false
                                        walletRepo.syncUserWallet(profile.uid)
                                        onLoginSuccess(profile)
                                    },
                                    onError = { err ->
                                        isSubmitting = false
                                        localError = err
                                    }
                                )
                            },
                            enabled = !isSubmitting && otpCode.length == 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamingRed,
                                disabledContainerColor = GamingRedDark.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Verify & Enter",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resend Timer Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isTimerRunning) {
                                Text(
                                    text = "Resend OTP in ${resendTimer}s",
                                    fontSize = 13.sp,
                                    color = GamingTextMuted
                                )
                            } else {
                                Text(
                                    text = "Didn't receive OTP? ",
                                    fontSize = 13.sp,
                                    color = GamingTextSecondary
                                )
                                Text(
                                    text = "Resend OTP",
                                    fontSize = 13.sp,
                                    color = GamingGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            if (activity != null) {
                                                isTimerRunning = true
                                                resendTimer = 60
                                                authRepo.sendOtp(
                                                    activity = activity,
                                                    phoneNumber = phoneNumber,
                                                    onCodeSent = {},
                                                    onError = { localError = it }
                                                )
                                            }
                                        }
                                        .testTag("resend_otp_button")
                                )
                            }
                        }
                    }
                } else {
                    // Mobile Number Entry Step
                    Column {
                        Text(
                            text = "Login with Mobile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamingTextPrimary
                        )

                        Text(
                            text = "Enter your 10-digit mobile number for OTP login",
                            fontSize = 12.sp,
                            color = GamingTextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phoneNumber = it
                                }
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("9876543210") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = GamingGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+91",
                                        fontWeight = FontWeight.Bold,
                                        color = GamingTextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp)
                                            .background(GamingCardBorder)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingRed,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary,
                                cursorColor = GamingRed
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_number_input")
                        )

                        // Error message
                        val activeError = localError ?: errorMessage
                        if (activeError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeError,
                                color = GamingRed,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Send OTP Button
                        Button(
                            onClick = {
                                if (phoneNumber.length < 10) {
                                    localError = "Please enter full 10-digit mobile number"
                                    return@Button
                                }
                                localError = null
                                isSubmitting = true
                                if (activity != null) {
                                    authRepo.sendOtp(
                                        activity = activity,
                                        phoneNumber = phoneNumber,
                                        onCodeSent = {
                                            isSubmitting = false
                                            isTimerRunning = true
                                            resendTimer = 60
                                        },
                                        onError = { err ->
                                            isSubmitting = false
                                            localError = err
                                        }
                                    )
                                } else {
                                    isSubmitting = false
                                    isTimerRunning = true
                                    resendTimer = 60
                                }
                            },
                            enabled = !isSubmitting && phoneNumber.length == 10,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamingRed,
                                disabledContainerColor = GamingRedDark.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Send OTP",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Divider OR
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(GamingCardBorder)
                            )
                            Text(
                                text = "  OR CONTINUE WITH  ",
                                fontSize = 11.sp,
                                color = GamingTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(GamingCardBorder)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isSubmitting = true
                                    authRepo.signInWithGoogle(
                                        context = context,
                                        onSuccess = { profile ->
                                            isSubmitting = false
                                            walletRepo.syncUserWallet(profile.uid)
                                            onLoginSuccess(profile)
                                        },
                                        onError = { err ->
                                            isSubmitting = false
                                            localError = err
                                        }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF1E1D2B),
                                contentColor = GamingTextPrimary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GamingCardBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_signin_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    color = GamingGold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trust & Security badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Secure",
                    tint = GamingEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% Safe & Secure • RNG Certified Fair Play",
                    fontSize = 11.sp,
                    color = GamingTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
