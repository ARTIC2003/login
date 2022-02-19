package com.example.loginfirebas

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.loginfirebas.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.Provider

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val GOOGLE_SIGN_IN = 100
    private val RC_SIGN_IN = 100
    private val TAG = "GOOGLE_SIGN_IN_TAG"
    private lateinit var googleSignInclient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Eventos personalizados para ver en google analytics, asi se lanza el evento cada vez que instaciamos el evento en nuestra pantalla
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("Message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("default_web_client_id")
            .requestEmail()
            .build()
        googleSignInclient = GoogleSignIn.getClient(this, googleSignInOptions)

        //iniciando firebase autenticacion
        firebaseAuth = FirebaseAuth.getInstance()
        //checkUser()

        /*binding.googleButton.setOnClickListener{
            Log.d(TAG, "onCreate: Begin Google SignIn")
            val intent = googleSignInclient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }*/
        setup()
        session()

    }
    //sobre escribimos este metodo para el momento que volvamos a iniciar esta pantalla
    override fun onStart(){
        super.onStart()
        binding.authLayout.visibility = View.VISIBLE

    }

    //vamos a comprobar si esta una sesion activa
    private fun session(){
        val prefs: SharedPreferences = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email:String? = prefs.getString("email", null)
        val provider:String? = prefs.getString("provider", null)

        if(email != null && provider != null){
            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }
    //funcion para autenticar al usuario
    private fun setup(){
        title = "Autenticacion"
        binding.signUpButton.setOnClickListener{
            //comprobar los datos
            if(binding.emailEdit.text.isNotEmpty()&& binding.passEdit.text.isNotEmpty())
            {
                //Si se cumple la condicion podemos registrar a nuestro usuario
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailEdit.text.toString(), binding.passEdit.text.toString()).addOnCompleteListener{
                    //Agregamos el metodo addOnCompleteListener para notificar si el registro fue sastifactoria o no
                    if(it.isSuccessful){
                        //se nos puede producir un error al no existir el usuario e email pero eso no debe pasar, para eso pasamos a que podria venir nulo y se nos mande un string vacio
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        binding.SignInButton.setOnClickListener{
            if(binding.emailEdit.text.isNotEmpty()&& binding.passEdit.text.isNotEmpty())
            {
                //Si se cumple la condicion podemos registrar a nuestro usuario
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.emailEdit.text.toString(), binding.passEdit.text.toString()).addOnCompleteListener{
                    //Agregamos el metodo addOnCompleteListener para notificar si el registro fue sastifactoria o no
                    if(it.isSuccessful){
                        //se nos puede producir un error al no existir el usuario e email pero eso no debe pasar, para eso pasamos a que podria venir nulo y se nos mande un string vacio
                        showHome(it.result?.user?.email ?: "",ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }

        }
       binding.googleButton.setOnClickListener{
            //configurando el google login

           val googleConf: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("default_web_client_id")
                .requestEmail()
                .build()

            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN )
        }
    }

    /*indu private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            startActivity(Intent(this@AuthActivity, HomeActivity::class.java))
            finish()
        }

    }*/

    //Creamos una funcion para crear una alerta si algo ha pasado en el registro y hubo algun error
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error en registro")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    //Creamos esta funcion para ir a la proxima pantalla si la autenticacion fue exitosa
    private fun showHome(email:String, provider:ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN)
        {
           //indu val accountTask =  GoogleSignIn.getSignedInAccountFromIntent(data)
           val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //indu val account = accountTask.getResult(ApiException::class.java)
                //indu firebaseAuthWithGoogleAccount(account)
               val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

               if(account != null) {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(account.email ?: "", ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }

                }
            }catch (e: Exception){
                showAlert()
            }



        }

    }
    //otro tutorial
    /*private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = firebaseAuth.currentUser

                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                if(authResult.additionalUserInfo!!.isNewUser)
                {
                    Toast.makeText(this@AuthActivity, "Cuenta Creada...\n$email", Toast.LENGTH_LONG).show()

                }else{
                    Toast.makeText(this@AuthActivity, "Logueado...\n$email", Toast.LENGTH_LONG).show()
                }

                startActivity(Intent(this@AuthActivity, HomeActivity::class.java))
                finish()


            }
            .addOnFailureListener{ e->
                Toast.makeText(this@AuthActivity, "Error en login...\n${e.message}", Toast.LENGTH_LONG).show()
            }

    }*/

}