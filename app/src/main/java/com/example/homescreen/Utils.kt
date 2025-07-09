package com.example.homescreen

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue

object Utils {

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getUsableScreenHeight(activity: Activity): Int {
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.height()
    }
}

fun Context.getActionBarHeight(): Int {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
        TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    } else {
        0
    }
}

