package com.palepeak.closet_tracker

import android.app.Application

class ApplicationBase: Application() {
    //changed item tracker for edit mode
    var changedCategories = HashMap<Int, ClothesCategory>()
    var changedItems = HashMap<String, ClothesItem>()
}