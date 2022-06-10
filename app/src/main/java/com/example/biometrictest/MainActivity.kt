package com.example.biometrictest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        biometricAuthenticator.isWeakAuthenticationEnabled = true
        biometricAuthenticator.isDeviceCredentialAuthenticationEnabled = false
        biometricAuthenticator.showNegativeButton = true
        biometricAuthenticator.showAuthenticationConfirmation = true

        LogUtil.log("Can auth ${biometricAuthenticator.canAuthenticate(this)}")
//        LogUtil.log("Auth without Crypto ${biometricAuthenticator.authenticateWithoutCrypto(this)}")
        biometricAuthenticator.authenticateAndEncrypt(this)
    }
}