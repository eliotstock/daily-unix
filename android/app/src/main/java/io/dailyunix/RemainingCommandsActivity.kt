package io.dailyunix

class RemainingCommandsActivity : ListActivity() {

    override fun getCommandsForList(): List<String> {
        return model!!.remainingCommands(applicationContext).sorted()
    }

}
