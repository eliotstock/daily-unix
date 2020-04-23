package io.dailyunix

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.name

    private val contentDirName = "content"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        extractContent()

        // TODO (P0): Get this from the Intent that started us. Pass it in from the local
        //  notification.
        val command = "ar"
        val commandDir = File(contentDir(), command)

        val whatIs = File(commandDir, "whatis.txt").readText()
        val tldr = File(commandDir, "tldr.md").readText()
        val man = File(commandDir, "man.txt").readText()

        commandName.setText(command)

        // TODO (P0): Add the whatis string to the command TextView, underneath the command name.

        page.setText(tldr)

        // TODO (P0): Wire a listener up to the tabs. Change the contents of the page on taps.

        // Schedule local notifications for every morning.
        reschedule(applicationContext)

        // createNotificationChannel(applicationContext)
    }

    private fun contentDir(): File {
        return File(applicationContext.filesDir, contentDirName)
    }

    /**
     * Extract res/raw/content.zip into [app data]/files/content/.
     */
    private fun extractContent() {
        val contentResourceId = applicationContext.resources.getIdentifier("content",
            "raw", packageName)

        val contentZip: InputStream =
            applicationContext.resources.openRawResource(contentResourceId)

        val zipInputStream = ZipInputStream(BufferedInputStream(contentZip))

        var ze: ZipEntry

        val contentDir = contentDir()

        if (contentDir.exists()) {
            // TODO (P1): Only extract this if the date on the file is more recent than the app
            //  data, or on the first run after each update of the app.
            return
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

}
