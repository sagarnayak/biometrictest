package com.example.biometrictest.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometrictest.crypto.CryptographyManager
import com.example.biometrictest.crypto.EncryptedData
import com.example.biometrictest.crypto.EncryptionMode

abstract class BiometricAuthenticator(
    activity: FragmentActivity,
    protected val listener: Listener
) {

    var showNegativeButton = false
    var isDeviceCredentialAuthenticationEnabled = false
    var isStrongAuthenticationEnabled = false
    var isWeakAuthenticationEnabled = false
    var showAuthenticationConfirmation = false

    /** Handle using biometrics + cryptography to encrypt/decrypt data securely */
    protected val cryptographyManager = CryptographyManager.instance()
    protected lateinit var encryptionMode: EncryptionMode
    protected lateinit var encryptedData: EncryptedData

    /** Receives callbacks from an authentication operation */
    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            listener.onNewMessage("Authentication succeeded")

            val type = result.authenticationType
            val cryptoObject = result.cryptoObject
            listener.onNewMessage("Type: ${getAuthenticationType(type)} - Crypto: $cryptoObject")

            val cipher = cryptoObject?.cipher ?: return
            when (encryptionMode) {
                EncryptionMode.ENCRYPT -> {
                    encryptedData = cryptographyManager.encrypt(PAYLOAD, cipher)
                    listener.onNewMessage("Encrypted text: ${encryptedData.encrypted}")
                }
                EncryptionMode.DECRYPT -> {
                    val plainData = cryptographyManager.decrypt(encryptedData.encrypted, cipher)
                    listener.onNewMessage("Decrypted text: $plainData")
                }
            }
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            listener.onNewMessage("Authentication error[${getBiometricError(errorCode)}] - $errString")
        }

        override fun onAuthenticationFailed() {
            listener.onNewMessage("Authentication failed - Biometric is valid but not recognized")
        }
    }

    /** Manages a biometric prompt, and allows to perform an authentication operation */
    protected val biometricPrompt =
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authenticationCallback)

    abstract fun canAuthenticate(context: Context)

    fun authenticateWithoutCrypto(context: Context) {
        val promptInfo = buildPromptInfo(context) ?: return
        biometricPrompt.authenticate(promptInfo)
    }

    abstract fun authenticateAndEncrypt(context: Context)

    abstract fun authenticateAndDecrypt(context: Context)

    abstract fun setAllowedAuthenticators(builder: PromptInfo.Builder)

    fun cancelAuthentication() {
        biometricPrompt.cancelAuthentication()
    }

    /** Build a [PromptInfo] that defines the properties of the biometric prompt dialog. */
    protected fun buildPromptInfo(context: Context): PromptInfo? {
        val builder = PromptInfo.Builder()
            .setTitle("prompt_title")
            .setSubtitle("prompt_subtitle")
            .setDescription("prompt_description")
            .setNegativeButtonText("prompt_negative_text")

        // Show a confirmation button after authentication succeeds
        builder.setConfirmationRequired(showAuthenticationConfirmation)

        // Allow authentication with a password, pin or pattern
        setAllowedAuthenticators(builder)

        // Set a negative button. It would typically display "Cancel"
        if (showNegativeButton) {
            builder.setNegativeButtonText("prompt_negative_text")
        }

        return try {
            builder.build()
        } catch (exception: IllegalArgumentException) {
            listener.onNewMessage("Building prompt info error - ${exception.message}")
            null
        }
    }

    interface Listener {
        fun onNewMessage(message: String)
    }

    companion object {
        private const val PAYLOAD = "Biometrics sample"

        fun instance(activity: FragmentActivity, listener: Listener): BiometricAuthenticator {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                listener.onNewMessage("Providing instance for below M")
                return BiometricAuthenticatorLegacy(activity, listener)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                listener.onNewMessage("Providing instance for below R")
                return BiometricAuthenticatorApi23(activity, listener)
            }
            listener.onNewMessage("Providing instance for Api 30")
            return BiometricAuthenticatorApi30(activity, listener)
        }
    }
}