package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ItemCategoryBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.categories

class SubCategoryAdapter(
    private val subCategories: List<categories>,
    private val onClick: (categories) -> Unit
) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {

    private var selectedPosition = -1

    inner class SubCategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subCat: categories, position: Int) {
            binding.categoryName.text = subCat.name
//            binding.categoryImage.setImageResource(subCat.)
            binding.root.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                onClick(subCat)
            }

            Glide.with(binding.root.context)
                .load(subCat.image)
                .placeholder(R.drawable.placeholderimg)
                .error(R.drawable.placeholderimg)
                .into(binding.categoryImage)

            // Highlight selected subcategory
            binding.root.isSelected = position == selectedPosition
            if (position == selectedPosition) {
                binding.root.setBackgroundResource(R.drawable.subcategory_selected) // selected color
            } else {
                binding.root.setBackgroundResource(R.drawable.subcategory_unselected) // normal color
            }

            binding.root.setOnClickListener {
                if (selectedPosition != position) {
                    selectedPosition = position
                    notifyDataSetChanged() // refresh all items
                    onClick(subCat)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(subCategories[position], position)
    }

    // ✅ Add this method to select a position programmatically
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = subCategories.size
}
