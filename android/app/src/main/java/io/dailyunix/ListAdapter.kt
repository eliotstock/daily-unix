package io.dailyunix

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val dataset: List<String>) :
        RecyclerView.Adapter<ListAdapter.CommandViewHolder>() {

    private val tag = ListAdapter::class.java.name

    class CommandViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_list_item, parent, false) as TextView

        textView.setOnClickListener() {
            v ->
                val t = v as TextView

                val action = RemainingCommandsFragmentDirections
                    .actionRemainingCommandsFragmentToCommandFragment(t.text.toString())

                Log.v(tag, "Action: command fragment with command ${t.text}")

                v.findNavController().navigate(action)
        }

        return CommandViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.textView.text = dataset[position]
    }

    override fun getItemCount() = dataset.size
}