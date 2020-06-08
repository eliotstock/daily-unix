package io.dailyunix

class RemainingCommandsFragment : ListFragment() {

    override fun getCommandsForList(): List<String> {
        return model!!.remainingCommands(requireContext()).sorted()
    }

}
