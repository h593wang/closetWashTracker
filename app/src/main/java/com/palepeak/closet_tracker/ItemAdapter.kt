package com.palepeak.closet_tracker

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class ItemAdapter(
    private var context: Activity,
    private val items: ArrayList<ClothesItem>,
    private val itemListener: View.OnClickListener,
    private val itemDeleteListener: View.OnClickListener,
    private val addItemListener: View.OnClickListener,
    private val catId: Int,
    private var editMode: Boolean
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(rowView: View, vt: Int) : RecyclerView.ViewHolder(rowView) {
        val viewType = vt
    }

    //0 == item
    //1 = add item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val rowView = LayoutInflater.from(context).inflate(R.layout.add_button, parent, false)
            return ViewHolder(rowView, viewType)
        }
        val rowView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(rowView, viewType)
    }

    //last item is always the add item button
    override fun getItemViewType(position: Int): Int {
        if (position == items.size) {
            return 1
        }
        return 0
    }

    //add one for the add item button
    override fun getItemCount(): Int {
        //if its empty, we still want a placeholder item
        return items.size + 1
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.viewType == 1) {
            //handling for add item button

            val button = holder.itemView
            //save category id as tag for the onClickListener handling
            button.tag = catId
            button.setOnClickListener(addItemListener)
            button.setBackgroundResource(R.drawable.rectangle_bottom_selector)
            button.findViewById<TextView>(R.id.buttonLabel).setText(R.string.add_item)
            return
        }

        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.itembg))
        val wash = holder.itemView.findViewById<TextView>(R.id.itemWash)
        val name = holder.itemView.findViewById<SingleListenEditText>(R.id.itemName)
        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        val add = holder.itemView.findViewById<TextView>(R.id.addButton)
        val washMax = holder.itemView.findViewById<SingleListenEditText>(R.id.itemWashMax)
        val item = items[position]
        val wornTotal = holder.itemView.findViewById<TextView>(R.id.itemWornTotal)
        val washTotal = holder.itemView.findViewById<TextView>(R.id.itemWashTotal)

        //remove text change listeners
        washMax.removeSingleTextChangedListener()
        name.removeSingleTextChangedListener()
        //setting the text based on the section info
        wash.text = context.resources.getString(R.string.worn, item.worn)
        washMax.setText(item.maxWorn.toString())
        wornTotal.text = context.resources.getString(R.string.totals, item.wornTotal)
        washTotal.text = item.washTotal.toString()
        name.setText(item.name)
        //set text color based on wash data
        if (item.worn >= item.maxWorn) {
            wash.setTextColor(context.resources.getColor(R.color.red))
            washMax.setTextColor(context.resources.getColor(R.color.red))
        }
        else {
            wash.setTextColor(context.resources.getColor(R.color.grey))
            washMax.setTextColor(context.resources.getColor(R.color.grey))
        }

        if (item.photoPath.isEmpty()) {
            photo.setImageResource(R.drawable.round_camera_24)
        } else {
            //load image into imageview using picasso
            Picasso.get()
                .load("file://" + item.photoPath)
                .into(photo)
        }

        //handling for editMode
        if (editMode) {
            //change add button to be item deleter
            add.setOnClickListener(itemDeleteListener)
            add.setText(R.string.remove)

            //editing handling for maxWash
            washMax.setBackgroundResource(R.drawable.bottom_line)
            washMax.isEnabled = true
            washMax.isClickable = true
            washMax.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!editMode) return
                    if (s.toString().isEmpty() || s.toString().toIntOrNull() == null || s.toString().toInt() < 1) {
                        (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id]?.maxWorn = item.maxWorn
                        return
                    }
                    if (!(context.application as ApplicationBase).changedItems.containsKey(item.categoryId.toString() + "-" + item.id)) {
                        (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id] = item.copy()
                    }
                    (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id]?.maxWorn = s.toString().toInt()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })

            //editing handling for name
            name.setBackgroundResource(R.drawable.bottom_line)
            name.isEnabled = true
            name.isClickable = true
            name.addSingleTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (!editMode) return
                    if (s.toString().isEmpty()) {
                        (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id]?.name = item.name
                        return
                    }
                    if (!(context.application as ApplicationBase).changedItems.containsKey(item.categoryId.toString() + "-" + item.id)) {
                        (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id] = item.copy()
                    }
                    (context.application as ApplicationBase).changedItems[item.categoryId.toString() + "-" + item.id]?.name = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })
        } else {
            //change add button back to wear item
            add.setText(R.string.wear)
            add.setOnClickListener {
                (context.application as ApplicationBase).vibrate(10)
                itemListener.onClick(it)
            }

                //disable edit texts
                washMax.setBackgroundResource(0)
                washMax.isEnabled = false
                washMax.isClickable = false
                name.setBackgroundResource(0)
                name.isEnabled = false
                name.isClickable = false

        }
        //set tag to be item id info no matter what
        add.tag = ItemId(item.categoryId, item.id)
    }
}

