//package com.example.buddy_kart_store.ui.recyclerviews
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.buddy_kart_store.R
//import com.example.buddy_kart_store.databinding.ItemCategoryBinding
//import com.example.buddy_kart_store.model.retrofit_setup.login.Category // ✅ use Category
//
//class CategoryAdapter(private var list: MutableList<Category>) :
//    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
//
//    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
//        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CategoryViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
//        val item = list[position]
//        holder.binding.categoryName.text = item.name
//        Glide.with(holder.itemView.context)
//            .load(item.image)
//            .placeholder(R.drawable.ic_placeholder)
//            .into(holder.binding.categoryImage)
//    }
//
//    override fun getItemCount() = list.size
//
////     Function to update list dynamically
//    fun updateList(newList: List<Category>) {
//        list.clear()
//        list.addAll(newList)
//        notifyDataSetChanged()
//    }
//}
