package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

// TODO (P1): whatis string in lists

// TODO (P2): Circle graphic for progress
// TODO (P2): Starred/bookmarked commands
// TODO (P2): Back to dark theme
// TODO (P2): Test that completion of all commands doesn't break
// TODO (P2): Counts of completed and remaining commands
// TODO (P2): Play Store listing, including privacy policy URL
// TODO (P2): Launcher icon, icon for notifications. Ideas for iconography: brain, reference,
//  completion circle (dynamic?)
// TODO (P2): Licenses fragment

// TODO (P3): minSdkVersion to 26 (Android O), targetSdkVersion to 30 (Android R), test on O to R
// TODO (P3): Completion celebration. Find something in Unix history, Unix version 1 manual.
// TODO (P3): Search of command names
// TODO (P3): Firebase crash reporting
// TODO (P3): Consider getting dailyunix.io and hosting on Firebase
// TODO (P3): Sharing
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp(appBarConfiguration)
    }

}