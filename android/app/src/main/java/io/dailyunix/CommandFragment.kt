package io.dailyunix

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_command.*

object Constants {
    const val ARGUMENT_COMMAND = "command"
}

class CommandFragment : Fragment() {

    private val logTag = CommandFragment::class.java.name

    private var model: Model? = null

    private var command: Command? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(tag, "onCreate()")

        model = getModel(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_command, container, false)
    }

    override fun onStart() {
        super.onStart()

        Log.v(tag, "onStart()")

        if (requireArguments().isEmpty) {
            command = model?.commandOftheDay
        }
        else {
            val commandName = requireArguments().getString(Constants.ARGUMENT_COMMAND)

            Log.v(tag, "command: $commandName")

            command = model?.commandByName(commandName!!, requireContext())
        }

        showCommand()

        button.setOnClickListener {
            if (command?.name != null ) {
                model?.completedCommands?.add(command!!.name)
            }

            model?.nextCommand(requireContext())
            command = model?.commandOftheDay

            model?.save(requireContext())

            showCommand()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showCommand() {
        // Now is NOT the time to advance to the next random command to show. The notification
        // worker already did that before showing the notification.

        commandName.text = command?.name

        // TODO (P1): Remove once we have a hamburger icon in the top left.
//        commandName.setOnClickListener() {
//            drawerLayout.openDrawer(Gravity.LEFT)
//        }

        // Remove any unpopulated TextView instances from the layout.
        if (command?.whatIs != null) {
            whatis.text = command?.whatIs
            whatis.visibility = View.VISIBLE
        } else {
            whatis.visibility = View.GONE
        }

        // Not all commands are provided by a package. When they're not, say so.
        if (command?.providerPackage.isNullOrBlank()) {
            providerPackage.text = getString(R.string.not_provided)

        } else {
            providerPackage.text = getString(R.string.provided, command?.providerPackage)
        }

        if (command?.tldr.isNullOrBlank()) {
            tldr.visibility = View.GONE
        } else {
            // TODO (P1): Remove the heading before rendering the markdown. We already show the
            //  command name above.
            val tldrWithoutHeading = command?.tldr!!.replace(Regex("^#.*"), "")

            // Markwon is a markdown renderer that doesn't need a WebView:
            //   https://noties.io/Markwon/docs/v4/core/getting-started.html
            val markwon = Markwon.create(requireContext())
            markwon.setMarkdown(tldr, tldrWithoutHeading)
        }

        if (command?.man.isNullOrBlank()) {
            man.visibility = View.GONE
        } else {
            man.text = command?.man
            man.visibility = View.VISIBLE
        }

        completion.text = model?.completionMessage(requireContext())

        // TODO (P2): This doesn't always work. See:
        //   https://stackoverflow.com/questions/4119441/how-to-scroll-to-top-of-long-scrollview-layout/19677350
        scrollView.fullScroll(ScrollView.FOCUS_UP)
    }

}
