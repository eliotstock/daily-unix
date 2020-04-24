package io.dailyunix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.*
import java.util.concurrent.TimeUnit

private const val channelId = "daily"

// Package level function
fun reschedule(appContext: Context) {
    // Thanks: https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
    val dueDate = Calendar.getInstance()
    val currentDate = Calendar.getInstance()

    // Set Execution around 08:45:00
    dueDate.set(Calendar.HOUR_OF_DAY, 8)
    dueDate.set(Calendar.MINUTE, 45)
    dueDate.set(Calendar.SECOND, 0)

    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

    val dailyWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
        .addTag(NotificationWorker::class.java.name)
        .build()

    WorkManager.getInstance(appContext).enqueue(dailyWorkRequest)

//    val workRequest = PeriodicWorkRequest.Builder(NotificationWorker::class.java, 24,
//        TimeUnit.HOURS).build()
//    WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
//            NotificationWorker::class.java.name, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
}

// TODO (P1): Call this exactly once in the lifetime of the application, such as when the welcome
//  activity is shown.
fun createNotificationChannel(appContext: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // TODO (P2): Use appContext.getString()
        val name = "Daily Unix"
        val descriptionText = "Command of the day"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showNotification(appContext: Context, command: String, whatis: String) {
    val builder = NotificationCompat.Builder(appContext, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO (P2): Notification icon
        .setContentTitle("Daily Unix Command")
        .setContentText("$command. $whatis")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    // TODO (P1): Add tap action.

    with(NotificationManagerCompat.from(appContext)) {
        // We only have one notification, so the notification ID can be the same every time.
        val notificationId = 1
        notify(notificationId, builder.build())
    }
}

class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams) {

    private val tag = NotificationWorker::class.java.name

    override fun startWork(): ListenableFuture<Result> {
        Log.i(tag, "Popping notification for command of the day.")

        showNotification(applicationContext, "ar", "Create a tape archive file")

        return CallbackToFutureAdapter.getFuture {
            it.set(ListenableWorker.Result.success())
        }
    }
}
