package com.cypherchat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cypherchat.ui.theme.CypherchatTheme

class MainActivity : ComponentActivity() {

    private val prefs by lazy {
        getSharedPreferences("cypherchat_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CypherchatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasCompletedOnboarding = prefs.getBoolean("onboarding_completed", false)
                    CypherchatNavigation(startAtChatList = hasCompletedOnboarding)
                }
            }
        }
    }

    fun markOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }
}
