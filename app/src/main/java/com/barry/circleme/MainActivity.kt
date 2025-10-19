package com.barry.circleme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.barry.circleme.data.DeepLinkHandler
import com.barry.circleme.ui.navigation.AppNavigation
import com.barry.circleme.ui.theme.CircleMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CircleMeTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkAction = intent?.action
        val appLinkData = intent?.data
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val circleId = appLinkData.getQueryParameter("circleId")
            if (circleId != null) {
                Log.d("MainActivity", "Received circleId from App Link: $circleId")
                DeepLinkHandler.setPendingCircleId(circleId)
            }
        }
    }
}
