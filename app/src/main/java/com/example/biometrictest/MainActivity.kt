package com.example.biometrictest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.navigation.ui.AppBarConfiguration
import com.example.biometrictest.biometric.BiometricAuthenticator
import com.example.biometrictest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        LogUtil.log("START")

        biometricAuthenticator =
            BiometricAuthenticator.instance(this, object : BiometricAuthenticator.Listener {
                override fun onNewMessage(message: String) {
                    LogUtil.log(message)
                }
            })

        biometricAuthenticator.isStrongAuthenticationEnabled = true
//        biometricAuthenticator.isWeakAuthenticationEnabled = true
        biometricAuthenticator.isDeviceCredentialAuthenticationEnabled = false
        biometricAuthenticator.showNegativeButton = true
        biometricAuthenticator.showAuthenticationConfirmation = true

//        LogUtil.log("Can auth ${biometricAuthenticator.canAuthenticate(this)}")
//        LogUtil.log("Auth without Crypto ${biometricAuthenticator.authenticateWithoutCrypto(this)}")
//        biometricAuthenticator.authenticateAndEncrypt(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                )
            }
            startActivity(enrollIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
        } else {
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }
}