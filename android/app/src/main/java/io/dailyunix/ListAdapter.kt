package io.dailyunix

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
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
                // Log.v(tag, "onClick: ${t.text}")

                val intent = Intent(t.context, CommandActivity::class.java)
                intent.putExtra(Constants.INTENT_EXTRA_COMMAND, t.text)
                t.context.startActivity(intent)
        }

        return CommandViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.textView.text = dataset[position]
    }

    override fun getItemCount() = dataset.size
}