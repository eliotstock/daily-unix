package io.dailyunix

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_content.*

class ManFragment : Fragment() {

    private val logTag = ManFragment::class.java.name

    private val viewModel: CommandViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.command.observe(viewLifecycleOwner, Observer { command ->
            Log.d(logTag, "Observing change to command: ${command.name}")

            if (command?.man.isNullOrBlank()) {
                contentText.text = getString(R.string.no_man)
            } else {
                val html: Spanned  = Html.fromHtml(command?.man, 0)
                contentText.text = html
            }

            scrollView.fullScroll(ScrollView.FOCUS_UP)

            contentText.breakStrategy
        })
    }

}
