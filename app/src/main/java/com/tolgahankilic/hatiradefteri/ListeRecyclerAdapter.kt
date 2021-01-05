package com.tolgahankilic.hatiradefteri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.tolgahankilic.hatiradefteri.databinding.RecyclerRowBinding

class ListeRecyclerAdapter(val hatiraListesi : ArrayList<String>, val idListesi : ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.HatiraHolder>() {

    class HatiraHolder(private val itemBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(itemBinding.root){
        fun bind(hatiraIsmi : String){
            itemBinding.recyclerRowText.text = hatiraIsmi
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HatiraHolder {
        val itemBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HatiraHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: HatiraHolder, position: Int) {
        val hatiraismi = hatiraListesi[position]
        holder.bind(hatiraismi)
       holder.itemView.setOnClickListener {
           val action = ListeFragmentDirections.actionListeFragmentToHatiraFragment("recyclerdangeldim",idListesi[position])
           Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int = hatiraListesi.size
}