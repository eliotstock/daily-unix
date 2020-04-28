package io.dailyunix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(tag, "onCreate()")

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        Log.v(tag, "onStart()")

        val model: Model = getModel(applicationContext)

        // Now is NOT the time to advance to the next random command to show. The notification
        // worker already did that before showing the notification.
        val command = model.commandOftheDay

        commandName.text = command?.name
        page.text = command?.tldr

        // TODO (P0): Add the whatis string to the command TextView, underneath the command name.
        // TODO (P0): Wire a listener up to the tabs. Change the contents of the page on taps.
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.v(tag, "onNewIntent()")
    }

}
