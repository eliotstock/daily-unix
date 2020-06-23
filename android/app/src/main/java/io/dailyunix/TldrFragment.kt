package io.dailyunix

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_content.*

class TldrFragment : Fragment() {

    private val logTag = TldrFragment::class.java.name

    private val viewModel: CommandViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.command.observe(viewLifecycleOwner, Observer { command ->
            Log.d(logTag, "Observing change to command: ${command.name}")

            if (command?.tldr.isNullOrBlank()) {
                contentText.text = getString(R.string.no_tldr)
            } else {
                // Remove the heading before rendering the markdown. We already show the command
                // name above.
                val tldrWithoutHeading = command?.tldr!!.replace(Regex("^#.*"), "")

                // Markwon is a markdown renderer that doesn't need a WebView:
                //   https://noties.io/Markwon/docs/v4/core/getting-started.html
                val markwon = Markwon.create(requireContext())
                markwon.setMarkdown(contentText, tldrWithoutHeading)
            }

            scrollView.fullScroll(ScrollView.FOCUS_UP)
        })
    }

}
