package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import kotlinx.android.synthetic.main.activity_main.*

// TODO (P1): Wrapping on man pages
// TODO (P1): Move progress into header of side nav

// TODO (P2): Back to dark theme
// TODO (P2): Circle graphic for progress
// TODO (P2): Starred commands
// TODO (P2): Sharing
// TODO (P2): Test completion doesn't break
// TODO (P2): Counts of completed and remaining commands
// TODO (P2): Play Store listing, including privacy policy URL
// TODO (P2): Launcher icon, icon for notifications. Ideas for iconography: brain, reference,
//  completion circle (dynamic?)
// TODO (P2): Licenses fragment

// TODO (P3): Target O and test on O, P, Q, R
// TODO (P3): Completion celebration
// TODO (P3): Search
// TODO (P3): Firebase crash reporting
// TODO (P3): Consider getting dailyunix.io and hosting on Firebase
class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.java.name

    private lateinit var appBarConfiguration : AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(logTag, "onCreate()")

        setContentView(R.layout.activity_main)

//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar) // Causes: "This Activity already has an action bar supplied by the window decor"
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        val navController = findNavController(R.id.navHostFragment)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigation.setupWithNavController(navController)

        val viewModel: CommandViewModel by viewModels()
        viewModel.command.observe(this,  Observer<Command>{command ->
            Log.d(logTag, "Observing change to command: ${command.name}")
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