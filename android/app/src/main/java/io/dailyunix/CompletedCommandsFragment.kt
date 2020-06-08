package io.dailyunix

class CompletedCommandsFragment : ListFragment() {

    override fun getCommandsForList(): List<String> {
        return model!!.completedCommands.sorted()
    }

}
