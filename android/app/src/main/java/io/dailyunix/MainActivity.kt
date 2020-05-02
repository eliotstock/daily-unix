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
        whatis.text = command?.whatIs
        providerPackage.text = command?.providerPackage
        tldr.text = command?.tldr
        man.text = command?.man
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.v(tag, "onNewIntent()")
    }

}
