package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import kotlinx.android.synthetic.main.activity_main.*

// TODO (P1): Tabs for TLDR and MAN, with swipe support
// TODO (P3): Fix layout issues
// TODO (P1): Wrapping on man pages
// TODO (P1): Move progress into header of side nav
// TODO (P1): Back to dark theme

// TODO (P2): Circle graphic for progress
// TODO (P2): Starred commands
// TODO (P2): Sharing
// TODO (P2): Test completion doesn't break
// TODO (P2): Counts of completed and remaining commands
// TODO (P2): Play Store listing
// TODO (P2): Launcher icon, icon for notifications

// TODO (P3): Target O and test on O, P, Q, R
// TODO (P3): Completion celebration
// TODO (P3): Search
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

        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigation.setupWithNavController(navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

}