package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class ListFragment : Fragment() {

    private val logTag = ListFragment::class.java.name

    var model: Model? = null

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = getModel(requireContext())
        val sortedCommands : List<String> = getCommandsForList()

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = ListAdapter(sortedCommands)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : RecyclerView = inflater.inflate(R.layout.fragment_list, container, false) as RecyclerView

        view.setHasFixedSize(true)
        view.layoutManager = viewManager
        view.adapter = viewAdapter

        return view
    }

    override fun onStart() {
        super.onStart()

        Log.v(logTag, "onStart()")
    }

    override fun onResume() {
        super.onResume()

        Log.v(logTag, "onResume()")

        // Remove the provider package that the last command we looked at added to the action bar.
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.subtitle = null
    }

    abstract fun getCommandsForList(): List<String>

}
