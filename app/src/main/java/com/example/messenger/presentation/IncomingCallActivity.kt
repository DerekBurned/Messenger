package com.example.messenger.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_OPEN_INCOMING_CALL, true)

            if (intent.getBooleanExtra(MainActivity.EXTRA_ACCEPT_CALL, false)) {
                putExtra(MainActivity.EXTRA_ACCEPT_CALL, true)
            }
        }
        startActivity(forward)
        finish()
    }
}
