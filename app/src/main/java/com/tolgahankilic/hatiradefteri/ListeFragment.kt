package com.tolgahankilic.hatiradefteri

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tolgahankilic.hatiradefteri.databinding.FragmentListeBinding
import java.lang.Exception

class ListeFragment : Fragment() {

    var hatiraIsmiListesi = ArrayList<String>()
    var hatiraIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter : ListeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding:FragmentListeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeAdapter = ListeRecyclerAdapter(hatiraIsmiListesi,hatiraIdListesi)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listeAdapter

        sqlVeriAlma()
    }

    fun sqlVeriAlma(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Hatiralar", Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM hatiralar",null)
                val hatiraIsmiIndex = cursor.getColumnIndex("hatiraismi")
                val hatiraIdIndex = cursor.getColumnIndex("id")

                hatiraIsmiListesi.clear()
                hatiraIdListesi.clear()

                while (cursor.moveToNext()){
                    hatiraIsmiListesi.add(cursor.getString(hatiraIsmiIndex))
                    hatiraIdListesi.add(cursor.getInt(hatiraIdIndex))
                }

                listeAdapter.notifyDataSetChanged()

                cursor.close()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}