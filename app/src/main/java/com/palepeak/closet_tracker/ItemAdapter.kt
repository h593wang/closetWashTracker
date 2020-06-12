package com.palepeak.closet_tracker

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*
import java.util.concurrent.Executors

class ItemAdapter(private var context: Activity, val clothes: ArrayList<ClothesItem>, val deleteListener:View.OnClickListener) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    var washItems = Array(clothes.size) {false}

    fun notifyDataChange(removedIndex: Int) {
        val tmpExpanded = Array(clothes.size) {false}
        var offset = 0
        for (i in washItems.indices) {
            if (removedIndex == i) {
                offset = 1
            }
            if (i < tmpExpanded.size) tmpExpanded[i] = washItems[i + offset]
        }
        washItems = tmpExpanded
        notifyDataSetChanged()
    }

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
            holder.itemView.findViewById<View>(R.id.itemPreview).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWashLabel).text = "Nothing added to today's outfit"
            holder.itemView.findViewById<View>(R.id.itemWashBox).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemName).text = ""
            holder.itemView.findViewById<TextView>(R.id.itemDelete).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = ""
            holder.itemView.isSelected = false
            holder.itemView.setOnClickListener {  }
            return
        }

        //set the selection status based on the selectedSections info
        val box = holder.itemView.findViewById<CheckBox>(R.id.itemWashBox)
        box.visibility = View.VISIBLE
        box.isChecked = washItems[position]
        box.setOnClickListener {
            washItems[position] = !washItems[position]
            var washValue = ""
            washValue = if (washItems[position]) clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → 0/" + clothes[position].maxWorn
            else clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → " + (clothes[position].worn + 1).toString() + "/" + clothes[position].maxWorn
            if (!washItems[position] && clothes[position].worn + 1 > clothes[position].maxWorn)
                holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.red))
            else holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.grey))
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = washValue
        }

        holder.itemView.findViewById<TextView>(R.id.itemWashLabel).text = "Wash Item?"
        var washValue = ""
        washValue = if (washItems[position]) clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → 0/" + clothes[position].maxWorn
        else clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → " + (clothes[position].worn + 1).toString() + "/" + clothes[position].maxWorn
        if (!washItems[position] && clothes[position].worn + 1 > clothes[position].maxWorn)
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.red))
        else holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.grey))
        holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = washValue

        holder.itemView.findViewById<TextView>(R.id.itemDelete).visibility = View.VISIBLE
        holder.itemView.findViewById<TextView>(R.id.itemDelete).setOnClickListener(deleteListener)
        holder.itemView.findViewById<TextView>(R.id.itemDelete).tag = ItemId(clothes[position].categoryId, clothes[position].id)

        holder.itemView.findViewById<TextView>(R.id.itemName).text = clothes[position].name

        holder.itemView.findViewById<View>(R.id.itemPreview).visibility = View.VISIBLE

        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        photo.setImageResource(0)
        Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build().load("file://"+clothes[position].photoPath).into(photo)
    }


}