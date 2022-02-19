package com.example.loginfirebas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.loginfirebas.databinding.ActivityHomeBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
//este provider type nos sirve para designar que metodo o proveedor vamos a usar
enum class ProviderType{
    BASIC,
    GOOGLE

}
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        //checkUser()

      /*  binding.signOutButton.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }*/

        //llamamos a intent.extras y tendremos un bundle del que vamos a poder recuperar los parametros
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardado de datos
        val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null)
        {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }else{
            val email = firebaseUser.email
            binding.emailTextview.text = email
        }
    }

    private fun setup(email:String, provider:String){
        title = "Inicio"
        //Aqui recibimos los put extras del login
        binding.emailTextview.text = email
        binding.providerTextview.text = provider

        binding.signOutButton.setOnClickListener{
            //borrado de datos
            val prefs: SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }



}