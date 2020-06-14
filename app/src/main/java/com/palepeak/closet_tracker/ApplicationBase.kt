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
    var shortAnimationDuration: Int = 0

    @Suppress("DEPRECATION")
    fun vibrate(length: Long) {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(length, 20))
        } else {
            v.vibrate(length)
        }
    }

    override fun onCreate() {
        super.onCreate()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }
}