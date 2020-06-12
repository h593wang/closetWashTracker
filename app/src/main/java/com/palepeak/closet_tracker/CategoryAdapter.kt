package com.palepeak.closet_tracker

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class CategoryAdapter(
    private var context: Activity,
    val categories: ArrayList<ClothesCategory>,
    val deleteListener: View.OnClickListener,
    val itemListener: View.OnClickListener,
    val itemDeleteListener: View.OnClickListener,
    val addCategoryListener: View.OnClickListener,
    val addItemListener: View.OnClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var changedCategories = HashMap<Int, ClothesCategory>()
    var changedItems = HashMap<String, ClothesItem>()
    var expanded = Array(categories.size) { deleteMode }
    var tempDeleteModeArray = expanded.clone()
    var deleteMode = false
    var count = 0

    class ViewHolder(rowView: View, vt: Int) : RecyclerView.ViewHolder(rowView) {
        val viewType = vt
    }

    fun notifyDataChange(removedIndex: Int) {
        var tmpExpanded = Array(categories.size) { false }
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
            expanded = Array(categories.size) { true }
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
        val rowView =
            LayoutInflater.from(context).inflate(R.layout.category_list_view, parent, false)
        return ViewHolder(rowView, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == count - 1) {
            return 1
        }
        var count = 0
        for (category in categories) {
            if (position == count) return 0
            if (position == count - 1) return 3
            count += category.items.size + 2
        }
        if (position == count) return 0
        if (position == count - 1) return 3
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
            holder.itemView.findViewById<Button>(R.id.button)
                .setOnClickListener(addCategoryListener)
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
            } else {
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

        val wears = holder.itemView.findViewById<SingleListenEditText>(R.id.categoryWears)
        val size = holder.itemView.findViewById<TextView>(R.id.categoryItemSize)
        val name = holder.itemView.findViewById<SingleListenEditText>(R.id.categoryName)
        val expand = holder.itemView.findViewById<ImageView>(R.id.categoryExpand)
        val bg = holder.itemView.findViewById<View>(R.id.categoryItemHolder)
        val clickHolder = holder.itemView.findViewById<View>(R.id.categoryItemClicker)
        //get category for current position
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

        //fields based on category info
        if (expanded[index]) {
            bg.setBackgroundResource(R.drawable.dark_primate_rectangle_top)
            expand.rotation = 0f
        } else {
            bg.setBackgroundResource(R.drawable.dark_primate_rectangle)
            expand.rotation = 180f
        }
        name.removeSingleTextChangedListener()
        wears.removeSingleTextChangedListener()
        wears.setText(category.desiredWorn.toString())
        size.text = "Number of items: " + category.items.size.toString()
        name.setText(category.name)
        clickHolder.setOnClickListener(null)

        //handle delete mode and undoing deletemode
        if (deleteMode) {
            name.setBackgroundResource(R.drawable.bottom_line)
            name.isEnabled = true
            name.isClickable = true
            name.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!deleteMode) return
                    if (s.toString().isNullOrEmpty()) return
                    if (!changedCategories.containsKey(category.id)) {
                        changedCategories[category.id] = category
                    }
                    changedCategories[category.id]?.name = s.toString()
                    category.name = s.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            wears.setBackgroundResource(R.drawable.bottom_line)
            wears.isEnabled = true
            wears.isClickable = true
            wears.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!deleteMode) return
                    if (s.toString().isNullOrEmpty() || s.toString().toIntOrNull() == null) return
                    if (!changedCategories.containsKey(category.id)) {
                        changedCategories[category.id] = category
                    }
                    changedCategories[category.id]?.desiredWorn = s.toString().toInt()
                    category.desiredWorn = s.toString().toInt()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            clickHolder.isClickable = false
            expand.isClickable = true
            expand.setImageResource(R.drawable.delete)
            expand.tag = category.id
            expand.setOnClickListener(deleteListener)
        } else {
            name.setBackgroundResource(0)
            name.isEnabled = false
            name.isClickable = false

            wears.setBackgroundResource(0)
            wears.isEnabled = false
            wears.isClickable = false

            expand.isClickable = false
            clickHolder.isClickable = true
            expand.setImageResource(R.drawable.expand)
            clickHolder.setOnClickListener {
                vibratePhone()
                expanded[index] = !expanded[index]
                notifyDataSetChanged()
            }
        }

        holder.itemView.tag = category.id
    }

    fun handleItem(holder: ViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.itembg))
        val wash = holder.itemView.findViewById<TextView>(R.id.itemWash)
        val name = holder.itemView.findViewById<SingleListenEditText>(R.id.itemName)
        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        val add = holder.itemView.findViewById<Button>(R.id.addButton)
        val dummy = holder.itemView.findViewById<View>(R.id.dummyView)
        val washMax = holder.itemView.findViewById<SingleListenEditText>(R.id.itemWashMax)
        //setting the text based on the section info
        lateinit var item: ClothesItem
        var count = 0
        var index = 0
        for (category in categories) {
            if (position > count && position < count + category.items.size + 1) {
                item = category.items[position - count - 1]
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
            dummy.visibility = View.GONE
            washMax.visibility = View.GONE

        } else {
            holder.itemView.visibility = View.VISIBLE
            wash.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            photo.visibility = View.VISIBLE
            add.visibility = View.VISIBLE
            dummy.visibility = View.VISIBLE
            washMax.visibility = View.VISIBLE
        }

        washMax.removeSingleTextChangedListener()
        name.removeSingleTextChangedListener()
        wash.text = "Worn: " + item.worn + "/"
        washMax.setText(item.maxWorn.toString())
        if (item.worn > item.maxWorn)
            wash.setTextColor(context.resources.getColor(R.color.red))
        else wash.setTextColor(context.resources.getColor(R.color.grey))
        name.setText(item.name)
        if (deleteMode) add.text = "Remove"
        else add.text = "Wear"

        Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build()
            .load("file://" + item.photoPath).into(photo)


        if (deleteMode) {
            add.setOnClickListener(itemDeleteListener)

            washMax.setBackgroundResource(R.drawable.bottom_line)
            washMax.isEnabled = true
            washMax.isClickable = true
            washMax.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!deleteMode) return
                    if (s.toString().isNullOrEmpty() || s.toString().toIntOrNull() == null) return
                    if (!changedItems.containsKey(item.categoryId.toString() + "-" + item.id)) {
                        changedItems[item.categoryId.toString() + "-" + item.id] = item
                    }
                    changedItems[item.categoryId.toString() + "-" + item.id]?.maxWorn =
                        s.toString().toInt()
                    item.maxWorn = s.toString().toInt()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            name.setBackgroundResource(R.drawable.bottom_line)
            name.isEnabled = true
            name.isClickable = true
            name.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!deleteMode) return
                    if (s.toString().isNullOrEmpty()) return
                    if (!changedItems.containsKey(item.categoryId.toString() + "-" + item.id)) {
                        changedItems[item.categoryId.toString() + "-" + item.id] = item
                    }
                    changedItems[item.categoryId.toString() + "-" + item.id]?.name = s.toString()
                    item.name = s.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })
        } else {
            add.setOnClickListener {
                vibratePhone()
                itemListener.onClick(it)
            }

            washMax.setBackgroundResource(0)
            washMax.isEnabled = false
            washMax.isClickable = false
            name.setBackgroundResource(0)
            name.isEnabled = false
            name.isClickable = false
        }
        add.tag = ItemId(item.categoryId, item.id)
    }

    fun vibratePhone() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 29) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(20)
        }
    }
}

