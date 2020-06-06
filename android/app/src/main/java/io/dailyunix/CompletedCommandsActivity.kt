package io.dailyunix

class CompletedCommandsActivity : ListActivity() {

    override fun getCommandsForList(): List<String> {
        return model!!.completedCommands.sorted()
    }

}
