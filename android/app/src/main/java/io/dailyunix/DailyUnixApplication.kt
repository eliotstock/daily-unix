package io.dailyunix

import android.app.Application
import android.util.Log

class DailyUnixApplication : Application() {

    private val tag = DailyUnixApplication::class.java.name

    override fun onCreate() {
        super.onCreate()

        Log.v(tag, "onCreate()")

        val model: Model = getModel(applicationContext)

        if (model.versionCode == 0 || model.versionCode < BuildConfig.VERSION_CODE) {
            Log.v(tag, "First run on v${BuildConfig.VERSION_CODE}. Extracting content and" +
                    " (re)scheduling notification")

            extractContent(applicationContext)

            createNotificationChannel(applicationContext)

            reschedule(applicationContext)

            model.nextCommand(applicationContext)
            model.save(applicationContext)
        }
    }
}