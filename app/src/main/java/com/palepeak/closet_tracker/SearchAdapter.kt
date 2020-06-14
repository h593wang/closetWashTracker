package com.palepeak.closet_tracker

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*
import java.util.concurrent.Executors

//adapter for search result recyclerview
class SearchAdapter(private var context: Activity, private val clothes: ArrayList<ClothesItem>, private val listener: View.OnClickListener) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = LayoutInflater.from(context).inflate(R.layout.search_item_box, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        //if its empty, we still want a placeholder item
        if (clothes.size == 0) return 1
        return clothes.size
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = holder.itemView.findViewById<TextView>(R.id.searchItemName)
        val preview = holder.itemView.findViewById<ImageView>(R.id.searchItemPreview)
        val wearButton = holder.itemView.findViewById<Button>(R.id.searchItemWear)
        val worn = holder.itemView.findViewById<TextView>(R.id.searchItemWorn)
        if (clothes.size == 0) {
            //handling for placeholder item
            name.visibility = View.VISIBLE
            name.setText(R.string.no_results_found)
            preview.visibility = View.GONE
            wearButton.visibility = View.GONE
            worn.visibility = View.GONE
            return
        }

        name.visibility = View.VISIBLE
        name.text = clothes[position].name

        //load image using picasso
        preview.visibility = View.VISIBLE
        if (clothes[position].photoPath.isEmpty()) {
            preview.setImageResource(R.drawable.round_camera_24)
        } else {
            Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build()
                .load("file://" + clothes[position].photoPath).noFade().noPlaceholder().into(preview)
        }

        wearButton.visibility = View.VISIBLE
        wearButton.tag = ItemId(clothes[position].categoryId, clothes[position].id)
        wearButton.setOnClickListener {
            (context.application as ApplicationBase).vibrate(10)
            listener.onClick(it)
        }

        worn.visibility = View.VISIBLE
        worn.text = context.resources.getString(R.string.worn_max, clothes[position].worn, clothes[position].maxWorn)
        //set text color based on if the wear amount was exceeded
        if (clothes[position].worn >= clothes[position].maxWorn)
            worn.setTextColor(context.resources.getColor(R.color.red))
        else worn.setTextColor(context.resources.getColor(R.color.grey))


    }
}