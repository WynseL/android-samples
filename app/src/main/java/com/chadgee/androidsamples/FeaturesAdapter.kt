package com.chadgee.androidsamples

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.chadgee.androidsamples.databinding.ItemFeatureBinding

class FeaturesAdapter(private val listener: FeaturesAdapterListener):
        RecyclerView.Adapter<FeaturesAdapter.FeatureViewHolder>() {

    private val list = mutableListOf<FeatureItem>()

    interface FeaturesAdapterListener: FeatureViewHolder.FeatureViewHolderListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFeatureBinding.inflate(inflater, parent, false)
        return FeatureViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.setItem(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setFeatures(featureList: List<FeatureItem>) {
        list.clear()
        list.addAll(featureList)
        notifyDataSetChanged()
    }

    class FeatureViewHolder(binding: ItemFeatureBinding,
                            private val listener: FeatureViewHolderListener): RecyclerView.ViewHolder(binding.root) {

        interface FeatureViewHolderListener {
            fun onFeatureItemClick(featureItem: FeatureItem)
        }

        private val title: AppCompatTextView = binding.title
        private val description: AppCompatTextView = binding.description

        fun setItem(featureItem: FeatureItem) {
            title.text = featureItem.title
            description.text = featureItem.description

            itemView.setOnClickListener { listener.onFeatureItemClick(featureItem) }
        }
    }
}