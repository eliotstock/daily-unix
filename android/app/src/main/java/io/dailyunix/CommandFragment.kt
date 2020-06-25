package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_command.*
import java.lang.IllegalArgumentException

object Constants {
    const val ARGUMENT_COMMAND = "command"
}

class CommandFragment : Fragment() {

    private val logTag = CommandFragment::class.java.name

    private var model: Model? = null

    private val viewModel: CommandViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(logTag, "onCreate()")

        model = getModel(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_command, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ContentFragmentStateAdapter(this)
        pager.adapter = adapter

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "tldr"
                }
                1 -> {
                    tab.text = "man"
                }
                else -> {
                    throw IllegalArgumentException("Only two positions. No position: $position")
                }
            }
        }.attach()

        viewModel.command.observe(viewLifecycleOwner, Observer { command ->
            Log.d(logTag, "Observing change to command: ${command.name}")

            // Set the title in the action bar/toolbar to the name of the command
            (activity as AppCompatActivity?)!!.supportActionBar?.title = command?.name

            // Not all commands are provided by a package. When they're not, just leave the
            // subtitle off the action bar.
            val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
            if (command?.providerPackage.isNullOrBlank()) {
                actionBar?.subtitle = null
            }
            else {
                actionBar?.subtitle = command?.providerPackage
            }

            // TODO (P1): Move to custom view
            //  completion.text = model?.completionMessage(requireContext())
        })
    }

    override fun onStart() {
        super.onStart()

        Log.v(logTag, "onStart()")

        var commandToShow: Command? = null

        if (requireArguments().isEmpty) {
            // Start from launcher or tap on notification. Just show the current command of the
            // day.
            commandToShow = model?.commandOftheDay
        }
        else {
            // Start from tap on the completed or remaining commands list in side nav. Show this
            // command.
            val commandName = requireArguments().getString(Constants.ARGUMENT_COMMAND)

            Log.v(logTag, "command: $commandName")

            commandToShow = model?.commandByName(commandName!!, requireContext())
        }

        viewModel.command.value = commandToShow

        button.setOnClickListener {
            // Save the command we're currently showing as completed.
            if (viewModel.command.value?.name != null ) {
                model?.completedCommands?.add(viewModel.command.value!!.name)
            }

            // Advance to the next command of the day.
            model?.nextCommand(requireContext())

            // Save again.
            model?.save(requireContext())

            // Update the UI with the new command.
            viewModel.command.value = model?.commandOftheDay
        }
    }

}
