package com.example.finalproject.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.finalproject.databinding.ActivityTelaEditFotoAdmBinding
import com.example.finalproject.util.FirebaseStorageManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tela_edit_foto_adm.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TelaEditFotoAdm : AppCompatActivity() {

    private lateinit var binding:ActivityTelaEditFotoAdmBinding
    private lateinit var imageUri:Uri
    private lateinit var bitmap: Bitmap
    private lateinit var inst:String
    private lateinit var progressDialog:ProgressDialog
    val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaEditFotoAdmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.extras
        val email = data?.getString("email")
        val senha = data?.getString("senha")
        val nome = data?.getString("nome")
        inst = data?.getString("inst").toString()

        getImage()

        with(binding){
            linearEdit.setOnClickListener {
                selectImage()
            }
            txtDel.setOnClickListener {
                deleteImage(inst)
            }
            txtVoltarTela.setOnClickListener {
                val intent = Intent(this@TelaEditFotoAdm, TelaAdmProf::class.java)
                intent.putExtra("email", email)
                intent.putExtra("senha", senha)
                intent.putExtra("nome", nome)
                intent.putExtra("inst", inst)
                startActivity(intent)
            }
        }
    }

    private fun getImage(){
        val localFile = File.createTempFile("tempImage", "png")
        storageRef.child("Adm/$inst/admImage.png").getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Log.d("TAG", bitmap.toString())
                binding.imgEditFoto.setImageBitmap(bitmap)
            }.addOnFailureListener{
                Log.e("TAG", it.message.toString())
            }
    }

    private fun deleteImage(inst:String){

        binding.linearCaixaDel.isVisible = true
        binding.txtDel.isVisible = false

        binding.txtVoltar.setOnClickListener {
            binding.linearCaixaDel.isVisible = false
            binding.txtDel.isVisible = true
        }

        binding.txtExcluirFoto.setOnClickListener {
            storageRef.child("Adm/$inst/admImage.png")
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this@TelaEditFotoAdm, "Imagem deletada", Toast.LENGTH_LONG).show()
                    binding.linearCaixaDel.isVisible = false
                    binding.txtDel.isVisible = true
                    getImage()
                }.addOnFailureListener {
                    Toast.makeText(this@TelaEditFotoAdm, "Falha ao excluir imagem", Toast.LENGTH_LONG).show()
                    Log.d("TAG", it.message.toString())
                }
        }
    }

    private fun selectImage(){
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) { // izin al??nmad??ysa
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1)
        } else {
            val galeriIntext = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeriIntext,2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galeriIntext = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntext,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            if (imageUri != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(this.contentResolver,imageUri!!)
                    bitmap = ImageDecoder.decodeBitmap(source)
                    imgEditFoto.setImageBitmap(bitmap)
                    progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Imagem carregando...")
                    val uploadTask = mStorageRef.child("Adm/$inst/admImage.png").putFile(imageUri)
                    uploadTask.addOnSuccessListener {
                        Toast.makeText(this@TelaEditFotoAdm, "Imagem atualizada", Toast.LENGTH_LONG).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }.addOnFailureListener{
                        Log.e("TAG", it.message.toString())
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                }
                else {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,imageUri)
                    imgEditFoto.setImageBitmap(bitmap)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}