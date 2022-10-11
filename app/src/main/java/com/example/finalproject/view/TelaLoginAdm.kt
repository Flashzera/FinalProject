package com.example.finalproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.finalproject.databinding.ActivityTelaLoginAdmBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class TelaLoginAdm : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var data:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        data = FirebaseFirestore.getInstance()
        val binding = ActivityTelaLoginAdmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.txtCriaConta.setOnClickListener {
            startActivity(Intent(this, TelaCadAdm::class.java))
        }
        binding.botaoLogin.setOnClickListener {
            if(binding.textPassword.text.isNotEmpty() && binding.textemail.text.isNotEmpty()){
                validaLogin(binding.textemail.text.toString(), binding.textPassword.text.toString())
            }else{
                Toast.makeText(this@TelaLoginAdm, "Preencha os campos", Toast.LENGTH_LONG).show()
            }
            binding.textemail.text.clear()
            binding.textPassword.text.clear()
        }

        binding.txtEsqSenha.setOnClickListener {
            if(binding.textemail.text.isEmpty()){
                Toast.makeText(this@TelaLoginAdm, "Preencha o Email", Toast.LENGTH_LONG).show()
            }else{
                auth.sendPasswordResetEmail(binding.textemail.text.toString())
                    .addOnCompleteListener { task->
                        if(task.isSuccessful){
                            Toast.makeText(this@TelaLoginAdm, "Email de redefinição de senha enviado", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@TelaLoginAdm, "Erro ao enviar o Email", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            binding.textemail.text.clear()
            binding.textPassword.text.clear()
        }
    }
    private fun validaLogin(email:String, senha:String){

        var name = ""
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener (this){ task->
                if(task.isSuccessful){
                    data.collection("Adm")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents){
                                Log.d("TAG", "${document.data}")
                                name = document.get("nome").toString()
                                val inst = document.get("instituicao").toString()
                                val intent = Intent(this, TelaAdmProf::class.java)
                                intent.putExtra("email",email)
                                intent.putExtra("inst", inst)
                                intent.putExtra("nome", name)
                                Log.d("TAG", email);
                                startActivity(intent);
                            }
                            Toast.makeText(this, "Bem vindo(a) $name", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener { exception ->
                            Log.d("TAG", "get failed with ", exception)
                            Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_LONG).show()
                        }
                }else{
                    Log.e("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Email ou senha incorretos",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }
}
