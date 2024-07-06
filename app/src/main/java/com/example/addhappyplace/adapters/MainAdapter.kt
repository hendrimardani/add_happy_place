package com.example.addhappyplace.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.addhappyplace.databinding.ItemMainBinding
import com.example.addhappyplace.models.HappyPlaceModel

class MainAdapter(val items: ArrayList<HappyPlaceModel>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root) {
        val iv = binding.ivMain
        val title = binding.tvTitleMain
        val desc = binding.tvDescMain

    }

    // When clicking on a item adapter
    interface OnClickListener {

        fun onClick(position: Int, model: HappyPlaceModel)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.iv.setImageURI(Uri.parse(item.image))
        holder.title.text = item.title
        holder.desc.text = item.description

        holder.itemView.setOnClickListener{
            if (onClickListener != null) {
                onClickListener!!.onClick(position, item)
            }
        }
    }
}