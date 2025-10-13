package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.Address
import com.example.buddy_kart_store.ui.drawer_section.AddAddresspage
import com.google.android.material.button.MaterialButton

class AddressAdapter(
    private var addresses: MutableList<Address>,
    private val onDeleteClick: (Address) -> Unit,
    private val onEditClick: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvDefaultBadge: TextView = itemView.findViewById(R.id.tvDefaultBadge)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.address_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses[position]

        holder.tvName.text = "${address.firstname} ${address.lastname}"
        holder.tvAddress.text = listOf(
            address.address1,
            address.address2,
            address.city,
            address.zone,
            address.postcode,
            address.country
        ).filter { !it.isNullOrBlank() }  // Empty ya null values skip
            .joinToString(separator = ", ")

        holder.tvDefaultBadge.visibility = if (position == 0) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Yes") { _, _ ->
                    onDeleteClick(address)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()  // Cancel delete
                }
                .create()
                .show()


        }
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddAddresspage::class.java)
            intent.putExtra("address", address)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = addresses.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Address>) {
        addresses.clear()
        addresses.addAll(newList)
        notifyDataSetChanged()

    }
}
