package com.palepeak.closet_tracker

import android.view.View
import java.io.Serializable
data class ClothesItem (var id: Int, var categoryId: Int, var worn: Int, var maxWorn: Int, var name: String, var photoPath: String) : Serializable
data class ClothesCategory (var id: Int, var desiredWorn: Int, var name: String, var items: ArrayList<ClothesItem>) : Serializable
data class SaveData(var activeItems: ArrayList<ClothesItem>, var savedCategories: ArrayList<ClothesCategory>): Serializable
data class ItemId(var catId: Int, var itemId: Int) : Serializable
