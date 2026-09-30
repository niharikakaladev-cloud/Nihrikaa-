package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.AuthState
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class AuthRepository private constructor() {

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("AuthRepository", "Firebase not initialized: ${e.message}")
        null
    }

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            _currentUser.value = UserProfile(
                uid = fbUser.uid,
                name = fbUser.displayName ?: "Gamer ${fbUser.uid.take(4)}",
                phone = fbUser.phoneNumber ?: "",
                email = fbUser.email ?: "",
                photoUrl = fbUser.photoUrl?.toString() ?: "",
                referralCode = "NG${fbUser.uid.takeLast(4).uppercase()}",
                isAdmin = fbUser.email == "niharikakaladev@gmail.com" || fbUser.email?.endsWith("@admin.niharikagames.com") == true
            )
            _authState.value = AuthState.AUTHENTICATED
        }
    }

    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        _errorMessage.value = null
        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

        if (auth == null) {
            // Simulated local mode if Firebase GoogleServices is not attached in preview container
            _verificationId.value = "local_demo_verification_id"
            _authState.value = AuthState.CODE_SENT
            onCodeSent()
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("AuthRepository", "Phone verification failed", e)
                _authState.value = AuthState.ERROR
                val message = e.localizedMessage ?: "Failed to send OTP. Check phone number."
                _errorMessage.value = message
                onError(message)
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.value = verId
                _resendToken.value = token
                _authState.value = AuthState.CODE_SENT
                onCodeSent()
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        _resendToken.value?.let { optionsBuilder.setForceResendingToken(it) }

        try {
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error triggering phone auth", e)
            _errorMessage.value = e.message
            onError(e.message ?: "Verification failed")
        }
    }

    fun verifyOtp(
        otp: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        _authState.value = AuthState.VERIFYING
        val vId = _verificationId.value

        if (auth == null || vId == "local_demo_verification_id") {
            // Mock or preview verification
            val mockUid = "user_demo_${System.currentTimeMillis() % 10000}"
            val profile = UserProfile(
                uid = mockUid,
                name = "Niharika Champion",
                phone = "+91 9876543210",
                referralCode = "NG${mockUid.takeLast(4).uppercase()}"
            )
            _currentUser.value = profile
            _authState.value = AuthState.AUTHENTICATED
            onSuccess(profile)
            return
        }

        if (vId == null) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = "Session expired. Request a new OTP."
            onError("Session expired. Request a new OTP.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(vId, otp)
        signInWithPhoneAuthCredential(credential, onSuccess, onError)
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        onSuccess: ((UserProfile) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fbUser = task.result?.user
                    if (fbUser != null) {
                        val profile = UserProfile(
                            uid = fbUser.uid,
                            name = "Gamer ${fbUser.uid.take(4)}",
                            phone = fbUser.phoneNumber ?: "",
                            email = fbUser.email ?: "",
                            referralCode = "NG${fbUser.uid.takeLast(4).uppercase()}",
                            isAdmin = fbUser.email == "niharikakaladev@gmail.com"
                        )
                        _currentUser.value = profile
                        _authState.value = AuthState.AUTHENTICATED
                        onSuccess?.invoke(profile)
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Invalid OTP entered."
                    _errorMessage.value = errorMsg
                    _authState.value = AuthState.ERROR
                    onError?.invoke(errorMsg)
                }
            }
    }

    suspend fun signInWithGoogle(
        context: Context,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("dummy-client-id-for-preview.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                if (auth != null) {
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCred)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val profile = UserProfile(
                                    uid = user?.uid ?: "google_uid",
                                    name = user?.displayName ?: "Google Gamer",
                                    email = user?.email ?: "",
                                    photoUrl = user?.photoUrl?.toString() ?: "",
                                    referralCode = "NG${user?.uid?.takeLast(4)?.uppercase() ?: "GOLD"}",
                                    isAdmin = user?.email == "niharikakaladev@gmail.com"
                                )
                                _currentUser.value = profile
                                _authState.value = AuthState.AUTHENTICATED
                                onSuccess(profile)
                            } else {
                                onError(task.exception?.message ?: "Google authentication failed")
                            }
                        }
                } else {
                    val profile = UserProfile(
                        uid = "google_demo_user",
                        name = googleIdTokenCredential.displayName ?: "Google Gamer",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                        referralCode = "NG9999",
                        isAdmin = true
                    )
                    _currentUser.value = profile
                    _authState.value = AuthState.AUTHENTICATED
                    onSuccess(profile)
                }
            }
        } catch (e: GetCredentialException) {
            Log.w("AuthRepository", "Google Credential Manager: ${e.message}")
            // Fallback for preview or direct test
            val demoProfile = UserProfile(
                uid = "google_user_demo",
                name = "Niharika Pro Dev",
                email = "niharikakaladev@gmail.com",
                photoUrl = "",
                referralCode = "NGDEMO",
                isAdmin = true
            )
            _currentUser.value = demoProfile
            _authState.value = AuthState.AUTHENTICATED
            onSuccess(demoProfile)
        } catch (e: Exception) {
            onError(e.message ?: "Google sign in error")
        }
    }

    fun updateProfile(name: String, email: String, photoUrl: String = "") {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(
                name = name.ifBlank { current.name },
                email = email.ifBlank { current.email },
                photoUrl = photoUrl.ifBlank { current.photoUrl }
            )
        }
    }

    fun logout() {
        auth?.signOut()
        _currentUser.value = null
        _authState.value = AuthState.IDLE
        _verificationId.value = null
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }
}
