package com.palepeak.closet_tracker

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*

//adapter for today's outfit recyclerview
class PreviewAdapter(private var context: Activity, private val clothes: ArrayList<ClothesItem>, private val deleteListener:View.OnClickListener) : RecyclerView.Adapter<PreviewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = LayoutInflater.from(context).inflate(R.layout.list_item_box, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        //if its empty, we still want a placeholder item
        if (clothes.size == 0) return 1
        return clothes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //handling for placeholder item
        if (clothes.size == 0) {
            //handling for placeholder item
            holder.itemView.findViewById<View>(R.id.itemPreview).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWashLabel).setText(R.string.outfit_preview_placeholder)
            holder.itemView.findViewById<View>(R.id.itemWashBox).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemName).visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.itemDelete).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = ""
            holder.itemView.isSelected = false
            holder.itemView.findViewById<View>(R.id.arrow).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWash).text = ""
            holder.itemView.setOnClickListener {  }
            return
        }

        holder.itemView.findViewById<View>(R.id.arrow).visibility = View.VISIBLE
        holder.itemView.findViewById<TextView>(R.id.itemWashLabel).setText(R.string.wash_item)
        //set the selection status based on the selectedSections info
        val box = holder.itemView.findViewById<CheckBox>(R.id.itemWashBox)
        val clickHolder = holder.itemView.findViewById<View>(R.id.activeItemClickHolder)
        box.visibility = View.VISIBLE
        box.isChecked = clothes[position].wash
        clickHolder.setOnClickListener {
            //if the wash limit is met or exceeded, highlight in red
            clothes[position].wash = !clothes[position].wash
            box.isChecked = clothes[position].wash

            val washResult = holder.itemView.findViewById<TextView>(R.id.itemWashResult)
            val wash = holder.itemView.findViewById<TextView>(R.id.itemWash)
            handleWash(washResult, wash, clothes[position])
        }
        val washResult = holder.itemView.findViewById<TextView>(R.id.itemWashResult)
        val wash = holder.itemView.findViewById<TextView>(R.id.itemWash)
        handleWash(washResult, wash, clothes[position])

        //remove from outfit button handling
        holder.itemView.findViewById<View>(R.id.itemDelete).visibility = View.VISIBLE
        holder.itemView.findViewById<View>(R.id.itemDelete).setOnClickListener(deleteListener)
        holder.itemView.findViewById<View>(R.id.itemDelete).tag = ItemId(clothes[position].categoryId, clothes[position].id)
        holder.itemView.findViewById<View>(R.id.itemDelete).bringToFront()

        holder.itemView.findViewById<TextView>(R.id.itemName).visibility = View.VISIBLE
        holder.itemView.findViewById<TextView>(R.id.itemName).text = clothes[position].name

        //load photo into imageView
        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        photo.visibility = View.VISIBLE
        if (clothes[position].photoPath.isEmpty()) {
            photo.setImageResource(R.drawable.round_camera_24)
        } else {
            Picasso.get()
                .load("file://" + clothes[position].photoPath).into(photo)
        }
    }

    @Suppress("DEPRECATION")
    fun handleWash(washResult: TextView, wash: TextView, item: ClothesItem) {
        val washValue = when {
            item.maxWorn == NO_MAX_WEARS && !item.wash -> (item.worn + 1).toString()
            item.maxWorn == NO_MAX_WEARS -> "0"
            item.wash -> "0/" + item.maxWorn
            else -> (item.worn + 1).toString() + "/" + item.maxWorn
        }
        washResult.text = washValue
        wash.text = if (item.maxWorn == NO_MAX_WEARS) item.worn.toString()
            else context.getString(R.string.fraction, item.worn, item.maxWorn)

        //if the wash limit is met or exceeded, highlight in red
        if (item.maxWorn != NO_MAX_WEARS && !item.wash && item.worn+1 >= item.maxWorn) {
            washResult.setTextColor(context.resources.getColor(R.color.red))
        }
        else {
            washResult.setTextColor(context.resources.getColor(android.R.color.black))
        }
        if (item.maxWorn != NO_MAX_WEARS && !item.wash && item.worn >= item.maxWorn) {
            wash.setTextColor(context.resources.getColor(R.color.red))
        }
        else {
            wash.setTextColor(context.resources.getColor(android.R.color.black))
        }
    }

}