package com.example.lifetimerwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class WidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        setContentView(R.layout.widget_configure)

        val yearInput = findViewById<EditText>(R.id.year_input)
        val monthInput = findViewById<EditText>(R.id.month_input)
        val dayInput = findViewById<EditText>(R.id.day_input)
        val hourInput = findViewById<EditText>(R.id.hour_input)
        val minuteInput = findViewById<EditText>(R.id.minute_input)
        val eventNameInput = findViewById<EditText>(R.id.event_name)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        
        // Set current date and time
        val calendar = Calendar.getInstance()
        yearInput.setText(calendar.get(Calendar.YEAR).toString())
        monthInput.setText((calendar.get(Calendar.MONTH) + 1).toString()) // Month is 0-based
        dayInput.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
        hourInput.setText(calendar.get(Calendar.HOUR_OF_DAY).toString())
        minuteInput.setText(calendar.get(Calendar.MINUTE).toString())

        confirmButton.setOnClickListener {
            val eventName = eventNameInput.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Please enter an event name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get and validate year
            val yearStr = yearInput.text.toString()
            if (yearStr.isEmpty()) {
                Toast.makeText(this, "Please enter year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val year = yearStr.toIntOrNull() ?: 0
            if (year < 1900 || year > 2100) {
                Toast.makeText(this, "Year must be between 1900 and 2100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get and validate month
            val monthStr = monthInput.text.toString()
            if (monthStr.isEmpty()) {
                Toast.makeText(this, "Please enter month", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val month = monthStr.toIntOrNull() ?: 0
            if (month !in 1..12) {
                Toast.makeText(this, "Month must be between 1 and 12", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get and validate day
            val dayStr = dayInput.text.toString()
            if (dayStr.isEmpty()) {
                Toast.makeText(this, "Please enter day", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val day = dayStr.toIntOrNull() ?: 0
            if (day !in 1..31) {
                Toast.makeText(this, "Day must be between 1 and 31", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get and validate hour
            val hourStr = hourInput.text.toString()
            if (hourStr.isEmpty()) {
                Toast.makeText(this, "Please enter hour", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val hour = hourStr.toIntOrNull() ?: 0
            if (hour !in 0..23) {
                Toast.makeText(this, "Hour must be between 0 and 23", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get and validate minute
            val minuteStr = minuteInput.text.toString()
            if (minuteStr.isEmpty()) {
                Toast.makeText(this, "Please enter minute", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val minute = minuteStr.toIntOrNull() ?: 0
            if (minute !in 0..59) {
                Toast.makeText(this, "Minute must be between 0 and 59", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val targetCalendar = Calendar.getInstance().apply {
                set(year, month - 1, day, hour, minute) // Month is 0-based
            }
            
            // Check if the selected time is in the future
            if (targetCalendar.timeInMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val targetTime = targetCalendar.timeInMillis

            // Save the target time and event name
            LifeTimerWidget.saveTargetTimePref(this, appWidgetId, targetTime, eventName)

            // Update the widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            LifeTimerWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

            // Show success message
            Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show()

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
} 