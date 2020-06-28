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
import io.dailyunix.Constants.ARGUMENT_COMMAND
import java.util.*
import java.util.concurrent.TimeUnit

private val tag = NotificationWorker::class.java.name

private const val channelId = "daily"

fun reschedule(appContext: Context) {
    // Thanks: https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
    val dueDate = Calendar.getInstance()
    val currentDate = Calendar.getInstance()

    // Execute at around this time every day.
    dueDate.set(Calendar.HOUR_OF_DAY, 6)
    dueDate.set(Calendar.MINUTE, 30)
    dueDate.set(Calendar.SECOND, 0)

    // If it's now after that time, we mean that time tomorrow.
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
        intent, PendingIntent.FLAG_UPDATE_CURRENT)

    // Note: do not add a large icon here. The Android notification developer guide says that these
    // are "usually used only for contact photos; do not use it for your app icon".
    val builder = NotificationCompat.Builder(appContext, channelId)
        .setSmallIcon(android.R.drawable.ic_menu_info_details) // TODO (P2): Notification icon
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
        // TODO (P2): Test what happens if we let a day pass and have two notifications. Do they
        //  both go to the command shown on them? Do we even get two notifications or does the
        //  second replace the first?
        val command = model.commandOftheDay

        val intent = Intent(applicationContext, MainActivity::class.java)

        // Keep in mind that the user could easily be advancing through commands in between this
        // notification being presented and the user tapping on it. The command of the day might
        // have moved on. Pass the command that's on this notification in the intent extras for
        // the CommandFragment to pick up.
        intent.putExtra(ARGUMENT_COMMAND, command?.name)

        if (command != null) {
            // The indexing script should make sure that only commands with a whatis end up in the
            // content zip.
            val text: String = command.whatIs ?: "Tap for more info"

            showNotification(applicationContext, command.name, text, intent)
        }

        return CallbackToFutureAdapter.getFuture {
            reschedule(applicationContext)
            it.set(Result.success())
        }
    }
}
