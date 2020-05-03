package io.dailyunix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

        // Remove any unpopulated TextView instances from the layout.
        if (command?.whatIs != null) {
            whatis.text = command.whatIs
            whatis.visibility = View.VISIBLE
        }
        else {
            whatis.visibility = View.GONE
        }

        if (command?.providerPackage.isNullOrBlank()) {
            providerPackage.visibility = View.GONE
        }
        else {
            providerPackage.text = getString(R.string.provided, command?.providerPackage)
            providerPackage.visibility = View.VISIBLE
        }

        if (command?.tldr.isNullOrBlank()) {
            tldr.visibility = View.GONE
        }
        else {
            tldr.text = command?.tldr
            tldr.visibility = View.VISIBLE
        }

        if (command?.man.isNullOrBlank()) {
            man.visibility = View.GONE
        }
        else {
            man.text = command?.man
            man.visibility = View.VISIBLE
        }

        // TODO (P2): For now, simply tapping on the notification constitutes "completing" a
        //  command. Later, add a "Done" button to the activity.
        command?.name?.let { model.completedCommands.add(it) }

        model.save(applicationContext)

        completion.text = model.completionMessage(applicationContext)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.v(tag, "onNewIntent()")
    }

}
