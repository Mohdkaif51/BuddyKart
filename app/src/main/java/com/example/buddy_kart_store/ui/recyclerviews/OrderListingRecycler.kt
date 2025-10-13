package com.example.buddy_kart_store.ui.recyclerviews

import android.R.attr.text
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.order
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class OrderListingRecycler(private val orderList: MutableList<order>,
                           private val onItemClick: (order) -> Unit // lambda for click
) :
    RecyclerView.Adapter<OrderListingRecycler.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productPrice: TextView = itemView.findViewById(R.id.txtTotalAmount)
        val name: TextView = itemView.findViewById(R.id.txtCustomerName)
        val address: TextView = itemView.findViewById(R.id.txtShippingAddress)
        val paymentMode: TextView = itemView.findViewById(R.id.txtPaymentMethod)
        val orderDate: TextView = itemView.findViewById(R.id.txtOrderDate)
        val orderId: TextView = itemView.findViewById(R.id.txtOrderId)
        val status: TextView = itemView.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ordercard, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        holder.name.text = order.customerName
        holder.productPrice.text = "₹ "+ formatAmount(order.total)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = try {
            val date = inputFormat.parse(order.dateAdded)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            order.dateAdded
        }
        holder.orderDate.text = "Order Date: $formattedDate"
        holder.orderId.text = "ID: #${order.orderId}"
        holder.address.text = order.shippingAddress
        holder.paymentMode.text = "Payment: ${order.paymentMethod}"

        when (order.orderStatus) {
            "1" -> {
                holder.status.text = "Pending"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_pending)
            }
            "2" -> {
                holder.status.text = "Processing"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_processing)
            }
            "3" -> {
                holder.status.text = "Shipped"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_shipped)
            }
            "5" -> {
                holder.status.text = "Completed"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_completed)
            }
            "7" -> {
                holder.status.text = "Cancelled"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_cancelled)
            }
            "8" -> {
                holder.status.text = "Denied"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_denied)
            }
            "9" -> {
                holder.status.text = "Cancelled Reversal"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_cancelled_reversal)
            }
            "10" -> {
                holder.status.text = "Failed"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_failed)
            }
            "11" -> {
                holder.status.text = "Refunded"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_refunded)
            }
            "12" -> {
                holder.status.text = "Reversed"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_reversed)
            }
            "13" -> {
                holder.status.text = "Chargeback"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_chargeback)
            }
            "14" -> {
                holder.status.text = "Expired"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_expired)
            }
            "15" -> {
                holder.status.text = "Processed"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_processed)
            }
            else -> {
                holder.status.text = "Voided"
                holder.status.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.status_void)
            }
        }


        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

        fun formatAmount(amount: String): String {
            val df = DecimalFormat("#.##")
            return df.format(amount.toDouble())
    }

    override fun getItemCount(): Int = orderList.size
}
