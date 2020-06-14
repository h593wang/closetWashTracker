package com.palepeak.closet_tracker

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator


class ApplicationBase: Application() {
    //changed item tracker for edit mode
    var changedCategories = HashMap<Int, ClothesCategory>()
    var changedItems = HashMap<String, ClothesItem>()

    @Suppress("DEPRECATION")
    fun vibrate(length: Long) {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(length, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(length)
        }
    }
}