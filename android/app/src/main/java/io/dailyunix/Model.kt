package io.dailyunix

import android.content.Context
import android.util.Log
import java.io.*
import java.lang.IllegalStateException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.random.Random
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

private val logTag = Model::class.java.name

private const val modelFileName = "model.json"
private const val contentDirName = "content"

class Model {

    var versionCode: Int = 0
    var commandOftheDay: Command? = null
    private var commandOfTheDayIndex: Int = 0

    var completedCommands: MutableSet<String> = TreeSet()

    var bookmarkedCommands: MutableSet<String> = TreeSet()

    // For debug purposes only. Remove when stable.
    var notificationHistory: MutableList<String> = ArrayList()

    fun save(context: Context) {
        versionCode = BuildConfig.VERSION_CODE

        val json = Gson().toJson(this)

        Log.d(logTag, "Model: $json")

        modelFile(context).writeText(json)
    }

    // Pick the next command of the day at random. This must be called at least once in the life of
    // the app before commandOftheDay is populated. extractContent() needs to have been called
    // beforehand.
    fun nextCommand(context: Context) {
        val remainingCommands = remainingCommands(context)

        // nextInt() is *exclusive* of the second parameter, ie. it will never return it. This is
        // good because the size of an array is one higher than the index of the last element.
        commandOfTheDayIndex = Random.nextInt(0, remainingCommands.size)

        val randomCommandName = remainingCommands[commandOfTheDayIndex]

        Log.d(logTag, "Picking $randomCommandName, command $commandOfTheDayIndex of " +
                "${remainingCommands.size}")

        commandOftheDay = commandByName(randomCommandName, context)
    }

    fun remainingCommands(context: Context): List<String> {
        return getCommandNames(context, completedCommands)
    }

    fun commandByName(name: String, context: Context): Command {
        val c = Command(name, null, null, null, null)

        val commandDir = File(contentDir(context), c.name)

        // The whatis, package and the man file are all expected to be there, but let's handle any
        // that are missing anyway.
        try {
            c.whatIs = File(commandDir, "whatis.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.w(logTag, "${c.name} has no whatis file")
        }

        try {
            // This file will be present but empty for any commands that are not installed as part
            // of a package.
            c.providerPackage = File(commandDir, "package.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.w(logTag, "${c.name} has no package file")
        }

        try {
            c.tldr = File(commandDir, "tldr.md").readText()
        }
        catch (e: FileNotFoundException) {
            Log.d(logTag, "${c.name} has no tldr file")
        }

        try {
            c.man = File(commandDir, "man.html").readText()
        }
        catch (e: FileNotFoundException) {
            Log.d(logTag, "${c.name} has no man file")
        }

        return c
    }

    fun completionMessage(context: Context): String {
        val commandsNotYetCompleted = getCommandNames(context, completedCommands)
        val totalCommands = completedCommands.size + commandsNotYetCompleted.size
        val percent: Float = (completedCommands.size.toFloat() / totalCommands.toFloat()) * 100

        val m = "Completed ${completedCommands.size} of $totalCommands (${percent.toInt()}%)"

        Log.i(logTag, m)

        return m
    }

    fun toggleBookmark(command: String) {
        if (bookmarkedCommands.contains(command)) {
            bookmarkedCommands.remove(command)

            Log.v(logTag, "$command is now unbookmarked")
        }
        else {
            bookmarkedCommands.add(command)

            Log.v(logTag, "$command is now bookmarked")
        }
    }
}

fun getModel(context: Context): Model {
    val jsonString = modelFile(context).readText()

    if (jsonString.isBlank()) {
        return Model()
    }

    return Gson().fromJson(jsonString, Model::class.java)
}

private fun modelFile(context: Context): File {
    val modelFile = File(context.filesDir, modelFileName)

    if (!modelFile.exists()) {
        val success = modelFile.createNewFile()
        Log.d(logTag, "Created $modelFileName: $success")
    }

    return modelFile
}

private fun contentDir(context: Context): File {
    return File(context.filesDir, contentDirName)
}

/**
 * Extract res/raw/content.zip into [app data]/files/content/.
 */
fun extractContent(context: Context) {
    val contentResourceId = context.resources.getIdentifier("content",
        "raw", context.packageName)

    val contentZip: InputStream =
        context.resources.openRawResource(contentResourceId)

    val zipInputStream = ZipInputStream(BufferedInputStream(contentZip))

    var ze: ZipEntry

    val contentDir = contentDir(context)

    if (contentDir.exists()) {
        Log.i(logTag, "Deleting existing content and overwriting")

        try {
            if (!contentDir.deleteRecursively()) {
                Log.e(logTag, "Can't delete: $contentDir")
            }
        }
        catch (e: Exception) {
            Log.e(logTag, e.message ?: "Can't delete: $contentDir")
        }
    }

    Log.i(logTag, "Writing content to app data:")

    // Create the content directory in app data if it doesn't exist already.
    try {
        contentDir.mkdir()
    }
    catch (e: Exception) {
        Log.e(logTag, e.message ?: "can't mkdir: $contentDir")
    }

    while (true)
    {
        try {
            ze = zipInputStream.nextEntry
        }
        catch (e: IllegalStateException) {
            // We're done.
            break
        }

        val file = File(contentDir, ze.name)

        try {
            file.parentFile?.mkdir()
        }
        catch (e: Exception) {
            Log.e(logTag, e.message ?: "Can't mkdir: ${file.parentFile}")
        }

        val fileOutputStream = FileOutputStream(file)

        Log.i(logTag, "$ze")

        zipInputStream.copyTo(fileOutputStream)

        fileOutputStream.close()
        zipInputStream.closeEntry()
    }

    zipInputStream.close()
}

private fun getCommandNames(context: Context, excluding: Set<String>): List<String> {
    val allCommands: MutableList<String> = ArrayList()

    contentDir(context).listFiles(FileFilter { it.isDirectory })?.forEach {
        if (!excluding.contains(it.name)) {
            allCommands.add(it.name)
        }
    }

    return allCommands
}
