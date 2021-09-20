package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.*

fun Calendar.equalInDateAlone(cal: Calendar): Boolean =
        get(Calendar.YEAR) == cal.get(Calendar.YEAR) && get(Calendar.MONTH) == cal.get(Calendar.MONTH) && get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)

fun Calendar.resetTimeFields(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

abstract class DateChangedBroadcastReceiver : BroadcastReceiver() {

    private var curDate = Calendar.getInstance().resetTimeFields()

    /**called when the receiver detected the date has changed. You should still check it yourself, because you might already be synced with the new date*/
    abstract fun onDateChanged(previousDate: Calendar, newDate: Calendar)

    @Suppress("MemberVisibilityCanBePrivate")
    fun register(context: Context, date: Calendar) {
        curDate = (date.clone() as Calendar).resetTimeFields()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        context.registerReceiver(this, filter)
        val newDate = Calendar.getInstance().resetTimeFields()
        if (!newDate.equalInDateAlone(curDate)) {
            curDate = newDate.clone() as Calendar
            onDateChanged(date, newDate)
        }
    }

    /**a convenient way to auto-unregister when activity/fragment has stopped. This should be called on the onStart method of the fragment/activity*/
    @Suppress("unused")
    fun registerOnStart(activity: FragmentActivity, date: Calendar, fragment: Fragment? = null) {
        register(activity, date)
        val lifecycle = fragment?.lifecycle ?: activity.lifecycle
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                lifecycle.removeObserver(this)
                activity.unregisterReceiver(this@DateChangedBroadcastReceiver)
            }
        })
    }

    override fun onReceive(context: Context, intent: Intent) {
        val newDate = Calendar.getInstance().resetTimeFields()
        if (!newDate.equalInDateAlone(curDate)) {
            val previousDate = curDate
            curDate = newDate
            onDateChanged(previousDate, newDate)
        }
    }
}