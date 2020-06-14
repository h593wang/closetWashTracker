package com.palepeak.closet_tracker

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList


class CategoryAdapter(
    private var context: Activity,
    private val categories: ArrayList<ClothesCategory>,
    private val deleteListener: View.OnClickListener,
    private val itemListener: View.OnClickListener,
    private val itemDeleteListener: View.OnClickListener,
    private val addCategoryListener: View.OnClickListener,
    private val addItemListener: View.OnClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var editMode = false

    class ViewHolder(rowView: View, vt: Int) : RecyclerView.ViewHolder(rowView) {
        val viewType = vt
    }

    //toggle between edit and nonedit mode
    fun toggleMode() {
        editMode = !editMode
        notifyDataSetChanged()
    }

    //0 == category
    //1 == add category button
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val rowView = LayoutInflater.from(context).inflate(R.layout.add_button, parent, false)
            return ViewHolder(rowView, viewType)
        }
        val rowView =
            LayoutInflater.from(context).inflate(R.layout.category_list_view, parent, false)
        return ViewHolder(rowView, viewType)
    }

    //last item is always the add category button
    override fun getItemViewType(position: Int): Int {
        if (position == categories.size) {
            return 1
        }
        return 0
    }

    //add one item for the add category item
    override fun getItemCount(): Int {
        //if its empty, we still want a placeholder item
        return categories.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.viewType == 1) {
            //initialize add category button
            val button = holder.itemView
            button.setOnClickListener(addCategoryListener)
            button.setBackgroundResource(R.drawable.rectangle_bg_grey_selector)
            button.findViewById<TextView>(R.id.buttonLabel).setText(R.string.add_category)
            return
        }

        val wears = holder.itemView.findViewById<SingleListenEditText>(R.id.categoryWears)
        val size = holder.itemView.findViewById<TextView>(R.id.categoryItemSize)
        val name = holder.itemView.findViewById<SingleListenEditText>(R.id.categoryName)
        val bg = holder.itemView.findViewById<View>(R.id.categoryWrapper)
        //clickHolder holds the onClickListener for expanding category
        val clickHolder = holder.itemView.findViewById<View>(R.id.categoryItemClicker)
        val icon = holder.itemView.findViewById<ImageView>(R.id.categoryIcon)
        val itemsList = holder.itemView.findViewById<RecyclerView>(R.id.categoryItemsList)

        //get category for current position
        val category = categories[position]

        //initialize items recyclerview
        val adapter = ItemAdapter(context, category.items, itemListener, itemDeleteListener, addItemListener, category.id, editMode)
        val linearLayoutManager =  LinearLayoutManager(context)
        itemsList.layoutManager = linearLayoutManager
        itemsList.adapter = adapter

        //handle expanded info
        if (editMode || category.expanded) {
            bg.setBackgroundResource(R.drawable.rectangle_category_expanded)
            icon.rotation = 0f
            itemsList.visibility = View.VISIBLE
        } else {
            bg.setBackgroundResource(R.drawable.rectangle_category_header)
            icon.rotation = 180f
            itemsList.visibility = View.GONE
        }

        //set fields based on category data
        name.removeSingleTextChangedListener()
        wears.removeSingleTextChangedListener()
        wears.setText(category.desiredWorn.toString())
        size.text = context.resources.getString(R.string.number_of_items, category.items.size)
        name.setText(category.name)
        clickHolder.setOnClickListener(null)

        //handle edit mode and undoing edit mode
        if (editMode) {
            //make name into edit mode
            name.setBackgroundResource(R.drawable.bottom_line)
            name.bringToFront()
            name.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!editMode) return
                    if (s.toString().isEmpty()) return
                    //save change to changedCategories
                    if (!(context.application as ApplicationBase).changedCategories.containsKey(category.id)) {
                        (context.application as ApplicationBase).changedCategories[category.id] = category
                    }
                    (context.application as ApplicationBase).changedCategories[category.id]?.name = s.toString()
                    category.name = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            //change wears to edit mode
            wears.setBackgroundResource(R.drawable.bottom_line)
            wears.bringToFront()
            wears.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!editMode) return
                    if (s.toString().isEmpty() || s.toString().toIntOrNull() == null || s.toString().toInt() < 1) return
                    //save change to changedCategories
                    if (!(context.application as ApplicationBase).changedCategories.containsKey(category.id)) {
                        (context.application as ApplicationBase).changedCategories[category.id] = category
                    }
                    (context.application as ApplicationBase).changedCategories[category.id]?.desiredWorn = s.toString().toInt()
                    category.desiredWorn = s.toString().toInt()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            //clickHolder holds the clicker for expanding
            clickHolder.isClickable = false
            //make the icon delete, and add the delete click listener to it
            icon.isClickable = true
            icon.setImageResource(R.drawable.round_delete_forever_24)
            icon.tag = category.id
            icon.setOnClickListener(deleteListener)
        } else {
            //disable name and wears editing
            name.setBackgroundResource(0)
            wears.setBackgroundResource(0)
            //disable delete icon and change it to the expand icon
            icon.isClickable = false
            //add expand handling to clickHolder
            clickHolder.isClickable = true
            clickHolder.bringToFront()
            icon.setImageResource(R.drawable.expand)
            clickHolder.setOnClickListener {
                (context.application as ApplicationBase).vibrate(10)
                category.expanded = !category.expanded
                if (editMode || category.expanded) {
                    bg.setBackgroundResource(R.drawable.rectangle_category_expanded)
                    icon.rotation = 0f
                    itemsList.visibility = View.VISIBLE
                } else {
                    bg.setBackgroundResource(R.drawable.rectangle_category_header)
                    icon.rotation = 180f
                    itemsList.visibility = View.GONE
                }
            }
        }
    }
}

