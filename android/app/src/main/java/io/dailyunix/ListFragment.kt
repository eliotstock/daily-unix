package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // TODO (P1) : Fix:
        /*
        java.lang.IllegalArgumentException: LayoutManager androidx.recyclerview.widget.LinearLayoutManager@115e7c2 is already attached to a RecyclerView: androidx.recyclerview.widget.RecyclerView{4fcf9d3 VFED.V... ......ID 0,0-1080,2162 #7f0800df app:id/recycler_view}, adapter:io.dailyunix.ListAdapter@6d910, layout:androidx.recyclerview.widget.LinearLayoutManager@115e7c2, context:io.dailyunix.MainActivity@2238c2d
        at androidx.recyclerview.widget.RecyclerView.setLayoutManager(RecyclerView.java:1345)
        at io.dailyunix.ListFragment.onCreateView(ListFragment.kt:40)
        at androidx.fragment.app.Fragment.performCreateView(Fragment.java:2698)
        at androidx.fragment.app.FragmentStateManager.createView(FragmentStateManager.java:320)
        at androidx.fragment.app.FragmentManager.moveToState(FragmentManager.java:1187)
        at androidx.fragment.app.FragmentManager.addAddedFragments(FragmentManager.java:2224)
        at androidx.fragment.app.FragmentManager.executeOpsTogether(FragmentManager.java:1997)
        at androidx.fragment.app.FragmentManager.removeRedundantOperationsAndExecute(FragmentManager.java:1953)
        at androidx.fragment.app.FragmentManager.execPendingActions(FragmentManager.java:1849)
        at androidx.fragment.app.FragmentManager$4.run(FragmentManager.java:413)
        at android.os.Handler.handleCallback(Handler.java:938)
        at android.os.Handler.dispatchMessage(Handler.java:99)
        at android.os.Looper.loop(Looper.java:223)
        at android.app.ActivityThread.main(ActivityThread.java:7596)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:592)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:941)
        */

        view.setHasFixedSize(true)
        view.layoutManager = viewManager
        view.adapter = viewAdapter

        return view
    }

    override fun onStart() {
        super.onStart()

        Log.v(tag, "onStart()")
    }

    abstract fun getCommandsForList(): List<String>

}
