package com.example.lofinotes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<Button>(R.id.btnNuevaNota).setOnClickListener {
            // Navegar al Editor Enfocado
            startActivity(Intent(this, EditorActivity::class.java))
        }

        val navMenu = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notes -> {
                    Toast.makeText(this, "Ya estás en el Tablón", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_pomodoro -> {
                    // Reutilizaremos el EditorActivity que tiene el módulo de Pomodoro integrado
                    startActivity(Intent(this, EditorActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}