package com.palepeak.closet_tracker

import java.io.Serializable

//Data class for individual item, wash is it's wash status if it's an active item
data class ClothesItem (var id: Int, var categoryId: Int, var worn: Int, var maxWorn: Int, var name: String, var photoPath: String, var wash: Boolean, var wornTotal: Int, var washTotal: Int) : Serializable
//Data class for category, expanded is if it's expanded in the main catalog
data class ClothesCategory (var id: Int, var desiredWorn: Int, var name: String, var items: ArrayList<ClothesItem>, var expanded: Boolean) : Serializable
data class SaveData(var activeItems: ArrayList<ClothesItem>, var savedCategories: ArrayList<ClothesCategory>): Serializable
data class ItemId(var catId: Int, var itemId: Int) : Serializable
