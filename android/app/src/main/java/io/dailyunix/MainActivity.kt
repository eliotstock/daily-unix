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

        // Schedule local notifications for every morning.
        // TODO (P0): Do this only following a (re)install. Doing it here is doubling up on the
        //  rescheduling in NotificationWorker.
        reschedule(applicationContext)

        // createNotificationChannel(applicationContext)

        // TODO (P0): Do this only following a (re)install.
        extractContent(applicationContext)
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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.v(tag, "onNewIntent()")

//        val command = intent?.getStringExtra("command")
//        val whatIs = intent?.getStringExtra("whatIs")
//        val tldr = intent?.getStringExtra("tldr")
//        val man = intent?.getStringExtra("man")
//
//        commandName.text = command
//
//        // TODO (P0): Add the whatis string to the command TextView, underneath the command name.
//
//        page.text = tldr
//
//        // TODO (P0): Wire a listener up to the tabs. Change the contents of the page on taps.
    }

}
