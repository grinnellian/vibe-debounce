package com.vibedebounce.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vibedebounce.databinding.ItemAppToggleBinding

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val enabled: Boolean
)

class AppListAdapter(
    private val onToggle: (packageName: String, enabled: Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private val items = mutableListOf<AppItem>()

    fun submitList(newItems: List<AppItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppToggleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemAppToggleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppItem) {
            binding.appName.text = item.label
            binding.appIcon.setImageDrawable(item.icon)
            binding.appToggle.setOnCheckedChangeListener(null)
            binding.appToggle.isChecked = item.enabled
            binding.appToggle.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item.packageName, isChecked)
            }
        }
    }
}
