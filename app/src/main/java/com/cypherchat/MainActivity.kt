package com.cypherchat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cypherchat.ui.theme.Cyph3rChatTheme

class MainActivity : ComponentActivity() {

    private val prefs by lazy {
        getSharedPreferences("cyph3rchat_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cyph3rChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasCompletedOnboarding = prefs.getBoolean("onboarding_completed", false)
                    Cyph3rChatNavigation(
                        startAtChatList = hasCompletedOnboarding,
                        onOnboardingComplete = { markOnboardingComplete() }
                    )
                }
            }
        }
    }

    private fun markOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }
}
