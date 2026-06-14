package com.example.lofinotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirige de forma automática al flujo de Login validado por Google
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Cierra MainActivity para que el usuario no pueda regresar con el botón atrás
    }
}