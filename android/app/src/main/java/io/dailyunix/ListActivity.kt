package io.dailyunix

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListActivity : Activity() {

    private val tag = ListActivity::class.java.name

    private var model: Model? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        model = getModel(applicationContext)
        val sortedCommands : List<String> = model!!.completedCommands.sorted()

        viewManager = LinearLayoutManager(this)
        viewAdapter = ListAdapter(sortedCommands)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // Improve performance: changes in content do not change the layout size of the
            // RecyclerView.
            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter
        }
    }
}
