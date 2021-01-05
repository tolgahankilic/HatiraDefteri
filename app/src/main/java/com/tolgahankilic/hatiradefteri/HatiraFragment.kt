package com.tolgahankilic.hatiradefteri

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.tolgahankilic.hatiradefteri.databinding.FragmentHatiraBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception

class HatiraFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentHatiraBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHatiraBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            kaydet(it)
        }

        binding.imageView.setOnClickListener {
            gorselSec(it)
        }

        arguments?.let {
            var gelenBilgi = HatiraFragmentArgs.fromBundle(it).bilgi

            if (gelenBilgi.equals("menudengeldim")){
                //yeni ekle
                binding.hatiraIsmiText.setText("")
                binding.hatiraDetayText.setText("")
                binding.button.visibility = View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.resimekle)
                binding.imageView.setImageBitmap(gorselSecmeArkaPlani)
            } else {
                //hatira gosterilecek
                binding.button.visibility = View.INVISIBLE

                val secilenId = HatiraFragmentArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Hatiralar",Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery("SELECT * FROM hatiralar WHERE id = ?", arrayOf(secilenId.toString()))

                        val hatiraIsmiIndex = cursor.getColumnIndex("hatiraismi")
                        val hatiraDetayIndex = cursor.getColumnIndex("hatiradetayi")
                        val hatiraGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            binding.hatiraIsmiText.setText(cursor.getString(hatiraIsmiIndex))
                            binding.hatiraDetayText.setText(cursor.getString(hatiraDetayIndex))

                            val byteDizisi = cursor.getBlob(hatiraGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)

                            binding.imageView.setImageBitmap(bitmap)
                        }

                        cursor.close()
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun kaydet(view: View){
        val hatiraIsmi = binding.hatiraIsmiText.text.toString()
        val hatiraDetay = binding.hatiraDetayText.text.toString()

        if(secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Hatiralar", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS hatiralar (id INTEGER PRIMARY KEY, hatiraismi VARCHAR, hatiradetayi VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO hatiralar (hatiraismi, hatiradetayi, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,hatiraIsmi)
                    statement.bindString(2,hatiraDetay)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()
                }
            } catch (e: Exception){
                e.printStackTrace()
            }

            val action = HatiraFragmentDirections.actionHatiraFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }
    }

    fun gorselSec(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi, izin istememiz gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else{
                //izin zaten verilmiş, tekrar istemeden galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 1){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izin alındı
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            secilenGorsel = data.data
            try{
                context?.let {
                    if(secilenGorsel != null){
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap{

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani > 1){
            //görsel yatay ise
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            //görsel dikey ise
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

}