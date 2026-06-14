package com.example.lofinotes

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    private lateinit var etTitulo: EditText
    private lateinit var etContenido: EditText
    private lateinit var btnGuardar: FloatingActionButton

    private var notaId: String? = null // Si es nulo es nota nueva, si tiene texto edita existente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        // Vincular componentes de tu XML (Revisa que coincidan los IDs de tu activity_editor.xml)
        etTitulo = findViewById(R.id.txtTitulo)
        etContenido = findViewById(R.id.txtContenido)
        btnGuardar = findViewById(R.id.btnGuardarCloud) // Tu botón para guardar

        // Verificar si venimos de hacer clic en una nota existente para editarla
        notaId = intent.getStringExtra("NOTA_ID")
        if (notaId != null) {
            etTitulo.setText(intent.getStringExtra("NOTA_TITULO"))
            etContenido.setText(intent.getStringExtra("NOTA_CONTENIDO"))
        }

        // Acción del botón guardar
        btnGuardar.setOnClickListener {
            guardarNota()
        }
    }

    private fun guardarNota() {
        val titulo = etTitulo.text.toString().trim()
        val contenido = etContenido.text.toString().trim()
        val uId = auth.currentUser?.uid

        if (uId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        if (titulo.isEmpty()) {
            etTitulo.error = "El título es obligatorio"
            return
        }

        // Referencia directa a la ubicación de las notas de este usuario en el árbol JSON
        val userNotasRef = dbRef.reference.child("users").child(uId).child("notas")

        // Si notaId es null, generamos una llave nueva (Nota Nueva)
        if (notaId == null) {
            notaId = userNotasRef.push().key
        }

        // Estructura de datos que se subirá
        val notaData = mapOf(
            "id" to notaId,
            "titulo" to titulo,
            "contenido" to contenido,
            "timestamp" to System.currentTimeMillis()
        )

        // Guardar de forma destructiva o actualización en esa ruta exacta
        if (notaId != null) {
            userNotasRef.child(notaId!!).setValue(notaData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Nota guardada con éxito", Toast.LENGTH_SHORT).show()
                    finish() // Regresa automáticamente al Dashboard
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}