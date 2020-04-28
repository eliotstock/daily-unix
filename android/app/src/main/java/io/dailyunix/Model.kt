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
        val allCommands = getAllCommands(context)

        // TODO (P2): Check for out by one error. Is this inclusive of allCommands.size?
        // TODO (P1): first remove commands that have already been seen by the user.
        commandOfTheDayIndex = Random.nextInt(0, allCommands.size)

        val randomCommandName = allCommands[commandOfTheDayIndex]

        Log.d(tag, "Picking $randomCommandName, command $commandOfTheDayIndex of " +
                "${allCommands.size}")

        commandOftheDay = Command(randomCommandName, null, null, null)

        val commandDir = File(contentDir(context), commandOftheDay!!.name)

        // Both the whatis and the man file are expected to be there, but let's handle any that are
        // missing.
        try {
            commandOftheDay!!.whatIs = File(commandDir, "whatis.txt").readText()
        }
        catch (e: FileNotFoundException) {
            Log.d(tag, "${commandOftheDay!!.name} has no whatis file")
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
}

fun getModel(context: Context): Model {
    val jsonString = modelFile(context).readText()

    if (jsonString.isBlank()) {
        return Model()
    }

    val model = Gson().fromJson(jsonString, Model::class.java)

    return model
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
        // TODO (P2): We should really delete everything and replace it here. Simply overwriting
        //  makes it impossible to remove a directory in a subsequent release.
        Log.i(tag, "Overwriting existing content")
    }

    Log.i(tag, "Writing content to app data:")

    // Create the content directory in app data if it doesn't exist already.
    try {
        contentDir.mkdir()
    }
    catch (e: Exception) {
        Log.e(tag, e.message)
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

        val file: File = File(contentDir, ze.name)

        try {
            file.parentFile.mkdir()
        }
        catch (e: Exception) {
            Log.e(tag, e.message)
        }

        val fileOutputStream: FileOutputStream = FileOutputStream(file)

        Log.i(tag, "$ze.name")

        zipInputStream.copyTo(fileOutputStream)

        fileOutputStream.close();
        zipInputStream.closeEntry();
    }

    zipInputStream.close();
}

private fun getAllCommands(context: Context): List<String> {
    val allCommands: MutableList<String> = ArrayList()

    contentDir(context).listFiles(FileFilter { it.isDirectory })?.forEach {
        allCommands.add(it.name)
    }

    return allCommands
}
