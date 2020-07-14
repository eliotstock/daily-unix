package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import io.dailyunix.Constants.ARGUMENT_COMMAND
import kotlinx.android.synthetic.main.fragment_command.*
import java.lang.IllegalArgumentException

object Constants {
    const val ARGUMENT_COMMAND = "command"
}

class CommandFragment : Fragment() {

    private val logTag = CommandFragment::class.java.name

    private val viewModel: CommandViewModel by activityViewModels()

    private var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(logTag, "onCreate()")

        // We want the options menu shown for commands.
        setHasOptionsMenu(true)

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

            // Cause onPrepareOptionsMenu() to be called, so that the bookmark menu item might
            // toggle.
            requireActivity().invalidateOptionsMenu()
        })
    }

    override fun onStart() {
        super.onStart()

        Log.v(logTag, "onStart()")

        var commandToShow: Command? = null

        // Log.d(logTag, "Command extra: ${activity?.intent?.extras?.get("command")}")

        if (activity?.intent?.hasExtra(ARGUMENT_COMMAND)!!) {
            // Start from tap on the notification. Show the command shown on that notification.
            commandToShow = model?.commandByName(
                    activity?.intent?.getStringExtra(ARGUMENT_COMMAND)!!, requireContext())
        }
        else if (requireArguments().isEmpty) {
            // Start from launcher. Just show the current command of the day.
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Swap out the bookmark icon in the options menu depending on whether this command is
    // bookmarked or not.
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val bookmarkMenuItem = menu.getItem(0)
        val commandName = viewModel.command.value?.name

        if (model?.bookmarkedCommands?.contains(commandName)!!) {
            Log.v(logTag, "${commandName} is bookmarked")

            bookmarkMenuItem?.icon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_bookmark_on)
        }
        else {
            Log.v(logTag, "${commandName} is not bookmarked")

            bookmarkMenuItem?.icon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_bookmark_off)
        }
    }

}
