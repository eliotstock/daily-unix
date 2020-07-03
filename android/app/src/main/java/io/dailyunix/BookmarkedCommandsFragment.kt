package io.dailyunix

class BookmarkedCommandsFragment : ListFragment() {

    override fun getCommandsForList(): List<String> {
        return model!!.bookmarkedCommands.sorted()
    }

}
