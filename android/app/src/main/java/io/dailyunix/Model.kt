package io.dailyunix

import android.content.Context
import android.util.Log
import java.io.*
import java.lang.IllegalStateException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.random.Random
import com.google.gson.Gson

private val tag = Model::class.java.name

private const val modelFileName = "model.json"
private const val contentDirName = "content"

class Model {
    var versionCode: Int = 0
    var commandOftheDay: Command? = null
    private var commandOfTheDayIndex: Int = 0

    var completedCommands: MutableSet<String> = HashSet()

    // For debug purposes only. Remove when stable.
    var notificationHistory: MutableList<String> = ArrayList()

    fun save(context: Context) {
        versionCode = BuildConfig.VERSION_CODE

        val json = Gson().toJson(this)

        Log.d(tag, "Model: $json")

        modelFile(context).writeText(json)
    }

    // Pick the next command of the day at random. This must be called at least once in the life of
    // the app before commandOftheDay is populated. extractContent() needs to have been called
    // beforehand.
    fun nextCommand(context: Context) {
        val commandsNotYetCompleted = getCommandNames(context, completedCommands)

        // nextInt() is *exclusive* of the second parameter, ie. it will never return it. This is
        // good because the size of an array is one higher than the index of the last element.
        commandOfTheDayIndex = Random.nextInt(0, commandsNotYetCompleted.size)

        val randomCommandName = commandsNotYetCompleted[commandOfTheDayIndex]

        Log.d(tag, "Picking $randomCommandName, command $commandOfTheDayIndex of " +
                "${commandsNotYetCompleted.size}")

        commandOftheDay = Command(randomCommandName, null, null, null,
                null)

        val commandDir = File(contentDir(context), commandOftheDay!!.name)

        // The whatis, package and the man file are all expected to be there, but let's handle any
        // that are missing anyway.
        try {
            commandOftheDay!!.whatIs = File(commandDir, "whatis.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.w(tag, "${commandOftheDay!!.name} has no whatis file")
        }

        try {
            commandOftheDay!!.providerPackage = File(commandDir, "package.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.w(tag, "${commandOftheDay!!.name} has no package file")
        }

        try {
            commandOftheDay!!.tldr = File(commandDir, "tldr.md").readText()
        }
        catch (e: FileNotFoundException) {
            Log.d(tag, "${commandOftheDay!!.name} has no tldr file")
        }

        try {
            commandOftheDay!!.man = File(commandDir, "man.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.d(tag, "${commandOftheDay!!.name} has no man file")
        }
    }

    fun completionMessage(context: Context): String {
        val commandsNotYetCompleted = getCommandNames(context, completedCommands)
        val totalCommands = completedCommands.size + commandsNotYetCompleted.size
        val percent: Float = (completedCommands.size.toFloat() / totalCommands.toFloat()) * 100

        val m = "Completed ${completedCommands.size} of ${totalCommands} (${percent.toInt()}%)"

        Log.i(tag, m)

        return m
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
        Log.d(tag, "Created $modelFileName: $success")
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
        Log.i(tag, "Deleting existing content and overwriting")

        try {
            contentDir.delete()
        }
        catch (e: Exception) {
            Log.e(tag, e.message ?: "Can't delete: {contentDir}")
        }
    }

    Log.i(tag, "Writing content to app data:")

    // Create the content directory in app data if it doesn't exist already.
    try {
        contentDir.mkdir()
    }
    catch (e: Exception) {
        Log.e(tag, e.message ?: "can't mkdir: {contentDir}")
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
            Log.e(tag, e.message ?: "Can't mkdir: {file.parentFile}")
        }

        val fileOutputStream = FileOutputStream(file)

        Log.i(tag, "$ze.name")

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
