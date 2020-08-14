package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import kotlinx.android.synthetic.main.activity_main.*

// TODO (P2): Licenses fragment.
//   * Add licence name for each command's package's man page to the content.zip.
//   * Get the set of licence names and put each license file into the content.zip.
//   * Add a "Licenses" item to the side nav whcih goes to a list of all commands.
//   * Each command on the list goes to the licence for that command's man page.
//   * Explain the license for the TLDR content somewhere.
// TODO (P2): Circle custom view for progress
// TODO (P2): Play Store listing, including privacy policy URL
// TODO (P2): Firebase crash reporting
// TODO (P2): When a list is empty, show "No commands" rather than an empty view

// TODO (P3): minSdkVersion to 26 (Android O), targetSdkVersion to 30 (Android R), test on O to R
// TODO (P3): Put content in nobackup and test Auto Backup again.
// TODO (P3): Completion celebration. Find something in Unix history, Unix version 1 manual.
// TODO (P3): whatis string in lists
//  * Have Model return Command instances rather than just strings for completedCommands property
//    and remainingCommands() function
//  * But persist only the command names to JSON, not the whole command instance serialisation
//  * ListFragment and subclasses should deal with lists of Commands, not lists of Strings
//  * ListAdapter constructor should take a list of Commands, not a list of Strings
//  * ListAdapter.onBindViewHolder() should build formatted strings for the TextView with the
//    command name as monospace and the whatis string as normal
// TODO (P3): Search of command names

// TODO (P4): Consider getting dailyunix.io and hosting on Firebase
// TODO (P4): Sharing
// TODO (P4): Context bundles for minimal Ubuntu desktop, AWS server, Mac OS 11 without XCode
// TODO (P4): User generated per-command rating for usefulness. Start with the most useful commands
class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.java.name

    private lateinit var appBarConfiguration : AppBarConfiguration

    private var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(logTag, "onCreate()")

        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.navHostFragment)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        setSupportActionBar(toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigation.setupWithNavController(navController)

        val viewModel: CommandViewModel by viewModels()
        viewModel.command.observe(this,  Observer<Command>{command ->
            Log.d(logTag, "Observing change to command: ${command.name}")

            // TODO (P2): Move all this to a custom NavHeaderView that initially just includes a
            //  TextView.
            model = getModel(this)
            val navHeaderView = navigation.getHeaderView(0)
            val completion  = navHeaderView.findViewById<TextView>(R.id.completion)
            completion.text = model?.completionMessage(this)
        })
    }

    // Docs: "Return false to allow normal menu processing to proceed, true to consume it here."
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if ("bookmark".equals(item.title?.toString(), ignoreCase = true)) {
            val viewModel: CommandViewModel by viewModels()

            val commandName = viewModel.command.value?.name

            if (commandName.isNullOrEmpty()) {
                Log.e(logTag, "No command name to bookmark/unbookmark")
            }
            else {
                model?.toggleBookmark(viewModel.command.value?.name!!)
                model?.save(this)
            }

            // Cause onPrepareOptionsMenu() to be called, so that the bookmark menu item might
            // toggle.
            invalidateOptionsMenu()

            return true
        }
        else {
            return item.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                    || super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp(appBarConfiguration)
    }

}
