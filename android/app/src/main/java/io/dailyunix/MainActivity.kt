package io.dailyunix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.name

    private var model: Model? = null

    private var command: Command? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(tag, "onCreate()")

        setContentView(R.layout.activity_main)

        model = getModel(applicationContext)
        command = model?.commandOftheDay
    }

    override fun onStart() {
        super.onStart()

        Log.v(tag, "onStart()")

        showCommand()

        button.setOnClickListener {
            if (command?.name != null ) {
                model?.completedCommands?.add(command!!.name)
            }

            model?.nextCommand(applicationContext)
            command = model?.commandOftheDay

            model?.save(applicationContext)

            showCommand()
        }
    }

    private fun showCommand() {
        // Now is NOT the time to advance to the next random command to show. The notification
        // worker already did that before showing the notification.

        commandName.text = command?.name

        // Remove any unpopulated TextView instances from the layout.
        if (command?.whatIs != null) {
            whatis.text = command?.whatIs
            whatis.visibility = View.VISIBLE
        } else {
            whatis.visibility = View.GONE
        }

        if (command?.providerPackage.isNullOrBlank()) {
            providerPackage.visibility = View.GONE
        } else {
            providerPackage.text = getString(R.string.provided, command?.providerPackage)
            providerPackage.visibility = View.VISIBLE
        }

        if (command?.tldr.isNullOrBlank()) {
            tldr.visibility = View.GONE
        } else {
            tldr.text = command?.tldr
            tldr.visibility = View.VISIBLE
        }

        if (command?.man.isNullOrBlank()) {
            man.visibility = View.GONE
        } else {
            man.text = command?.man
            man.visibility = View.VISIBLE
        }

        completion.text = model?.completionMessage(applicationContext)

        // TODO (P2): This doesn't always work. See:
        //   https://stackoverflow.com/questions/4119441/how-to-scroll-to-top-of-long-scrollview-layout/19677350
        scrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.v(tag, "onNewIntent()")
    }

}
