package io.dailyunix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.*
import java.util.concurrent.TimeUnit

private val tag = NotificationWorker::class.java.name

private const val channelId = "daily"

// Package level function
fun reschedule(appContext: Context) {
    // Thanks: https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
    val dueDate = Calendar.getInstance()
    val currentDate = Calendar.getInstance()

    // Execute at around 06:45.
    dueDate.set(Calendar.HOUR_OF_DAY, 6)
    dueDate.set(Calendar.MINUTE, 45)
    dueDate.set(Calendar.SECOND, 0)

    // If it's now after 06:45, we mean 06:45 tomorrow.
    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

    Log.d(tag, "Next notification scheduled for ${timeDiff/1000/60/60} hours from now.")

    // This is a one-time request. We'll need to reschedule on every run.
    val dailyWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
        .addTag(NotificationWorker::class.java.name)
        .build()

    // Replace the existing scheduled request if there is one.
    WorkManager.getInstance(appContext).cancelAllWorkByTag(NotificationWorker::class.java.name)

    WorkManager.getInstance(appContext).enqueue(dailyWorkRequest)
}

fun createNotificationChannel(appContext: Context) {
    // Note that if we ever want to support N-, we should only execute this code for O+.
    // Notification channels were new in O.

    val name = appContext.getString(R.string.app_name)
    val descriptionText = appContext.getString(R.string.channel_name)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelId, name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun showNotification(appContext: Context, title: String, text: String, intent: Intent) {
    val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0,
        intent, 0)

    val builder = NotificationCompat.Builder(appContext, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO (P2): Notification icon
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(appContext)) {
        // We only have one notification, so the notification ID can be the same every time.
        val notificationId = 1
        notify(notificationId, builder.build())
    }
}

class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams) {

    override fun startWork(): ListenableFuture<Result> {
        Log.i(tag, "Popping notification for command of the day.")

        val model: Model = getModel(applicationContext)

        // For debugging purposes only. Remove when stable.
        val timestamp = Date().toInstant().toString()
        model.notificationHistory.add(timestamp)

        // Now is the time to advance to the next random command to show.
        model.nextCommand(applicationContext)
        model.save(applicationContext)

        // Make sure we don't call Model.nextCommand() again before the user taps on the
        // notification, otherwise the activity will show a different command to the notification.
        val command = model.commandOftheDay

        val intent = Intent(applicationContext, MainActivity::class.java)

        if (command != null) {
            var text: String? = command.whatIs

            // For a command with no whatis string, use only the first line of the tldr.
            if (text == null && command.tldr != null) {
                text = command.tldr?.split("\n")?.get(0)
            }
            // And for a command without either, use the first line from the man page.
            else if (command.man != null) {
                text = command.man?.split("\n")?.get(0)
            }
            else {
                text = "Tap for more info"
            }

            showNotification(applicationContext, command.name, text!!, intent)
        }

        return CallbackToFutureAdapter.getFuture {
            reschedule(applicationContext)
            it.set(Result.success())
        }
    }
}
