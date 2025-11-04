package com.example.buddy_kart_store.ui.recyclerviews

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.categories

class LeftCategory(
    private val category: MutableList<categories>,
    private val onCategoryClick: (categories) -> Unit
) :
    RecyclerView.Adapter<LeftCategory.ViewHolder>() {

    private var selectedPosition = -1


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.categoryImagee)
        val name: TextView = itemView.findViewById(R.id.categoryNamee)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_card, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = category[position]
        holder.name.text = category.name

        Glide.with(holder.itemView.context)
            .load(category.image)
            .placeholder(R.drawable.noproduct)
            .error(R.drawable.noproduct)
            .into(holder.image)

        // Dynamically add badge/indicator
        val indicatorSize = 8 // in dp
        val scale = holder.itemView.context.resources.displayMetrics.density
        val sizePx = (indicatorSize * scale + 0.5f).toInt()

        // Check if indicator already added
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.subcategory_selected) // light gray for selected
        } else {
            holder.itemView.setBackgroundResource(R.drawable.subcategory_unselected) // white for others
        }

        holder.itemView.setOnClickListener {
            setSelectedCategory(position) // update selection
            onCategoryClick(category)
        }

        // Show only for selected

        holder.itemView.setOnClickListener {
            setSelectedCategory(position) // update selection
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int {
        return category.size
    }
    fun setSelectedCategory(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

}