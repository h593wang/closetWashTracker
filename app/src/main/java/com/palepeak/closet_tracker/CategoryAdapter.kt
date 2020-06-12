package com.palepeak.closet_tracker

import android.animation.ObjectAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class CategoryAdapter(private var context: Activity,val categories: ArrayList<ClothesCategory>,
                      val deleteListener: View.OnClickListener, val itemListener:View.OnClickListener, val itemDeleteListener: View.OnClickListener,
                      val addCategoryListener: View.OnClickListener, val addItemListener: View.OnClickListener)
    : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var expanded = Array(categories.size) {deleteMode}
    var tempDeleteModeArray = expanded.clone()
    var deleteMode = false
    var count = 0

    class ViewHolder(rowView: View, vt: Int) : RecyclerView.ViewHolder(rowView) {
        val viewType = vt
    }

    fun notifyDataChange(removedIndex: Int) {
        var tmpExpanded = Array(categories.size) {false}
        var offset = 0
        for (i in expanded.indices) {
            if (i == removedIndex) offset = 1
            if (i < tmpExpanded.size) tmpExpanded[i] = expanded[i + offset]
        }
        expanded = tmpExpanded
        notifyDataSetChanged()
    }

    fun toggleMode() {
        if (deleteMode) {
            expanded = tempDeleteModeArray
        } else {
            tempDeleteModeArray = expanded
            expanded = Array(categories.size) {true}
        }
        deleteMode = !deleteMode
        notifyDataSetChanged()
    }

    //0 == category
    //1 == add category button
    //2 == item
    //3 = add item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val rowView = LayoutInflater.from(context).inflate(R.layout.add_button, parent, false)
            return ViewHolder(rowView, viewType)
        }
        if (viewType == 2) {
            val rowView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            return ViewHolder(rowView, viewType)
        }
        if (viewType == 3) {
            val rowView = LayoutInflater.from(context).inflate(R.layout.add_button, parent, false)
            return ViewHolder(rowView, viewType)
        }
        val rowView = LayoutInflater.from(context).inflate(R.layout.category_list_view, parent, false)
        return ViewHolder(rowView, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == count-1) {
            return 1
        }
        var count = 0
        for (category in categories) {
            if (position == count) return 0
            if (position == count-1) return 3
            count += category.items.size + 2
        }
        if (position == count) return 0
        if (position == count-1) return 3
        return 2
    }

    override fun getItemCount(): Int {
        //if its empty, we still want a placeholder item
        count = categories.size + 1
        for (category in categories) {
            count += category.items.size + 1
        }
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.viewType == 1) {
            holder.itemView.findViewById<Button>(R.id.button).setOnClickListener(addCategoryListener)
            holder.itemView.findViewById<Button>(R.id.button).text = "Add Category"
            return
        }
        if (holder.viewType == 3) {
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.itembg))
            var count = 0
            var index = 0
            val button = holder.itemView.findViewById<Button>(R.id.button)
            button.setOnClickListener(addItemListener)
            for (cat in categories) {
                if (count + cat.items.size + 1 == position) {
                    button.tag = cat.id
                    break
                }
                index++
                count += cat.items.size + 2
            }

            if (!expanded[index]) {
                holder.itemView.visibility = View.GONE
                button.visibility = View.GONE
            }
            else {
                holder.itemView.visibility = View.VISIBLE
                button.visibility = View.VISIBLE
            }
            holder.itemView.setBackgroundResource(R.drawable.rectangle_bottom)
            button.text = "Add Item"
            return
        }
        if (holder.viewType == 2) {
            handleItem(holder, position)
            return
        }

        //data class ClothesCategory (var id: Int, var desiredWorn: Int, var name: String, var items: ArrayList<ClothesItem>) : Serializable

        val wears = holder.itemView.findViewById<TextView>(R.id.categoryWears)
        val size = holder.itemView.findViewById<TextView>(R.id.categoryItemSize)
        val name = holder.itemView.findViewById<TextView>(R.id.categoryName)
        val expand = holder.itemView.findViewById<ImageView>(R.id.categoryExpand)
        val bg = holder.itemView.findViewById<View>(R.id.categoryItemBackground)
        //setting the text based on the section info
        var category = categories[0]
        var count = 0
        var index = 0
        for (cat in categories) {
            if (count == position) {
                category = cat
                break
            }
            index++
            count += cat.items.size + 2
        }

        if (expanded[index]) bg.setBackgroundResource(R.drawable.dark_primate_rectangle_top)
        else bg.setBackgroundResource(R.drawable.dark_primate_rectangle)
        wears.text = "Default Wears: " + category.desiredWorn.toString()
        size.text = "Number of items: " + category.items.size.toString()
        name.text = category.name
        if (deleteMode) {
            expand.setImageResource(R.drawable.delete)
            bg.tag = category.id
            bg.setOnClickListener(deleteListener)
        } else {
            expand.setImageResource(R.drawable.expand)
            bg.setOnClickListener {
                expand.pivotX = expand.width/2f
                expand.pivotY = expand.height/2f
                val rotate = ObjectAnimator.ofFloat(expand, "rotation", expand.rotation, expand.rotation+180)
                rotate.duration = 150
                rotate.interpolator = LinearInterpolator()
                rotate.start()
                rotate.doOnEnd {
                    if (expanded[index]){
                        expand.rotation = 0f
                    } else expand.rotation = 180f
                }
                expanded[index] = !expanded[index]
                notifyDataSetChanged()
            }
        }

        holder.itemView.tag = category.id
    }

    fun handleItem(holder: ViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.itembg))
        val wash = holder.itemView.findViewById<TextView>(R.id.itemWash)
        val name = holder.itemView.findViewById<TextView>(R.id.itemName)
        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        val add = holder.itemView.findViewById<Button>(R.id.addButton)
        //setting the text based on the section info
        lateinit var item: ClothesItem
        var count = 0
        var index = 0
        for (category in categories) {
            if (position > count && position < count + category.items.size + 1) {
                item = category.items[position-count-1]
                break
            }
            index++
            count += category.items.size + 2
        }

        if (!expanded[index]) {
            holder.itemView.visibility = View.GONE
            wash.visibility = View.GONE
            name.visibility = View.GONE
            photo.visibility = View.GONE
            add.visibility = View.GONE
        }
        else {
            holder.itemView.visibility = View.VISIBLE
            wash.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            photo.visibility = View.VISIBLE
            add.visibility = View.VISIBLE
        }

        wash.text = item.worn.toString() + "/" + item.maxWorn.toString()
        name.text = item.name
        if (deleteMode) add.text = "Remove"
        else add.text = "Wear"

        Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build().load("file://"+item.photoPath).into(photo)


        if (deleteMode) {
            add.setOnClickListener(itemDeleteListener)
        } else {
            add.setOnClickListener(itemListener)
        }
        add.tag = ItemId(item.categoryId, item.id)
    }

}

