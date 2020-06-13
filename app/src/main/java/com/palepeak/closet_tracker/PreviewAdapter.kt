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
import java.util.concurrent.Executors

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

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //handling for placeholder item
        if (clothes.size == 0) {
            //handling for placeholder item
            holder.itemView.findViewById<View>(R.id.itemPreview).visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.itemWashLabel).setText(R.string.outfit_preview_placeholder)
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
        box.isChecked = clothes[position].wash
        box.setOnClickListener {
            //if the wash limit is met or exceeded, highlight in red
            clothes[position].wash = !clothes[position].wash
            val washValue = if (clothes[position].wash) clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → 0/" + clothes[position].maxWorn
            else clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → " + (clothes[position].worn + 1).toString() + "/" + clothes[position].maxWorn
            if (!clothes[position].wash && clothes[position].worn+1 >= clothes[position].maxWorn)
                holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.red))
            else holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.grey))
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = washValue
        }

        holder.itemView.findViewById<TextView>(R.id.itemWashLabel).setText(R.string.wash_item)
        //if the wash limit is met or exceeded, highlight in red
        val washValue = if (clothes[position].wash) clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → 0/" + clothes[position].maxWorn
        else clothes[position].worn.toString() + "/" + clothes[position].maxWorn + " → " + (clothes[position].worn + 1).toString() + "/" + clothes[position].maxWorn
        if (!clothes[position].wash && clothes[position].worn+1 >= clothes[position].maxWorn)
            holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.red))
        else holder.itemView.findViewById<TextView>(R.id.itemWashResult).setTextColor(context.resources.getColor(R.color.grey))
        holder.itemView.findViewById<TextView>(R.id.itemWashResult).text = washValue

        //remove from outfit button handling
        holder.itemView.findViewById<TextView>(R.id.itemDelete).visibility = View.VISIBLE
        holder.itemView.findViewById<TextView>(R.id.itemDelete).setOnClickListener(deleteListener)
        holder.itemView.findViewById<TextView>(R.id.itemDelete).tag = ItemId(clothes[position].categoryId, clothes[position].id)

        holder.itemView.findViewById<TextView>(R.id.itemName).text = clothes[position].name

        //load photo into imageView
        val photo = holder.itemView.findViewById<ImageView>(R.id.itemPreview)
        photo.visibility = View.VISIBLE
        Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build().load("file://"+clothes[position].photoPath).into(photo)
    }


}