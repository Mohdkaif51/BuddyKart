package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.Review

class ProductReviewRecycler(
    private var reviewList: MutableList<Review>
) : RecyclerView.Adapter<ProductReviewRecycler.ViewHolder>() {

    // Track which items are expanded
    private val expandedStates = mutableMapOf<Int, Boolean>()

    // Fixed card height (initial collapsed state)
    private val cardCollapsedHeight = 140 // dp, same as XML

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvReviewerName)
        val date: TextView = itemView.findViewById(R.id.tvReviewDate)
        val desc: TextView = itemView.findViewById(R.id.tvReviewDesc)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val seeMoreBtn: TextView = itemView.findViewById(R.id.seeMoreBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = reviewList[position]

        holder.name.text = item.name
        holder.date.text = item.date
        holder.ratingBar.rating = item.rating
        holder.date.visibility = View.GONE
        holder.desc.text = item.review

        // Determine if this item is expanded
        val isExpanded = expandedStates[position] ?: false

        // Temporarily remove maxLines to measure full lines
        holder.desc.maxLines = Int.MAX_VALUE

        holder.desc.post {
            val totalLines = holder.desc.layout?.lineCount ?: 0

            // Show button if more than 1 line
            holder.seeMoreBtn.visibility = if (totalLines > 1) View.VISIBLE else View.GONE

            // Apply proper maxLines according to expanded/collapsed state
            if (isExpanded) {
                holder.desc.maxLines = Int.MAX_VALUE
                holder.seeMoreBtn.text = "See Less"
                // Optional: expand card height dynamically
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                holder.desc.maxLines = 1
                holder.seeMoreBtn.text = "See More"
                // Set collapsed card height
                holder.itemView.layoutParams.height =
                    (cardCollapsedHeight * holder.itemView.context.resources.displayMetrics.density).toInt()
            }

            holder.itemView.requestLayout()
        }

        // Toggle click listener
        holder.seeMoreBtn.setOnClickListener {
            val currentlyExpanded = expandedStates[position] ?: false
            expandedStates[position] = !currentlyExpanded

            // Smooth height animation (optional)
            holder.itemView.animate()
                .setDuration(200)
                .withEndAction { notifyItemChanged(position) }
                .start()
        }
    }

    override fun getItemCount(): Int = reviewList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Review>) {
        reviewList.clear()
        reviewList.addAll(newList)
        expandedStates.clear()
        notifyDataSetChanged()
    }
}
