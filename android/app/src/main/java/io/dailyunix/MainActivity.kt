package io.dailyunix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() /*, NavigationView.OnNavigationItemSelectedListener */ {

    private val logTag = MainActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(logTag, "onCreate()")

        setContentView(R.layout.activity_main)

        // TODO (P1): Remove once navigation working.
        // navigation.setNavigationItemSelectedListener(this)
    }

    /*
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.completed -> {
                val intent = Intent(this, CompletedCommandsActivity::class.java)
                startActivity(intent)
            }
            R.id.remaining -> {
                val intent = Intent(this, RemainingCommandsActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }
    */

}