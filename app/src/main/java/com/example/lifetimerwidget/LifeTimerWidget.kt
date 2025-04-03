package com.example.lifetimerwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LifeTimerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                intent.component
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_TARGET_TIME_KEY + appWidgetId)
            prefs.remove(PREF_TARGET_EVENT_KEY + appWidgetId)
            prefs.apply()
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.lifetimerwidget.UPDATE_WIDGET"
        private const val PREFS_NAME = "LifeTimerWidgetPrefs"
        private const val PREF_TARGET_TIME_KEY = "target_time_"
        private const val PREF_TARGET_EVENT_KEY = "target_event_"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.life_timer_widget)
            
            // Get target time and event from preferences
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val targetTime = prefs.getLong(PREF_TARGET_TIME_KEY + appWidgetId, 0)
            val targetEvent = prefs.getString(PREF_TARGET_EVENT_KEY + appWidgetId, "Event")
            
            if (targetTime != 0L) {
                val remainingHours = calculateRemainingHours(targetTime)
                views.setTextViewText(R.id.remaining_hours, remainingHours.toString())
                views.setTextViewText(R.id.target_event, targetEvent)
            } else {
                views.setTextViewText(R.id.remaining_hours, "Set time")
                views.setTextViewText(R.id.target_event, "")
            }

            // Create an Intent to launch the configuration activity
            val configIntent = Intent(context, WidgetConfigureActivity::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_layout, configPendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun calculateRemainingHours(targetTime: Long): Long {
            val currentTime = System.currentTimeMillis()
            val diffInMillis = targetTime - currentTime
            return TimeUnit.MILLISECONDS.toHours(diffInMillis)
        }

        fun saveTargetTimePref(context: Context, appWidgetId: Int, targetTime: Long, targetEvent: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putLong(PREF_TARGET_TIME_KEY + appWidgetId, targetTime)
            prefs.putString(PREF_TARGET_EVENT_KEY + appWidgetId, targetEvent)
            prefs.apply()
        }

        fun loadTargetTimePref(context: Context, appWidgetId: Int): Pair<Long, String> {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val targetTime = prefs.getLong(PREF_TARGET_TIME_KEY + appWidgetId, 0)
            val targetEvent = prefs.getString(PREF_TARGET_EVENT_KEY + appWidgetId, "Event") ?: "Event"
            return Pair(targetTime, targetEvent)
        }
    }
} 