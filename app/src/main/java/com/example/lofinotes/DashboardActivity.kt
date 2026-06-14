package com.example.lofinotes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: NotasAdapter
    private lateinit var btnNuevaNota: FloatingActionButton
    private lateinit var btnLogout: ImageButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        // 2. Vincular componentes del XML
        recyclerView = findViewById(R.id.rvNotas)
        btnNuevaNota = findViewById(R.id.btnNuevaNota)
        btnLogout = findViewById(R.id.btnLogout)
        bottomNav = findViewById(R.id.bottomNavigation)

        // 3. Configurar el RecyclerView y su Adaptador
        recyclerView.layoutManager = LinearLayoutManager(this)

        adaptador = NotasAdapter(emptyList()) { nota ->
            val intent = Intent(this, EditorActivity::class.java).apply {
                putExtra("NOTA_ID", nota["id"] as? String)
                putExtra("NOTA_TITULO", nota["titulo"] as? String)
                putExtra("NOTA_CONTENIDO", nota["contenido"] as? String)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adaptador

        // 4. Escuchar y cargar las notas desde Firebase
        obtenerNotasDesdeFirebase()

        // 5. Botón Flotante para crear una nota NUEVA
        btnNuevaNota.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            startActivity(intent)
        }

        // 6. Botón de Cerrar Sesión
        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        // 7. Configurar Navegación
        bottomNav.selectedItemId = R.id.nav_notes
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notes -> true
                R.id.nav_pomodoro -> {
                    val intent = Intent(this, PomodoroActivity::class.java)
                    startActivity(intent)
                    false 
                }
                else -> false
            }
        }
    }

    private fun cerrarSesion() {
        // Cerrar sesión en Firebase
        auth.signOut()

        // Cerrar sesión en Google para que permita elegir cuenta de nuevo
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Volver al Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_notes
    }

    private fun obtenerNotasDesdeFirebase() {
        val uId = auth.currentUser?.uid
        if (uId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userNotasRef = dbRef.reference.child("users").child(uId).child("notas")

        userNotasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notasList = mutableListOf<Map<String, Any>>()
                for (data in snapshot.children) {
                    val nota = data.value as? Map<String, Any>
                    if (nota != null) {
                        notasList.add(nota)
                    }
                }
                adaptador.actualizarLista(notasList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Error al cargar notas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}